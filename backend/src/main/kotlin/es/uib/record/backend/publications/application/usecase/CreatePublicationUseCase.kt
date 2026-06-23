package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.groups.open.GroupFacade
import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.publications.application.usecase.dto.CreatePublicationRequestDto
import es.uib.record.backend.publications.application.usecase.dto.PublicationDetailDto
import es.uib.record.backend.publications.application.usecase.dto.toDetailDto
import es.uib.record.backend.publications.domain.exception.GroupNotFoundForPublicationException
import es.uib.record.backend.publications.domain.exception.JournalNotFoundForPublicationException
import es.uib.record.backend.publications.domain.exception.UserNotGroupMemberException
import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.PublicationStatus
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
        toSave.addAuthor(userId)

        val saved = this.publicationRepository.save(toSave)
        val journalName =
            this.journalFacade.getJournalsByIds(setOf(saved.journalId))[saved.journalId]?.name

        return saved.toDetailDto(journalName)
    }
}
