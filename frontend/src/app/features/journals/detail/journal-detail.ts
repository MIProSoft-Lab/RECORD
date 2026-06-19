import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { JournalDetailResponse, JournalsService, Quartile } from '@core/api';

@Component({
  selector: 'record-journal-detail',
  imports: [TranslatePipe, Button, TableModule, Tag],
  templateUrl: './journal-detail.html',
})
export class JournalDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly journalsService = inject(JournalsService);
  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);

  readonly journal = signal<JournalDetailResponse | null>(null);
  readonly isLoading = signal(false);

  ngOnInit() {
    const journalId = this.route.snapshot.paramMap.get('id');
    if (!journalId) {
      this.router.navigate(['/journals']);
      return;
    }
    this.loadJournal(journalId);
  }

  loadJournal(journalId: string) {
    this.isLoading.set(true);
    this.journalsService.getJournalDetail(journalId).subscribe({
      next: (journal) => {
        this.journal.set(journal);
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        const key =
          err.status === 404
            ? 'JOURNALS.DETAIL.ERRORS.NOT_FOUND'
            : 'JOURNALS.DETAIL.ERRORS.GENERIC';
        this.messageService.add({
          severity: 'error',
          summary: this.translate.instant('JOURNALS.TITLE'),
          detail: this.translate.instant(key),
        });
        this.router.navigate(['/journals']);
      },
    });
  }

  goBack() {
    this.router.navigate(['/journals']);
  }

  /** Mark/unmark the current journal as interest, flipping optimistically and reverting on error. */
  toggleInterest() {
    const current = this.journal();
    if (!current) {
      return;
    }
    const next = !current.isInterest;
    this.journal.set({ ...current, isInterest: next });

    const request = next
      ? this.journalsService.markJournalAsInterest(current.id)
      : this.journalsService.unmarkJournalAsInterest(current.id);

    request.subscribe({
      error: () => {
        this.journal.set({ ...current, isInterest: current.isInterest });
        this.messageService.add({
          severity: 'error',
          summary: this.translate.instant('JOURNALS.TITLE'),
          detail: this.translate.instant('JOURNALS.ACTIONS.ERROR'),
        });
      },
    });
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
