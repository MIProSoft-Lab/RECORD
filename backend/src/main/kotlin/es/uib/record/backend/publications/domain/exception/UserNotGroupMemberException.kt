package es.uib.record.backend.publications.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import java.util.UUID

class UserNotGroupMemberException(groupId: UUID, userId: UUID) :
    DomainException(
        message = "User $userId is not a member of group $groupId",
        code = "USER_NOT_GROUP_MEMBER",
        type = ErrorType.FORBIDDEN,
        params = mapOf("groupId" to groupId.toString(), "userId" to userId.toString()),
    )
