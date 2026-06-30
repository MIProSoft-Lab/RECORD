package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.domain.model.JournalSearchItem
import es.uib.record.backend.journals.domain.model.Quartile
import es.uib.record.backend.journals.domain.repository.JournalRepository
import es.uib.record.backend.journals.domain.repository.UserJournalInterestRepository
import es.uib.record.backend.shared.domain.PageResult
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.springframework.stereotype.Component

/**
 * Búsqueda paginada de revistas por nombre y/o filtros de categoría y cuartil (todos opcionales).
 */
@Component
class SearchJournalsUseCase(
    private val journalRepository: JournalRepository,
    private val userJournalInterestRepository: UserJournalInterestRepository,
    private val userFacade: UserFacade,
) {
    fun execute(
        email: String,
        name: String?,
        categoryId: UUID?,
        quartile: Quartile?,
        page: Int,
        size: Int,
    ): PageResult<JournalSearchItem> {
        val userId = this.userFacade.getUserIdByEmail(email)
        val normalizedName = name?.trim()?.takeIf { it.isNotEmpty() }
        val result = this.journalRepository.search(normalizedName, categoryId, quartile, page, size)
        val interestIds = this.userJournalInterestRepository.findInterestJournalIds(userId)
        return result.copy(
            items = result.items.map { it.copy(isInterest = it.journal.id in interestIds) }
        )
    }
}
