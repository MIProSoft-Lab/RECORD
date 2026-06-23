package es.uib.record.backend.journals.infrastructure.persistence.adapter

import es.uib.record.backend.journals.domain.model.InterestedJournal
import es.uib.record.backend.journals.domain.model.Journal
import es.uib.record.backend.journals.domain.model.JournalDetail
import es.uib.record.backend.journals.domain.model.JournalSearchItem
import es.uib.record.backend.journals.domain.model.Quartile
import es.uib.record.backend.journals.domain.repository.JournalRepository
import es.uib.record.backend.journals.domain.repository.UserJournalInterestRepository
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
    private val userJournalInterestRepository: UserJournalInterestRepository,
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
        return this.hydrateSearchPage(idPage, page, size, isInterest = false)
    }

    override fun searchInterests(
        userId: UUID,
        name: String?,
        categoryId: UUID?,
        quartile: Quartile?,
        page: Int,
        size: Int,
    ): PageResult<JournalSearchItem> {
        val namePattern = name?.let { "%${it.lowercase()}%" }
        val idPage =
            this.springDataJpaJournalRepository.searchInterestJournalIds(
                userId,
                namePattern,
                categoryId,
                quartile,
                PageRequest.of(page, size),
            )
        return this.hydrateSearchPage(idPage, page, size, isInterest = true)
    }

    override fun findInterestedJournalsByUsers(
        memberIds: Set<UUID>,
        page: Int,
        size: Int,
    ): PageResult<InterestedJournal> {
        if (memberIds.isEmpty()) return PageResult(emptyList(), 0, page, size)

        val interestedUsersByJournal =
            this.userJournalInterestRepository.findInterestedUserIdsByJournal(memberIds)
        if (interestedUsersByJournal.isEmpty()) return PageResult(emptyList(), 0, page, size)

        // Orden por número de miembros que la marcan (desc); el id desempata de forma determinista.
        val orderedJournalIds =
            interestedUsersByJournal.entries
                .sortedWith(
                    compareByDescending<Map.Entry<UUID, List<UUID>>> { it.value.size }
                        .thenBy { it.key }
                )
                .map { it.key }

        val totalElements = orderedJournalIds.size.toLong()
        val pageIds = orderedJournalIds.drop(page * size).take(size)
        if (pageIds.isEmpty()) return PageResult(emptyList(), totalElements, page, size)

        val journalsById =
            this.springDataJpaJournalRepository.findAllById(pageIds).associateBy { it.id }
        val categoriesByJournal =
            this.springDataJpaJournalCategoryQuartileRepository
                .findLatestYearViews(pageIds)
                .groupBy { it.journalId }

        // Reordenamos según la página de IDs (ordenada por nº de marcas).
        val items =
            pageIds.mapNotNull { id ->
                val entity = journalsById[id] ?: return@mapNotNull null
                val categories = categoriesByJournal[id].orEmpty().map { it.toInfo() }
                InterestedJournal(
                    journal = entity.toDomain(),
                    year = categories.firstOrNull()?.year,
                    categories = categories,
                    interestedUserIds = interestedUsersByJournal[id].orEmpty(),
                )
            }

        return PageResult(items, totalElements, page, size)
    }

    /**
     * Hidrata una página de IDs de revistas (ya ordenada por nombre) con sus revistas y las
     * categorías/cuartiles de su último año disponible, preservando el orden de la página.
     */
    private fun hydrateSearchPage(
        idPage: org.springframework.data.domain.Page<UUID>,
        page: Int,
        size: Int,
        isInterest: Boolean,
    ): PageResult<JournalSearchItem> {
        val ids = idPage.content
        if (ids.isEmpty()) {
            return PageResult(emptyList(), idPage.totalElements, page, size)
        }

        val journalsById =
            this.springDataJpaJournalRepository.findAllById(ids).associateBy { it.id }
        val categoriesByJournal =
            this.springDataJpaJournalCategoryQuartileRepository.findLatestYearViews(ids).groupBy {
                it.journalId
            }

        // Reordenamos según la página de IDs (que viene ordenada por nombre).
        val items =
            ids.mapNotNull { id ->
                val entity = journalsById[id] ?: return@mapNotNull null
                val categories = categoriesByJournal[id].orEmpty().map { it.toInfo() }
                JournalSearchItem(
                    journal = entity.toDomain(),
                    year = categories.firstOrNull()?.year,
                    categories = categories,
                    isInterest = isInterest,
                )
            }

        return PageResult(items, idPage.totalElements, page, size)
    }

    override fun existsById(id: UUID): Boolean {
        return this.springDataJpaJournalRepository.existsById(id)
    }

    override fun findByIds(ids: Set<UUID>): List<Journal> {
        if (ids.isEmpty()) return emptyList()
        return this.springDataJpaJournalRepository.findAllById(ids).map { it.toDomain() }
    }

    override fun findDetailById(id: UUID): JournalDetail? {
        val journal =
            this.springDataJpaJournalRepository.findById(id).orElse(null)?.toDomain() ?: return null
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
