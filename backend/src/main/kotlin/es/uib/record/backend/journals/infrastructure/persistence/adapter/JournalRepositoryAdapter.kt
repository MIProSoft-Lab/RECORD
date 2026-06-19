package es.uib.record.backend.journals.infrastructure.persistence.adapter

import es.uib.record.backend.journals.domain.model.Journal
import es.uib.record.backend.journals.domain.model.JournalDetail
import es.uib.record.backend.journals.domain.model.JournalSearchItem
import es.uib.record.backend.journals.domain.model.Quartile
import es.uib.record.backend.journals.domain.repository.JournalRepository
import es.uib.record.backend.journals.infrastructure.mapper.toDomain
import es.uib.record.backend.journals.infrastructure.mapper.toEntity
import es.uib.record.backend.journals.infrastructure.mapper.toInfo
import es.uib.record.backend.journals.infrastructure.persistence.repository.SpringDataJpaJournalCategoryQuartileRepository
import es.uib.record.backend.journals.infrastructure.persistence.repository.SpringDataJpaJournalMetricRepository
import es.uib.record.backend.journals.infrastructure.persistence.repository.SpringDataJpaJournalRepository
import es.uib.record.backend.shared.domain.PageResult
import java.time.Instant
import java.util.UUID
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class JournalRepositoryAdapter(
    private val springDataJpaJournalRepository: SpringDataJpaJournalRepository,
    private val springDataJpaJournalMetricRepository: SpringDataJpaJournalMetricRepository,
    private val springDataJpaJournalCategoryQuartileRepository:
        SpringDataJpaJournalCategoryQuartileRepository,
) : JournalRepository {

    override fun save(journal: Journal): Journal {
        return this.springDataJpaJournalRepository.save(journal.toEntity()).toDomain()
    }

    override fun findByClarivateId(clarivateId: String): Journal? {
        return this.springDataJpaJournalRepository.findByClarivateId(clarivateId)?.toDomain()
    }

    override fun markSynced(id: UUID, syncedAt: Instant) {
        this.springDataJpaJournalRepository.markSynced(id, syncedAt)
    }

    override fun search(
        name: String?,
        categoryId: UUID?,
        quartile: Quartile?,
        page: Int,
        size: Int,
    ): PageResult<JournalSearchItem> {
        val namePattern = name?.let { "%${it.lowercase()}%" }
        val idPage =
            this.springDataJpaJournalRepository.searchJournalIds(
                namePattern,
                categoryId,
                quartile,
                PageRequest.of(page, size),
            )
        val ids = idPage.content
        if (ids.isEmpty()) {
            return PageResult(emptyList(), idPage.totalElements, page, size)
        }

        val journalsById =
            this.springDataJpaJournalRepository.findAllById(ids).associateBy { it.id }
        val categoriesByJournal =
            this.springDataJpaJournalCategoryQuartileRepository
                .findLatestYearViews(ids)
                .groupBy { it.journalId }

        // Reordenamos según la página de IDs (que viene ordenada por nombre).
        val items =
            ids.mapNotNull { id ->
                val entity = journalsById[id] ?: return@mapNotNull null
                val categories = categoriesByJournal[id].orEmpty().map { it.toInfo() }
                JournalSearchItem(
                    journal = entity.toDomain(),
                    year = categories.firstOrNull()?.year,
                    categories = categories,
                )
            }

        return PageResult(items, idPage.totalElements, page, size)
    }

    override fun findDetailById(id: UUID): JournalDetail? {
        val journal =
            this.springDataJpaJournalRepository.findById(id).orElse(null)?.toDomain()
                ?: return null
        val metrics =
            this.springDataJpaJournalMetricRepository.findByJournalIdOrderByYearDesc(id).map {
                it.toDomain()
            }
        val categoryQuartiles =
            this.springDataJpaJournalCategoryQuartileRepository.findViewsByJournalId(id).map {
                it.toInfo()
            }
        return JournalDetail(journal, metrics, categoryQuartiles)
    }
}
