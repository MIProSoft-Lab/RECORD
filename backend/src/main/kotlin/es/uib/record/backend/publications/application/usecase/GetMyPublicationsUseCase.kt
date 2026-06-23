package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.publications.application.usecase.dto.PublicationSummaryDto
import es.uib.record.backend.publications.application.usecase.dto.toSummaryDto
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.users.open.UserFacade
import org.springframework.stereotype.Component

@Component
class GetMyPublicationsUseCase(
    private val publicationRepository: PublicationRepository,
    private val userFacade: UserFacade,
    private val journalFacade: JournalFacade,
) {
    fun execute(email: String): List<PublicationSummaryDto> {
        val userId = this.userFacade.getUserIdByEmail(email)
        val publications = this.publicationRepository.findAllByCreatedBy(userId)

        val journalNamesById =
            this.journalFacade
                .getJournalsByIds(publications.map { it.journalId }.toSet())
                .mapValues { it.value.name }

        return publications.map { it.toSummaryDto(journalNamesById[it.journalId]) }
    }
}
