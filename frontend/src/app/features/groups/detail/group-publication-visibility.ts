import { Component, OnInit, inject, input, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { ToggleSwitch } from 'primeng/toggleswitch';
import { Tooltip } from 'primeng/tooltip';
import { GroupsService, PublicationVisibilityMember } from '@core/api';

/**
 * Sección de la pestaña de administración (visible para todos los miembros) donde el usuario
 * decide qué otros miembros pueden ver su historial de publicaciones. Por defecto todos pueden;
 * los administradores siempre pueden y aparecen bloqueados. Los cambios se aplican al instante.
 */
@Component({
  selector: 'record-group-publication-visibility',
  imports: [TranslatePipe, FormsModule, ToggleSwitch, Tooltip],
  templateUrl: './group-publication-visibility.html',
})
export class GroupPublicationVisibility implements OnInit {
  readonly groupId = input.required<string>();

  private readonly groupsService = inject(GroupsService);
  private readonly messageService = inject(MessageService);
  private readonly translate = inject(TranslateService);

  readonly members = signal<PublicationVisibilityMember[]>([]);
  readonly loading = signal(false);
  readonly updatingMemberId = signal<string | null>(null);

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.groupsService.getPublicationVisibility(this.groupId()).subscribe({
      next: (response) => {
        this.members.set(response.members);
        this.loading.set(false);
      },
      error: () => {
        this.members.set([]);
        this.loading.set(false);
      },
    });
  }

  onToggle(member: PublicationVisibilityMember, canSee: boolean) {
    if (member.locked) return;

    const previous = member.canSee;
    this.patchMember(member.userId, canSee);
    this.updatingMemberId.set(member.userId);

    this.groupsService
      .updatePublicationVisibility(this.groupId(), member.userId, { canSee })
      .subscribe({
        next: () => {
          this.updatingMemberId.set(null);
          const name = `${member.firstName} ${member.lastName}`;
          this.messageService.add({
            severity: 'success',
            summary: this.translate.instant(
              'GROUPS.DETAIL.ADMINISTRATION.PRIVACY.TOAST_SUCCESS_SUMMARY',
            ),
            detail: this.translate.instant(
              canSee
                ? 'GROUPS.DETAIL.ADMINISTRATION.PRIVACY.TOAST_SHOWN'
                : 'GROUPS.DETAIL.ADMINISTRATION.PRIVACY.TOAST_HIDDEN',
              { name },
            ),
          });
        },
        error: (_err: HttpErrorResponse) => {
          this.updatingMemberId.set(null);
          this.patchMember(member.userId, previous);
          this.messageService.add({
            severity: 'error',
            summary: this.translate.instant(
              'GROUPS.DETAIL.ADMINISTRATION.PRIVACY.TOAST_ERROR_SUMMARY',
            ),
            detail: this.translate.instant(
              'GROUPS.DETAIL.ADMINISTRATION.PRIVACY.TOAST_ERROR_DETAIL',
            ),
          });
        },
      });
  }

  private patchMember(userId: string, canSee: boolean) {
    this.members.update((members) =>
      members.map((m) => (m.userId === userId ? { ...m, canSee } : m)),
    );
  }
}
