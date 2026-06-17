package es.uib.record.backend.users.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType

class UserDeactivatedException :
    DomainException(
        message = "User account is deactivated",
        code = "USER_DEACTIVATED",
        type = ErrorType.FORBIDDEN,
    )
