package es.uib.record.backend.groups.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import java.util.UUID

class LastGroupAdminException(userId: UUID, groupId: UUID) :
    DomainException(
        message =
            "Cannot downgrade user with id $userId because they are the last admin of the group with id $groupId",
        code = "GROUP_LAST_ADMIN",
        type = ErrorType.CONFLICT,
        params = mapOf("userId" to userId.toString(), "groupId" to groupId.toString()),
    )
