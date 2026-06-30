package es.uib.record.backend.groups.infrastructure.adapter

import es.uib.record.backend.groups.domain.model.GroupRole
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.groups.domain.repository.PublicationVisibilityRepository
import es.uib.record.backend.groups.open.GroupFacade
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class GroupFacadeImpl(
    private val groupRepository: GroupRepository,
    private val publicationVisibilityRepository: PublicationVisibilityRepository,
) : GroupFacade {

    override fun existsById(groupId: UUID): Boolean {
        return this.groupRepository.findById(groupId) != null
    }

    override fun isMember(groupId: UUID, userId: UUID): Boolean {
        return this.groupRepository.findById(groupId)?.isMember(userId) ?: false
    }

    override fun isAdmin(groupId: UUID, userId: UUID): Boolean {
        val group = this.groupRepository.findById(groupId) ?: return false
        return group.isMember(userId) && group.getMemberRole(userId) == GroupRole.ADMIN
    }

    override fun getMemberIds(groupId: UUID): List<UUID> {
        return this.groupRepository.findById(groupId)?.getMembersIds() ?: emptyList()
    }

    override fun getOwnersHiddenFromViewer(groupId: UUID, viewerId: UUID): Set<UUID> {
        return this.publicationVisibilityRepository.findOwnersHiddenFromViewer(groupId, viewerId)
    }
}
