package es.uib.record.backend.groups.infrastructure.persistence.repository

import es.uib.record.backend.groups.infrastructure.persistence.entity.GroupPublicationVisibilityEntity
import es.uib.record.backend.groups.infrastructure.persistence.entity.GroupPublicationVisibilityId
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataJpaGroupPublicationVisibilityRepository :
    JpaRepository<GroupPublicationVisibilityEntity, GroupPublicationVisibilityId> {

    fun findByIdGroupIdAndIdOwnerId(
        groupId: UUID,
        ownerId: UUID,
    ): List<GroupPublicationVisibilityEntity>

    fun findByIdGroupIdAndIdHiddenFromUserId(
        groupId: UUID,
        hiddenFromUserId: UUID,
    ): List<GroupPublicationVisibilityEntity>

    fun deleteByIdGroupIdAndIdOwnerId(groupId: UUID, ownerId: UUID)

    fun deleteByIdGroupIdAndIdHiddenFromUserId(groupId: UUID, hiddenFromUserId: UUID)
}
