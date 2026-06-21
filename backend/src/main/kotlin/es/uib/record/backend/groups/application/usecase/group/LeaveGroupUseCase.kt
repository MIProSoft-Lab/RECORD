package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.LastGroupAdminException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class LeaveGroupUseCase(
    private val groupRepository: GroupRepository,
    private val userFacade: UserFacade,
) {
    fun execute(email: String, groupId: UUID) {
        val group = groupRepository.findById(groupId) ?: throw GroupNotFoundException(groupId)
        val userId = userFacade.getUserIdByEmail(email)

        if (!group.isMember(userId)) throw NotGroupMemberException(userId, groupId)

        if (group.getMemberRole(userId) == GroupRole.ADMIN && group.getAdminsCount() == 1)
            throw LastGroupAdminException(userId, groupId)

        group.removeMember(userId)
        groupRepository.save(group)
    }
}
