import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { TranslateService } from '@ngx-translate/core';
import { MenuItem } from 'primeng/api';
import { Breadcrumb } from 'primeng/breadcrumb';
import { BreadcrumbService } from '@shared/services/breadcrumb.service';

@Component({
  selector: 'record-breadcrumbs',
  imports: [Breadcrumb],
  templateUrl: './breadcrumbs.html',
})
export class Breadcrumbs {
  private readonly breadcrumbService = inject(BreadcrumbService);
  private readonly translate = inject(TranslateService);
  private readonly destroyRef = inject(DestroyRef);

  /** Bumped when the active language changes so the model recomputes labels. */
  private readonly langTick = signal(0);

  protected readonly model = computed<MenuItem[]>(() => {
    // Subscribe to lang changes by reading the tick signal.
    this.langTick();
    return this.breadcrumbService.trail().map((item) => ({
      label: item.dynamic ? item.labelKey : this.translate.instant(item.labelKey),
      routerLink: item.url,
      queryParams: item.queryParams,
    }));
  });

  protected readonly home: MenuItem = {
    icon: 'pi pi-home',
    routerLink: '/dashboard',
  };

  constructor() {
    this.translate.onLangChange
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.langTick.update((n) => n + 1));
  }
}
