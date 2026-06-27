import { DatePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { Avatar } from 'primeng/avatar';
import { Button } from 'primeng/button';
import { Tag } from 'primeng/tag';
import { PublicationResponse, PublicationsService } from '@core/api';
import { UserState } from '@core/services/user-state';
import { BreadcrumbService } from '@shared/services/breadcrumb.service';

@Component({
  selector: 'record-publication-detail',
  imports: [TranslatePipe, DatePipe, Avatar, Button, Tag],
  templateUrl: './publication-detail.html',
})
export class PublicationDetail implements OnInit, OnDestroy {
  private readonly publicationsService = inject(PublicationsService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly userState = inject(UserState);
  private readonly breadcrumbService = inject(BreadcrumbService);

  private breadcrumbUrl: string | null = null;

  publication = signal<PublicationResponse | null>(null);
  isLoading = signal(true);

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

  initialsFor(person: { firstName?: string; lastName?: string }): string {
    return ((person.firstName?.[0] ?? '') + (person.lastName?.[0] ?? '')).toUpperCase() || '?';
  }
}
