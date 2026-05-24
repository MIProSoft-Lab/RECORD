package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.application.usecase.group.dto.InvitableUserResponseDto
import es.uib.record.backend.groups.domain.exception.GroupMemberNotAdminException
import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.groups.domain.repository.InvitationRepository
import es.uib.record.backend.users.open.UserFacade
import es.uib.record.backend.users.open.UserOpenDto
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class SearchInvitableUsersUseCase(
    private val groupRepository: GroupRepository,
    private val invitationRepository: InvitationRepository,
    private val userFacade: UserFacade,
) {
    fun execute(email: String, groupId: UUID, query: String): List<InvitableUserResponseDto> {
        val group = this.groupRepository.findById(groupId) ?: throw GroupNotFoundException(groupId)
        val userId = this.userFacade.getUserIdByEmail(email)

        if (!group.isMember(userId)) throw NotGroupMemberException(userId, groupId)

        if (group.getMemberRole(userId) != GroupRole.ADMIN)
            throw GroupMemberNotAdminException(userId, groupId)

        val groupInvitations = this.invitationRepository.findByGroupId(groupId)

        val usersToInvite =
            this.userFacade
                .searchUsers(query)
                .filter { user ->
                    !group.isMember(user.userId) &&
                        groupInvitations.none { it.inviteeUserId == user.userId }
                }
                .map { this.toInvitableUserResponseDto(it) }

        return usersToInvite
    }

    private fun toInvitableUserResponseDto(user: UserOpenDto): InvitableUserResponseDto {
        return InvitableUserResponseDto(
            id = user.userId,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            profileImageUrl = user.profileImageUrl,
        )
    }
}
