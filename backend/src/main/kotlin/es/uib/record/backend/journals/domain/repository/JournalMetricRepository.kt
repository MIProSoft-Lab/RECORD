package es.uib.record.backend.journals.domain.repository

import es.uib.record.backend.journals.domain.model.JournalMetric
import java.util.UUID

interface JournalMetricRepository {
    fun save(journalMetric: JournalMetric): JournalMetric

    fun findByJournalIdAndYear(journalId: UUID, year: Int): JournalMetric?

    /** Poda de retención: elimina las métricas del journal cuyos años no están en [years]. */
    fun deleteByJournalIdAndYearNotIn(journalId: UUID, years: List<Int>)
}
