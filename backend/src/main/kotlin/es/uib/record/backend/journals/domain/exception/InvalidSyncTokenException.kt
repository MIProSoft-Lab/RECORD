package es.uib.record.backend.journals.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType

class InvalidSyncTokenException :
    DomainException(
        message = "Invalid or missing journal sync token",
        code = "errors.invalid_sync_token",
        type = ErrorType.UNAUTHORIZED,
    )
