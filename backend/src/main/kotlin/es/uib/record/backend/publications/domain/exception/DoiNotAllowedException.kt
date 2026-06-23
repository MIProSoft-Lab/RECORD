package es.uib.record.backend.publications.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType

class DoiNotAllowedException :
    DomainException(
        message = "DOI is only allowed when the publication status is PUBLISHED",
        code = "DOI_NOT_ALLOWED",
        type = ErrorType.BAD_REQUEST,
    )
