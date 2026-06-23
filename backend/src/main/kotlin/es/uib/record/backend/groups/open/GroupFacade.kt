package es.uib.record.backend.groups.open

import java.util.UUID

/** Acceso de otros módulos a la información de grupos de investigación. */
interface GroupFacade {
    /** Indica si existe un grupo con el [groupId] dado. */
    fun existsById(groupId: UUID): Boolean

    /** Indica si el usuario [userId] es miembro del grupo [groupId]. */
    fun isMember(groupId: UUID, userId: UUID): Boolean
}
