package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.publications.application.usecase.dto.PublicationDetailDto
import es.uib.record.backend.publications.application.usecase.dto.toDetailDto
import es.uib.record.backend.publications.domain.exception.PublicationNotFoundException
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class GetPublicationDetailUseCase(
    private val publicationRepository: PublicationRepository,
    private val journalFacade: JournalFacade,
) {
    fun execute(publicationId: UUID): PublicationDetailDto {
        val publication =
            this.publicationRepository.findById(publicationId)
                ?: throw PublicationNotFoundException(publicationId)

        val journalName =
            this.journalFacade.getJournalsByIds(setOf(publication.journalId))[publication.journalId]
                ?.name

        return publication.toDetailDto(journalName)
    }
}
