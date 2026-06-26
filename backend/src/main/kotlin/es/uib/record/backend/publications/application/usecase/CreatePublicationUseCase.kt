package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.groups.open.GroupFacade
import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.publications.application.usecase.dto.CreatePublicationRequestDto
import es.uib.record.backend.publications.application.usecase.dto.PublicationAuthorInputDto
import es.uib.record.backend.publications.application.usecase.dto.PublicationDetailDto
import es.uib.record.backend.publications.application.usecase.dto.toAuthorDtos
import es.uib.record.backend.publications.application.usecase.dto.toDetailDto
import es.uib.record.backend.publications.domain.exception.AuthorUserNotFoundException
import es.uib.record.backend.publications.domain.exception.GroupNotFoundForPublicationException
import es.uib.record.backend.publications.domain.exception.InvalidPublicationAuthorException
import es.uib.record.backend.publications.domain.exception.JournalNotFoundForPublicationException
import es.uib.record.backend.publications.domain.exception.UserNotGroupMemberException
import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.PublicationStatus
import es.uib.record.backend.publications.domain.model.internalUserId
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.users.open.UserFacade
import org.springframework.stereotype.Component

@Component
class CreatePublicationUseCase(
    private val publicationRepository: PublicationRepository,
    private val userFacade: UserFacade,
    private val journalFacade: JournalFacade,
    private val groupFacade: GroupFacade,
) {
    fun execute(
        createPublicationRequestDto: CreatePublicationRequestDto,
        email: String,
    ): PublicationDetailDto {
        val userId = this.userFacade.getUserIdByEmail(email)

        if (!this.journalFacade.existsById(createPublicationRequestDto.journalId))
            throw JournalNotFoundForPublicationException(createPublicationRequestDto.journalId)

        if (!this.groupFacade.existsById(createPublicationRequestDto.groupId))
            throw GroupNotFoundForPublicationException(createPublicationRequestDto.groupId)

        if (!this.groupFacade.isMember(createPublicationRequestDto.groupId, userId))
            throw UserNotGroupMemberException(createPublicationRequestDto.groupId, userId)

        // Los co-autores internos pueden ser cualquier usuario registrado (no han de
        // pertenecer al grupo); solo se valida que existan.
        this.validateInternalAuthorsExist(createPublicationRequestDto.authors)

        val toSave =
            Publication(
                title = createPublicationRequestDto.title,
                abstractText = createPublicationRequestDto.abstractText,
                doi = createPublicationRequestDto.doi,
                journalId = createPublicationRequestDto.journalId,
                groupId = createPublicationRequestDto.groupId,
                status = createPublicationRequestDto.status ?: PublicationStatus.PLANNED,
                createdBy = userId,
            )
        // Se respeta el orden de autores recibido (el creador puede ir en cualquier
        // posición). Si el creador no viniera en la lista, se antepone para garantizar
        // que siempre es autor.
        createPublicationRequestDto.authors.forEach { author ->
            when {
                author.userId != null -> toSave.addInternalAuthor(author.userId)
                author.firstName != null && author.lastName != null ->
                    toSave.addExternalAuthor(author.firstName, author.lastName)
                else -> throw InvalidPublicationAuthorException()
            }
        }
        toSave.prependInternalAuthorIfAbsent(userId)

        val saved = this.publicationRepository.save(toSave)
        val journalName =
            this.journalFacade.getJournalsByIds(setOf(saved.journalId))[saved.journalId]?.name
        val usersById =
            this.userFacade
                .getUsersByIds(saved.authors.mapNotNull { it.internalUserId() })
                .associateBy { it.userId }

        return saved.toDetailDto(journalName, saved.authors.toAuthorDtos(usersById))
    }

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
