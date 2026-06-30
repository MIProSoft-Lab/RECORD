import { Injectable } from '@angular/core';
import { JournalSummaryResponse, PublicationStatus } from '@core/api';

/** Filtros y página del historial de publicaciones. */
export interface PublicationListFilters {
  title: string;
  /** Journal seleccionado en el autocomplete (guarda id + name); null = sin filtro. */
  journal: JournalSummaryResponse | null;
  status: PublicationStatus | null;
  /** Días mínimos en el estado actual (publicaciones estancadas); null = sin filtro. */
  minDaysInStatus: number | null;
  onlyAsMainAuthor: boolean;
  first: number;
}

function emptyFilters(): PublicationListFilters {
  return {
    title: '',
    journal: null,
    status: null,
    minDaysInStatus: null,
    onlyAsMainAuthor: false,
    first: 0,
  };
}

/**
 * Conserva los filtros y la página del historial de publicaciones entre navegaciones (p. ej. al
 * entrar al detalle de una publicación y volver atrás). Es un singleton, así que su estado
 * sobrevive mientras viva la SPA.
 */
@Injectable({ providedIn: 'root' })
export class PublicationFilterState {
  readonly filters = emptyFilters();
}
