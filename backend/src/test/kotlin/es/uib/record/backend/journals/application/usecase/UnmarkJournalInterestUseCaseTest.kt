package es.uib.record.backend.journals.application.usecase

import es.uib.record.backend.journals.domain.repository.UserJournalInterestRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class UnmarkJournalInterestUseCaseTest {

    companion object {
        private const val EMAIL = "user@uib.es"
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1")
        private val JOURNAL_ID = UUID.fromString("00000000-0000-0000-0000-0000000000b1")
    }

    @Mock private lateinit var userJournalInterestRepository: UserJournalInterestRepository
    @Mock private lateinit var userFacade: UserFacade

    private fun useCase() = UnmarkJournalInterestUseCase(userJournalInterestRepository, userFacade)

    @Test
    fun `removes the interest for the resolved user`() {
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)

        useCase().execute(EMAIL, JOURNAL_ID)

        verify(userJournalInterestRepository).remove(USER_ID, JOURNAL_ID)
    }
}
