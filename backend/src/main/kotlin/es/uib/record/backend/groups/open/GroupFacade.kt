package es.uib.record.backend.groups.open

import java.util.UUID

/** Acceso de otros módulos a la información de grupos de investigación. */
interface GroupFacade {
    /** Indica si existe un grupo con el [groupId] dado. */
    fun existsById(groupId: UUID): Boolean

    /** Indica si el usuario [userId] es miembro del grupo [groupId]. */
    fun isMember(groupId: UUID, userId: UUID): Boolean

    /** Indica si el usuario [userId] es administrador del grupo [groupId]. */
    fun isAdmin(groupId: UUID, userId: UUID): Boolean

    /** Devuelve los identificadores de los miembros del grupo [groupId] (vacío si no existe). */
    fun getMemberIds(groupId: UUID): List<UUID>

    /**
     * Dueños que ocultan su historial de publicaciones a [viewerId] en el grupo [groupId]. No
     * aplica la excepción de administradores: el llamante decide si [viewerId] es admin.
     */
    fun getOwnersHiddenFromViewer(groupId: UUID, viewerId: UUID): Set<UUID>
}
