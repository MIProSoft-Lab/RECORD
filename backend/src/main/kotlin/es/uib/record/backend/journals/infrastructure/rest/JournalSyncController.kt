package es.uib.record.backend.journals.infrastructure.rest

import es.uib.record.backend.api.JournalsApi
import es.uib.record.backend.journals.application.usecase.TriggerJournalSyncUseCase
import es.uib.record.backend.journals.infrastructure.mapper.toResponse
import es.uib.record.backend.model.JournalSyncStatusResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class JournalSyncController(private val triggerJournalSyncUseCase: TriggerJournalSyncUseCase) :
    JournalsApi {

    override fun triggerJournalSync(
        xSyncToken: String?
    ): ResponseEntity<JournalSyncStatusResponse> {
        val state = this.triggerJournalSyncUseCase.execute(xSyncToken)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(state.toResponse())
    }
}
