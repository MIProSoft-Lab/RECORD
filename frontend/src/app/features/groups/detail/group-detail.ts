import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ConfirmationService, MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TabsModule } from 'primeng/tabs';
import { Tag } from 'primeng/tag';
import {
  GroupDetailResponse,
  GroupMemberDetail,
  GroupRole,
  GroupsService,
} from '@core/api';
import { UserState } from '@core/services/user-state';
import { BreadcrumbService } from '@shared/services/breadcrumb.service';
import { EditGroupDialog } from './edit-group-dialog';
import { GroupJournalInterests } from './group-journal-interests';
import { InviteUsersDialog } from './invite-users-dialog';

interface RoleOption {
  label: string;
  value: GroupRole;
}

@Component({
  selector: 'record-group-detail',
  imports: [
    DatePipe,
    FormsModule,
    TranslatePipe,
    TableModule,
    TabsModule,
    Tag,
    Button,
    Select,
    InviteUsersDialog,
    EditGroupDialog,
    GroupJournalInterests,
  ],
  templateUrl: './group-detail.html',
})
export class GroupDetail implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly groupsService = inject(GroupsService);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly translate = inject(TranslateService);
  private readonly breadcrumbService = inject(BreadcrumbService);
  readonly userState = inject(UserState);

  private breadcrumbUrl: string | null = null;

  group = signal<GroupDetailResponse | null>(null);
  isLoading = signal(false);
  activeTab = signal<string>('about');
  showInviteDialog = signal(false);
  showEditDialog = signal(false);
  updatingMemberId = signal<string | null>(null);
  kickingMemberId = signal<string | null>(null);
  leavingGroup = signal(false);
  deletingGroup = signal(false);

  members = computed(() => this.group()?.members ?? []);

  /** True when the current authenticated user is an ADMIN of this group. */
  isAdmin = computed(() => {
    const currentUserId = this.userState.currentUser()?.id;
    if (!currentUserId) return false;
    return this.members().some((m) => m.userId === currentUserId && m.role === 'ADMIN');
  });

  /** True when the current user is an ADMIN and the only admin left in the group. */
  isLastAdmin = computed(() => {
    const currentUserId = this.userState.currentUser()?.id;
    if (!currentUserId) return false;
    const currentMember = this.members().find((m) => m.userId === currentUserId);
    if (currentMember?.role !== 'ADMIN') return false;
    return this.members().filter((m) => m.role === 'ADMIN').length === 1;
  });

  readonly roleOptions = computed<RoleOption[]>(() => [
    { label: this.translate.instant('GROUPS.ROLES.ADMIN'), value: 'ADMIN' },
    { label: this.translate.instant('GROUPS.ROLES.MEMBER'), value: 'MEMBER' },
  ]);

  ngOnInit() {
    const groupId = this.route.snapshot.paramMap.get('id');
    if (!groupId) {
      this.router.navigate(['/groups']);
      return;
    }
    this.loadGroup(groupId);
  }

  loadGroup(groupId: string) {
    this.isLoading.set(true);
    this.groupsService.getGroupDetail(groupId).subscribe({
      next: (group) => {
        this.group.set(group);
        this.isLoading.set(false);
        this.breadcrumbUrl = this.router.url;
        this.breadcrumbService.setDynamicLabel(this.breadcrumbUrl, group.name);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        const key =
          err.status === 404
            ? 'GROUPS.DETAIL.ERRORS.NOT_FOUND'
            : err.status === 403
              ? 'GROUPS.DETAIL.ERRORS.FORBIDDEN'
              : 'GROUPS.DETAIL.ERRORS.GENERIC';
        this.messageService.add({
          severity: 'error',
          summary: this.translate.instant('GROUPS.DETAIL.ERRORS.TITLE'),
          detail: this.translate.instant(key),
        });
        this.router.navigate(['/groups']);
      },
    });
  }

  onRoleChange(member: GroupMemberDetail, newRole: GroupRole) {
    const previousRole = member.role;
    if (previousRole === newRole) return;

    const currentUserId = this.userState.currentUser()?.id;
    const isSelfDemote =
      member.userId === currentUserId && previousRole === 'ADMIN' && newRole === 'MEMBER';

    if (isSelfDemote) {
      this.confirmationService.confirm({
        header: this.translate.instant(
          'GROUPS.DETAIL.ADMINISTRATION.MEMBERS.SELF_DEMOTE_CONFIRM_HEADER',
        ),
        message: this.translate.instant(
          'GROUPS.DETAIL.ADMINISTRATION.MEMBERS.SELF_DEMOTE_CONFIRM_MESSAGE',
        ),
        acceptLabel: this.translate.instant('GROUPS.DETAIL.ADMINISTRATION.MEMBERS.CONFIRM_YES'),
        rejectLabel: this.translate.instant('GROUPS.DETAIL.ADMINISTRATION.MEMBERS.CONFIRM_NO'),
        accept: () => this.sendRoleUpdate(member, newRole, previousRole),
        reject: () => this.revertRole(member, previousRole),
      });
      return;
    }

    this.sendRoleUpdate(member, newRole, previousRole);
  }

  private sendRoleUpdate(member: GroupMemberDetail, newRole: GroupRole, previousRole: GroupRole) {
    const groupId = this.group()?.id;
    if (!groupId) return;

    this.updatingMemberId.set(member.userId);
    this.groupsService
      .updateGroupMemberRole(groupId, member.userId, { role: newRole })
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: this.translate.instant(
              'GROUPS.DETAIL.ADMINISTRATION.MEMBERS.TOAST_SUCCESS_SUMMARY',
            ),
            detail: this.translate.instant(
              'GROUPS.DETAIL.ADMINISTRATION.MEMBERS.TOAST_SUCCESS_DETAIL',
            ),
          });
          this.updatingMemberId.set(null);
          this.loadGroup(groupId);
        },
        error: (err: HttpErrorResponse) => {
          this.updatingMemberId.set(null);
          this.revertRole(member, previousRole);
          const code = (err.error as { code?: string } | null)?.code;
          const detailKey =
            code === 'GROUP_LAST_ADMIN'
              ? 'GROUPS.DETAIL.ADMINISTRATION.MEMBERS.TOAST_ERROR_LAST_ADMIN'
              : code === 'GROUP_MEMBER_NOT_ADMIN'
                ? 'GROUPS.DETAIL.ADMINISTRATION.MEMBERS.TOAST_ERROR_FORBIDDEN'
                : 'GROUPS.DETAIL.ADMINISTRATION.MEMBERS.TOAST_ERROR_GENERIC';
          this.messageService.add({
            severity: 'error',
            summary: this.translate.instant(
              'GROUPS.DETAIL.ADMINISTRATION.MEMBERS.TOAST_ERROR_SUMMARY',
            ),
            detail: this.translate.instant(detailKey),
          });
        },
      });
  }

  onKickMember(member: GroupMemberDetail) {
    const groupId = this.group()?.id;
    if (!groupId) return;

    this.confirmationService.confirm({
      header: this.translate.instant('GROUPS.DETAIL.ADMINISTRATION.MEMBERS.KICK_CONFIRM_HEADER'),
      message: this.translate.instant('GROUPS.DETAIL.ADMINISTRATION.MEMBERS.KICK_CONFIRM_MESSAGE'),
      acceptLabel: this.translate.instant('GROUPS.DETAIL.ADMINISTRATION.MEMBERS.CONFIRM_KICK_YES'),
      rejectLabel: this.translate.instant('GROUPS.DETAIL.ADMINISTRATION.MEMBERS.CONFIRM_NO'),
      accept: () => {
        this.kickingMemberId.set(member.userId);
        this.groupsService.kickGroupMember(groupId, member.userId).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: this.translate.instant(
                'GROUPS.DETAIL.ADMINISTRATION.MEMBERS.TOAST_SUCCESS_SUMMARY',
              ),
              detail: this.translate.instant(
                'GROUPS.DETAIL.ADMINISTRATION.MEMBERS.TOAST_KICK_SUCCESS_DETAIL',
              ),
            });
            this.kickingMemberId.set(null);
            this.loadGroup(groupId);
          },
          error: (err: HttpErrorResponse) => {
            this.kickingMemberId.set(null);
            const code = (err.error as { code?: string } | null)?.code;
            const detailKey =
              code === 'GROUP_LAST_ADMIN'
                ? 'GROUPS.DETAIL.ADMINISTRATION.MEMBERS.TOAST_ERROR_KICK_LAST_ADMIN'
                : code === 'GROUP_MEMBER_NOT_ADMIN'
                  ? 'GROUPS.DETAIL.ADMINISTRATION.MEMBERS.TOAST_ERROR_KICK_FORBIDDEN'
                  : 'GROUPS.DETAIL.ADMINISTRATION.MEMBERS.TOAST_ERROR_KICK_GENERIC';
            this.messageService.add({
              severity: 'error',
              summary: this.translate.instant(
                'GROUPS.DETAIL.ADMINISTRATION.MEMBERS.TOAST_ERROR_SUMMARY',
              ),
              detail: this.translate.instant(detailKey),
            });
          },
        });
      },
    });
  }

  onLeaveGroup() {
    const groupId = this.group()?.id;
    if (!groupId) return;

    if (this.isLastAdmin()) {
      this.messageService.add({
        severity: 'warn',
        summary: this.translate.instant('GROUPS.DETAIL.LEAVE.TOAST_LAST_ADMIN_SUMMARY'),
        detail: this.translate.instant('GROUPS.DETAIL.LEAVE.LAST_ADMIN_MESSAGE'),
      });
      return;
    }

    this.confirmationService.confirm({
      header: this.translate.instant('GROUPS.DETAIL.LEAVE.CONFIRM_HEADER'),
      message: this.translate.instant('GROUPS.DETAIL.LEAVE.CONFIRM_MESSAGE'),
      acceptLabel: this.translate.instant('GROUPS.DETAIL.LEAVE.CONFIRM_YES'),
      rejectLabel: this.translate.instant('GROUPS.DETAIL.ADMINISTRATION.MEMBERS.CONFIRM_NO'),
      accept: () => {
        this.leavingGroup.set(true);
        this.groupsService.leaveGroup(groupId).subscribe({
          next: () => {
            this.leavingGroup.set(false);
            this.messageService.add({
              severity: 'success',
              summary: this.translate.instant('GROUPS.DETAIL.LEAVE.TOAST_SUCCESS_SUMMARY'),
              detail: this.translate.instant('GROUPS.DETAIL.LEAVE.TOAST_SUCCESS_DETAIL'),
            });
            this.router.navigate(['/groups']);
          },
          error: (err: HttpErrorResponse) => {
            this.leavingGroup.set(false);
            const code = (err.error as { code?: string } | null)?.code;
            const detailKey =
              code === 'GROUP_LAST_ADMIN'
                ? 'GROUPS.DETAIL.LEAVE.LAST_ADMIN_MESSAGE'
                : 'GROUPS.DETAIL.LEAVE.TOAST_ERROR_GENERIC';
            this.messageService.add({
              severity: 'error',
              summary: this.translate.instant('GROUPS.DETAIL.LEAVE.TOAST_ERROR_SUMMARY'),
              detail: this.translate.instant(detailKey),
            });
          },
        });
      },
    });
  }

  onDeleteGroup() {
    const groupId = this.group()?.id;
    if (!groupId) return;

    this.confirmationService.confirm({
      header: this.translate.instant('GROUPS.DETAIL.DELETE.CONFIRM_HEADER'),
      message: this.translate.instant('GROUPS.DETAIL.DELETE.CONFIRM_MESSAGE'),
      acceptLabel: this.translate.instant('GROUPS.DETAIL.DELETE.CONFIRM_YES'),
      rejectLabel: this.translate.instant('GROUPS.DETAIL.ADMINISTRATION.MEMBERS.CONFIRM_NO'),
      accept: () => {
        this.deletingGroup.set(true);
        this.groupsService.deleteGroup(groupId).subscribe({
          next: () => {
            this.deletingGroup.set(false);
            this.messageService.add({
              severity: 'success',
              summary: this.translate.instant('GROUPS.DETAIL.DELETE.TOAST_SUCCESS_SUMMARY'),
              detail: this.translate.instant('GROUPS.DETAIL.DELETE.TOAST_SUCCESS_DETAIL'),
            });
            this.router.navigate(['/groups']);
          },
          error: (err: HttpErrorResponse) => {
            this.deletingGroup.set(false);
            const code = (err.error as { code?: string } | null)?.code;
            const detailKey =
              code === 'GROUP_MEMBER_NOT_ADMIN'
                ? 'GROUPS.DETAIL.DELETE.TOAST_ERROR_FORBIDDEN'
                : 'GROUPS.DETAIL.DELETE.TOAST_ERROR_GENERIC';
            this.messageService.add({
              severity: 'error',
              summary: this.translate.instant('GROUPS.DETAIL.DELETE.TOAST_ERROR_SUMMARY'),
              detail: this.translate.instant(detailKey),
            });
          },
        });
      },
    });
  }

  private revertRole(member: GroupMemberDetail, previousRole: GroupRole) {
    this.group.update((current) => {
      if (!current) return current;
      return {
        ...current,
        members: current.members.map((m) =>
          m.userId === member.userId ? { ...m, role: previousRole } : m,
        ),
      };
    });
  }

  ngOnDestroy() {
    if (this.breadcrumbUrl) {
      this.breadcrumbService.clearDynamicLabel(this.breadcrumbUrl);
    }
  }
}
