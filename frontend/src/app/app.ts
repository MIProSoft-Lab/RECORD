import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
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
