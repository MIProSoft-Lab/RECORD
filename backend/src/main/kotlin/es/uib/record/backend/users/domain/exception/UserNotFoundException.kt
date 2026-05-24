package es.uib.record.backend.users.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import java.util.UUID

class UserNotFoundException : DomainException {
    constructor(
        email: String
    ) : super(
        message = "User with email $email not found",
        code = "USER_NOT_FOUND",
        type = ErrorType.NOT_FOUND,
        params = mapOf("email" to email),
    )

    constructor(
        userId: UUID
    ) : super(
        message = "User with id $userId not found",
        code = "USER_NOT_FOUND",
        type = ErrorType.NOT_FOUND,
        params = mapOf("id" to userId.toString()),
    )
}
