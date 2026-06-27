import { DatePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, computed, inject, signal, viewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { MenuItem, MessageService } from 'primeng/api';
import { Avatar } from 'primeng/avatar';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';
import { Tag } from 'primeng/tag';
import { Tooltip } from 'primeng/tooltip';
import {
  JournalDetailResponse,
  JournalsService,
  PublicationStatus,
  PublicationResponse,
  PublicationsService,
  Quartile,
} from '@core/api';
import { UserState } from '@core/services/user-state';
import { BreadcrumbService } from '@shared/services/breadcrumb.service';
import { allowedStatusTransitions, publicationStatusSeverity } from '../publication-status';
import { ResubmitPublicationDialog } from '../resubmit/resubmit-publication-dialog';

@Component({
  selector: 'record-publication-detail',
  imports: [TranslatePipe, DatePipe, Avatar, Button, Menu, Tag, Tooltip, ResubmitPublicationDialog],
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
  `,
})
export class PublicationDetail implements OnInit, OnDestroy {
  private readonly publicationsService = inject(PublicationsService);
  private readonly journalsService = inject(JournalsService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly userState = inject(UserState);
  private readonly breadcrumbService = inject(BreadcrumbService);
  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);

  private readonly statusMenu = viewChild.required<Menu>('statusMenu');

  private breadcrumbUrl: string | null = null;

  publication = signal<PublicationResponse | null>(null);
  /** Detalle del journal asociado, cargado aparte para enriquecer la vista. */
  journal = signal<JournalDetailResponse | null>(null);
  isLoading = signal(true);
  statusMenuItems = signal<MenuItem[]>([]);
  resubmitDialogVisible = signal(false);

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

    this.publicationsService.getPublicationDetail(publicationId).subscribe({
      next: (publication) => {
        this.publication.set(publication);
        this.isLoading.set(false);
        this.breadcrumbUrl = this.router.url;
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
    if (pub) this.router.navigate(['/publications', pub.id, 'edit']);
  }

  // Abre el menú con las transiciones de estado válidas desde el estado actual.
  openStatusMenu(event: Event) {
    const pub = this.publication();
    if (!pub) return;
    this.statusMenuItems.set(
      allowedStatusTransitions(pub.status).map((status) => ({
        label: this.translate.instant(`PUBLICATIONS.STATUS.${status}`),
        command: () => this.changeStatus(pub.id, status),
      })),
    );
    this.statusMenu().toggle(event);
  }

  private changeStatus(publicationId: string, status: PublicationStatus) {
    this.publicationsService.changePublicationStatus(publicationId, { status }).subscribe({
      next: (updated) => {
        this.publication.set(updated);
        this.messageService.add({
          severity: 'success',
          summary: this.translate.instant('PUBLICATIONS.TOASTS.SUCCESS'),
          detail: this.translate.instant('PUBLICATIONS.TOASTS.STATUS_UPDATED'),
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: this.translate.instant('PUBLICATIONS.TOASTS.ERROR'),
          detail: this.translate.instant('PUBLICATIONS.CHANGE_STATUS.ERROR'),
        });
      },
    });
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
