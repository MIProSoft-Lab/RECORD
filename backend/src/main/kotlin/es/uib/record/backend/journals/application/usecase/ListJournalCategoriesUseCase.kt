package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.domain.model.Category
import es.uib.record.backend.journals.domain.repository.CategoryRepository
import org.springframework.stereotype.Component

/** Catálogo de categorías ordenado por nombre, para poblar el filtro de la búsqueda de revistas. */
@Component
class ListJournalCategoriesUseCase(private val categoryRepository: CategoryRepository) {
    fun execute(): List<Category> = this.categoryRepository.findAllOrderedByName()
}
