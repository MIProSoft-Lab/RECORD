package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.publications.application.usecase.dto.PublicationSummaryDto
import es.uib.record.backend.publications.application.usecase.dto.toSummaryDto
import es.uib.record.backend.publications.domain.model.PublicationStatus
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.shared.domain.PageResult
import es.uib.record.backend.users.open.UserFacade
import java.time.Duration
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Component

/**
 * Búsqueda paginada del historial de publicaciones del usuario (creadas por él o donde figura como
 * autor). Todos los filtros son opcionales y combinables.
 */
@Component
class SearchMyPublicationsUseCase(
    private val publicationRepository: PublicationRepository,
    private val userFacade: UserFacade,
    private val journalFacade: JournalFacade,
) {
    fun execute(
        email: String,
        title: String?,
        journalId: UUID?,
        status: PublicationStatus?,
        minDaysInStatus: Int?,
        onlyAsMainAuthor: Boolean,
        page: Int,
        size: Int,
    ): PageResult<PublicationSummaryDto> {
        val userId = this.userFacade.getUserIdByEmail(email)
        val normalizedTitle = title?.trim()?.takeIf { it.isNotEmpty() }

        // "Estancada al menos N días" = su último cambio de estado fue hace N o más días, es
        // decir ocurrió antes (o justo) de `now - N días`. Al aplicar este filtro carece de
        // sentido incluir publicaciones en estado final (ya no esperan respuesta).
        val staleBefore = minDaysInStatus?.let { Instant.now().minus(Duration.ofDays(it.toLong())) }
        val excludeFinalStatuses = staleBefore != null

        val result =
            this.publicationRepository.searchByAuthor(
                userId = userId,
                title = normalizedTitle,
                status = status,
                journalId = journalId,
                staleBefore = staleBefore,
                excludeFinalStatuses = excludeFinalStatuses,
                onlyMainAuthor = onlyAsMainAuthor,
                page = page,
                size = size,
            )

        val journalNamesById =
            this.journalFacade
                .getJournalsByIds(result.items.map { it.journalId }.toSet())
                .mapValues { it.value.name }

        return PageResult(
            items = result.items.map { it.toSummaryDto(journalNamesById[it.journalId]) },
            totalElements = result.totalElements,
            page = result.page,
            size = result.size,
        )
    }
}
