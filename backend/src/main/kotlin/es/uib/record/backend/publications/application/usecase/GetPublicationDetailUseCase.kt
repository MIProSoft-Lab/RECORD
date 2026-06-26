package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.publications.application.usecase.dto.PublicationDetailDto
import es.uib.record.backend.publications.application.usecase.dto.toAuthorDtos
import es.uib.record.backend.publications.application.usecase.dto.toDetailDto
import es.uib.record.backend.publications.domain.exception.PublicationNotFoundException
import es.uib.record.backend.publications.domain.model.internalUserId
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class GetPublicationDetailUseCase(
    private val publicationRepository: PublicationRepository,
    private val journalFacade: JournalFacade,
    private val userFacade: UserFacade,
) {
    fun execute(publicationId: UUID): PublicationDetailDto {
        val publication =
            this.publicationRepository.findById(publicationId)
                ?: throw PublicationNotFoundException(publicationId)

        val journalName =
            this.journalFacade.getJournalsByIds(setOf(publication.journalId))[publication.journalId]
                ?.name
        val usersById =
            this.userFacade
                .getUsersByIds(publication.authors.mapNotNull { it.internalUserId() })
                .associateBy { it.userId }

        return publication.toDetailDto(journalName, publication.authors.toAuthorDtos(usersById))
    }
}
