package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.journals.open.JournalRefDto
import es.uib.record.backend.publications.domain.exception.PublicationNotFoundException
import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.PublicationStatus
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given

@ExtendWith(MockitoExtension::class)
class GetPublicationDetailUseCaseTest {

    companion object {
        private val PUBLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val JOURNAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
    }

    @Mock private lateinit var publicationRepository: PublicationRepository

    @Mock private lateinit var journalFacade: JournalFacade

    @InjectMocks private lateinit var getPublicationDetailUseCase: GetPublicationDetailUseCase

    @Test
    fun `should return the publication detail with the journal name when it exists`() {
        // Given
        val publication =
            Publication(
                id = PUBLICATION_ID,
                title = "First",
                journalId = JOURNAL_ID,
                groupId = UUID.randomUUID(),
                status = PublicationStatus.PLANNED,
                createdBy = UUID.randomUUID(),
            )
        given(publicationRepository.findById(PUBLICATION_ID)).willReturn(publication)
        given(journalFacade.getJournalsByIds(setOf(JOURNAL_ID)))
            .willReturn(mapOf(JOURNAL_ID to JournalRefDto(JOURNAL_ID, "Nature", null)))

        // When
        val result = getPublicationDetailUseCase.execute(PUBLICATION_ID)

        // Then
        assertEquals(PUBLICATION_ID, result.id)
        assertEquals("First", result.title)
        assertEquals("Nature", result.journalName)
    }

    @Test
    fun `should throw PublicationNotFoundException when the publication does not exist`() {
        // Given
        given(publicationRepository.findById(PUBLICATION_ID)).willReturn(null)

        // When / Then
        assertThrows<PublicationNotFoundException> {
            getPublicationDetailUseCase.execute(PUBLICATION_ID)
        }
    }
}
