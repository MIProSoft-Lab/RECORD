package es.uib.record.backend.publications.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType

class InvalidPublicationAuthorException :
    DomainException(
        message =
            "Each author must be either internal (userId) or external (firstName and lastName)",
        code = "INVALID_PUBLICATION_AUTHOR",
        type = ErrorType.BAD_REQUEST,
    )
