package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.domain.exception.JournalNotFoundException
import es.uib.record.backend.journals.domain.model.JournalDetail
import es.uib.record.backend.journals.domain.repository.JournalRepository
import java.util.UUID
import org.springframework.stereotype.Component

/** Detalle de una revista; lanza [JournalNotFoundException] si no existe. */
@Component
class GetJournalDetailUseCase(private val journalRepository: JournalRepository) {
    fun execute(journalId: UUID): JournalDetail =
        this.journalRepository.findDetailById(journalId)
            ?: throw JournalNotFoundException(journalId)
}
