import { DatePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { Tag } from 'primeng/tag';
import { PublicationResponse, PublicationsService } from '@core/api';
import { BreadcrumbService } from '@shared/services/breadcrumb.service';

@Component({
  selector: 'record-publication-detail',
  imports: [TranslatePipe, DatePipe, Tag],
  templateUrl: './publication-detail.html',
})
export class PublicationDetail implements OnInit, OnDestroy {
  private readonly publicationsService = inject(PublicationsService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly breadcrumbService = inject(BreadcrumbService);

  private breadcrumbUrl: string | null = null;

  publication = signal<PublicationResponse | null>(null);
  isLoading = signal(true);

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
}
