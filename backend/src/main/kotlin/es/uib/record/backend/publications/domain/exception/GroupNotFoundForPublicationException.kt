package es.uib.record.backend.publications.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import java.util.UUID

class GroupNotFoundForPublicationException(groupId: UUID) :
    DomainException(
        message = "Group with id $groupId not found",
        code = "GROUP_NOT_FOUND",
        type = ErrorType.NOT_FOUND,
        params = mapOf("id" to groupId.toString()),
    )
