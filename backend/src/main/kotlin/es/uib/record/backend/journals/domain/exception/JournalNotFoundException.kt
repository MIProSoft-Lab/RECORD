package es.uib.record.backend.journals.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import java.util.UUID

class JournalNotFoundException(journalId: UUID) :
    DomainException(
        message = "Journal not found",
        code = "errors.journal_not_found",
        type = ErrorType.NOT_FOUND,
        params = mapOf("journalId" to journalId),
    )
