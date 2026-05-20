package es.uib.record.backend.groups.infrastructure.persistence.repository

import es.uib.record.backend.groups.infrastructure.persistence.entity.GroupEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpringDataJpaGroupRepository : JpaRepository<GroupEntity, UUID> {
    fun findByName(name: String): GroupEntity?
    fun findAllByMembersUserId(userId: UUID): List<GroupEntity>
}