package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.publications.application.usecase.dto.PublicationAuthorInputDto
import es.uib.record.backend.publications.application.usecase.dto.PublicationDetailDto
import es.uib.record.backend.publications.application.usecase.dto.UpdatePublicationRequestDto
import es.uib.record.backend.publications.application.usecase.dto.toAuthorDtos
import es.uib.record.backend.publications.application.usecase.dto.toDetailDto
import es.uib.record.backend.publications.domain.exception.AuthorUserNotFoundException
import es.uib.record.backend.publications.domain.exception.InvalidPublicationAuthorException
import es.uib.record.backend.publications.domain.exception.PublicationEditForbiddenException
import es.uib.record.backend.publications.domain.exception.PublicationNotFoundException
import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.internalUserId
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class UpdatePublicationUseCase(
    private val publicationRepository: PublicationRepository,
    private val userFacade: UserFacade,
    private val journalFacade: JournalFacade,
) {
    fun execute(
        publicationId: UUID,
        updatePublicationRequestDto: UpdatePublicationRequestDto,
        email: String,
    ): PublicationDetailDto {
        val userId = this.userFacade.getUserIdByEmail(email)

        val existing =
            this.publicationRepository.findById(publicationId)
                ?: throw PublicationNotFoundException(publicationId)

        // El creador y cualquier autor asociado pueden editar la publicación.
        if (!canEdit(existing, userId))
            throw PublicationEditForbiddenException(publicationId, userId)

        this.validateInternalAuthorsExist(updatePublicationRequestDto.authors)

        // Solo se actualizan título, resumen y autores. El journal/grupo, el estado y el
        // DOI son inmutables tras la creación, por lo que se conservan del agregado actual.
        val updated =
            Publication(
                id = existing.id,
                title = updatePublicationRequestDto.title,
                abstractText = updatePublicationRequestDto.abstractText,
                doi = existing.doi,
                journalId = existing.journalId,
                groupId = existing.groupId,
                status = existing.status,
                createdBy = existing.createdBy,
                createdAt = existing.createdAt,
            )
        // Se respeta el orden de autores recibido. El creador se mantiene siempre como
        // autor interno aunque no venga en la lista enviada.
        updatePublicationRequestDto.authors.forEach { author ->
            when {
                author.userId != null -> updated.addInternalAuthor(author.userId)
                author.firstName != null && author.lastName != null ->
                    updated.addExternalAuthor(author.firstName, author.lastName)
                else -> throw InvalidPublicationAuthorException()
            }
        }
        updated.prependInternalAuthorIfAbsent(existing.createdBy)

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

    /** Valida que cada co-autor interno indicado exista como usuario registrado. */
    private fun validateInternalAuthorsExist(authors: List<PublicationAuthorInputDto>) {
        val requestedIds = authors.mapNotNull { it.userId }.toSet()
        if (requestedIds.isEmpty()) return

        val existingIds =
            this.userFacade.getUsersByIds(requestedIds.toList()).map { it.userId }.toSet()
        val missing = requestedIds - existingIds
        if (missing.isNotEmpty()) throw AuthorUserNotFoundException(missing.first())
    }
}
