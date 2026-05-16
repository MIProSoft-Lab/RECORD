package es.uib.record.backend.groups.infrastructure.persistence.entity

import es.uib.record.backend.groups.domain.GroupRole
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.util.UUID

@Embeddable
class GroupMemberEntity(
    @Column(name = "user_id", nullable = false, updatable = false)
    var userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: GroupRole
)