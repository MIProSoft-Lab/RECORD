import { Component, inject, input, model, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { Tag } from 'primeng/tag';
import { Textarea } from 'primeng/textarea';
import { PublicationResponse, PublicationStatus, PublicationsService } from '@core/api';
import { publicationStatusSeverity } from '../publication-status';

/**
 * Diálogo de confirmación de un cambio de estado de publicación. Permite añadir un
 * comentario opcional a la transición. Realiza la llamada al backend y emite la
 * publicación actualizada. Lo usan tanto el detalle como la lista de publicaciones.
 */
@Component({
  selector: 'record-change-status-dialog',
  imports: [FormsModule, TranslatePipe, Dialog, Button, Tag, Textarea],
  templateUrl: './change-status-dialog.html',
})
export class ChangeStatusDialog {
  readonly publicationId = input.required<string>();
  /** Estado destino de la transición; cuando es null el diálogo no muestra contenido. */
  readonly targetStatus = input<PublicationStatus | null>(null);
  readonly visible = model<boolean>(false);
  /** Emite la publicación actualizada tras un cambio de estado correcto. */
  readonly changed = output<PublicationResponse>();

  private readonly publicationsService = inject(PublicationsService);
  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);

  readonly comment = signal('');
  readonly isSaving = signal(false);

  statusSeverity(status: PublicationStatus): 'success' | 'info' | 'warn' | 'danger' {
    return publicationStatusSeverity(status);
  }

  confirm() {
    const status = this.targetStatus();
    if (!status) return;

    this.isSaving.set(true);
    const comment = this.comment().trim();
    this.publicationsService
      .changePublicationStatus(this.publicationId(), {
        status,
        comment: comment || undefined,
      })
      .subscribe({
        next: (updated) => {
          this.isSaving.set(false);
          this.messageService.add({
            severity: 'success',
            summary: this.translate.instant('PUBLICATIONS.TOASTS.SUCCESS'),
            detail: this.translate.instant('PUBLICATIONS.TOASTS.STATUS_UPDATED'),
          });
          this.changed.emit(updated);
          this.visible.set(false);
        },
        error: () => {
          this.isSaving.set(false);
          this.messageService.add({
            severity: 'error',
            summary: this.translate.instant('PUBLICATIONS.TOASTS.ERROR'),
            detail: this.translate.instant('PUBLICATIONS.CHANGE_STATUS.ERROR'),
          });
        },
      });
  }

  onHide() {
    // Estado limpio al cerrar para que reabrir empiece de cero.
    this.comment.set('');
    this.isSaving.set(false);
  }
}
