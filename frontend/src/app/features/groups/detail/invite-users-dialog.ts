import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, model, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { Avatar } from 'primeng/avatar';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { Skeleton } from 'primeng/skeleton';
import { catchError, debounceTime, distinctUntilChanged, of, switchMap, tap } from 'rxjs';
import { GroupsService, InvitableUserResponse } from '@core/api';

const MIN_QUERY_LENGTH = 2;
const DEBOUNCE_MS = 300;
/** Show the loading skeleton only if the request is still pending after this delay. */
const LOADER_DELAY_MS = 250;

@Component({
  selector: 'record-invite-users-dialog',
  imports: [FormsModule, TranslatePipe, Dialog, InputText, Button, Avatar, Skeleton],
  templateUrl: './invite-users-dialog.html',
})
export class InviteUsersDialog {
  readonly groupId = input.required<string>();
  readonly visible = model<boolean>(false);

  private readonly groupsService = inject(GroupsService);
  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);

  readonly query = signal('');
  readonly searching = signal(false);
  /** True only when `searching` has been true continuously for at least LOADER_DELAY_MS. */
  readonly showLoader = signal(false);
  readonly invitingId = signal<string | null>(null);

  /** Users invited during this dialog session — removed from results so they don't reappear. */
  private readonly invitedIds = signal<ReadonlySet<string>>(new Set());

  private loaderTimer: ReturnType<typeof setTimeout> | null = null;

  private readonly searchResults = toSignal(
    toObservable(this.query).pipe(
      debounceTime(DEBOUNCE_MS),
      distinctUntilChanged(),
      switchMap((q) => {
        const trimmed = q.trim();
        if (trimmed.length < MIN_QUERY_LENGTH) {
          this.setSearching(false);
          return of<InvitableUserResponse[]>([]);
        }
        this.setSearching(true);
        return this.groupsService.getInvitableUsers(this.groupId(), trimmed).pipe(
          tap(() => this.setSearching(false)),
          catchError(() => {
            this.setSearching(false);
            return of<InvitableUserResponse[]>([]);
          }),
        );
      }),
    ),
    { initialValue: [] as InvitableUserResponse[] },
  );

  /** Update `searching` and schedule the skeleton only if it stays true past LOADER_DELAY_MS. */
  private setSearching(value: boolean) {
    this.searching.set(value);
    if (this.loaderTimer) {
      clearTimeout(this.loaderTimer);
      this.loaderTimer = null;
    }
    if (value) {
      this.loaderTimer = setTimeout(() => {
        if (this.searching()) this.showLoader.set(true);
      }, LOADER_DELAY_MS);
    } else {
      this.showLoader.set(false);
    }
  }

  /** Search results minus the users already invited in this dialog session. */
  readonly results = computed(() =>
    this.searchResults().filter((u) => !this.invitedIds().has(u.id)),
  );

  readonly showMinHint = computed(() => {
    const len = this.query().trim().length;
    return len > 0 && len < MIN_QUERY_LENGTH;
  });

  readonly showEmptyState = computed(
    () =>
      !this.searching() &&
      this.query().trim().length >= MIN_QUERY_LENGTH &&
      this.results().length === 0,
  );

  invite(user: InvitableUserResponse) {
    if (this.invitingId() !== null) return;

    this.invitingId.set(user.id);
    this.groupsService
      .inviteUserToGroup(this.groupId(), { inviteeUserId: user.id })
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: this.translate.instant('GROUPS.DETAIL.INVITE.TOAST_SUCCESS_SUMMARY'),
            detail: this.translate.instant('GROUPS.DETAIL.INVITE.TOAST_SUCCESS_DETAIL'),
          });
          this.invitedIds.update((s) => new Set([...s, user.id]));
          this.invitingId.set(null);
        },
        error: (err: HttpErrorResponse) => {
          this.invitingId.set(null);
          const code = (err.error as { code?: string } | null)?.code;
          const detailKey =
            code === 'ALREADY_GROUP_MEMBER'
              ? 'GROUPS.DETAIL.INVITE.TOAST_ERROR_ALREADY_MEMBER'
              : code === 'USER_ALREADY_INVITED'
                ? 'GROUPS.DETAIL.INVITE.TOAST_ERROR_ALREADY_INVITED'
                : 'GROUPS.DETAIL.INVITE.TOAST_ERROR_GENERIC';
          this.messageService.add({
            severity: 'error',
            summary: this.translate.instant('GROUPS.DETAIL.INVITE.TOAST_ERROR_SUMMARY'),
            detail: this.translate.instant(detailKey),
          });
        },
      });
  }

  onHide() {
    // Reset state on close so re-opening starts fresh.
    this.query.set('');
    this.invitedIds.set(new Set());
    this.invitingId.set(null);
    this.setSearching(false);
  }

  initialsFor(user: InvitableUserResponse): string {
    return ((user.firstName?.[0] ?? '') + (user.lastName?.[0] ?? '')).toUpperCase() || '?';
  }

  fullName(user: InvitableUserResponse): string {
    return `${user.firstName} ${user.lastName}`.trim();
  }
}
