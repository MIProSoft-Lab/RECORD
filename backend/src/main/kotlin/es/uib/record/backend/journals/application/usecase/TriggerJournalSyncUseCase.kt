package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.domain.exception.InvalidSyncTokenException
import es.uib.record.backend.journals.domain.exception.JournalSyncAlreadyRunningException
import es.uib.record.backend.journals.domain.model.SyncState
import es.uib.record.backend.journals.domain.repository.JournalSyncStateRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Disparo manual del volcado, protegido por un secreto estático por cabecera (no existe un sistema
 * de roles de usuario). Lanza la sincronización si no hay otra en curso.
 */
@Component
class TriggerJournalSyncUseCase(
    private val syncStateRepository: JournalSyncStateRepository,
    private val syncJournalsUseCase: SyncJournalsUseCase,
    @Value($$"${application.clarivate.sync.trigger-token}") private val triggerToken: String,
) {
    fun execute(providedToken: String?): SyncState {
        if (triggerToken.isBlank() || providedToken != triggerToken) {
            throw InvalidSyncTokenException()
        }
        if (!syncJournalsUseCase.tryStart()) {
            throw JournalSyncAlreadyRunningException()
        }
        syncJournalsUseCase.runAsync()
        return syncStateRepository.get()
    }
}
