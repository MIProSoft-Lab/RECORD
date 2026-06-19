import { Injectable } from '@angular/core';
import { Quartile } from '@core/api';

/**
 * Conserva los filtros y la página de la búsqueda de revistas entre navegaciones (p. ej. al entrar
 * al detalle y volver atrás). Es un singleton, así que su estado sobrevive mientras viva la SPA.
 */
@Injectable({ providedIn: 'root' })
export class JournalSearchState {
  name = '';
  categoryId: string | null = null;
  quartile: Quartile | null = null;
  first = 0;
}
