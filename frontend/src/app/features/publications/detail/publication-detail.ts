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
import { PublicationStatus, PublicationResponse, PublicationsService } from '@core/api';
import { UserState } from '@core/services/user-state';
import { BreadcrumbService } from '@shared/services/breadcrumb.service';
import { allowedStatusTransitions, publicationStatusSeverity } from '../publication-status';

@Component({
  selector: 'record-publication-detail',
  imports: [TranslatePipe, DatePipe, Avatar, Button, Menu, Tag, Tooltip],
  templateUrl: './publication-detail.html',
})
export class PublicationDetail implements OnInit, OnDestroy {
  private readonly publicationsService = inject(PublicationsService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly userState = inject(UserState);
  private readonly breadcrumbService = inject(BreadcrumbService);
  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);

  private readonly statusMenu = viewChild.required<Menu>('statusMenu');

  private breadcrumbUrl: string | null = null;

  publication = signal<PublicationResponse | null>(null);
  isLoading = signal(true);
  statusMenuItems = signal<MenuItem[]>([]);

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
      },
      error: () => {
        this.isLoading.set(false);
      },
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

  statusSeverity(status: PublicationResponse['status']): 'success' | 'info' | 'warn' | 'danger' {
    return publicationStatusSeverity(status);
  }

  initialsFor(person: { firstName?: string; lastName?: string }): string {
    return ((person.firstName?.[0] ?? '') + (person.lastName?.[0] ?? '')).toUpperCase() || '?';
  }
}
