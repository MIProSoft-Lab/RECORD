package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.publications.application.usecase.dto.PublicationDetailDto
import es.uib.record.backend.publications.application.usecase.dto.toAuthorDtos
import es.uib.record.backend.publications.application.usecase.dto.toDetailDto
import es.uib.record.backend.publications.domain.exception.PublicationEditForbiddenException
import es.uib.record.backend.publications.domain.exception.PublicationNotFoundException
import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.PublicationStatus
import es.uib.record.backend.publications.domain.model.internalUserId
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class ChangePublicationStatusUseCase(
    private val publicationRepository: PublicationRepository,
    private val userFacade: UserFacade,
    private val journalFacade: JournalFacade,
) {
    fun execute(
        publicationId: UUID,
        newStatus: PublicationStatus,
        email: String,
        comment: String? = null,
    ): PublicationDetailDto {
        val userId = this.userFacade.getUserIdByEmail(email)

        val existing =
            this.publicationRepository.findById(publicationId)
                ?: throw PublicationNotFoundException(publicationId)

        // El creador y cualquier autor asociado pueden cambiar el estado, igual que en la edición.
        if (!canEdit(existing, userId))
            throw PublicationEditForbiddenException(publicationId, userId)

        // La validación de transición vive en el dominio (changeStatus).
        val updated = existing.changeStatus(newStatus, comment)

        val saved = this.publicationRepository.save(updated)
        // Incluye el journal actual y todos los del historial (p. ej. el anterior a un reenvío).
        val journalIds = (saved.statusHistory.map { it.journalId } + saved.journalId).toSet()
        val journalNamesById =
            this.journalFacade.getJournalsByIds(journalIds).mapValues { it.value.name }
        val usersById =
            this.userFacade
                .getUsersByIds(saved.authors.mapNotNull { it.internalUserId() })
                .associateBy { it.userId }

        return saved.toDetailDto(journalNamesById, saved.authors.toAuthorDtos(usersById))
    }

    private fun canEdit(publication: Publication, userId: UUID): Boolean =
        publication.createdBy == userId || publication.authors.any { it.internalUserId() == userId }
}
