import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { AutoComplete, AutoCompleteCompleteEvent } from 'primeng/autocomplete';
import { Button } from 'primeng/button';
import { Checkbox } from 'primeng/checkbox';
import { InputNumber } from 'primeng/inputnumber';
import { InputText } from 'primeng/inputtext';
import { Paginator, PaginatorState } from 'primeng/paginator';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Tooltip } from 'primeng/tooltip';
import {
  GroupMemberDetail,
  GroupPublicationSummaryResponse,
  JournalSummaryResponse,
  JournalsService,
  PublicationStatus,
  PublicationsService,
} from '@core/api';
import { UserState } from '@core/services/user-state';
import { DaysSincePipe } from '@shared/pipes/days-since.pipe';
import { publicationStatusSeverity } from '../../publications/publication-status';

const DEBOUNCE_MS = 300;
const PAGE_SIZE = 20;
const JOURNAL_SUGGESTIONS_PAGE_SIZE = 10;
/** Show the loading indicator only if the request is still pending after this delay. */
const LOADER_DELAY_MS = 250;

/**
 * Listado de las publicaciones que pertenecen al grupo (de todos sus miembros), con los mismos
 * filtros que el listado general y una barra lateral con un checkbox por miembro para incluir o
 * excluir sus publicaciones (por defecto, todos marcados). Desde aquí también se pueden crear
 * publicaciones ya asociadas a este grupo.
 */
@Component({
  selector: 'record-group-publications',
  imports: [
    TranslatePipe,
    DatePipe,
    DaysSincePipe,
    FormsModule,
    AutoComplete,
    Button,
    Checkbox,
    InputNumber,
    InputText,
    Paginator,
    Select,
    TableModule,
    Tag,
    Tooltip,
  ],
  templateUrl: './group-publications.html',
})
export class GroupPublications implements OnInit {
  readonly groupId = input.required<string>();
  readonly groupName = input.required<string>();
  readonly members = input.required<GroupMemberDetail[]>();

  private readonly publicationsService = inject(PublicationsService);
  private readonly journalsService = inject(JournalsService);
  private readonly router = inject(Router);
  private readonly userState = inject(UserState);

  publications = signal<GroupPublicationSummaryResponse[]>([]);
  totalRecords = signal(0);
  first = signal(0);
  showLoader = signal(false);
  hasLoaded = signal(false);

  readonly pageSize = PAGE_SIZE;

  /** Miembros cuyas publicaciones se muestran. Por defecto, todos. */
  readonly selectedMemberIds = signal<Set<string>>(new Set());

  // --- Filtros (mismos que el listado general, sin "solo autor principal") ---
  title = '';
  journal: JournalSummaryResponse | null = null;
  status: PublicationStatus | null = null;
  minDaysInStatus: number | null = null;

  readonly journalSuggestions = signal<JournalSummaryResponse[]>([]);
  readonly statusValues = Object.values(PublicationStatus);

  readonly currentUserId = computed(() => this.userState.currentUser()?.id);
  readonly allMembersSelected = computed(
    () => this.selectedMemberIds().size === this.members().length,
  );
  readonly noMembersSelected = computed(() => this.selectedMemberIds().size === 0);

  private debounceTimer?: ReturnType<typeof setTimeout>;
  private loaderTimer?: ReturnType<typeof setTimeout>;

  ngOnInit() {
    // Por defecto se incluyen todos los miembros.
    this.selectedMemberIds.set(new Set(this.members().map((m) => m.userId)));
    this.load();
  }

  // --- Barra lateral de miembros ---

  isMemberSelected(userId: string): boolean {
    return this.selectedMemberIds().has(userId);
  }

  onMemberToggle(userId: string, checked: boolean) {
    const next = new Set(this.selectedMemberIds());
    if (checked) {
      next.add(userId);
    } else {
      next.delete(userId);
    }
    this.selectedMemberIds.set(next);
    this.resetAndLoad();
  }

  // --- Filtros ---

  onTitleChange(value: string) {
    this.title = value;
    this.debounceResetAndLoad();
  }

  onJournalSearch(event: AutoCompleteCompleteEvent) {
    this.journalsService
      .searchJournals(event.query, undefined, undefined, 0, JOURNAL_SUGGESTIONS_PAGE_SIZE)
      .subscribe({
        next: (page) => this.journalSuggestions.set(page.content),
        error: () => this.journalSuggestions.set([]),
      });
  }

  onJournalSelect() {
    this.resetAndLoad();
  }

  onJournalClear() {
    this.journal = null;
    this.resetAndLoad();
  }

  onStatusChange(status: PublicationStatus | null) {
    this.status = status;
    this.resetAndLoad();
  }

  onMinDaysChange(value: number | null) {
    this.minDaysInStatus = value;
    this.debounceResetAndLoad();
  }

  clearFilters() {
    this.title = '';
    this.journal = null;
    this.status = null;
    this.minDaysInStatus = null;
    this.resetAndLoad();
  }

  hasActiveFilters(): boolean {
    return (
      this.title.trim().length > 0 ||
      this.journal !== null ||
      this.status !== null ||
      this.minDaysInStatus !== null
    );
  }

  onPageChange(event: PaginatorState) {
    this.first.set(event.first ?? 0);
    this.load();
  }

  private debounceResetAndLoad() {
    clearTimeout(this.debounceTimer);
    this.debounceTimer = setTimeout(() => this.resetAndLoad(), DEBOUNCE_MS);
  }

  private resetAndLoad() {
    this.first.set(0);
    this.load();
  }

  load() {
    const selected = this.selectedMemberIds();
    // Sin ningún miembro seleccionado no hay nada que mostrar: se evita la llamada.
    if (selected.size === 0) {
      this.publications.set([]);
      this.totalRecords.set(0);
      this.stopLoader();
      this.hasLoaded.set(true);
      return;
    }

    // Con todos los miembros marcados se omite el filtro (el backend devuelve todas las del grupo).
    const memberIds = this.allMembersSelected() ? undefined : Array.from(selected);
    const page = Math.floor(this.first() / this.pageSize);
    this.startLoader();

    this.publicationsService
      .listGroupPublications(
        this.groupId(),
        memberIds,
        this.title.trim() || undefined,
        this.journal?.id ?? undefined,
        this.status ?? undefined,
        this.minDaysInStatus ?? undefined,
        page,
        this.pageSize,
      )
      .subscribe({
        next: (response) => {
          this.publications.set(response.content);
          this.totalRecords.set(response.totalElements);
          this.stopLoader();
          this.hasLoaded.set(true);
        },
        error: () => {
          this.publications.set([]);
          this.totalRecords.set(0);
          this.stopLoader();
          this.hasLoaded.set(true);
        },
      });
  }

  private startLoader() {
    clearTimeout(this.loaderTimer);
    this.loaderTimer = setTimeout(() => this.showLoader.set(true), LOADER_DELAY_MS);
  }

  private stopLoader() {
    clearTimeout(this.loaderTimer);
    this.showLoader.set(false);
  }

  // --- Navegación ---

  /** URL del grupo (con su pestaña) a la que volver desde una publicación abierta desde aquí. */
  private returnUrl(): string {
    return `/groups/${this.groupId()}?tab=posts`;
  }

  /** Contexto de origen para el breadcrumb y los botones de volver de la publicación. */
  private originQueryParams() {
    return { returnUrl: this.returnUrl(), originLabel: this.groupName() };
  }

  /** Crea una publicación ya asociada a este grupo (el grupo quedará bloqueado en el formulario). */
  openCreate() {
    this.router.navigate(['/publications/create'], {
      queryParams: { groupId: this.groupId(), ...this.originQueryParams() },
    });
  }

  openDetail(publicationId: string) {
    this.router.navigate(['/publications', publicationId], {
      queryParams: this.originQueryParams(),
    });
  }

  initials(creator: GroupPublicationSummaryResponse['creator']): string {
    return ((creator.firstName?.[0] ?? '') + (creator.lastName?.[0] ?? '')).toUpperCase() || '?';
  }

  statusSeverity(
    status: GroupPublicationSummaryResponse['status'],
  ): 'success' | 'info' | 'warn' | 'danger' {
    return publicationStatusSeverity(status);
  }
}
