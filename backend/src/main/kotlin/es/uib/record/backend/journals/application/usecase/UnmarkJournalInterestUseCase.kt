package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.domain.repository.UserJournalInterestRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.springframework.stereotype.Component

/** Quita una revista de los intereses del usuario. Idempotente. */
@Component
class UnmarkJournalInterestUseCase(
    private val userJournalInterestRepository: UserJournalInterestRepository,
    private val userFacade: UserFacade,
) {
    fun execute(email: String, journalId: UUID) {
        val userId = this.userFacade.getUserIdByEmail(email)
        this.userJournalInterestRepository.remove(userId, journalId)
    }
}
