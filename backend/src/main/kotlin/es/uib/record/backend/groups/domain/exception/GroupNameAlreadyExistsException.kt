package es.uib.record.backend.groups.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType

class GroupNameAlreadyExistsException(name: String) :
    DomainException(
        message = "A group with the name $name already exists",
        code = "GROUP_NAME_ALREADY_EXISTS",
        type = ErrorType.CONFLICT,
        params = mapOf("name" to name),
    )
