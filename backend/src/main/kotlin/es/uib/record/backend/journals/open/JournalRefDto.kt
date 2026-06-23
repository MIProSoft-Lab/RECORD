package es.uib.record.backend.journals.open

import java.util.UUID

/** Referencia mínima a una revista, expuesta a otros módulos. */
data class JournalRefDto(val journalId: UUID, val name: String, val issn: String?)
