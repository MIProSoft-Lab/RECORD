package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.domain.model.JournalSearchItem
import es.uib.record.backend.journals.domain.model.Quartile
import es.uib.record.backend.journals.domain.repository.JournalRepository
import es.uib.record.backend.shared.domain.PageResult
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.springframework.stereotype.Component

/**
 * Búsqueda paginada restringida a las revistas que el usuario ha marcado como de interés. Mismos
 * filtros opcionales que la búsqueda general.
 */
@Component
class ListInterestJournalsUseCase(
    private val journalRepository: JournalRepository,
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
        return this.journalRepository.searchInterests(
            userId,
            normalizedName,
            categoryId,
            quartile,
            page,
            size,
        )
    }
}
