package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.domain.model.JournalSearchItem
import es.uib.record.backend.journals.domain.model.Quartile
import es.uib.record.backend.journals.domain.repository.JournalRepository
import es.uib.record.backend.shared.domain.PageResult
import java.util.UUID
import org.springframework.stereotype.Component

/** Búsqueda paginada de revistas por nombre y/o filtros de categoría y cuartil (todos opcionales). */
@Component
class SearchJournalsUseCase(private val journalRepository: JournalRepository) {
    fun execute(
        name: String?,
        categoryId: UUID?,
        quartile: Quartile?,
        page: Int,
        size: Int,
    ): PageResult<JournalSearchItem> {
        val normalizedName = name?.trim()?.takeIf { it.isNotEmpty() }
        return this.journalRepository.search(normalizedName, categoryId, quartile, page, size)
    }
}
