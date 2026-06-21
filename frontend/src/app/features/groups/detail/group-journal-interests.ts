import { Component, OnInit, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { Avatar } from 'primeng/avatar';
import { AvatarGroup } from 'primeng/avatargroup';
import { Paginator, PaginatorState } from 'primeng/paginator';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Tooltip } from 'primeng/tooltip';
import {
  GroupJournalInterestPageResponse,
  GroupJournalInterestResponse,
  GroupsService,
  Quartile,
} from '@core/api';

const PAGE_SIZE = 20;
/** Show the loading indicator only if the request is still pending after this delay. */
const LOADER_DELAY_MS = 250;
/** Maximum number of member avatars shown before collapsing into a "+N" badge. */
const MAX_AVATARS = 3;

/**
 * Vista de solo lectura con la unión de revistas marcadas como de interés por los miembros del
 * grupo, ordenada por número de miembros que las marcan. Por cada revista muestra cuántos miembros
 * la han marcado y quiénes (grupo de avatares con desbordamiento "+N").
 */
@Component({
  selector: 'record-group-journal-interests',
  imports: [TranslatePipe, TableModule, Tag, Avatar, AvatarGroup, Tooltip, Paginator],
  templateUrl: './group-journal-interests.html',
})
export class GroupJournalInterests implements OnInit {
  readonly groupId = input.required<string>();

  private readonly groupsService = inject(GroupsService);
  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);

  readonly journals = signal<GroupJournalInterestResponse[]>([]);
  readonly totalRecords = signal(0);
  readonly first = signal(0);
  readonly showLoader = signal(false);
  readonly hasLoaded = signal(false);

  readonly pageSize = PAGE_SIZE;
  readonly maxAvatars = MAX_AVATARS;

  private loaderTimer?: ReturnType<typeof setTimeout>;

  ngOnInit() {
    this.load();
  }

  onPageChange(event: PaginatorState) {
    this.first.set(event.first ?? 0);
    this.load();
  }

  openJournalDetail(journalId: string) {
    this.router.navigate(['/journals', journalId]);
  }

  visibleMembers(journal: GroupJournalInterestResponse) {
    return journal.members.slice(0, this.maxAvatars);
  }

  hiddenMembersCount(journal: GroupJournalInterestResponse): number {
    return Math.max(0, journal.members.length - this.maxAvatars);
  }

  membersTooltip(journal: GroupJournalInterestResponse): string {
    return journal.members.map((m) => `${m.firstName} ${m.lastName}`).join(', ');
  }

  quartileSeverity(quartile: Quartile): 'success' | 'info' | 'warn' | 'danger' {
    switch (quartile) {
      case Quartile.Q1:
        return 'success';
      case Quartile.Q2:
        return 'info';
      case Quartile.Q3:
        return 'warn';
      default:
        return 'danger';
    }
  }

  private load() {
    const page = Math.floor(this.first() / this.pageSize);
    this.startLoader();

    this.groupsService.getGroupJournalInterests(this.groupId(), page, this.pageSize).subscribe({
      next: (response: GroupJournalInterestPageResponse) => {
        this.journals.set(response.content);
        this.totalRecords.set(response.totalElements);
        this.stopLoader();
        this.hasLoaded.set(true);
      },
      error: () => {
        this.journals.set([]);
        this.totalRecords.set(0);
        this.stopLoader();
        this.hasLoaded.set(true);
        this.messageService.add({
          severity: 'error',
          summary: this.translate.instant('GROUPS.DETAIL.JOURNALS.TITLE'),
          detail: this.translate.instant('GROUPS.DETAIL.JOURNALS.ERROR'),
        });
      },
    });
  }

  private startLoader() {
    clearTimeout(this.loaderTimer);
    this.loaderTimer = setTimeout(() => this.showLoader.set(true), LOADER_DELAY_MS);
  }

  private stopLoader() {
    clearTimeout(this.loaderTimer);
    this.showLoader.set(false);
  }
}
