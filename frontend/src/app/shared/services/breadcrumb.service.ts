import { Injectable, Signal, inject, signal } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  NavigationEnd,
  Router,
} from '@angular/router';
import { filter } from 'rxjs/operators';

export interface BreadcrumbItem {
  /**
   * If `dynamic` is false, this is an i18n key to translate.
   * If `dynamic` is true, this is the literal label to render.
   */
  labelKey: string;
  url: string;
  dynamic: boolean;
}

@Injectable({ providedIn: 'root' })
export class BreadcrumbService {
  private readonly router = inject(Router);

  /** URL → dynamic label override published by detail pages. */
  private readonly overrides = signal<Map<string, string>>(new Map());

  private readonly _trail = signal<BreadcrumbItem[]>([]);
  readonly trail: Signal<BreadcrumbItem[]> = this._trail.asReadonly();

  constructor() {
    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe(() => this.rebuild());

    // Initial build for the first navigation that already happened.
    this.rebuild();
  }

  setDynamicLabel(url: string, label: string): void {
    const next = new Map(this.overrides());
    next.set(url, label);
    this.overrides.set(next);
    this.rebuild();
  }

  clearDynamicLabel(url: string): void {
    const current = this.overrides();
    if (!current.has(url)) return;
    const next = new Map(current);
    next.delete(url);
    this.overrides.set(next);
    this.rebuild();
  }

  private rebuild(): void {
    const items: BreadcrumbItem[] = [];
    const overrides = this.overrides();
    let route: ActivatedRouteSnapshot | null = this.router.routerState.snapshot.root;
    const segments: string[] = [];

    while (route) {
      const path = route.url.map((s) => s.path).join('/');
      if (path) segments.push(path);

      // Read from routeConfig.data (not snapshot.data) to avoid duplicates
      // caused by Angular inheriting parent data into child routes.
      const labelKey = route.routeConfig?.data?.['breadcrumb'] as string | undefined;
      if (labelKey) {
        const url = '/' + segments.join('/');
        const override = overrides.get(url);
        items.push({
          labelKey: override ?? labelKey,
          url,
          dynamic: override !== undefined,
        });
      }

      route = route.firstChild;
    }

    this._trail.set(items);
  }
}
