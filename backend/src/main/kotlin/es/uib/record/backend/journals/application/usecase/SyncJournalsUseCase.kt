package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.application.service.JournalSyncWriter
import es.uib.record.backend.journals.domain.model.SyncStatus
import es.uib.record.backend.journals.domain.repository.JournalRepository
import es.uib.record.backend.journals.domain.repository.JournalSyncStateRepository
import es.uib.record.backend.journals.infrastructure.client.ClarivateClient
import es.uib.record.backend.journals.infrastructure.client.dto.ClarivateJournalsPageResponse
import java.time.Instant
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Orquesta el volcado completo de journals desde Clarivate. Las llamadas HTTP ocurren fuera de
 * transacción; cada journal se persiste en su propia transacción (vía [JournalSyncWriter]). El
 * proceso es idempotente y reanudable: si un run falla, el siguiente reanuda saltando los journals
 * ya sincronizados en ese run (comparando `lastSyncedAt` con `runStartedAt`).
 */
@Component
class SyncJournalsUseCase(
    private val clarivateClient: ClarivateClient,
    private val journalRepository: JournalRepository,
    private val syncStateRepository: JournalSyncStateRepository,
    private val journalSyncWriter: JournalSyncWriter,
    @Value($$"${application.clarivate.sync.years-to-keep:2}") private val yearsToKeep: Int,
) {
    private val logger = LoggerFactory.getLogger(SyncJournalsUseCase::class.java)

    /**
     * Marca el estado como RUNNING si no hay un volcado en curso. Devuelve false si ya está
     * corriendo. Un run en estado FAILED se reanuda conservando su `runStartedAt`.
     */
    @Transactional
    fun tryStart(): Boolean {
        val state = syncStateRepository.get()
        if (state.status == SyncStatus.RUNNING) return false

        val resuming = state.status == SyncStatus.FAILED
        syncStateRepository.save(
            state.copy(
                status = SyncStatus.RUNNING,
                runStartedAt = if (resuming) state.runStartedAt ?: Instant.now() else Instant.now(),
                runFinishedAt = null,
                processedCount = if (resuming) state.processedCount else 0,
                totalCount = if (resuming) state.totalCount else null,
                failedCount = if (resuming) state.failedCount else 0,
            )
        )
        return true
    }

    @Async("journalSyncExecutor")
    fun runAsync() {
        doSync()
    }

    fun doSync() {
        val runStartedAt = syncStateRepository.get().runStartedAt ?: Instant.now()
        var processed = 0
        var failed = 0
        try {
            val lastUpdated = clarivateClient.getLastUpdated()

            var page = 1
            var pageResponse = clarivateClient.getJournalsPage(page, PAGE_SIZE)
            updateTotal(pageResponse.metadata.total)

            while (pageResponse.hits.isNotEmpty()) {
                for (hit in pageResponse.hits) {
                    try {
                        if (!isAlreadySynced(hit.id, runStartedAt)) syncOne(hit)
                        processed++
                    } catch (e: Exception) {
                        logger.error("Failed to sync journal {}: {}", hit.id, e.message, e)
                        failed++
                    }
                }
                updateProgress(processed, failed)
                page++
                pageResponse = clarivateClient.getJournalsPage(page, PAGE_SIZE)
            }

            finish(SyncStatus.SUCCESS, lastUpdated, processed, failed)
            logger.info("Journal sync finished: {} processed, {} failed", processed, failed)
        } catch (e: Exception) {
            logger.error("Journal sync run failed after {} processed: {}", processed, e.message, e)
            finish(SyncStatus.FAILED, null, processed, failed)
        }
    }

    private fun syncOne(hit: ClarivateJournalsPageResponse.Hit) {
        val bibliographic = clarivateClient.getBibliographic(hit.id) ?: return
        val years =
            bibliographic.journalCitationReports
                .map { it.year }
                .distinct()
                .sortedDescending()
                .take(yearsToKeep)
        val metricsByYear =
            years.mapNotNull { year ->
                clarivateClient.getMetrics(hit.id, year)?.takeUnless { it.suppressed }
            }
        journalSyncWriter.persist(hit.id, hit.name, bibliographic, metricsByYear)
    }

    private fun isAlreadySynced(clarivateId: String, runStartedAt: Instant): Boolean {
        val lastSyncedAt = journalRepository.findByClarivateId(clarivateId)?.lastSyncedAt
        return lastSyncedAt != null && !lastSyncedAt.isBefore(runStartedAt)
    }

    private fun updateTotal(total: Int) {
        syncStateRepository.save(syncStateRepository.get().copy(totalCount = total))
    }

    private fun updateProgress(processed: Int, failed: Int) {
        syncStateRepository.save(
            syncStateRepository.get().copy(processedCount = processed, failedCount = failed)
        )
    }

    private fun finish(status: SyncStatus, lastUpdated: String?, processed: Int, failed: Int) {
        val state = syncStateRepository.get()
        syncStateRepository.save(
            state.copy(
                status = status,
                runFinishedAt = Instant.now(),
                processedCount = processed,
                failedCount = failed,
                clarivateLastUpdated = lastUpdated ?: state.clarivateLastUpdated,
            )
        )
    }

    companion object {
        private const val PAGE_SIZE = 50
    }
}
