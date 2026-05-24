package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.domain.exception.GroupMemberNotAdminException
import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.LastGroupAdminException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class UpdateGroupMemberRoleUseCase(
    private val groupRepository: GroupRepository,
    private val userFacade: UserFacade,
) {
    fun execute(email: String, groupId: UUID, targetUserId: UUID, newRole: GroupRole) {
        val group = this.groupRepository.findById(groupId) ?: throw GroupNotFoundException(groupId)
        val actingUserId = this.userFacade.getUserIdByEmail(email)

        if (!group.isMember(actingUserId)) throw NotGroupMemberException(actingUserId, groupId)

        if (group.getMemberRole(actingUserId) != GroupRole.ADMIN)
            throw GroupMemberNotAdminException(actingUserId, groupId)

        if (!group.isMember(targetUserId)) throw NotGroupMemberException(targetUserId, groupId)

        val currentRole = group.getMemberRole(targetUserId)
        if (currentRole == newRole) return

        if (currentRole == GroupRole.ADMIN &&
            newRole == GroupRole.MEMBER &&
            group.getAdminsCount() == 1)
            throw LastGroupAdminException(targetUserId, groupId)

        group.updateMemberRole(targetUserId, newRole)
        this.groupRepository.save(group)
    }
}
