import { Component, inject, signal } from '@angular/core';
import { Card } from 'primeng/card';
import { Button } from 'primeng/button';
import { RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Select } from 'primeng/select';
import { FormsModule } from '@angular/forms';

interface Language {
  name: string;
  code: string;
}

@Component({
  selector: 'record-settings',
  imports: [Card, Button, RouterLink, TranslatePipe, Select, FormsModule],
  templateUrl: './settings.html',
})
export class Settings {
  private translate = inject(TranslateService);

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
}
