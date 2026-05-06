import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { UserState } from '@core/services/user-state';
import { AuthService, LogoutRequest } from '@core/api';
import { Avatar } from 'primeng/avatar';
import { MenuItem } from 'primeng/api';
import { Menu } from 'primeng/menu';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'record-main-layout',
  imports: [RouterLink, RouterOutlet, Avatar, Menu, TranslatePipe],
  templateUrl: './main-layout.html',
})
export class MainLayout implements OnInit {
  private router = inject(Router);
  private authApi = inject(AuthService);
  private translate = inject(TranslateService);

  userState = inject(UserState);
  user = this.userState.currentUser;
  userMenuItems: MenuItem[] = [];

  ngOnInit(): void {
    this.buildMenu();

    this.translate.onLangChange.subscribe((lang) => {
      this.buildMenu();
    });
  }

  private buildMenu() {
    this.userMenuItems = [
      {
        label: this.translate.instant('COMMON.MENU.SETTINGS'),
        icon: 'pi pi-cog',
        command: () => this.router.navigate(['/settings']),
      },
      {
        label: this.translate.instant('COMMON.MENU.LOGOUT'),
        icon: 'pi pi-sign-out',
        command: () => this.logout(),
      },
    ];
  }

  protected logout() {
    const refreshToken = localStorage.getItem('refresh_token');

    const cleanLocalState = () => {
      this.userState.clearCurrentUser();
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      this.router.navigate(['/login']).then();
    };

    if (refreshToken) {
      const logoutRequest: LogoutRequest = { token: refreshToken };

      this.authApi.logout(logoutRequest).subscribe({
        next: () => cleanLocalState(),

        error: (err) => {
          console.error('Error al revocar el token en el servidor', err);
          cleanLocalState();
        },
      });
    } else {
      cleanLocalState();
    }
  }
}
