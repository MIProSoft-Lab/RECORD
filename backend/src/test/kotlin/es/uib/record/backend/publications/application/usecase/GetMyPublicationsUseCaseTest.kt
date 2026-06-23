package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.journals.open.JournalRefDto
import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.PublicationStatus
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.users.open.UserFacade
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given

@ExtendWith(MockitoExtension::class)
class GetMyPublicationsUseCaseTest {

    companion object {
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val JOURNAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private const val EMAIL = "user@test.com"
    }

    @Mock private lateinit var publicationRepository: PublicationRepository

    @Mock private lateinit var userFacade: UserFacade

    @Mock private lateinit var journalFacade: JournalFacade

    @InjectMocks private lateinit var getMyPublicationsUseCase: GetMyPublicationsUseCase

    @Test
    fun `should return the publications of the resolved user enriched with the journal name`() {
        // Given
        val publication =
            Publication(
                id = UUID.randomUUID(),
                title = "First",
                journalId = JOURNAL_ID,
                groupId = UUID.randomUUID(),
                status = PublicationStatus.PLANNED,
                createdBy = USER_ID,
            )
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findAllByCreatedBy(USER_ID)).willReturn(listOf(publication))
        given(journalFacade.getJournalsByIds(setOf(JOURNAL_ID)))
            .willReturn(mapOf(JOURNAL_ID to JournalRefDto(JOURNAL_ID, "Nature", null)))

        // When
        val result = getMyPublicationsUseCase.execute(EMAIL)

        // Then
        assertEquals(1, result.size)
        assertEquals(publication.id, result[0].id)
        assertEquals("First", result[0].title)
        assertEquals("Nature", result[0].journalName)
    }
}
