package es.uib.record.backend.groups.infrastructure.persistence.repository

import es.uib.record.backend.groups.infrastructure.persistence.entity.InvitationEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataJpaInvitationRepository : JpaRepository<InvitationEntity, UUID> {
    fun findByInviteeUserId(inviteeUserId: UUID): List<InvitationEntity>
}
