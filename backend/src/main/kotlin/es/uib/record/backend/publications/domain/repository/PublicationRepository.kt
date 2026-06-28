package es.uib.record.backend.publications.domain.repository

import es.uib.record.backend.publications.domain.model.Publication
import java.util.UUID

interface PublicationRepository {
    fun save(publication: Publication): Publication

    fun findById(id: UUID): Publication?

    fun findAllByCreatedBy(createdBy: UUID): List<Publication>

    fun findAllByAuthor(userId: UUID): List<Publication>

    fun delete(id: UUID)
}
