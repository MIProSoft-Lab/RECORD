import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { UserState } from '../../core/services/user-state';
import { Button } from 'primeng/button';
import { AuthService, LogoutRequest } from '../../core/api';
import { Avatar } from 'primeng/avatar';
import { MenuItem } from 'primeng/api';
import { Menu } from 'primeng/menu';

@Component({
  selector: 'record-main-layout',
  imports: [RouterLink, RouterOutlet, Button, Avatar, Menu],
  templateUrl: './main-layout.html',
})
export class MainLayout {
  userState = inject(UserState);
  user = this.userState.currentUser;
  private router = inject(Router);
  private authApi = inject(AuthService);

  userMenuItems: MenuItem[] = [
    {
      label: 'Ajustes',
      icon: 'pi pi-cog',
      command: () => this.router.navigate(['/settings']),
    },
    {
      label: 'Cerrar sesión',
      icon: 'pi pi-sign-out',
      command: () => this.logout(),
    },
  ];

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
