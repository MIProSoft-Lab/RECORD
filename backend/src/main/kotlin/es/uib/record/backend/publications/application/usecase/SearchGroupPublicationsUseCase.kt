package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.groups.open.GroupFacade
import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.publications.application.usecase.dto.GroupPublicationSummaryDto
import es.uib.record.backend.publications.application.usecase.dto.toGroupSummaryDto
import es.uib.record.backend.publications.domain.exception.UserNotGroupMemberException
import es.uib.record.backend.publications.domain.model.PublicationStatus
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.shared.domain.PageResult
import es.uib.record.backend.users.open.UserFacade
import java.time.Duration
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Component

/**
 * Búsqueda paginada de las publicaciones que pertenecen a un grupo (de todos sus miembros). Solo
 * disponible para miembros del grupo. [memberIds] acota el listado a las publicaciones en las que
 * esos miembros figuran como autores (creador o co-autor); si es `null`, incluye las de todos los
 * miembros. El resto de filtros son opcionales y combinables, igual que en [SearchMyPublicationsUseCase].
 */
@Component
class SearchGroupPublicationsUseCase(
    private val publicationRepository: PublicationRepository,
    private val userFacade: UserFacade,
    private val journalFacade: JournalFacade,
    private val groupFacade: GroupFacade,
) {
    fun execute(
        email: String,
        groupId: UUID,
        memberIds: List<UUID>?,
        title: String?,
        journalId: UUID?,
        status: PublicationStatus?,
        minDaysInStatus: Int?,
        page: Int,
        size: Int,
    ): PageResult<GroupPublicationSummaryDto> {
        val userId = this.userFacade.getUserIdByEmail(email)
        if (!this.groupFacade.isMember(groupId, userId)) {
            throw UserNotGroupMemberException(groupId, userId)
        }

        // Si se piden miembros concretos, se intersectan con los del grupo (descarta ids ajenos).
        // Una selección vacía tras la intersección significa "ningún miembro" → página vacía.
        val authorIds: List<UUID>? =
            memberIds?.let { requested ->
                val groupMemberIds = this.groupFacade.getMemberIds(groupId).toSet()
                requested.filter { it in groupMemberIds }
            }
        if (authorIds != null && authorIds.isEmpty()) {
            return PageResult(emptyList(), totalElements = 0, page = page, size = size)
        }

        val normalizedTitle = title?.trim()?.takeIf { it.isNotEmpty() }
        val staleBefore = minDaysInStatus?.let { Instant.now().minus(Duration.ofDays(it.toLong())) }
        val excludeFinalStatuses = staleBefore != null

        val result =
            this.publicationRepository.searchByGroup(
                groupId = groupId,
                authorIds = authorIds,
                title = normalizedTitle,
                status = status,
                journalId = journalId,
                staleBefore = staleBefore,
                excludeFinalStatuses = excludeFinalStatuses,
                page = page,
                size = size,
            )

        val journalNamesById =
            this.journalFacade
                .getJournalsByIds(result.items.map { it.journalId }.toSet())
                .mapValues { it.value.name }
        val creatorsById =
            this.userFacade
                .getUsersByIds(result.items.map { it.createdBy }.distinct())
                .associateBy { it.userId }

        return PageResult(
            items =
                result.items.map {
                    it.toGroupSummaryDto(journalNamesById[it.journalId], creatorsById[it.createdBy])
                },
            totalElements = result.totalElements,
            page = result.page,
            size = result.size,
        )
    }
}
