import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { Paginator, PaginatorState } from 'primeng/paginator';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import {
  CategoryResponse,
  JournalSummaryResponse,
  JournalsService,
  Quartile,
} from '@core/api';
import { JournalSearchState } from './journal-search-state';

const DEBOUNCE_MS = 300;
const PAGE_SIZE = 20;
/** Show the loading indicator only if the request is still pending after this delay. */
const LOADER_DELAY_MS = 250;

interface SelectOption<T> {
  label: string;
  value: T;
}

@Component({
  selector: 'record-journals',
  imports: [FormsModule, TranslatePipe, Button, InputText, Select, Paginator, TableModule, Tag],
  templateUrl: './journals.html',
})
export class Journals implements OnInit {
  private readonly journalsService = inject(JournalsService);
  private readonly router = inject(Router);
  private readonly state = inject(JournalSearchState);

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

  private nameDebounce?: ReturnType<typeof setTimeout>;
  private loaderTimer?: ReturnType<typeof setTimeout>;

  ngOnInit() {
    // Restore filters and page from a previous visit (e.g. after returning from the detail page).
    this.name = this.state.name;
    this.categoryId = this.state.categoryId;
    this.quartile = this.state.quartile;
    this.first.set(this.state.first);

    this.loadCategories();
    this.load();
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

  private resetAndLoad() {
    this.first.set(0);
    this.load();
  }

  private load() {
    this.persistState();
    const page = Math.floor(this.first() / this.pageSize);
    this.startLoader();

    this.journalsService
      .searchJournals(
        this.name.trim() || undefined,
        this.categoryId ?? undefined,
        this.quartile ?? undefined,
        page,
        this.pageSize,
      )
      .subscribe({
        next: (response) => {
          this.journals.set(response.content);
          this.totalRecords.set(response.totalElements);
          this.stopLoader();
          this.hasLoaded.set(true);
        },
        error: () => {
          this.journals.set([]);
          this.totalRecords.set(0);
          this.stopLoader();
          this.hasLoaded.set(true);
        },
      });
  }

  private persistState() {
    this.state.name = this.name;
    this.state.categoryId = this.categoryId;
    this.state.quartile = this.quartile;
    this.state.first = this.first();
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
