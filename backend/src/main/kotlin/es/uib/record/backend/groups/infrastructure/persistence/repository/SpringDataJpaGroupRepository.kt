package es.uib.record.backend.groups.infrastructure.persistence.repository

import es.uib.record.backend.groups.infrastructure.persistence.entity.GroupEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataJpaGroupRepository : JpaRepository<GroupEntity, UUID> {
    fun findByName(name: String): GroupEntity?

    fun findAllByMembersUserId(userId: UUID): List<GroupEntity>
}
