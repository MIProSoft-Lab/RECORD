package es.uib.record.backend.journals.infrastructure.adapter

import es.uib.record.backend.journals.domain.model.InterestedJournal
import es.uib.record.backend.journals.domain.model.JournalCategoryQuartileInfo
import es.uib.record.backend.journals.domain.repository.JournalRepository
import es.uib.record.backend.journals.open.InterestedJournalCategoryDto
import es.uib.record.backend.journals.open.InterestedJournalDto
import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.shared.domain.PageResult
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class JournalFacadeImpl(private val journalRepository: JournalRepository) : JournalFacade {

    override fun getJournalsInterestedByUsers(
        userIds: Set<UUID>,
        page: Int,
        size: Int,
    ): PageResult<InterestedJournalDto> {
        val result = this.journalRepository.findInterestedJournalsByUsers(userIds, page, size)
        return PageResult(
            items = result.items.map { it.toOpenDto() },
            totalElements = result.totalElements,
            page = result.page,
            size = result.size,
        )
    }

    private fun InterestedJournal.toOpenDto() =
        InterestedJournalDto(
            journalId = this.journal.id!!,
            name = this.journal.name,
            issn = this.journal.issn,
            eIssn = this.journal.eIssn,
            publisherName = this.journal.publisherName,
            year = this.year,
            categories = this.categories.map { it.toOpenDto() },
            interestedUserIds = this.interestedUserIds,
        )

    private fun JournalCategoryQuartileInfo.toOpenDto() =
        InterestedJournalCategoryDto(
            categoryId = this.categoryId,
            categoryName = this.categoryName,
            edition = this.edition,
            quartile = this.quartile.name,
            impactFactor = this.impactFactor?.toDouble(),
        )
}
