import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal, viewChild } from '@angular/core';
import { Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ConfirmationService, MenuItem, MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Tooltip } from 'primeng/tooltip';
import { PublicationStatus, PublicationSummaryResponse, PublicationsService } from '@core/api';
import { DaysSincePipe } from '@shared/pipes/days-since.pipe';
import { allowedStatusTransitions, publicationStatusSeverity } from './publication-status';
import { ChangeStatusDialog } from './change-status/change-status-dialog';
import { ResubmitPublicationDialog } from './resubmit/resubmit-publication-dialog';

/** Show the loading indicator only if the request is still pending after this delay. */
const LOADER_DELAY_MS = 250;

@Component({
  selector: 'record-publications',
  imports: [
    TranslatePipe,
    DatePipe,
    DaysSincePipe,
    Button,
    Menu,
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
  private readonly router = inject(Router);
  private readonly translate = inject(TranslateService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly messageService = inject(MessageService);

  private readonly statusMenu = viewChild.required<Menu>('statusMenu');

  publications = signal<PublicationSummaryResponse[]>([]);
  isLoading = signal(false);
  showLoader = signal(false);
  hasLoaded = signal(false);
  statusMenuItems = signal<MenuItem[]>([]);

  /** Publicación rechazada seleccionada para reenviar (alimenta el diálogo). */
  resubmitTarget = signal<PublicationSummaryResponse | null>(null);
  resubmitDialogVisible = signal(false);

  /** Cambio de estado pendiente de confirmar (alimenta el diálogo de confirmación). */
  changeStatusTargetId = signal<string | null>(null);
  changeStatusTargetStatus = signal<PublicationStatus | null>(null);
  changeStatusDialogVisible = signal(false);

  ngOnInit() {
    this.loadPublications();
  }

  loadPublications() {
    this.isLoading.set(true);
    const loaderTimer = setTimeout(() => {
      if (this.isLoading()) this.showLoader.set(true);
    }, LOADER_DELAY_MS);

    this.publicationsService.listMyPublications().subscribe({
      next: (publications) => {
        clearTimeout(loaderTimer);
        this.publications.set(publications);
        this.isLoading.set(false);
        this.showLoader.set(false);
        this.hasLoaded.set(true);
      },
      error: () => {
        clearTimeout(loaderTimer);
        this.isLoading.set(false);
        this.showLoader.set(false);
        this.hasLoaded.set(true);
      },
    });
  }

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
        this.loadPublications();
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
    this.loadPublications();
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
    this.loadPublications();
  }
}
