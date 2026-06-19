package es.uib.record.backend.journals.domain.repository

import es.uib.record.backend.journals.domain.model.Category

interface CategoryRepository {
    fun save(category: Category): Category

    fun findByNameAndEdition(name: String, edition: String?): Category?

    fun findAllOrderedByName(): List<Category>
}
