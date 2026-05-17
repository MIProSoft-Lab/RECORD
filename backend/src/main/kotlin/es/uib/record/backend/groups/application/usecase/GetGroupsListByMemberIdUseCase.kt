package es.uib.record.backend.groups.application.usecase

import es.uib.record.backend.groups.application.usecase.dto.GroupSummaryResponseDto
import es.uib.record.backend.groups.domain.Group
import es.uib.record.backend.groups.domain.GroupRepository
import es.uib.record.backend.groups.infrastructure.mapper.toResponse
import es.uib.record.backend.users.open.UserFacade
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class GetGroupsListByMemberIdUseCase(
    private val groupRepository: GroupRepository,
    private val userFacade: UserFacade
) {
    fun execute(email: String): List<GroupSummaryResponseDto> {
        val userId = this.userFacade.getUserIdByEmail(email)
        val groups = this.groupRepository.findAllByMemberId(userId)
        return groups.map { group ->
            GroupSummaryResponseDto(
                id = group.id!!,
                name = group.name,
                description = group.description,
                role = group.getMemberRole(userId),
                memberCount = group.getMembersCount(),
                isOwner = group.createdBy == userId
            )
        }
    }
}