package es.uib.record.backend.groups.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import java.util.UUID

class UserAlreadyInvitedException(userId: UUID, groupId: UUID) :
    DomainException(
        message = "User with id $userId is already invited to the group with id $groupId",
        code = "USER_ALREADY_INVITED",
        type = ErrorType.CONFLICT,
        params = mapOf("userId" to userId.toString(), "groupId" to groupId.toString()),
    ) {}
