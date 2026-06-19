package es.uib.record.backend.journals.infrastructure.persistence.adapter

import es.uib.record.backend.journals.domain.model.Category
import es.uib.record.backend.journals.domain.repository.CategoryRepository
import es.uib.record.backend.journals.infrastructure.mapper.toDomain
import es.uib.record.backend.journals.infrastructure.mapper.toEntity
import es.uib.record.backend.journals.infrastructure.persistence.repository.SpringDataJpaCategoryRepository
import org.springframework.stereotype.Repository

@Repository
class CategoryRepositoryAdapter(
    private val springDataJpaCategoryRepository: SpringDataJpaCategoryRepository
) : CategoryRepository {

    override fun save(category: Category): Category {
        return this.springDataJpaCategoryRepository.save(category.toEntity()).toDomain()
    }

    override fun findByNameAndEdition(name: String, edition: String?): Category? {
        return this.springDataJpaCategoryRepository.findByNameAndEdition(name, edition)?.toDomain()
    }

    override fun findAllOrderedByName(): List<Category> {
        return this.springDataJpaCategoryRepository.findAllByOrderByNameAsc().map { it.toDomain() }
    }
}
