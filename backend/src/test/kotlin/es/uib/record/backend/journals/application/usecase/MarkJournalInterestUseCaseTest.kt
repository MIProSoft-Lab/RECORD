package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.domain.exception.JournalNotFoundException
import es.uib.record.backend.journals.domain.repository.JournalRepository
import es.uib.record.backend.journals.domain.repository.UserJournalInterestRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class MarkJournalInterestUseCaseTest {

    companion object {
        private const val EMAIL = "user@uib.es"
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1")
        private val JOURNAL_ID = UUID.fromString("00000000-0000-0000-0000-0000000000b1")
    }

    @Mock private lateinit var journalRepository: JournalRepository
    @Mock private lateinit var userJournalInterestRepository: UserJournalInterestRepository
    @Mock private lateinit var userFacade: UserFacade

    private fun useCase() =
        MarkJournalInterestUseCase(journalRepository, userJournalInterestRepository, userFacade)

    @Test
    fun `adds the interest when the journal exists`() {
        given(journalRepository.existsById(JOURNAL_ID)).willReturn(true)
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)

        useCase().execute(EMAIL, JOURNAL_ID)

        verify(userJournalInterestRepository).add(USER_ID, JOURNAL_ID)
    }

    @Test
    fun `throws not found and does not add when the journal does not exist`() {
        given(journalRepository.existsById(JOURNAL_ID)).willReturn(false)

        assertThrows<JournalNotFoundException> { useCase().execute(EMAIL, JOURNAL_ID) }

        verify(userJournalInterestRepository, never()).add(any(), any())
    }
}
