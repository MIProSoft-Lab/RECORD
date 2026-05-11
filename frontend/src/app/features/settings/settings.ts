import { Component, effect, inject, signal, OnDestroy } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Card } from 'primeng/card';
import { Button } from 'primeng/button';
import { RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Select } from 'primeng/select';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { UserState } from '../../core/services/user-state';
import { UsersService } from '../../core/api';
import { MessageService } from 'primeng/api';
import { Subscription } from 'rxjs';

interface Language {
  name: string;
  code: string;
}

@Component({
  selector: 'record-settings',
  imports: [Card, Button, RouterLink, TranslatePipe, Select, FormsModule, ReactiveFormsModule, InputText],
  templateUrl: './settings.html',
})
export class Settings implements OnDestroy {
  private translate = inject(TranslateService);
  private userState = inject(UserState);
  private usersService = inject(UsersService);
  private fb = inject(FormBuilder);
  private messageService = inject(MessageService);

  profileForm = this.fb.group({
    email: [{ value: '', disabled: true }],
    firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
    lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]]
  });

  isSaving = signal(false);
  hasChanges = signal(false);
  private sub?: Subscription;

  constructor() {
    this.sub = this.profileForm.valueChanges.subscribe(() => {
      this.checkChanges();
    });

    effect(() => {
      const user = this.userState.currentUser();
      if (user) {
        this.profileForm.patchValue({
          email: user.email,
          firstName: user.firstName,
          lastName: user.lastName
        }, { emitEvent: false });
        this.checkChanges();
      }
    });
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
  }

  checkChanges() {
    const user = this.userState.currentUser();
    if (!user) {
      this.hasChanges.set(false);
      return;
    }
    const { firstName, lastName } = this.profileForm.getRawValue();
    const changed = firstName !== user.firstName || lastName !== user.lastName;
    this.hasChanges.set(changed);
  }

  availableLanguages = signal<Language[]>([
    { name: 'Español', code: 'es' },
    { name: 'English', code: 'en' },
    { name: 'Català', code: 'cat' },
  ]);

  selectedLanguage = signal<Language>(
    this.availableLanguages().find(
      (lang) =>
        lang.code === localStorage.getItem('lang') ||
        lang.code === this.translate.getCurrentLang(),
    ) || this.availableLanguages()[0],
  );

  onLanguageChange(event: any) {
    const selectedLang: Language = event.value;
    localStorage.setItem('lang', selectedLang.code);
    this.translate.use(selectedLang.code);
  }

  onSaveProfile() {
    if (this.profileForm.invalid) return;

    this.isSaving.set(true);
    const { firstName, lastName } = this.profileForm.getRawValue();

    this.usersService.updateCurrentUser({ firstName: firstName!, lastName: lastName! }).subscribe({
      next: (user) => {
        this.userState.currentUser.set(user);
        this.isSaving.set(false);
        this.messageService.add({
          severity: 'success',
          summary: this.translate.instant('CONFIG.TOASTS.SUCCESS'),
          detail: this.translate.instant('CONFIG.TOASTS.PROFILE_UPDATED'),
        });
      },
      error: (err) => {
        console.error(err);
        this.isSaving.set(false);
      }
    });
  }
}
