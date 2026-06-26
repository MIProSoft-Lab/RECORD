package es.uib.record.backend.publications.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import java.util.UUID

class AuthorUserNotFoundException(userId: UUID) :
    DomainException(
        message = "Author user $userId does not exist",
        code = "AUTHOR_USER_NOT_FOUND",
        type = ErrorType.BAD_REQUEST,
        params = mapOf("userId" to userId.toString()),
    )
