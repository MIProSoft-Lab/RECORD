import { Component, OnInit, inject, output, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { Avatar } from 'primeng/avatar';
import { Skeleton } from 'primeng/skeleton';
import { MessageService } from 'primeng/api';
import { InvitationsService, InvitationResponse, InviterSummary } from '@core/api';

/** Show the loading skeleton only if the request is still pending after this delay. */
const LOADER_DELAY_MS = 250;

@Component({
  selector: 'record-pending-invitations',
  imports: [TranslatePipe, DatePipe, Button, Card, Avatar, Skeleton],
  templateUrl: './pending-invitations.html',
})
export class PendingInvitations implements OnInit {
  private readonly invitationsService = inject(InvitationsService);
  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);

  /** Emits the joined group's id so the host can react (e.g. refresh a group list). */
  readonly invitationAccepted = output<string>();

  invitations = signal<InvitationResponse[]>([]);
  isLoading = signal(false);
  showLoader = signal(false);
  processingId = signal<string | null>(null);

  ngOnInit() {
    this.loadInvitations();
  }

  loadInvitations() {
    this.isLoading.set(true);
    const loaderTimer = setTimeout(() => {
      if (this.isLoading()) this.showLoader.set(true);
    }, LOADER_DELAY_MS);

    this.invitationsService.listInvitations().subscribe({
      next: (invitations) => {
        clearTimeout(loaderTimer);
        this.invitations.set(invitations);
        this.isLoading.set(false);
        this.showLoader.set(false);
      },
      error: () => {
        clearTimeout(loaderTimer);
        this.isLoading.set(false);
        this.showLoader.set(false);
      },
    });
  }

  accept(invitation: InvitationResponse) {
    if (!invitation.id || this.processingId() !== null) return;

    this.processingId.set(invitation.id);
    this.invitationsService.acceptInvitation(invitation.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: this.translate.instant('INVITATIONS.TOASTS.SUCCESS'),
          detail: this.translate.instant('INVITATIONS.TOASTS.ACCEPTED'),
        });
        this.processingId.set(null);
        if (invitation.group?.groupId) {
          this.invitationAccepted.emit(invitation.group.groupId);
        }
        this.loadInvitations();
      },
      error: () => {
        this.processingId.set(null);
      },
    });
  }

  reject(invitation: InvitationResponse) {
    if (!invitation.id || this.processingId() !== null) return;

    this.processingId.set(invitation.id);
    this.invitationsService.rejectInvitation(invitation.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'info',
          summary: this.translate.instant('INVITATIONS.TOASTS.SUCCESS'),
          detail: this.translate.instant('INVITATIONS.TOASTS.REJECTED'),
        });
        this.processingId.set(null);
        this.loadInvitations();
      },
      error: () => {
        this.processingId.set(null);
      },
    });
  }

  inviterName(inviter: InviterSummary | undefined): string {
    if (!inviter) return '';
    return `${inviter.firstName ?? ''} ${inviter.lastName ?? ''}`.trim();
  }

  inviterInitials(inviter: InviterSummary | undefined): string {
    const first = inviter?.firstName?.[0] ?? '';
    const last = inviter?.lastName?.[0] ?? '';
    return (first + last).toUpperCase() || '?';
  }
}
