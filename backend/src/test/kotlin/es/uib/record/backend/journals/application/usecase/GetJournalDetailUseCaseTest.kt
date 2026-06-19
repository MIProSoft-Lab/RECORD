package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.domain.exception.JournalNotFoundException
import es.uib.record.backend.journals.domain.model.Journal
import es.uib.record.backend.journals.domain.model.JournalDetail
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given

@ExtendWith(MockitoExtension::class)
class GetJournalDetailUseCaseTest {

    companion object {
        private val JOURNAL_ID = UUID.fromString("00000000-0000-0000-0000-0000000000d1")
    }

    @Mock private lateinit var journalRepository: es.uib.record.backend.journals.domain.repository.JournalRepository

    private fun useCase() = GetJournalDetailUseCase(journalRepository)

    @Test
    fun `returns the detail when the journal exists`() {
        val detail =
            JournalDetail(
                journal = Journal(id = JOURNAL_ID, clarivateId = "C1", name = "Nature"),
                metrics = emptyList(),
                categoryQuartiles = emptyList(),
            )
        given(journalRepository.findDetailById(JOURNAL_ID)).willReturn(detail)

        assertSame(detail, useCase().execute(JOURNAL_ID))
    }

    @Test
    fun `throws not found when the journal does not exist`() {
        given(journalRepository.findDetailById(JOURNAL_ID)).willReturn(null)

        assertThrows<JournalNotFoundException> { useCase().execute(JOURNAL_ID) }
    }
}
