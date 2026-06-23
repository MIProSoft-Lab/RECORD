package es.uib.record.backend.groups.infrastructure.adapter

import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.groups.open.GroupFacade
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class GroupFacadeImpl(private val groupRepository: GroupRepository) : GroupFacade {

    override fun existsById(groupId: UUID): Boolean {
        return this.groupRepository.findById(groupId) != null
    }

    override fun isMember(groupId: UUID, userId: UUID): Boolean {
        return this.groupRepository.findById(groupId)?.isMember(userId) ?: false
    }
}
