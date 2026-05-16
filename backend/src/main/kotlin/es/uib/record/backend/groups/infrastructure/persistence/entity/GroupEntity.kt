package es.uib.record.backend.groups.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.CollectionTable
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "groups")
class GroupEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, unique = true)
    var name: String,

    var description: String? = null,

    @Column(nullable = false, name = "created_by")
    var createdBy: UUID,

    @Column(nullable = false, name = "created_at", updatable = false)
    var createdAt: Instant = Instant.now(),

    @ElementCollection
    @CollectionTable(
        name = "group_members",
        joinColumns = [JoinColumn(name = "group_id")]
    )
    var members: MutableSet<GroupMemberEntity> = mutableSetOf()
)