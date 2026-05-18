package es.uib.record.backend.groups.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import java.util.UUID

class GroupNotFoundException(id: UUID) : DomainException(
    message = "Group with id $id not found",
    code = "GROUP_NOT_FOUND",
    type = ErrorType.NOT_FOUND,
    params = mapOf("id" to id.toString())
)