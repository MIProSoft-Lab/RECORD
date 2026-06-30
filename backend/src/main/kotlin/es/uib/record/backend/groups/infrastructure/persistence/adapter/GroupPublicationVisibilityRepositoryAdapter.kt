package es.uib.record.backend.groups.infrastructure.persistence.adapter

import es.uib.record.backend.groups.domain.repository.PublicationVisibilityRepository
import es.uib.record.backend.groups.infrastructure.persistence.entity.GroupPublicationVisibilityEntity
import es.uib.record.backend.groups.infrastructure.persistence.entity.GroupPublicationVisibilityId
import es.uib.record.backend.groups.infrastructure.persistence.repository.SpringDataJpaGroupPublicationVisibilityRepository
import java.util.UUID
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class GroupPublicationVisibilityRepositoryAdapter(
    private val repository: SpringDataJpaGroupPublicationVisibilityRepository
) : PublicationVisibilityRepository {

    override fun hide(groupId: UUID, ownerId: UUID, viewerId: UUID) {
        val id = GroupPublicationVisibilityId(groupId, ownerId, viewerId)
        if (!this.repository.existsById(id)) {
            this.repository.save(GroupPublicationVisibilityEntity(id))
        }
    }

    override fun unhide(groupId: UUID, ownerId: UUID, viewerId: UUID) {
        val id = GroupPublicationVisibilityId(groupId, ownerId, viewerId)
        if (this.repository.existsById(id)) {
            this.repository.deleteById(id)
        }
    }

    override fun findViewersHiddenByOwner(groupId: UUID, ownerId: UUID): Set<UUID> {
        return this.repository
            .findByIdGroupIdAndIdOwnerId(groupId, ownerId)
            .map { it.id.hiddenFromUserId }
            .toSet()
    }

    override fun findOwnersHiddenFromViewer(groupId: UUID, viewerId: UUID): Set<UUID> {
        return this.repository
            .findByIdGroupIdAndIdHiddenFromUserId(groupId, viewerId)
            .map { it.id.ownerId }
            .toSet()
    }

    @Transactional
    override fun deleteAllForUser(groupId: UUID, userId: UUID) {
        this.repository.deleteByIdGroupIdAndIdOwnerId(groupId, userId)
        this.repository.deleteByIdGroupIdAndIdHiddenFromUserId(groupId, userId)
    }
}
