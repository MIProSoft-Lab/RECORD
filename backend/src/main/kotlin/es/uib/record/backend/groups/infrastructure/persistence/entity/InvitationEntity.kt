package es.uib.record.backend.groups.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "invitations")
class InvitationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, name = "group_id")
    var groupId: UUID,

    @Column(nullable = false, name = "invitee_user_id")
    var inviteeUserId: UUID,

    @Column(nullable = false, name = "inviter_user_id")
    var inviterUserId: UUID,

    @Column(nullable = false, name = "created_at")
    var createdAt: Instant = Instant.now()
)