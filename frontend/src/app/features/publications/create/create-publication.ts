import { Component, OnInit, effect, inject, signal, untracked } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import {
  AutoComplete,
  AutoCompleteCompleteEvent,
  AutoCompleteSelectEvent,
} from 'primeng/autocomplete';
import { Avatar } from 'primeng/avatar';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { Tag } from 'primeng/tag';
import { Textarea } from 'primeng/textarea';
import { Tooltip } from 'primeng/tooltip';
import {
  GroupSummaryResponse,
  GroupsService,
  JournalSummaryResponse,
  JournalsService,
  PublicationAuthorInput,
  PublicationStatus,
  PublicationsService,
  UserSummaryResponse,
  UsersService,
} from '@core/api';
import { UserState } from '@core/services/user-state';

const JOURNAL_SEARCH_PAGE_SIZE = 10;

/** Sugerencia de autor interno con el nombre completo precalculado para mostrar. */
interface AuthorOption extends UserSummaryResponse {
  fullName: string;
}

/** Autor añadido (interno o externo), mostrado como tarjeta reordenable en columna. */
interface SelectedAuthor {
  /** Clave estable para seguimiento y eliminación. */
  key: string;
  type: 'INTERNAL' | 'EXTERNAL';
  userId?: string;
  firstName: string;
  lastName: string;
  email?: string;
  profileImageUrl?: string;
  /** El creador: reordenable pero no eliminable. */
  isCreator?: boolean;
}

@Component({
  selector: 'record-create-publication',
  imports: [
    ReactiveFormsModule,
    TranslatePipe,
    AutoComplete,
    Avatar,
    Button,
    InputText,
    Select,
    Tag,
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
  private readonly usersService = inject(UsersService);
  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);
  private readonly router = inject(Router);
  private readonly userState = inject(UserState);

  groups = signal<GroupSummaryResponse[]>([]);
  journalSuggestions = signal<JournalSummaryResponse[]>([]);
  authorSuggestions = signal<AuthorOption[]>([]);
  /** Co-autores añadidos (internos y externos), mostrados como tarjetas en columna. */
  selectedAuthors = signal<SelectedAuthor[]>([]);
  isSaving = signal(false);

  /** Usuario actual (el creador). */
  readonly currentUser = this.userState.currentUser;

  // Inserta al creador como primer autor (reordenable, no eliminable) en cuanto se
  // conoce el usuario actual. Solo se añade una vez.
  private readonly seedCreatorEffect = effect(() => {
    const me = this.currentUser();
    if (!me) return;
    untracked(() => {
      if (this.selectedAuthors().some((author) => author.isCreator)) return;
      this.selectedAuthors.update((authors) => [
        {
          key: me.id,
          type: 'INTERNAL',
          userId: me.id,
          firstName: me.firstName,
          lastName: me.lastName,
          email: me.email,
          profileImageUrl: me.profileImageUrl,
          isCreator: true,
        },
        ...authors,
      ]);
    });
  });

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
    // Controles transitorios para gestionar autores (no se envían tal cual).
    authorSearch: [null as AuthorOption | null],
    externalFirstName: ['', [Validators.maxLength(255)]],
    externalLastName: ['', [Validators.maxLength(255)]],
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

  get canAddExternal(): boolean {
    return (
      !!this.createForm.controls.externalFirstName.value?.trim() &&
      !!this.createForm.controls.externalLastName.value?.trim()
    );
  }

  onJournalSearch(event: AutoCompleteCompleteEvent) {
    this.journalsService
      .searchJournals(event.query, undefined, undefined, 0, JOURNAL_SEARCH_PAGE_SIZE)
      .subscribe({
        next: (page) => this.journalSuggestions.set(page.content),
        error: () => this.journalSuggestions.set([]),
      });
  }

  onAuthorSearch(event: AutoCompleteCompleteEvent) {
    const currentUserId = this.currentUser()?.id;
    const selectedUserIds = this.selectedAuthors()
      .filter((a) => a.type === 'INTERNAL')
      .map((a) => a.userId);

    // Búsqueda global: un co-autor interno puede ser cualquier usuario registrado.
    this.usersService.searchUsers(event.query).subscribe({
      next: (users) =>
        this.authorSuggestions.set(
          users
            // Se excluye al propio creador (ya figura como autor) y a los ya seleccionados.
            .filter((u) => u.id !== currentUserId && !selectedUserIds.includes(u.id))
            .map((u) => ({ ...u, fullName: `${u.firstName} ${u.lastName}` })),
        ),
      error: () => this.authorSuggestions.set([]),
    });
  }

  onAuthorSelect(event: AutoCompleteSelectEvent) {
    const user = event.value as AuthorOption;
    if (!this.selectedAuthors().some((a) => a.type === 'INTERNAL' && a.userId === user.id)) {
      this.selectedAuthors.update((authors) => [
        ...authors,
        {
          key: user.id,
          type: 'INTERNAL',
          userId: user.id,
          firstName: user.firstName,
          lastName: user.lastName,
          email: user.email,
          profileImageUrl: user.profileImageUrl,
        },
      ]);
    }
    // Se limpia el buscador para que quede listo para añadir otro autor.
    this.createForm.controls.authorSearch.reset(null);
    this.authorSuggestions.set([]);
  }

  addExternalAuthor() {
    const firstName = this.createForm.controls.externalFirstName.value?.trim();
    const lastName = this.createForm.controls.externalLastName.value?.trim();
    if (!firstName || !lastName) return;

    this.selectedAuthors.update((authors) => [
      ...authors,
      { key: crypto.randomUUID(), type: 'EXTERNAL', firstName, lastName },
    ]);
    this.createForm.controls.externalFirstName.reset('');
    this.createForm.controls.externalLastName.reset('');
  }

  removeAuthor(key: string) {
    this.selectedAuthors.update((authors) => authors.filter((a) => a.key !== key));
  }

  /** Mueve un autor una posición arriba (-1) o abajo (+1). */
  moveAuthor(index: number, direction: -1 | 1) {
    const authors = this.selectedAuthors();
    const target = index + direction;
    if (target < 0 || target >= authors.length) return;

    const reordered = [...authors];
    [reordered[index], reordered[target]] = [reordered[target], reordered[index]];
    this.selectedAuthors.set(reordered);
  }

  initialsFor(person: { firstName?: string; lastName?: string }): string {
    return ((person.firstName?.[0] ?? '') + (person.lastName?.[0] ?? '')).toUpperCase() || '?';
  }

  onSave() {
    if (this.createForm.invalid) return;

    this.isSaving.set(true);
    const value = this.createForm.value;
    const authors: PublicationAuthorInput[] = this.selectedAuthors().map((author) =>
      author.type === 'INTERNAL'
        ? { userId: author.userId }
        : { firstName: author.firstName, lastName: author.lastName },
    );

    this.publicationsService
      .createPublication({
        title: value.title!,
        groupId: value.groupId!,
        journalId: value.journal!.id,
        abstract: value.abstract || undefined,
        status: value.status!,
        doi: this.isPublished ? value.doi || undefined : undefined,
        authors,
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
