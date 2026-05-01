package es.uib.record.backend.users.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType

class UserNotFoundException(email: String) : DomainException(
    message = "User with email $email not found",
    code = "",
    type = ErrorType.NOT_FOUND,
    params = mapOf("email" to email)
)