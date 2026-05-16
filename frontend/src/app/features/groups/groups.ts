import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { Textarea } from 'primeng/textarea';
import { MessageService } from 'primeng/api';
import { GroupsService } from '@core/api';

@Component({
  selector: 'record-groups',
  imports: [TranslatePipe, Button, Dialog, InputText, Textarea, ReactiveFormsModule],
  templateUrl: './groups.html',
})
export class Groups {
  private readonly fb = inject(FormBuilder);
  private readonly groupsService = inject(GroupsService);
  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);

  showCreateDialog = signal(false);
  isSaving = signal(false);

  createForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(255)]],
    description: ['', [Validators.maxLength(2000)]],
  });

  openCreateDialog() {
    this.createForm.reset();
    this.showCreateDialog.set(true);
  }

  closeCreateDialog() {
    this.showCreateDialog.set(false);
  }

  onCreateGroup() {
    if (this.createForm.invalid) return;

    this.isSaving.set(true);
    const { name, description } = this.createForm.value;

    this.groupsService
      .createGroup({ name: name!, description: description ?? undefined })
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: this.translate.instant('GROUPS.TOASTS.SUCCESS'),
            detail: this.translate.instant('GROUPS.TOASTS.GROUP_CREATED'),
          });
          this.showCreateDialog.set(false);
          this.isSaving.set(false);
        },
        error: () => {
          this.isSaving.set(false);
        },
      });
  }
}
