package es.uib.record.backend.groups.domain.repository

import java.util.UUID

/**
 * Visibilidad del historial de publicaciones entre miembros de un grupo. Solo se almacenan las
 * excepciones ocultas: una entrada `(groupId, ownerId, viewerId)` significa que [ownerId] oculta
 * sus publicaciones a [viewerId] en ese grupo. La ausencia de entrada implica visibilidad (por
 * defecto, todos los miembros se ven entre sí).
 */
interface PublicationVisibilityRepository {
    /** Oculta las publicaciones de [ownerId] a [viewerId] en el grupo [groupId] (idempotente). */
    fun hide(groupId: UUID, ownerId: UUID, viewerId: UUID)

    /** Restaura la visibilidad de las publicaciones de [ownerId] para [viewerId] (idempotente). */
    fun unhide(groupId: UUID, ownerId: UUID, viewerId: UUID)

    /** Miembros a los que [ownerId] oculta sus publicaciones en el grupo [groupId]. */
    fun findViewersHiddenByOwner(groupId: UUID, ownerId: UUID): Set<UUID>

    /** Dueños que ocultan sus publicaciones a [viewerId] en el grupo [groupId]. */
    fun findOwnersHiddenFromViewer(groupId: UUID, viewerId: UUID): Set<UUID>

    /** Elimina todas las entradas del usuario en el grupo, tanto como dueño como destinatario. */
    fun deleteAllForUser(groupId: UUID, userId: UUID)
}
