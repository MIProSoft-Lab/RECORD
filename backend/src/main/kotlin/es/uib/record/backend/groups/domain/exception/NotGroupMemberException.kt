package es.uib.record.backend.groups.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType

class NotGroupMemberException : DomainException(
    message = "User is not a member of the group",
    code = "NOT_GROUP_MEMBER",
    type = ErrorType.FORBIDDEN
)