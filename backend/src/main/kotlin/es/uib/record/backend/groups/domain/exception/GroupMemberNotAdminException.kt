package es.uib.record.backend.groups.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import java.util.UUID

class GroupMemberNotAdminException(userId: UUID, groupId: UUID) :
    DomainException(
        message = "Group member with id $userId is not admin of the group with id $groupId",
        code = "GROUP_MEMBER_NOT_ADMIN",
        type = ErrorType.FORBIDDEN,
        params = mapOf("userId" to userId.toString(), "groupId" to groupId.toString()),
    ) {}
