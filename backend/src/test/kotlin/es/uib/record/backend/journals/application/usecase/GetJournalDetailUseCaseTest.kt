package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.domain.exception.JournalNotFoundException
import es.uib.record.backend.journals.domain.model.Journal
import es.uib.record.backend.journals.domain.model.JournalDetail
import es.uib.record.backend.journals.domain.repository.JournalRepository
import es.uib.record.backend.journals.domain.repository.UserJournalInterestRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given

@ExtendWith(MockitoExtension::class)
class GetJournalDetailUseCaseTest {

    companion object {
        private const val EMAIL = "user@uib.es"
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1")
        private val JOURNAL_ID = UUID.fromString("00000000-0000-0000-0000-0000000000d1")
    }

    @Mock private lateinit var journalRepository: JournalRepository
    @Mock private lateinit var userJournalInterestRepository: UserJournalInterestRepository
    @Mock private lateinit var userFacade: UserFacade

    private fun useCase() =
        GetJournalDetailUseCase(journalRepository, userJournalInterestRepository, userFacade)

    private fun sampleDetail() =
        JournalDetail(
            journal = Journal(id = JOURNAL_ID, clarivateId = "C1", name = "Nature"),
            metrics = emptyList(),
            categoryQuartiles = emptyList(),
        )

    @Test
    fun `returns the detail flagged as interest when the user marked it`() {
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(journalRepository.findDetailById(JOURNAL_ID)).willReturn(sampleDetail())
        given(userJournalInterestRepository.exists(USER_ID, JOURNAL_ID)).willReturn(true)

        assertTrue(useCase().execute(EMAIL, JOURNAL_ID).isInterest)
    }

    @Test
    fun `returns the detail not flagged when the user did not mark it`() {
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(journalRepository.findDetailById(JOURNAL_ID)).willReturn(sampleDetail())
        given(userJournalInterestRepository.exists(USER_ID, JOURNAL_ID)).willReturn(false)

        assertFalse(useCase().execute(EMAIL, JOURNAL_ID).isInterest)
    }

    @Test
    fun `throws not found when the journal does not exist`() {
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(journalRepository.findDetailById(JOURNAL_ID)).willReturn(null)

        assertThrows<JournalNotFoundException> { useCase().execute(EMAIL, JOURNAL_ID) }
    }
}
