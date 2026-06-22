package es.uib.record.backend.groups.application.usecase.group

import es.uib.record.backend.groups.domain.exception.GroupMemberNotAdminException
import es.uib.record.backend.groups.domain.exception.GroupNameAlreadyExistsException
import es.uib.record.backend.groups.domain.exception.GroupNotFoundException
import es.uib.record.backend.groups.domain.exception.NotGroupMemberException
import es.uib.record.backend.groups.domain.model.Group
import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class UpdateGroupUseCase(
    private val groupRepository: GroupRepository,
    private val userFacade: UserFacade,
) {
    fun execute(email: String, groupId: UUID, name: String, description: String?): Group {
        val group = groupRepository.findById(groupId) ?: throw GroupNotFoundException(groupId)
        val actingUserId = userFacade.getUserIdByEmail(email)

        if (!group.isMember(actingUserId)) throw NotGroupMemberException(actingUserId, groupId)

        if (group.getMemberRole(actingUserId) != GroupRole.ADMIN)
            throw GroupMemberNotAdminException(actingUserId, groupId)

        val existing = groupRepository.findByName(name)
        if (existing != null && existing.id != group.id)
            throw GroupNameAlreadyExistsException(name)

        val updated =
            Group(
                id = group.id,
                name = name,
                description = description,
                createdBy = group.createdBy,
                createdAt = group.createdAt,
                members = group.members,
            )

        return groupRepository.save(updated)
    }
}
