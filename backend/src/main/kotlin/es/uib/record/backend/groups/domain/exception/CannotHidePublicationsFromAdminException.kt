package es.uib.record.backend.groups.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import java.util.UUID

class CannotHidePublicationsFromAdminException(adminId: UUID, groupId: UUID) :
    DomainException(
        message =
            "Cannot hide publications from admin with id $adminId in the group with id $groupId",
        code = "CANNOT_HIDE_PUBLICATIONS_FROM_ADMIN",
        type = ErrorType.FORBIDDEN,
        params = mapOf("adminId" to adminId.toString(), "groupId" to groupId.toString()),
    )
