package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.domain.exception.GroupMemberNotAdminException
import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.groups.domain.repository.InvitationRepository
import es.uib.record.backend.users.open.UserFacade
import jakarta.transaction.Transactional
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class DeleteGroupUseCase(
    private val groupRepository: GroupRepository,
    private val invitationRepository: InvitationRepository,
    private val userFacade: UserFacade,
) {
    @Transactional
    fun execute(email: String, groupId: UUID) {
        val group = groupRepository.findById(groupId) ?: throw GroupNotFoundException(groupId)
        val actingUserId = userFacade.getUserIdByEmail(email)

        if (!group.isMember(actingUserId)) throw NotGroupMemberException(actingUserId, groupId)

        if (group.getMemberRole(actingUserId) != GroupRole.ADMIN)
            throw GroupMemberNotAdminException(actingUserId, groupId)

        invitationRepository.deleteAllByGroupId(groupId)
        groupRepository.delete(groupId)
    }
}
