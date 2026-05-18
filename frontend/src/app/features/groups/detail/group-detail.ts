import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { TableModule } from 'primeng/table';
import { TabsModule } from 'primeng/tabs';
import { Tag } from 'primeng/tag';
import { HttpErrorResponse } from '@angular/common/http';
import { GroupDetailResponse, GroupsService } from '@core/api';

@Component({
  selector: 'record-group-detail',
  imports: [DatePipe, TranslatePipe, TableModule, TabsModule, Tag],
  templateUrl: './group-detail.html',
})
export class GroupDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly groupsService = inject(GroupsService);
  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);

  group = signal<GroupDetailResponse | null>(null);
  isLoading = signal(false);
  activeTab = signal<string>('about');

  members = computed(() => this.group()?.members ?? []);

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
}
