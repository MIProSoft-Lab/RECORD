package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.domain.exception.JournalNotFoundException
import es.uib.record.backend.journals.domain.repository.JournalRepository
import es.uib.record.backend.journals.domain.repository.UserJournalInterestRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.springframework.stereotype.Component

/**
 * Marca una revista como de interés para el usuario. Idempotente; lanza [JournalNotFoundException] si
 * la revista no existe.
 */
@Component
class MarkJournalInterestUseCase(
    private val journalRepository: JournalRepository,
    private val userJournalInterestRepository: UserJournalInterestRepository,
    private val userFacade: UserFacade,
) {
    fun execute(email: String, journalId: UUID) {
        if (!this.journalRepository.existsById(journalId)) {
            throw JournalNotFoundException(journalId)
        }
        val userId = this.userFacade.getUserIdByEmail(email)
        this.userJournalInterestRepository.add(userId, journalId)
    }
}
