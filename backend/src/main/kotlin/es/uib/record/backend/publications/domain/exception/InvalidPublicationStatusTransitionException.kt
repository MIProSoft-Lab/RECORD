package es.uib.record.backend.publications.domain.exception

import es.uib.record.backend.publications.domain.model.PublicationStatus
import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType

class InvalidPublicationStatusTransitionException(
    from: PublicationStatus,
    to: PublicationStatus,
) :
    DomainException(
        message = "Cannot transition publication status from $from to $to",
        code = "INVALID_PUBLICATION_STATUS_TRANSITION",
        type = ErrorType.CONFLICT,
        params =
            mapOf(
                "from" to from.name,
                "to" to to.name,
            ),
    )
