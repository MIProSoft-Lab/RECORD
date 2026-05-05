import { inject, Injectable, signal } from '@angular/core';
import { UserResponse, UsersService } from '../api';
import { tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class UserState {
  private usersApi = inject(UsersService);

  public currentUser = signal<UserResponse | null>(null);

  loadCurrentUser() {
    return this.usersApi.getCurrentUser().pipe(
      tap((user: UserResponse) => {
        this.currentUser.set(user);
      })
    );
  }

  clearCurrentUser() {
    this.currentUser.set(null);
  }
}
