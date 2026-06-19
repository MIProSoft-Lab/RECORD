package es.uib.record.backend.journals.domain.repository

import es.uib.record.backend.journals.domain.model.JournalCategoryQuartile
import java.util.UUID

interface JournalCategoryQuartileRepository {
    fun saveAll(quartiles: List<JournalCategoryQuartile>)

    fun deleteByJournalMetricId(journalMetricId: UUID)
}
