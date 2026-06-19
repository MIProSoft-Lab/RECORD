package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.domain.model.SyncStatus
import es.uib.record.backend.journals.domain.repository.JournalSyncStateRepository
import es.uib.record.backend.journals.infrastructure.client.ClarivateClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Comprueba (de forma ligera) si los datos de Clarivate han cambiado desde el último volcado y, si
 * es así, lanza la sincronización. También reanuda un volcado que quedó en estado FAILED.
 */
@Component
class CheckClarivateUpdateUseCase(
    private val clarivateClient: ClarivateClient,
    private val syncStateRepository: JournalSyncStateRepository,
    private val syncJournalsUseCase: SyncJournalsUseCase,
) {
    private val logger = LoggerFactory.getLogger(CheckClarivateUpdateUseCase::class.java)

    fun execute() {
        val state = syncStateRepository.get()
        if (state.status == SyncStatus.RUNNING) {
            logger.debug("Journal sync already running; skipping update check")
            return
        }

        if (state.status == SyncStatus.FAILED) {
            logger.info("Previous journal sync failed; resuming")
            startSync()
            return
        }

        val latest = clarivateClient.getLastUpdated()
        if (latest == null) {
            logger.warn("Could not fetch Clarivate last-updated; skipping this cycle")
            return
        }

        if (latest != state.clarivateLastUpdated) {
            logger.info("Clarivate data changed; triggering journal sync")
            startSync()
        } else {
            logger.debug("Clarivate data unchanged; nothing to do")
        }
    }

    private fun startSync() {
        if (syncJournalsUseCase.tryStart()) syncJournalsUseCase.runAsync()
    }
}
