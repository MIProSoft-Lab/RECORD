package es.uib.record.backend.journals.infrastructure.persistence.repository

import es.uib.record.backend.journals.infrastructure.persistence.entity.CategoryEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataJpaCategoryRepository : JpaRepository<CategoryEntity, UUID> {
    fun findByNameAndEdition(name: String, edition: String?): CategoryEntity?

    fun findAllByOrderByNameAsc(): List<CategoryEntity>
}
