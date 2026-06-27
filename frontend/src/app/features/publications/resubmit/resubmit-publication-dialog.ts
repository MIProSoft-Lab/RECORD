import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, model, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { AutoComplete, AutoCompleteCompleteEvent } from 'primeng/autocomplete';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import {
  JournalSummaryResponse,
  PublicationResponse,
  PublicationsService,
  JournalsService,
} from '@core/api';

const JOURNAL_SEARCH_PAGE_SIZE = 10;

/**
 * Diálogo para reenviar una publicación rechazada a otro journal. Solo se usa cuando la
 * publicación está en estado REJECTED; el journal destino debe ser distinto del actual.
 * Reutiliza el buscador de journals (`p-autoComplete` + `JournalsService.searchJournals`)
 * del formulario de creación.
 */
@Component({
  selector: 'record-resubmit-publication-dialog',
  imports: [FormsModule, TranslatePipe, Dialog, AutoComplete, Button],
  templateUrl: './resubmit-publication-dialog.html',
})
export class ResubmitPublicationDialog {
  readonly publicationId = input.required<string>();
  readonly currentJournalId = input.required<string>();
  readonly visible = model<boolean>(false);
  /** Emite la publicación actualizada tras un reenvío correcto. */
  readonly resubmitted = output<PublicationResponse>();

  private readonly publicationsService = inject(PublicationsService);
  private readonly journalsService = inject(JournalsService);
  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);

  readonly journalSuggestions = signal<JournalSummaryResponse[]>([]);
  readonly selectedJournal = signal<JournalSummaryResponse | string | null>(null);
  readonly isSaving = signal(false);

  /** Journal seleccionado válido (objeto), o null si aún es texto o no hay selección. */
  private readonly selectedJournalEntity = computed(() => {
    const value = this.selectedJournal();
    return value && typeof value === 'object' ? value : null;
  });

  /** El journal elegido coincide con el actual: no se permite reenviar al mismo. */
  readonly isSameJournal = computed(
    () => this.selectedJournalEntity()?.id === this.currentJournalId(),
  );

  readonly canConfirm = computed(
    () => !!this.selectedJournalEntity() && !this.isSameJournal() && !this.isSaving(),
  );

  onJournalSearch(event: AutoCompleteCompleteEvent) {
    this.journalsService
      .searchJournals(event.query, undefined, undefined, 0, JOURNAL_SEARCH_PAGE_SIZE)
      .subscribe({
        next: (page) => this.journalSuggestions.set(page.content),
        error: () => this.journalSuggestions.set([]),
      });
  }

  confirm() {
    const journal = this.selectedJournalEntity();
    if (!journal || this.isSameJournal()) return;

    this.isSaving.set(true);
    this.publicationsService
      .resubmitPublication(this.publicationId(), { journalId: journal.id })
      .subscribe({
        next: (updated) => {
          this.isSaving.set(false);
          this.messageService.add({
            severity: 'success',
            summary: this.translate.instant('PUBLICATIONS.TOASTS.SUCCESS'),
            detail: this.translate.instant('PUBLICATIONS.RESUBMIT.SUCCESS'),
          });
          this.resubmitted.emit(updated);
          this.visible.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.isSaving.set(false);
          const code = (err.error as { code?: string } | null)?.code;
          const detailKey =
            code === 'SAME_JOURNAL_RESUBMIT'
              ? 'PUBLICATIONS.RESUBMIT.SAME_JOURNAL'
              : 'PUBLICATIONS.RESUBMIT.ERROR';
          this.messageService.add({
            severity: 'error',
            summary: this.translate.instant('PUBLICATIONS.TOASTS.ERROR'),
            detail: this.translate.instant(detailKey),
          });
        },
      });
  }

  onHide() {
    // Estado limpio al cerrar para que reabrir empiece de cero.
    this.selectedJournal.set(null);
    this.journalSuggestions.set([]);
    this.isSaving.set(false);
  }
}
