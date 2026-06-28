package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.publications.domain.exception.PublicationDeleteForbiddenException
import es.uib.record.backend.publications.domain.exception.PublicationNotFoundException
import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.internalUserId
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class DeletePublicationUseCase(
    private val publicationRepository: PublicationRepository,
    private val userFacade: UserFacade,
) {
    fun execute(publicationId: UUID, email: String) {
        val userId = this.userFacade.getUserIdByEmail(email)

        val existing =
            this.publicationRepository.findById(publicationId)
                ?: throw PublicationNotFoundException(publicationId)

        // El creador y cualquier autor interno pueden eliminar la publicación.
        if (!canDelete(existing, userId))
            throw PublicationDeleteForbiddenException(publicationId, userId)

        this.publicationRepository.delete(publicationId)
    }

    private fun canDelete(publication: Publication, userId: UUID): Boolean =
        publication.createdBy == userId ||
            publication.authors.any { it.internalUserId() == userId }
}
