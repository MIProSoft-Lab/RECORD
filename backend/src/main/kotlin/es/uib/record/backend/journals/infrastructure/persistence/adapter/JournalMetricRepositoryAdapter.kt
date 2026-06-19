package es.uib.record.backend.journals.infrastructure.persistence.adapter

import es.uib.record.backend.journals.domain.model.JournalMetric
import es.uib.record.backend.journals.domain.repository.JournalMetricRepository
import es.uib.record.backend.journals.infrastructure.mapper.toDomain
import es.uib.record.backend.journals.infrastructure.mapper.toEntity
import es.uib.record.backend.journals.infrastructure.persistence.repository.SpringDataJpaJournalMetricRepository
import java.util.UUID
import org.springframework.stereotype.Repository

@Repository
class JournalMetricRepositoryAdapter(
    private val springDataJpaJournalMetricRepository: SpringDataJpaJournalMetricRepository
) : JournalMetricRepository {

    override fun save(journalMetric: JournalMetric): JournalMetric {
        return this.springDataJpaJournalMetricRepository.save(journalMetric.toEntity()).toDomain()
    }

    override fun findByJournalIdAndYear(journalId: UUID, year: Int): JournalMetric? {
        return this.springDataJpaJournalMetricRepository
            .findByJournalIdAndYear(journalId, year)
            ?.toDomain()
    }

    override fun deleteByJournalIdAndYearNotIn(journalId: UUID, years: List<Int>) {
        this.springDataJpaJournalMetricRepository.deleteByJournalIdAndYearNotIn(journalId, years)
    }
}
