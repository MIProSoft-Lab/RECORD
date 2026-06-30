import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ConfirmationService, MenuItem, MessageService } from 'primeng/api';
import { AutoComplete, AutoCompleteCompleteEvent } from 'primeng/autocomplete';
import { Button } from 'primeng/button';
import { Checkbox } from 'primeng/checkbox';
import { InputNumber } from 'primeng/inputnumber';
import { InputText } from 'primeng/inputtext';
import { Menu } from 'primeng/menu';
import { Paginator, PaginatorState } from 'primeng/paginator';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Tooltip } from 'primeng/tooltip';
import {
  JournalSummaryResponse,
  JournalsService,
  PublicationStatus,
  PublicationSummaryResponse,
  PublicationsService,
} from '@core/api';
import { DaysSincePipe } from '@shared/pipes/days-since.pipe';
import { allowedStatusTransitions, publicationStatusSeverity } from './publication-status';
import { ChangeStatusDialog } from './change-status/change-status-dialog';
import { PublicationFilterState } from './publication-filter-state';
import { ResubmitPublicationDialog } from './resubmit/resubmit-publication-dialog';

const DEBOUNCE_MS = 300;
const PAGE_SIZE = 20;
const JOURNAL_SUGGESTIONS_PAGE_SIZE = 10;
/** Show the loading indicator only if the request is still pending after this delay. */
const LOADER_DELAY_MS = 250;

@Component({
  selector: 'record-publications',
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
    Menu,
    Paginator,
    Select,
    TableModule,
    Tag,
    Tooltip,
    ChangeStatusDialog,
    ResubmitPublicationDialog,
  ],
  templateUrl: './publications.html',
})
export class Publications implements OnInit {
  private readonly publicationsService = inject(PublicationsService);
  private readonly journalsService = inject(JournalsService);
  private readonly router = inject(Router);
  private readonly translate = inject(TranslateService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly messageService = inject(MessageService);
  private readonly state = inject(PublicationFilterState);

  private readonly statusMenu = viewChild.required<Menu>('statusMenu');

  publications = signal<PublicationSummaryResponse[]>([]);
  totalRecords = signal(0);
  first = signal(0);
  showLoader = signal(false);
  hasLoaded = signal(false);
  statusMenuItems = signal<MenuItem[]>([]);

  readonly pageSize = PAGE_SIZE;

  /** Committed filter state (what the next request uses). Restored from the shared state. */
  title = '';
  journal: JournalSummaryResponse | null = null;
  status: PublicationStatus | null = null;
  minDaysInStatus: number | null = null;
  onlyAsMainAuthor = false;

  readonly journalSuggestions = signal<JournalSummaryResponse[]>([]);

  readonly statusValues = Object.values(PublicationStatus);

  /** Publicación rechazada seleccionada para reenviar (alimenta el diálogo). */
  resubmitTarget = signal<PublicationSummaryResponse | null>(null);
  resubmitDialogVisible = signal(false);

  /** Cambio de estado pendiente de confirmar (alimenta el diálogo de confirmación). */
  changeStatusTargetId = signal<string | null>(null);
  changeStatusTargetStatus = signal<PublicationStatus | null>(null);
  changeStatusDialogVisible = signal(false);

  private debounceTimer?: ReturnType<typeof setTimeout>;
  private loaderTimer?: ReturnType<typeof setTimeout>;

  ngOnInit() {
    // Restore filters and page from a previous visit (e.g. after returning from the detail page).
    const filters = this.state.filters;
    this.title = filters.title;
    this.journal = filters.journal;
    this.status = filters.status;
    this.minDaysInStatus = filters.minDaysInStatus;
    this.onlyAsMainAuthor = filters.onlyAsMainAuthor;
    this.first.set(filters.first);

    this.load();
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

  onMainAuthorChange(value: boolean) {
    this.onlyAsMainAuthor = value;
    this.resetAndLoad();
  }

  clearFilters() {
    this.title = '';
    this.journal = null;
    this.status = null;
    this.minDaysInStatus = null;
    this.onlyAsMainAuthor = false;
    this.resetAndLoad();
  }

  hasActiveFilters(): boolean {
    return (
      this.title.trim().length > 0 ||
      this.journal !== null ||
      this.status !== null ||
      this.minDaysInStatus !== null ||
      this.onlyAsMainAuthor
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
    this.persistState();
    const page = Math.floor(this.first() / this.pageSize);
    this.startLoader();

    this.publicationsService
      .listMyPublications(
        this.title.trim() || undefined,
        this.journal?.id ?? undefined,
        this.status ?? undefined,
        this.minDaysInStatus ?? undefined,
        this.onlyAsMainAuthor || undefined,
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

  private persistState() {
    const filters = this.state.filters;
    filters.title = this.title;
    filters.journal = this.journal;
    filters.status = this.status;
    filters.minDaysInStatus = this.minDaysInStatus;
    filters.onlyAsMainAuthor = this.onlyAsMainAuthor;
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

  // --- Navegación y acciones ---

  openCreate() {
    this.router.navigate(['/publications/create']);
  }

  openDetail(publicationId: string) {
    this.router.navigate(['/publications', publicationId]);
  }

  // Edición directa desde la tabla: se evita que el clic propague a la fila (detalle).
  openEdit(event: Event, publicationId: string) {
    event.stopPropagation();
    this.router.navigate(['/publications', publicationId, 'edit']);
  }

  // Borrado directo desde la tabla con confirmación previa. El listado solo contiene
  // publicaciones del usuario (creador o autor), por lo que no requiere comprobación extra.
  confirmDelete(event: Event, publication: PublicationSummaryResponse) {
    event.stopPropagation();
    this.confirmationService.confirm({
      header: this.translate.instant('PUBLICATIONS.DELETE.CONFIRM_HEADER'),
      message: this.translate.instant('PUBLICATIONS.DELETE.CONFIRM_MESSAGE', {
        title: publication.title,
      }),
      acceptLabel: this.translate.instant('PUBLICATIONS.DELETE.CONFIRM_YES'),
      rejectLabel: this.translate.instant('PUBLICATIONS.DELETE.CONFIRM_NO'),
      accept: () => this.deletePublication(publication.id),
    });
  }

  private deletePublication(publicationId: string) {
    this.publicationsService.deletePublication(publicationId).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: this.translate.instant('PUBLICATIONS.TOASTS.SUCCESS'),
          detail: this.translate.instant('PUBLICATIONS.DELETE.SUCCESS'),
        });
        this.load();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: this.translate.instant('PUBLICATIONS.TOASTS.ERROR'),
          detail: this.translate.instant('PUBLICATIONS.DELETE.ERROR'),
        });
      },
    });
  }

  statusSeverity(
    status: PublicationSummaryResponse['status'],
  ): 'success' | 'info' | 'warn' | 'danger' {
    return publicationStatusSeverity(status);
  }

  /** Indica si la publicación tiene transiciones de estado disponibles. */
  canChangeStatus(status: PublicationSummaryResponse['status']): boolean {
    return allowedStatusTransitions(status).length > 0;
  }

  /** Una publicación rechazada puede reenviarse a otro journal. */
  canResubmit(status: PublicationSummaryResponse['status']): boolean {
    return status === PublicationStatus.Rejected;
  }

  // Abre el diálogo de reenvío para la publicación rechazada de la fila.
  openResubmit(event: Event, publication: PublicationSummaryResponse) {
    event.stopPropagation();
    this.resubmitTarget.set(publication);
    this.resubmitDialogVisible.set(true);
  }

  onResubmitted() {
    this.load();
  }

  // Acción rápida por fila: abre el menú con las transiciones válidas para esa publicación.
  openStatusMenu(event: Event, publication: PublicationSummaryResponse) {
    event.stopPropagation();
    this.statusMenuItems.set(
      allowedStatusTransitions(publication.status).map((status) => ({
        label: this.translate.instant(`PUBLICATIONS.STATUS.${status}`),
        command: () => this.promptStatusChange(publication.id, status),
      })),
    );
    this.statusMenu().toggle(event);
  }

  // Abre el diálogo de confirmación (con comentario opcional) antes de cambiar el estado.
  private promptStatusChange(publicationId: string, status: PublicationStatus) {
    this.changeStatusTargetId.set(publicationId);
    this.changeStatusTargetStatus.set(status);
    this.changeStatusDialogVisible.set(true);
  }

  onStatusChanged() {
    this.load();
  }
}
