package es.uib.record.backend.groups.infrastructure.persistence.repository

import es.uib.record.backend.groups.infrastructure.persistence.entity.InvitationEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpringDataJpaInvitationRepository : JpaRepository<InvitationEntity, UUID> {
    fun findByInviteeUserId(inviteeUserId: UUID): List<InvitationEntity>
}