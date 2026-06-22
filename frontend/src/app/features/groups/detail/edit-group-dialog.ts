import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, model, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { Textarea } from 'primeng/textarea';
import { GroupsService } from '@core/api';

@Component({
  selector: 'record-edit-group-dialog',
  imports: [ReactiveFormsModule, TranslatePipe, Dialog, InputText, Textarea, Button],
  templateUrl: './edit-group-dialog.html',
})
export class EditGroupDialog {
  readonly groupId = input.required<string>();
  readonly name = input<string>('');
  readonly description = input<string | undefined>(undefined);
  readonly visible = model<boolean>(false);
  /** Emitted after a successful update so the parent can refresh the group. */
  readonly saved = output<void>();

  private readonly fb = inject(FormBuilder);
  private readonly groupsService = inject(GroupsService);
  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);

  readonly isSaving = signal(false);

  editForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(255)]],
    description: ['', [Validators.maxLength(2000)]],
  });

  onShow() {
    this.editForm.reset({
      name: this.name(),
      description: this.description() ?? '',
    });
  }

  onSave() {
    if (this.editForm.invalid) return;

    this.isSaving.set(true);
    const { name, description } = this.editForm.value;

    this.groupsService
      .updateGroup(this.groupId(), { name: name!, description: description ?? undefined })
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: this.translate.instant('GROUPS.DETAIL.EDIT.TOAST_SUCCESS_SUMMARY'),
            detail: this.translate.instant('GROUPS.DETAIL.EDIT.TOAST_SUCCESS_DETAIL'),
          });
          this.isSaving.set(false);
          this.visible.set(false);
          this.saved.emit();
        },
        error: (err: HttpErrorResponse) => {
          this.isSaving.set(false);
          const code = (err.error as { code?: string } | null)?.code;
          if (code === 'GROUP_NAME_ALREADY_EXISTS') {
            this.editForm.controls.name.setErrors({ nameTaken: true });
            return;
          }
          const detailKey =
            code === 'GROUP_MEMBER_NOT_ADMIN'
              ? 'GROUPS.DETAIL.EDIT.TOAST_ERROR_FORBIDDEN'
              : 'GROUPS.DETAIL.EDIT.TOAST_ERROR_GENERIC';
          this.messageService.add({
            severity: 'error',
            summary: this.translate.instant('GROUPS.DETAIL.EDIT.TOAST_ERROR_SUMMARY'),
            detail: this.translate.instant(detailKey),
          });
        },
      });
  }

  close() {
    this.visible.set(false);
  }
}
