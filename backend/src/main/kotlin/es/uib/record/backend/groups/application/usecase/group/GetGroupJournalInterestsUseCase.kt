package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.application.usecase.group.dto.GroupJournalInterestCategoryDto
import es.uib.record.backend.groups.application.usecase.group.dto.GroupJournalInterestMemberDto
import es.uib.record.backend.groups.application.usecase.group.dto.GroupJournalInterestResponseDto
import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.journals.open.InterestedJournalCategoryDto
import es.uib.record.backend.journals.open.InterestedJournalDto
import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.shared.domain.PageResult
import es.uib.record.backend.users.open.UserFacade
import es.uib.record.backend.users.open.UserOpenDto
import java.util.UUID
import org.springframework.stereotype.Component

/**
 * Unión paginada de las revistas marcadas como de interés por los miembros del grupo, con el número
 * de miembros que las marcan y quiénes. El usuario debe ser miembro del grupo.
 */
@Component
class GetGroupJournalInterestsUseCase(
    private val groupRepository: GroupRepository,
    private val userFacade: UserFacade,
    private val journalFacade: JournalFacade,
) {
    fun execute(
        groupId: UUID,
        email: String,
        page: Int,
        size: Int,
    ): PageResult<GroupJournalInterestResponseDto> {
        val userId = this.userFacade.getUserIdByEmail(email)
        val group = this.groupRepository.findById(groupId) ?: throw GroupNotFoundException(groupId)

        if (!group.isMember(userId)) throw NotGroupMemberException(userId, groupId)

        val interestsPage =
            this.journalFacade.getJournalsInterestedByUsers(
                group.getMembersIds().toSet(),
                page,
                size,
            )

        val membersById =
            this.userFacade
                .getUsersByIds(interestsPage.items.flatMap { it.interestedUserIds }.distinct())
                .associateBy { it.userId }

        return PageResult(
            items = interestsPage.items.map { it.toResponseDto(membersById) },
            totalElements = interestsPage.totalElements,
            page = interestsPage.page,
            size = interestsPage.size,
        )
    }

    private fun InterestedJournalDto.toResponseDto(membersById: Map<UUID, UserOpenDto>) =
        GroupJournalInterestResponseDto(
            id = this.journalId,
            name = this.name,
            issn = this.issn,
            eIssn = this.eIssn,
            publisherName = this.publisherName,
            year = this.year,
            categories = this.categories.map { it.toResponseDto() },
            favoriteCount = this.interestedUserIds.size,
            members = this.interestedUserIds.mapNotNull { membersById[it]?.toMemberDto() },
        )

    private fun InterestedJournalCategoryDto.toResponseDto() =
        GroupJournalInterestCategoryDto(
            categoryId = this.categoryId,
            categoryName = this.categoryName,
            edition = this.edition,
            quartile = this.quartile,
            impactFactor = this.impactFactor,
        )

    private fun UserOpenDto.toMemberDto() =
        GroupJournalInterestMemberDto(
            userId = this.userId,
            firstName = this.firstName,
            lastName = this.lastName,
            profileImageUrl = this.profileImageUrl,
        )
}
