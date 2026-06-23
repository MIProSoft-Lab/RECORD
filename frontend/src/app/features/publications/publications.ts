import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { Button } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { PublicationStatus, PublicationSummaryResponse, PublicationsService } from '@core/api';

/** Show the loading indicator only if the request is still pending after this delay. */
const LOADER_DELAY_MS = 250;

@Component({
  selector: 'record-publications',
  imports: [TranslatePipe, DatePipe, Button, TableModule, Tag],
  templateUrl: './publications.html',
})
export class Publications implements OnInit {
  private readonly publicationsService = inject(PublicationsService);
  private readonly router = inject(Router);

  publications = signal<PublicationSummaryResponse[]>([]);
  isLoading = signal(false);
  showLoader = signal(false);
  hasLoaded = signal(false);

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

  statusSeverity(
    status: PublicationSummaryResponse['status'],
  ): 'success' | 'info' | 'warn' | 'danger' {
    switch (status) {
      case PublicationStatus.Published:
      case PublicationStatus.Accepted:
        return 'success';
      case PublicationStatus.Rejected:
        return 'danger';
      case PublicationStatus.UnderReview:
      case PublicationStatus.MinorRevision:
      case PublicationStatus.MajorRevision:
        return 'warn';
      default:
        return 'info';
    }
  }
}
