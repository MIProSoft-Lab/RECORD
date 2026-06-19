package es.uib.record.backend.journals.infrastructure.persistence.adapter

import es.uib.record.backend.journals.domain.model.JournalCategoryQuartile
import es.uib.record.backend.journals.domain.repository.JournalCategoryQuartileRepository
import es.uib.record.backend.journals.infrastructure.mapper.toEntity
import es.uib.record.backend.journals.infrastructure.persistence.repository.SpringDataJpaJournalCategoryQuartileRepository
import java.util.UUID
import org.springframework.stereotype.Repository

@Repository
class JournalCategoryQuartileRepositoryAdapter(
    private val springDataJpaJournalCategoryQuartileRepository:
        SpringDataJpaJournalCategoryQuartileRepository
) : JournalCategoryQuartileRepository {

    override fun saveAll(quartiles: List<JournalCategoryQuartile>) {
        this.springDataJpaJournalCategoryQuartileRepository.saveAll(quartiles.map { it.toEntity() })
    }

    override fun deleteByJournalMetricId(journalMetricId: UUID) {
        this.springDataJpaJournalCategoryQuartileRepository.deleteByJournalMetricId(journalMetricId)
    }
}
