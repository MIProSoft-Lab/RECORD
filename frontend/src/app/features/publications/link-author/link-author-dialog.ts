import { Component, computed, inject, input, model, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { AutoComplete, AutoCompleteCompleteEvent } from 'primeng/autocomplete';
import { Avatar } from 'primeng/avatar';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import {
  PublicationAuthorInput,
  PublicationAuthorResponse,
  PublicationResponse,
  PublicationStatus,
  PublicationsService,
  UserSummaryResponse,
  UsersService,
} from '@core/api';

/** Sugerencia de usuario con el nombre completo precalculado para mostrar. */
interface AuthorOption extends UserSummaryResponse {
  fullName: string;
}

/**
 * Diálogo para convertir un autor externo en interno vinculándolo a un usuario registrado.
 * Reutiliza el buscador de usuarios (`p-autoComplete` + `UsersService.searchUsers`) y guarda
 * con `updatePublication`: la entrada externa se reemplaza in situ —en la misma posición—
 * por un autor interno, preservando el resto de la lista. La validación de fondo (usuario
 * existe, dedup de internos, creador preservado) la realiza el backend en `UpdatePublication`.
 */
@Component({
  selector: 'record-link-author-dialog',
  imports: [FormsModule, TranslatePipe, Dialog, AutoComplete, Avatar, Button],
  templateUrl: './link-author-dialog.html',
})
export class LinkAuthorDialog {
  readonly publication = input.required<PublicationResponse>();
  readonly externalAuthor = input.required<PublicationAuthorResponse>();
  readonly visible = model<boolean>(false);
  /** Emite la publicación actualizada tras una conversión correcta. */
  readonly converted = output<PublicationResponse>();

  private readonly publicationsService = inject(PublicationsService);
  private readonly usersService = inject(UsersService);
  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);

  readonly userSuggestions = signal<AuthorOption[]>([]);
  readonly selectedUser = signal<AuthorOption | string | null>(null);
  readonly isSaving = signal(false);

  /** Usuario seleccionado válido (objeto), o null si aún es texto o no hay selección. */
  private readonly selectedUserEntity = computed(() => {
    const value = this.selectedUser();
    return value && typeof value === 'object' ? value : null;
  });

  readonly canConfirm = computed(() => !!this.selectedUserEntity() && !this.isSaving());

  onUserSearch(event: AutoCompleteCompleteEvent) {
    const pub = this.publication();
    // Se excluye al creador y a los usuarios que ya figuran como autores internos: vincular a
    // uno de ellos no tendría efecto (el backend deduplica) y solo confundiría al usuario.
    const existingUserIds = new Set(
      (pub.authors ?? [])
        .filter((author) => author.type === 'INTERNAL' && author.userId)
        .map((author) => author.userId),
    );
    existingUserIds.add(pub.createdBy);

    this.usersService.searchUsers(event.query).subscribe({
      next: (users) =>
        this.userSuggestions.set(
          users
            .filter((user) => !existingUserIds.has(user.id))
            .map((user) => ({ ...user, fullName: `${user.firstName} ${user.lastName}` })),
        ),
      error: () => this.userSuggestions.set([]),
    });
  }

  initialsFor(person: { firstName?: string; lastName?: string }): string {
    return ((person.firstName?.[0] ?? '') + (person.lastName?.[0] ?? '')).toUpperCase() || '?';
  }

  confirm() {
    const user = this.selectedUserEntity();
    const pub = this.publication();
    const targetAuthorId = this.externalAuthor().authorId;
    if (!user) return;

    // Se reconstruye la lista completa de autores reemplazando, en su misma posición, la
    // entrada externa objetivo por un autor interno; el resto se conserva tal cual.
    const authors: PublicationAuthorInput[] = (pub.authors ?? []).map((author) =>
      author.authorId === targetAuthorId
        ? { userId: user.id }
        : author.type === 'INTERNAL'
          ? { userId: author.userId }
          : { firstName: author.firstName, lastName: author.lastName },
    );

    // El DOI solo es válido cuando la publicación está PUBLISHED; en otro caso se omite.
    const doi = pub.status === PublicationStatus.Published ? pub.doi || undefined : undefined;

    this.isSaving.set(true);
    this.publicationsService
      .updatePublication(pub.id, {
        title: pub.title,
        abstract: pub.abstract || undefined,
        doi,
        authors,
      })
      .subscribe({
        next: (updated) => {
          this.isSaving.set(false);
          this.messageService.add({
            severity: 'success',
            summary: this.translate.instant('PUBLICATIONS.TOASTS.SUCCESS'),
            detail: this.translate.instant('PUBLICATIONS.LINK_AUTHOR.SUCCESS'),
          });
          this.converted.emit(updated);
          this.visible.set(false);
        },
        error: () => {
          this.isSaving.set(false);
          this.messageService.add({
            severity: 'error',
            summary: this.translate.instant('PUBLICATIONS.TOASTS.ERROR'),
            detail: this.translate.instant('PUBLICATIONS.LINK_AUTHOR.ERROR'),
          });
        },
      });
  }

  onHide() {
    // Estado limpio al cerrar para que reabrir empiece de cero.
    this.selectedUser.set(null);
    this.userSuggestions.set([]);
    this.isSaving.set(false);
  }
}
