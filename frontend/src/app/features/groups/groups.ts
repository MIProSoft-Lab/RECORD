import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { Textarea } from 'primeng/textarea';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { MessageService } from 'primeng/api';
import { GroupsService, GroupSummaryResponse } from '@core/api';

/** Show the loading indicator only if the request is still pending after this delay. */
const LOADER_DELAY_MS = 250;

@Component({
  selector: 'record-groups',
  imports: [
    TranslatePipe,
    Button,
    Dialog,
    InputText,
    Textarea,
    TableModule,
    Tag,
    ReactiveFormsModule,
  ],
  templateUrl: './groups.html',
})
export class Groups implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly groupsService = inject(GroupsService);
  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);
  private readonly router = inject(Router);

  groups = signal<GroupSummaryResponse[]>([]);
  isLoading = signal(false);
  showLoader = signal(false);
  hasLoaded = signal(false);
  showCreateDialog = signal(false);
  isSaving = signal(false);

  createForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(255)]],
    description: ['', [Validators.maxLength(2000)]],
  });

  ngOnInit() {
    this.loadGroups();
  }

  loadGroups() {
    this.isLoading.set(true);
    const loaderTimer = setTimeout(() => {
      if (this.isLoading()) this.showLoader.set(true);
    }, LOADER_DELAY_MS);

    this.groupsService.listGroups().subscribe({
      next: (groups) => {
        clearTimeout(loaderTimer);
        this.groups.set(groups);
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

  openGroupDetail(groupId: string) {
    this.router.navigate(['/groups', groupId]);
  }

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
          this.loadGroups();
        },
        error: () => {
          this.isSaving.set(false);
        },
      });
  }
}
