package es.uib.record.backend.groups.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import java.util.UUID

class NotGroupMemberException(userId: UUID, groupId: UUID) :
    DomainException(
        message = "User with id $userId is not a member of the group with id $groupId",
        code = "NOT_GROUP_MEMBER",
        type = ErrorType.FORBIDDEN,
    )
