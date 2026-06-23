import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { AutoComplete, AutoCompleteCompleteEvent } from 'primeng/autocomplete';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { Textarea } from 'primeng/textarea';
import { Tooltip } from 'primeng/tooltip';
import {
  GroupSummaryResponse,
  GroupsService,
  JournalSummaryResponse,
  JournalsService,
  PublicationStatus,
  PublicationsService,
} from '@core/api';

const JOURNAL_SEARCH_PAGE_SIZE = 10;

@Component({
  selector: 'record-create-publication',
  imports: [
    ReactiveFormsModule,
    TranslatePipe,
    AutoComplete,
    Button,
    InputText,
    Select,
    Textarea,
    Tooltip,
  ],
  templateUrl: './create-publication.html',
})
export class CreatePublication implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly publicationsService = inject(PublicationsService);
  private readonly groupsService = inject(GroupsService);
  private readonly journalsService = inject(JournalsService);
  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);
  private readonly router = inject(Router);

  groups = signal<GroupSummaryResponse[]>([]);
  journalSuggestions = signal<JournalSummaryResponse[]>([]);
  isSaving = signal(false);

  readonly statusOptions = Object.values(PublicationStatus).map((value) => ({
    value,
    label: `PUBLICATIONS.STATUS.${value}`,
  }));

  createForm = this.fb.group({
    groupId: ['', Validators.required],
    title: ['', [Validators.required, Validators.maxLength(512)]],
    abstract: ['', [Validators.maxLength(5000)]],
    journal: [null as JournalSummaryResponse | null, Validators.required],
    status: [PublicationStatus.Planned as PublicationStatus, Validators.required],
    doi: [''],
  });

  ngOnInit() {
    this.groupsService.listGroups().subscribe({
      next: (groups) => this.groups.set(groups),
      error: () => this.groups.set([]),
    });

    // El DOI solo aplica cuando la publicación está PUBLISHED; al cambiar de estado se limpia.
    this.createForm.controls.status.valueChanges.subscribe((status) => {
      if (status !== PublicationStatus.Published) {
        this.createForm.controls.doi.reset('');
      }
    });
  }

  get isPublished(): boolean {
    return this.createForm.controls.status.value === PublicationStatus.Published;
  }

  onJournalSearch(event: AutoCompleteCompleteEvent) {
    this.journalsService
      .searchJournals(event.query, undefined, undefined, 0, JOURNAL_SEARCH_PAGE_SIZE)
      .subscribe({
        next: (page) => this.journalSuggestions.set(page.content),
        error: () => this.journalSuggestions.set([]),
      });
  }

  onSave() {
    if (this.createForm.invalid) return;

    this.isSaving.set(true);
    const value = this.createForm.value;

    this.publicationsService
      .createPublication({
        title: value.title!,
        groupId: value.groupId!,
        journalId: value.journal!.id,
        abstract: value.abstract || undefined,
        status: value.status!,
        doi: this.isPublished ? value.doi || undefined : undefined,
      })
      .subscribe({
        next: (created) => {
          this.messageService.add({
            severity: 'success',
            summary: this.translate.instant('PUBLICATIONS.TOASTS.SUCCESS'),
            detail: this.translate.instant('PUBLICATIONS.TOASTS.CREATED'),
          });
          this.router.navigate(['/publications', created.id]);
        },
        error: () => {
          this.isSaving.set(false);
        },
      });
  }

  onDiscard() {
    this.router.navigate(['/publications']);
  }
}
