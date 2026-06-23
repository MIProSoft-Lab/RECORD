package es.uib.record.backend.publications.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import java.util.UUID

class JournalNotFoundForPublicationException(journalId: UUID) :
    DomainException(
        message = "Journal with id $journalId not found",
        code = "JOURNAL_NOT_FOUND",
        type = ErrorType.NOT_FOUND,
        params = mapOf("id" to journalId.toString()),
    )
