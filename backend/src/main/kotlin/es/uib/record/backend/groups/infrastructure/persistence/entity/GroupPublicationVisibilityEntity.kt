package es.uib.record.backend.groups.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable
import java.util.UUID

/**
 * Excepción de visibilidad: el dueño [GroupPublicationVisibilityId.ownerId] oculta sus
 * publicaciones al miembro [GroupPublicationVisibilityId.hiddenFromUserId] dentro del grupo.
 */
@Entity
@Table(name = "group_publication_visibility")
class GroupPublicationVisibilityEntity(@EmbeddedId var id: GroupPublicationVisibilityId)

@Embeddable
data class GroupPublicationVisibilityId(
    @Column(name = "group_id", nullable = false) var groupId: UUID,
    @Column(name = "owner_id", nullable = false) var ownerId: UUID,
    @Column(name = "hidden_from_user_id", nullable = false) var hiddenFromUserId: UUID,
) : Serializable
