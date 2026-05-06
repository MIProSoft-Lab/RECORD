import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { AuthResponse, AuthService, ErrorResponse, RegisterRequest } from '@core/api';
import { Router, RouterLink } from '@angular/router';
import { UserState } from '@core/services/user-state';
import { switchMap } from 'rxjs';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { Password } from 'primeng/password';
import { HttpErrorResponse } from '@angular/common/http';
import { Card } from 'primeng/card';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'record-register',
  imports: [Button, InputText, Password, ReactiveFormsModule, RouterLink, Card, TranslatePipe],
  templateUrl: './register.html',
})
export class Register {
  private fb = inject(FormBuilder);
  private authApi = inject(AuthService);
  private router = inject(Router);
  private userStateService = inject(UserState);

  matchPasswordValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');

    if (password && confirmPassword && password.value !== confirmPassword.value) {
      return { passwordsNotMatching: true };
    }

    return null;
  }

  registerForm = this.fb.nonNullable.group(
    {
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(8)]],
    },
    {
      validators: [this.matchPasswordValidator],
    },
  );

  isLoading = signal<boolean>(false);
  error = signal<ErrorResponse | null>(null);

  onSubmit() {
    if (this.registerForm.invalid) {
      this.registerForm.markAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.error.set(null);

    const { confirmPassword, ...userData } = this.registerForm.getRawValue();
    const registerRequest: RegisterRequest = userData;

    this.authApi
      .register(registerRequest)
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
      });
  }
}
