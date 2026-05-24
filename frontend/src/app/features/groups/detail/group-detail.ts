import { DatePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { TabsModule } from 'primeng/tabs';
import { Tag } from 'primeng/tag';
import { HttpErrorResponse } from '@angular/common/http';
import { GroupDetailResponse, GroupsService } from '@core/api';
import { UserState } from '@core/services/user-state';
import { BreadcrumbService } from '@shared/services/breadcrumb.service';
import { InviteUsersDialog } from './invite-users-dialog';

@Component({
  selector: 'record-group-detail',
  imports: [DatePipe, TranslatePipe, TableModule, TabsModule, Tag, Button, InviteUsersDialog],
  templateUrl: './group-detail.html',
})
export class GroupDetail implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly groupsService = inject(GroupsService);
  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);
  private readonly breadcrumbService = inject(BreadcrumbService);
  private readonly userState = inject(UserState);

  private breadcrumbUrl: string | null = null;

  group = signal<GroupDetailResponse | null>(null);
  isLoading = signal(false);
  activeTab = signal<string>('about');
  showInviteDialog = signal(false);

  members = computed(() => this.group()?.members ?? []);

  /** True when the current authenticated user is an ADMIN of this group. */
  isAdmin = computed(() => {
    const currentUserId = this.userState.currentUser()?.id;
    if (!currentUserId) return false;
    return this.members().some((m) => m.userId === currentUserId && m.role === 'ADMIN');
  });

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

  ngOnDestroy() {
    if (this.breadcrumbUrl) {
      this.breadcrumbService.clearDynamicLabel(this.breadcrumbUrl);
    }
  }
}
