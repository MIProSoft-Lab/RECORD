package es.uib.record.backend.publications.domain.model

enum class PublicationStatus {
    PLANNED,
    SUBMITTED,
    UNDER_REVIEW,
    MINOR_REVISION,
    MAJOR_REVISION,
    REJECTED,
    ACCEPTED,
    PUBLISHED;

    /** Estados a los que se puede transicionar desde este estado. */
    fun allowedTransitions(): Set<PublicationStatus> = ALLOWED_TRANSITIONS[this] ?: emptySet()

    /** Indica si la transición de este estado a [target] está permitida. */
    fun canTransitionTo(target: PublicationStatus): Boolean = target in allowedTransitions()

    /**
     * Indica si es un estado final (terminal) del ciclo de vida: no admite más transiciones. Hoy
     * son REJECTED y PUBLISHED. Se deriva de la máquina de estados para no duplicar la lista.
     */
    fun isFinal(): Boolean = allowedTransitions().isEmpty()

    private companion object {
        /**
         * Máquina de estados del ciclo de vida de una publicación. REJECTED y PUBLISHED son estados
         * terminales para los cambios de estado genéricos ([changeStatus]).
         *
         * El reenvío de una publicación rechazada (REJECTED -> SUBMITTED cambiando de journal) se
         * modela aparte en [Publication.resubmit], que es una operación atómica con sus propias
         * reglas y no abre esta transición genérica.
         */
        val ALLOWED_TRANSITIONS: Map<PublicationStatus, Set<PublicationStatus>> =
            mapOf(
                PLANNED to setOf(SUBMITTED),
                SUBMITTED to setOf(UNDER_REVIEW, REJECTED),
                UNDER_REVIEW to setOf(MINOR_REVISION, MAJOR_REVISION, ACCEPTED, REJECTED),
                MINOR_REVISION to setOf(UNDER_REVIEW),
                MAJOR_REVISION to setOf(UNDER_REVIEW),
                ACCEPTED to setOf(PUBLISHED),
                REJECTED to emptySet(),
                PUBLISHED to emptySet(),
            )
    }
}
