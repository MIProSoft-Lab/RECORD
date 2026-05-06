import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthResponse, AuthService, ErrorResponse, LoginRequest } from '@core/api';
import { Router, RouterLink } from '@angular/router';
import { InputText } from 'primeng/inputtext';
import { Password } from 'primeng/password';
import { Button } from 'primeng/button';
import { switchMap } from 'rxjs';
import { UserState } from '@core/services/user-state';
import { HttpErrorResponse } from '@angular/common/http';
import { Card } from 'primeng/card';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'record-login',
  imports: [ReactiveFormsModule, InputText, Password, Button, RouterLink, Card, TranslatePipe],
  templateUrl: './login.html',
})
export class Login {
  private fb = inject(FormBuilder);
  private authApi = inject(AuthService);
  private router = inject(Router);
  private userStateService = inject(UserState);

  loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  isLoading = signal<boolean>(false);
  error = signal<ErrorResponse | null>(null);

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.error.set(null);

    const request: LoginRequest = this.loginForm.getRawValue();

    this.authApi
      .login(request)
      .pipe(
        switchMap((response: AuthResponse) => {
          localStorage.setItem('access_token', response.token);
          localStorage.setItem('refresh_token', response.refreshToken);

          return this.userStateService.loadCurrentUser();
        }),
      )
      .subscribe({
        next: () => {
          this.router.navigate(['/dashboard']).then();
        },
        error: (err: HttpErrorResponse) => {
          const errorResponse = err.error as ErrorResponse;
          this.isLoading.set(false);
          this.error.set(errorResponse);
        },
        complete: () => {
          this.isLoading.set(false);
        },
      }
    );
  }
}
