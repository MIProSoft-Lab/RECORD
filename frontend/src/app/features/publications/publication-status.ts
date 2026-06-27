import { PublicationStatus } from '@core/api';

/**
 * Máquina de estados del ciclo de vida de una publicación. Es el espejo en frontend
 * de la definición de dominio del backend (PublicationStatus.kt). El backend sigue
 * siendo la fuente de verdad y valida toda transición; aquí solo se usa para mostrar
 * únicamente las transiciones válidas en la UI.
 *
 * REJECTED y PUBLISHED son estados terminales.
 */
const ALLOWED_TRANSITIONS: Record<PublicationStatus, PublicationStatus[]> = {
  [PublicationStatus.Planned]: [PublicationStatus.Submitted],
  [PublicationStatus.Submitted]: [PublicationStatus.UnderReview, PublicationStatus.Rejected],
  [PublicationStatus.UnderReview]: [
    PublicationStatus.MinorRevision,
    PublicationStatus.MajorRevision,
    PublicationStatus.Accepted,
    PublicationStatus.Rejected,
  ],
  [PublicationStatus.MinorRevision]: [PublicationStatus.UnderReview],
  [PublicationStatus.MajorRevision]: [PublicationStatus.UnderReview],
  [PublicationStatus.Accepted]: [PublicationStatus.Published],
  [PublicationStatus.Rejected]: [],
  [PublicationStatus.Published]: [],
};

/** Estados a los que se puede transicionar desde [status]. */
export function allowedStatusTransitions(status: PublicationStatus): PublicationStatus[] {
  return ALLOWED_TRANSITIONS[status] ?? [];
}

/** Severidad de PrimeNG (`p-tag`) asociada a cada estado de publicación. */
export function publicationStatusSeverity(
  status: PublicationStatus,
): 'success' | 'info' | 'warn' | 'danger' {
  switch (status) {
    case PublicationStatus.Published:
    case PublicationStatus.Accepted:
      return 'success';
    case PublicationStatus.Rejected:
      return 'danger';
    case PublicationStatus.UnderReview:
    case PublicationStatus.MinorRevision:
    case PublicationStatus.MajorRevision:
      return 'warn';
    default:
      return 'info';
  }
}
