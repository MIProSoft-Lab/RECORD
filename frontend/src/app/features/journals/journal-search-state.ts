import { Injectable } from '@angular/core';
import { Quartile } from '@core/api';

/** Filtros y página de una lista de revistas. */
export interface JournalListFilters {
  name: string;
  categoryId: string | null;
  quartile: Quartile | null;
  first: number;
}

function emptyFilters(): JournalListFilters {
  return { name: '', categoryId: null, quartile: null, first: 0 };
}

/**
 * Conserva los filtros y la página de cada lista de revistas (búsqueda general e intereses) entre
 * navegaciones (p. ej. al entrar al detalle y volver atrás) y al cambiar de pestaña. Es un
 * singleton, así que su estado sobrevive mientras viva la SPA.
 */
@Injectable({ providedIn: 'root' })
export class JournalSearchState {
  readonly search = emptyFilters();
  readonly interests = emptyFilters();
}
