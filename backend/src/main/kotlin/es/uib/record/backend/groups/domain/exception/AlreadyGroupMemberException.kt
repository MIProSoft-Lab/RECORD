package es.uib.record.backend.groups.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import java.util.UUID

class AlreadyGroupMemberException(userId: UUID, groupId: UUID) :
    DomainException(
        message = "User with id $userId is already a member of the group with id $groupId",
        code = "ALREADY_GROUP_MEMBER",
        type = ErrorType.CONFLICT,
        params = mapOf("userId" to userId.toString(), "groupId" to groupId.toString()),
    )
