package es.uib.record.backend.journals.application.service

import es.uib.record.backend.journals.domain.model.Category
import es.uib.record.backend.journals.domain.model.Journal
import es.uib.record.backend.journals.domain.model.JournalCategoryQuartile
import es.uib.record.backend.journals.domain.model.JournalMetric
import es.uib.record.backend.journals.domain.repository.CategoryRepository
import es.uib.record.backend.journals.domain.repository.JournalCategoryQuartileRepository
import es.uib.record.backend.journals.domain.repository.JournalMetricRepository
import es.uib.record.backend.journals.domain.repository.JournalRepository
import es.uib.record.backend.journals.infrastructure.client.dto.ClarivateBibliographicResponse
import es.uib.record.backend.journals.infrastructure.client.dto.ClarivateMetricsResponse
import es.uib.record.backend.journals.infrastructure.mapper.parseImpactFactor
import es.uib.record.backend.journals.infrastructure.mapper.parseQuartile
import java.time.Instant
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Persiste un journal completo (journal + métricas por año + cuartiles por categoría) de forma
 * idempotente y en una única transacción. Se separa del orquestador para que cada journal tenga su
 * propio límite transaccional y un fallo no arrastre al resto del volcado.
 */
@Component
class JournalSyncWriter(
    private val journalRepository: JournalRepository,
    private val categoryRepository: CategoryRepository,
    private val journalMetricRepository: JournalMetricRepository,
    private val quartileRepository: JournalCategoryQuartileRepository,
) {

    @Transactional
    fun persist(
        clarivateId: String,
        fallbackName: String?,
        bibliographic: ClarivateBibliographicResponse,
        metricsByYear: List<ClarivateMetricsResponse>,
    ) {
        val now = Instant.now()
        val existing = journalRepository.findByClarivateId(clarivateId)

        val savedJournal =
            journalRepository.save(
                Journal(
                    id = existing?.id,
                    clarivateId = clarivateId,
                    name = bibliographic.name ?: fallbackName ?: clarivateId,
                    issn = bibliographic.issn,
                    eIssn = bibliographic.eIssn,
                    publisherName = bibliographic.publisher?.name,
                    publisherCountry = bibliographic.publisher?.countryRegion,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = if (existing != null) now else null,
                    lastSyncedAt = now,
                )
            )
        val journalId = savedJournal.id!!

        for (metrics in metricsByYear) {
            persistYear(journalId, metrics)
        }

        val keptYears = metricsByYear.map { it.year }
        if (keptYears.isNotEmpty()) {
            journalMetricRepository.deleteByJournalIdAndYearNotIn(journalId, keptYears)
        }
    }

    private fun persistYear(journalId: java.util.UUID, metrics: ClarivateMetricsResponse) {
        val existingMetric = journalMetricRepository.findByJournalIdAndYear(journalId, metrics.year)
        val impactFactor = parseImpactFactor(metrics.metrics?.impactMetrics?.jif)

        val savedMetric =
            journalMetricRepository.save(
                JournalMetric(
                    id = existingMetric?.id,
                    journalId = journalId,
                    year = metrics.year,
                    impactFactor = impactFactor,
                    createdAt = existingMetric?.createdAt ?: Instant.now(),
                )
            )
        val metricId = savedMetric.id!!

        quartileRepository.deleteByJournalMetricId(metricId)

        val quartiles =
            (metrics.ranks?.jif ?: emptyList())
                .mapNotNull { rank ->
                    val quartile = parseQuartile(rank.quartile) ?: return@mapNotNull null
                    val categoryName = rank.category ?: return@mapNotNull null
                    val category = resolveCategory(categoryName, rank.edition)
                    JournalCategoryQuartile(
                        journalMetricId = metricId,
                        journalId = journalId,
                        categoryId = category.id!!,
                        year = metrics.year,
                        quartile = quartile,
                        impactFactor = impactFactor,
                    )
                }
                .distinctBy { it.categoryId }

        quartileRepository.saveAll(quartiles)
    }

    private fun resolveCategory(name: String, edition: String?): Category =
        categoryRepository.findByNameAndEdition(name, edition)
            ?: categoryRepository.save(Category(name = name, edition = edition))
}
