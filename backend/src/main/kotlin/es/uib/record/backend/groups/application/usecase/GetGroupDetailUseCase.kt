package es.uib.record.backend.groups.application.usecase

import es.uib.record.backend.groups.application.usecase.dto.GroupDetailResponseDto
import es.uib.record.backend.groups.application.usecase.dto.GroupMemberDetailDto
import es.uib.record.backend.groups.domain.model.GroupMember
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.users.open.UserFacade
import es.uib.record.backend.users.open.UserOpenDto
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class GetGroupDetailUseCase(
    private val groupRepository: GroupRepository,
    private val userFacade: UserFacade
) {
    fun execute(groupId: UUID, email: String): GroupDetailResponseDto {
        val userId = this.userFacade.getUserIdByEmail(email)
        val group = this.groupRepository.findById(groupId) ?: throw GroupNotFoundException(groupId)

        if (!group.isMember(userId))
            throw NotGroupMemberException()

        val usersFromGroup = this.userFacade.getUsersByIds(group.getMembersIds())

        return GroupDetailResponseDto(
            group.id!!,
            group.name,
            group.description,
            group.createdBy,
            group.createdAt,
            this.joinUsersWithGroupMembers(group.members, usersFromGroup, group.createdBy)
        )
    }

    private fun joinUsersWithGroupMembers(
        members: List<GroupMember>,
        users: List<UserOpenDto>,
        createdBy: UUID
    ) : List<GroupMemberDetailDto> {
        return members.map { member ->
            val user = users.first { it.userId == member.userId }
            GroupMemberDetailDto(
                userId = member.userId,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                role = member.role,
                profileImageUrl = user.profileImageUrl,
                isCreator = createdBy == user.userId
            )
        }
    }
}