package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.publications.application.usecase.dto.PublicationDetailDto
import es.uib.record.backend.publications.application.usecase.dto.toAuthorDtos
import es.uib.record.backend.publications.application.usecase.dto.toDetailDto
import es.uib.record.backend.publications.domain.exception.JournalNotFoundForPublicationException
import es.uib.record.backend.publications.domain.exception.PublicationEditForbiddenException
import es.uib.record.backend.publications.domain.exception.PublicationNotFoundException
import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.internalUserId
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class ResubmitPublicationUseCase(
    private val publicationRepository: PublicationRepository,
    private val userFacade: UserFacade,
    private val journalFacade: JournalFacade,
) {
    fun execute(
        publicationId: UUID,
        newJournalId: UUID,
        email: String,
    ): PublicationDetailDto {
        val userId = this.userFacade.getUserIdByEmail(email)

        val existing =
            this.publicationRepository.findById(publicationId)
                ?: throw PublicationNotFoundException(publicationId)

        // El creador y cualquier autor asociado pueden reenviar, igual que en la edición.
        if (!canEdit(existing, userId))
            throw PublicationEditForbiddenException(publicationId, userId)

        if (!this.journalFacade.existsById(newJournalId))
            throw JournalNotFoundForPublicationException(newJournalId)

        // Las validaciones de estado (REJECTED) y journal distinto viven en el dominio.
        val updated = existing.resubmit(newJournalId)

        val saved = this.publicationRepository.save(updated)
        val journalName =
            this.journalFacade.getJournalsByIds(setOf(saved.journalId))[saved.journalId]?.name
        val usersById =
            this.userFacade
                .getUsersByIds(saved.authors.mapNotNull { it.internalUserId() })
                .associateBy { it.userId }

        return saved.toDetailDto(journalName, saved.authors.toAuthorDtos(usersById))
    }

    private fun canEdit(publication: Publication, userId: UUID): Boolean =
        publication.createdBy == userId ||
            publication.authors.any { it.internalUserId() == userId }
}
