import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Toast } from 'primeng/toast';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Toast],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private translate = inject(TranslateService);

  constructor() {
    this.translate.addLangs(['en', 'es', 'cat']);
    const browserLang = this.translate.getBrowserLang();
    const localStorageLang = localStorage.getItem('lang');
    if (localStorageLang) {
      this.translate.use(localStorageLang);
      return
    }

    this.translate.use(browserLang?.match(/en|es|cat/) ? browserLang : 'en');
  }
}
