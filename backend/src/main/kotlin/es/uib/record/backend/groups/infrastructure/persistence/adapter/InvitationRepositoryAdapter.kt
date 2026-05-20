package es.uib.record.backend.groups.infrastructure.persistence.adapter

import es.uib.record.backend.groups.domain.model.Invitation
import es.uib.record.backend.groups.domain.repository.InvitationRepository
import es.uib.record.backend.groups.infrastructure.mapper.toDomain
import es.uib.record.backend.groups.infrastructure.mapper.toEntity
import es.uib.record.backend.groups.infrastructure.persistence.entity.InvitationEntity
import es.uib.record.backend.groups.infrastructure.persistence.repository.SpringDataJpaInvitationRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class InvitationRepositoryAdapter(
    private val springDataJpaInvitationRepository: SpringDataJpaInvitationRepository
) : InvitationRepository {

    override fun save(invitation: Invitation): Invitation {
        val entity = this.springDataJpaInvitationRepository.save(invitation.toEntity())
        return entity.toDomain()
    }

    override fun delete(invitation: Invitation) {
        this.springDataJpaInvitationRepository.delete(invitation.toEntity())
    }

    override fun findByInviteeUserId(userId: UUID): List<Invitation> {
        return this.springDataJpaInvitationRepository
            .findByInviteeUserId(userId)
            .map(InvitationEntity::toDomain)
    }

    override fun findById(id: UUID): Invitation? {
        return this.springDataJpaInvitationRepository.findByIdOrNull(id)?.toDomain()
    }
}