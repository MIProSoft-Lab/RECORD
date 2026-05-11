import { Component, effect, inject, signal, OnDestroy } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Card } from 'primeng/card';
import { Button } from 'primeng/button';
import { RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Select } from 'primeng/select';
import { FormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { UserState } from '@core/services/user-state';
import { UsersService } from '@core/api';
import { MessageService } from 'primeng/api';
import { Subscription } from 'rxjs';
import { FileUploadModule, FileUploadHandlerEvent } from 'primeng/fileupload';
import { UpperCasePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ToggleSwitch } from 'primeng/toggleswitch';

interface Language {
  name: string;
  code: string;
}

@Component({
  selector: 'record-settings',
  imports: [
    Card,
    Button,
    RouterLink,
    TranslatePipe,
    Select,
    FormsModule,
    ReactiveFormsModule,
    InputText,
    UpperCasePipe,
    FileUploadModule,
    ToggleSwitch,
  ],
  templateUrl: './settings.html',
})
export class Settings implements OnDestroy {
  private translate = inject(TranslateService);
  private userState = inject(UserState);
  private usersService = inject(UsersService);
  private fb = inject(FormBuilder);
  private messageService = inject(MessageService);
  private http = inject(HttpClient);

  profileForm = this.fb.group({
    email: [{ value: '', disabled: true }],
    firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
    lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
  });

  isSaving = signal(false);
  hasChanges = signal(false);
  isUploadingAvatar = signal(false);
  isDarkMode = signal(localStorage.getItem('dark_mode') === 'true');
  private sub?: Subscription;

  constructor() {
    this.sub = this.profileForm.valueChanges.subscribe(() => {
      this.checkChanges();
    });

    effect(() => {
      const user = this.userState.currentUser();
      if (user) {
        this.profileForm.patchValue(
          {
            email: user.email,
            firstName: user.firstName,
            lastName: user.lastName,
          },
          { emitEvent: false },
        );
        this.checkChanges();
      }
    });
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
  }

  get currentUser() {
    return this.userState.currentUser;
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
        lang.code === localStorage.getItem('lang') || lang.code === this.translate.getCurrentLang(),
    ) || this.availableLanguages()[0],
  );

  onLanguageChange(event: any) {
    const selectedLang: Language = event.value;
    localStorage.setItem('lang', selectedLang.code);
    this.translate.use(selectedLang.code);
  }

  onDarkModeToggle(event: any) {
    const isDark = event.checked;
    localStorage.setItem('dark_mode', isDark ? 'true' : 'false');
    
    if (isDark) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
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
      },
    });
  }

  onFileSelected(event: Event | FileUploadHandlerEvent) {
    let file: File | undefined;

    if (event instanceof Event) {
      const input = event.target as HTMLInputElement;
      file = input.files?.[0];
    } else {
      file = event.files?.[0];
    }

    if (!file) return;

    this.uploadAvatar(file);
  }

  private uploadAvatar(file: File) {
    this.isUploadingAvatar.set(true);

    this.usersService.getUserProfileImageSignature().subscribe({
      next: (sigData) => {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('api_key', sigData.api_key);
        formData.append('timestamp', sigData.timestamp);
        formData.append('signature', sigData.signature);
        formData.append('folder', sigData.folder);
        formData.append('transformation', sigData.transformation);

        const cloudinaryUrl = `https://api.cloudinary.com/v1_1/${sigData.cloud_name}/image/upload`;

        this.http.post<any>(cloudinaryUrl, formData).subscribe({
          next: (res) => {
            console.log('Cloudinary response:', res);
            const secureUrl = res.secure_url;
            const { firstName, lastName } = this.profileForm.getRawValue();

            this.usersService
              .updateCurrentUser({
                firstName: firstName || this.userState.currentUser()?.firstName || '',
                lastName: lastName || this.userState.currentUser()?.lastName || '',
                profileImageUrl: secureUrl,
              })
              .subscribe({
                next: (updatedUser) => {
                  this.userState.currentUser.set(updatedUser);
                  this.isUploadingAvatar.set(false);
                  this.messageService.add({
                    severity: 'success',
                    summary: this.translate.instant('CONFIG.TOASTS.SUCCESS'),
                    detail: this.translate.instant('CONFIG.TOASTS.AVATAR_UPDATED'),
                  });
                },
                error: (err) => {
                  console.error(err);
                  this.isUploadingAvatar.set(false);
                  this.messageService.add({
                    severity: 'error',
                    summary: 'Error',
                    detail: 'Could not update user profile with the new avatar.',
                  });
                },
              });
          },
          error: (err) => {
            console.error('Cloudinary upload error:', err);
            this.isUploadingAvatar.set(false);
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Could not upload image to Cloudinary.',
            });
          },
        });
      },
      error: (err) => {
        console.error('Signature error:', err);
        this.isUploadingAvatar.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Could not get upload signature from server.',
        });
      },
    });
  }
}
