import { DatePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, computed, inject, signal, viewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ConfirmationService, MenuItem, MessageService } from 'primeng/api';
import { Avatar } from 'primeng/avatar';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';
import { Tag } from 'primeng/tag';
import { Timeline } from 'primeng/timeline';
import { Tooltip } from 'primeng/tooltip';
import {
  JournalDetailResponse,
  JournalsService,
  PublicationAuthorResponse,
  PublicationStatus,
  PublicationResponse,
  PublicationsService,
  Quartile,
} from '@core/api';
import { UserState } from '@core/services/user-state';
import { RelativeDurationPipe } from '@shared/pipes/relative-duration.pipe';
import { BreadcrumbService } from '@shared/services/breadcrumb.service';
import {
  allowedStatusTransitions,
  isFinalStatus,
  publicationStatusSeverity,
} from '../publication-status';
import { ChangeStatusDialog } from '../change-status/change-status-dialog';
import { LinkAuthorDialog } from '../link-author/link-author-dialog';
import { ResubmitPublicationDialog } from '../resubmit/resubmit-publication-dialog';

@Component({
  selector: 'record-publication-detail',
  imports: [
    TranslatePipe,
    DatePipe,
    RelativeDurationPipe,
    Avatar,
    Button,
    Menu,
    Tag,
    Timeline,
    Tooltip,
    ChangeStatusDialog,
    LinkAuthorDialog,
    ResubmitPublicationDialog,
  ],
  templateUrl: './publication-detail.html',
  styles: `
    .journal-card {
      cursor: pointer;
      transition:
        border-color 0.2s,
        box-shadow 0.2s,
        transform 0.2s;
    }
    .journal-card:hover {
      border-color: var(--p-primary-color);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      transform: translateY(-1px);
    }

    /* Marcador minimalista. Base: aro hueco (estado actual no terminal). */
    .timeline-marker {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 0.85rem;
      height: 0.85rem;
      border-radius: 50%;
      border: 2px solid var(--p-primary-color);
      background: var(--p-content-background, var(--p-surface-0));
    }
    /* Estado ya superado (no terminal): aro con un punto pequeño dentro. */
    .timeline-marker__inner {
      width: 0.35rem;
      height: 0.35rem;
      border-radius: 50%;
      background: var(--p-primary-color);
    }
    /* Estado terminal (REJECTED/PUBLISHED): punto completamente relleno. */
    .timeline-marker--final {
      background: var(--p-primary-color);
    }
  `,
})
export class PublicationDetail implements OnInit, OnDestroy {
  private readonly publicationsService = inject(PublicationsService);
  private readonly journalsService = inject(JournalsService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly userState = inject(UserState);
  private readonly breadcrumbService = inject(BreadcrumbService);
  private readonly translate = inject(TranslateService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly messageService = inject(MessageService);

  private readonly statusMenu = viewChild.required<Menu>('statusMenu');

  private breadcrumbUrl: string | null = null;

  /** Contexto de origen (p. ej. al abrir desde la pestaña de publicaciones de un grupo). */
  private returnUrl: string | null = null;
  private originLabel: string | null = null;

  publication = signal<PublicationResponse | null>(null);
  /** Detalle del journal asociado, cargado aparte para enriquecer la vista. */
  journal = signal<JournalDetailResponse | null>(null);
  isLoading = signal(true);
  statusMenuItems = signal<MenuItem[]>([]);
  resubmitDialogVisible = signal(false);

  /** Cambio de estado pendiente de confirmar (alimenta el diálogo de confirmación). */
  changeStatusTargetStatus = signal<PublicationStatus | null>(null);
  changeStatusDialogVisible = signal(false);

  /** Autor externo a convertir en interno (alimenta el diálogo de vinculación). */
  linkAuthorTarget = signal<PublicationAuthorResponse | null>(null);
  linkAuthorDialogVisible = signal(false);

  // Historial de estados enriquecido para la línea de tiempo: cada entrada guarda la fecha
  // de la transición anterior (para mostrar la duración transcurrida entre estados) y la
  // marca del estado actual. Se invierte para mostrar lo más reciente arriba.
  readonly timeline = computed(() => {
    const history = this.publication()?.statusHistory ?? [];
    return history
      .map((entry, index) => ({
        entry,
        previousChangedAt: index > 0 ? history[index - 1].changedAt : null,
        isCurrent: index === history.length - 1,
      }))
      .reverse();
  });

  // Mejor cuartil del journal en su año más reciente (Q1 es el mejor), para destacarlo.
  readonly bestQuartile = computed(() => {
    const quartiles = this.journal()?.categoryQuartiles;
    if (!quartiles?.length) return null;
    const latestYear = Math.max(...quartiles.map((q) => q.year));
    return (
      quartiles
        .filter((q) => q.year === latestYear)
        .sort((a, b) => a.quartile.localeCompare(b.quartile))[0] ?? null
    );
  });

  // El creador y cualquier autor interno pueden editar la publicación.
  readonly canEdit = computed(() => {
    const pub = this.publication();
    const userId = this.userState.currentUser()?.id;
    if (!pub || !userId) return false;
    return (
      pub.createdBy === userId ||
      (pub.authors ?? []).some((author) => author.type === 'INTERNAL' && author.userId === userId)
    );
  });

  // Hay transiciones de estado disponibles desde el estado actual.
  readonly canChangeStatus = computed(() => {
    const pub = this.publication();
    return !!pub && allowedStatusTransitions(pub.status).length > 0;
  });

  // Una publicación rechazada puede reenviarse a otro journal (solo creador/autor).
  readonly canResubmit = computed(() => {
    const pub = this.publication();
    return !!pub && pub.status === PublicationStatus.Rejected && this.canEdit();
  });

  ngOnInit() {
    const publicationId = this.route.snapshot.paramMap.get('id');
    if (!publicationId) {
      this.isLoading.set(false);
      return;
    }

    this.returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
    this.originLabel = this.route.snapshot.queryParamMap.get('originLabel');

    this.publicationsService.getPublicationDetail(publicationId).subscribe({
      next: (publication) => {
        this.publication.set(publication);
        this.isLoading.set(false);
        // La clave del breadcrumb es la ruta sin query string (el origen va como query param).
        this.breadcrumbUrl = this.router.url.split('?')[0];
        this.breadcrumbService.setDynamicLabel(this.breadcrumbUrl, publication.title);
        this.loadJournal(publication.journalId);
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }

  // Carga el detalle del journal para enriquecer la vista; si falla, se muestra solo el nombre.
  private loadJournal(journalId: string) {
    this.journalsService.getJournalDetail(journalId).subscribe({
      next: (journal) => this.journal.set(journal),
      error: () => this.journal.set(null),
    });
  }

  ngOnDestroy() {
    if (this.breadcrumbUrl) {
      this.breadcrumbService.clearDynamicLabel(this.breadcrumbUrl);
    }
  }

  goToEdit() {
    const pub = this.publication();
    if (pub) {
      // Se propaga el contexto de origen para que la edición y la vuelta sigan ligadas al grupo.
      this.router.navigate(['/publications', pub.id, 'edit'], {
        queryParams: this.originQueryParams(),
      });
    }
  }

  /** Query params de origen a propagar, o undefined si la publicación no se abrió con contexto. */
  private originQueryParams(): Record<string, string> | undefined {
    if (!this.returnUrl || !this.originLabel) return undefined;
    return { returnUrl: this.returnUrl, originLabel: this.originLabel };
  }

  // Borrado definitivo con confirmación previa. Solo accesible al creador y autores (canEdit).
  confirmDelete() {
    const pub = this.publication();
    if (!pub) return;
    this.confirmationService.confirm({
      header: this.translate.instant('PUBLICATIONS.DELETE.CONFIRM_HEADER'),
      message: this.translate.instant('PUBLICATIONS.DELETE.CONFIRM_MESSAGE', { title: pub.title }),
      acceptLabel: this.translate.instant('PUBLICATIONS.DELETE.CONFIRM_YES'),
      rejectLabel: this.translate.instant('PUBLICATIONS.DELETE.CONFIRM_NO'),
      accept: () => {
        this.publicationsService.deletePublication(pub.id).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: this.translate.instant('PUBLICATIONS.TOASTS.SUCCESS'),
              detail: this.translate.instant('PUBLICATIONS.DELETE.SUCCESS'),
            });
            // Tras borrar se vuelve al origen (el grupo) si se abrió desde ahí; si no, al listado.
            if (this.returnUrl) {
              this.router.navigateByUrl(this.returnUrl);
            } else {
              this.router.navigate(['/publications']);
            }
          },
          error: () => {
            this.messageService.add({
              severity: 'error',
              summary: this.translate.instant('PUBLICATIONS.TOASTS.ERROR'),
              detail: this.translate.instant('PUBLICATIONS.DELETE.ERROR'),
            });
          },
        });
      },
    });
  }

  // Abre el menú con las transiciones de estado válidas desde el estado actual.
  openStatusMenu(event: Event) {
    const pub = this.publication();
    if (!pub) return;
    this.statusMenuItems.set(
      allowedStatusTransitions(pub.status).map((status) => ({
        label: this.translate.instant(`PUBLICATIONS.STATUS.${status}`),
        command: () => this.promptStatusChange(status),
      })),
    );
    this.statusMenu().toggle(event);
  }

  // Abre el diálogo de confirmación (con comentario opcional) antes de cambiar el estado.
  private promptStatusChange(status: PublicationStatus) {
    this.changeStatusTargetStatus.set(status);
    this.changeStatusDialogVisible.set(true);
  }

  onStatusChanged(updated: PublicationResponse) {
    this.publication.set(updated);
  }

  // Abre el diálogo para vincular un autor externo a un usuario registrado (convertirlo en interno).
  openLinkAuthor(author: PublicationAuthorResponse) {
    this.linkAuthorTarget.set(author);
    this.linkAuthorDialogVisible.set(true);
  }

  // Tras la conversión, la respuesta trae la lista de autores ya actualizada: se refresca in situ.
  onAuthorConverted(updated: PublicationResponse) {
    this.publication.set(updated);
  }

  openResubmit() {
    this.resubmitDialogVisible.set(true);
  }

  onResubmitted(updated: PublicationResponse) {
    this.publication.set(updated);
    // El journal puede haber cambiado tras un reenvío: se recarga su detalle.
    this.journal.set(null);
    this.loadJournal(updated.journalId);
  }

  goToJournal() {
    const pub = this.publication();
    if (pub) this.router.navigate(['/journals', pub.journalId]);
  }

  statusSeverity(status: PublicationResponse['status']): 'success' | 'info' | 'warn' | 'danger' {
    return publicationStatusSeverity(status);
  }

  /** Un estado final (REJECTED/PUBLISHED) se representa con un punto relleno. */
  isFinal(status: PublicationStatus): boolean {
    return isFinalStatus(status);
  }

  /**
   * Variante del marcador de la línea de tiempo:
   * - `final`: estado terminal (relleno completo), aunque haya sido superado (rechazo→reenvío).
   * - `current`: estado actual no terminal (aro hueco).
   * - `passed`: estado intermedio ya superado (aro con punto dentro).
   */
  markerVariant(status: PublicationStatus, isCurrent: boolean): 'final' | 'current' | 'passed' {
    if (isFinalStatus(status)) return 'final';
    return isCurrent ? 'current' : 'passed';
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

  initialsFor(person: { firstName?: string; lastName?: string }): string {
    return ((person.firstName?.[0] ?? '') + (person.lastName?.[0] ?? '')).toUpperCase() || '?';
  }
}
