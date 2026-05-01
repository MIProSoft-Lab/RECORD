package es.uib.record.backend.auth.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType

class InvalidRefreshTokenException : DomainException(
    message = "Refresh token is invalid",
    code = "",
    type = ErrorType.UNAUTHORIZED,
    params = mapOf()
)