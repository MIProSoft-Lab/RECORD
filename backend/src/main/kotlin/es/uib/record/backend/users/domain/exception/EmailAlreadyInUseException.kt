package es.uib.record.backend.users.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType

class EmailAlreadyInUseException(email: String) : DomainException(
    message = "Email $email is already in use.",
    code = "EMAIL_ALREADY_IN_USE",
    type = ErrorType.CONFLICT,
    params = mapOf("email" to email)
)