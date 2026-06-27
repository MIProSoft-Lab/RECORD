package es.uib.record.backend.publications.domain.exception

import es.uib.record.backend.shared.exception.DomainException
import es.uib.record.backend.shared.exception.ErrorType
import java.util.UUID

class SameJournalResubmitException(journalId: UUID) :
    DomainException(
        message = "Cannot resubmit a publication to the same journal $journalId",
        code = "SAME_JOURNAL_RESUBMIT",
        type = ErrorType.CONFLICT,
        params = mapOf("journalId" to journalId.toString()),
    )
