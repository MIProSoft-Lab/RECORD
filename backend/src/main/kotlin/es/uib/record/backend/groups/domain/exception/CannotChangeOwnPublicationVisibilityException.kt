package es.uib.record.backend.groups.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import java.util.UUID

class CannotChangeOwnPublicationVisibilityException(userId: UUID, groupId: UUID) :
    DomainException(
        message =
            "User with id $userId cannot change publication visibility for themselves in the group with id $groupId",
        code = "CANNOT_CHANGE_OWN_PUBLICATION_VISIBILITY",
        type = ErrorType.BAD_REQUEST,
        params = mapOf("userId" to userId.toString(), "groupId" to groupId.toString()),
    )
