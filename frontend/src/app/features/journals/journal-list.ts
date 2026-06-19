import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { Paginator, PaginatorState } from 'primeng/paginator';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import {
  CategoryResponse,
  JournalSearchPageResponse,
  JournalSummaryResponse,
  JournalsService,
  Quartile,
} from '@core/api';
import { JournalListFilters, JournalSearchState } from './journal-search-state';

const DEBOUNCE_MS = 300;
const PAGE_SIZE = 20;
/** Show the loading indicator only if the request is still pending after this delay. */
const LOADER_DELAY_MS = 250;

export type JournalListMode = 'search' | 'interests';

interface SelectOption<T> {
  label: string;
  value: T;
}

/**
 * Lista paginada de revistas con filtros (nombre, categoría, cuartil) y una estrella por fila para
 * marcar/desmarcar el interés. Reutilizable en dos modos:
 * - `search`: busca en todo el catálogo.
 * - `interests`: lista solo las revistas que el usuario ha marcado; al desmarcar, la fila desaparece
 *   de forma optimista y reaparece (recargando) si la operación falla.
 */
@Component({
  selector: 'record-journal-list',
  imports: [FormsModule, TranslatePipe, Button, InputText, Select, Paginator, TableModule, Tag],
  templateUrl: './journal-list.html',
})
export class JournalList implements OnInit {
  readonly mode = input.required<JournalListMode>();

  private readonly journalsService = inject(JournalsService);
  private readonly router = inject(Router);
  private readonly state = inject(JournalSearchState);
  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);

  readonly journals = signal<JournalSummaryResponse[]>([]);
  readonly totalRecords = signal(0);
  readonly first = signal(0);
  readonly showLoader = signal(false);
  readonly hasLoaded = signal(false);

  readonly pageSize = PAGE_SIZE;

  /** Committed filter state (what the last/next request uses). Restored from the shared state. */
  name = '';
  categoryId: string | null = null;
  quartile: Quartile | null = null;

  private readonly categories = signal<CategoryResponse[]>([]);

  /** Category dropdown options, disambiguated by edition when present. */
  readonly categoryOptions = computed<SelectOption<string>[]>(() =>
    this.categories().map((c) => ({
      label: c.edition ? `${c.name} (${c.edition})` : c.name,
      value: c.id,
    })),
  );

  readonly quartileOptions: SelectOption<Quartile>[] = Object.values(Quartile).map((q) => ({
    label: q,
    value: q,
  }));

  readonly emptyStateKey = computed(() =>
    this.mode() === 'interests' ? 'JOURNALS.INTERESTS.EMPTY_STATE' : 'JOURNALS.EMPTY_STATE',
  );

  private nameDebounce?: ReturnType<typeof setTimeout>;
  private loaderTimer?: ReturnType<typeof setTimeout>;

  ngOnInit() {
    // Restore filters and page from a previous visit (e.g. after returning from the detail page).
    const filters = this.filters();
    this.name = filters.name;
    this.categoryId = filters.categoryId;
    this.quartile = filters.quartile;
    this.first.set(filters.first);

    this.loadCategories();
    this.load();
  }

  private filters(): JournalListFilters {
    return this.mode() === 'interests' ? this.state.interests : this.state.search;
  }

  private loadCategories() {
    this.journalsService.listJournalCategories().subscribe({
      next: (categories) => this.categories.set(categories),
      error: () => this.categories.set([]),
    });
  }

  onNameChange(value: string) {
    this.name = value;
    clearTimeout(this.nameDebounce);
    this.nameDebounce = setTimeout(() => this.resetAndLoad(), DEBOUNCE_MS);
  }

  onCategoryChange(categoryId: string | null) {
    this.categoryId = categoryId;
    this.resetAndLoad();
  }

  onQuartileChange(quartile: Quartile | null) {
    this.quartile = quartile;
    this.resetAndLoad();
  }

  clearFilters() {
    this.name = '';
    this.categoryId = null;
    this.quartile = null;
    this.resetAndLoad();
  }

  hasActiveFilters(): boolean {
    return this.name.trim().length > 0 || this.categoryId !== null || this.quartile !== null;
  }

  onPageChange(event: PaginatorState) {
    this.first.set(event.first ?? 0);
    this.load();
  }

  openJournalDetail(journalId: string) {
    this.router.navigate(['/journals', journalId]);
  }

  /** Reload the current page keeping the active filters. Used when this tab becomes active again. */
  reload() {
    this.load();
  }

  /** Toggle the interest flag of a journal without navigating to its detail. */
  toggleInterest(journal: JournalSummaryResponse, event: Event) {
    event.stopPropagation();
    if (journal.isInterest) {
      this.unmark(journal);
    } else {
      this.mark(journal);
    }
  }

  private mark(journal: JournalSummaryResponse) {
    this.setInterest(journal.id, true);
    this.journalsService.markJournalAsInterest(journal.id).subscribe({
      error: () => {
        this.setInterest(journal.id, false);
        this.toastError();
      },
    });
  }

  private unmark(journal: JournalSummaryResponse) {
    if (this.mode() === 'interests') {
      // Optimistic removal: drop the row immediately; restore by reloading if the request fails.
      this.journals.update((list) => list.filter((j) => j.id !== journal.id));
      this.totalRecords.update((n) => Math.max(0, n - 1));
      this.journalsService.unmarkJournalAsInterest(journal.id).subscribe({
        error: () => {
          this.load();
          this.toastError();
        },
      });
      return;
    }

    this.setInterest(journal.id, false);
    this.journalsService.unmarkJournalAsInterest(journal.id).subscribe({
      error: () => {
        this.setInterest(journal.id, true);
        this.toastError();
      },
    });
  }

  private setInterest(journalId: string, value: boolean) {
    this.journals.update((list) =>
      list.map((j) => (j.id === journalId ? { ...j, isInterest: value } : j)),
    );
  }

  private toastError() {
    this.messageService.add({
      severity: 'error',
      summary: this.translate.instant('JOURNALS.TITLE'),
      detail: this.translate.instant('JOURNALS.ACTIONS.ERROR'),
    });
  }

  private resetAndLoad() {
    this.first.set(0);
    this.load();
  }

  private load() {
    this.persistState();
    const page = Math.floor(this.first() / this.pageSize);
    this.startLoader();

    const request = (next: (response: JournalSearchPageResponse) => void, error: () => void) => {
      const name = this.name.trim() || undefined;
      const categoryId = this.categoryId ?? undefined;
      const quartile = this.quartile ?? undefined;
      const observer = { next, error };
      if (this.mode() === 'interests') {
        this.journalsService
          .listInterestJournals(name, categoryId, quartile, page, this.pageSize)
          .subscribe(observer);
      } else {
        this.journalsService
          .searchJournals(name, categoryId, quartile, page, this.pageSize)
          .subscribe(observer);
      }
    };

    request(
      (response) => {
        this.journals.set(response.content);
        this.totalRecords.set(response.totalElements);
        this.stopLoader();
        this.hasLoaded.set(true);
      },
      () => {
        this.journals.set([]);
        this.totalRecords.set(0);
        this.stopLoader();
        this.hasLoaded.set(true);
      },
    );
  }

  private persistState() {
    const filters = this.filters();
    filters.name = this.name;
    filters.categoryId = this.categoryId;
    filters.quartile = this.quartile;
    filters.first = this.first();
  }

  private startLoader() {
    clearTimeout(this.loaderTimer);
    this.loaderTimer = setTimeout(() => this.showLoader.set(true), LOADER_DELAY_MS);
  }

  private stopLoader() {
    clearTimeout(this.loaderTimer);
    this.showLoader.set(false);
  }

  quartileSeverity(quartile: Quartile): 'success' | 'info' | 'warn' | 'danger' {
    switch (quartile) {
      case Quartile.Q1:
        return 'success';
      case Quartile.Q2:
        return 'info';
      case Quartile.Q3:
        return 'warn';
      default:
        return 'danger';
    }
  }
}
