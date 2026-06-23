package es.uib.record.backend.publications.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import java.util.UUID

class PublicationNotFoundException(id: UUID) :
    DomainException(
        message = "Publication with id $id not found",
        code = "PUBLICATION_NOT_FOUND",
        type = ErrorType.NOT_FOUND,
        params = mapOf("id" to id.toString()),
    )
