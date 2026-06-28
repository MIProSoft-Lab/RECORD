package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.journals.open.JournalRefDto
import es.uib.record.backend.publications.domain.exception.InvalidPublicationStatusTransitionException
import es.uib.record.backend.publications.domain.exception.JournalNotFoundForPublicationException
import es.uib.record.backend.publications.domain.exception.PublicationEditForbiddenException
import es.uib.record.backend.publications.domain.exception.PublicationNotFoundException
import es.uib.record.backend.publications.domain.exception.SameJournalResubmitException
import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.PublicationAuthor
import es.uib.record.backend.publications.domain.model.PublicationStatus
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.users.open.UserFacade
import es.uib.record.backend.users.open.UserOpenDto
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class ResubmitPublicationUseCaseTest {

    companion object {
        private val PUBLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000000")
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val JOURNAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val NEW_JOURNAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000004")
        private val GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005")
        private val OUTSIDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000006")
        private val CREATED_AT = Instant.parse("2024-01-01T00:00:00Z")
        private const val EMAIL = "user@test.com"
    }

    @Mock private lateinit var publicationRepository: PublicationRepository

    @Mock private lateinit var userFacade: UserFacade

    @Mock private lateinit var journalFacade: JournalFacade

    @InjectMocks private lateinit var resubmitPublicationUseCase: ResubmitPublicationUseCase

    @Test
    fun `should let the creator resubmit a rejected publication to another journal`() {
        // Given: a REJECTED publication is resubmitted to a different journal.
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(existingPublication(createdBy = USER_ID, status = PublicationStatus.REJECTED))
        given(journalFacade.existsById(NEW_JOURNAL_ID)).willReturn(true)
        given(userFacade.getUsersByIds(listOf(USER_ID))).willReturn(listOf(userOpenDto(USER_ID)))
        givenSaveReturnsArgument()
        givenJournalLookup(NEW_JOURNAL_ID, "Science")

        // When
        val result = resubmitPublicationUseCase.execute(PUBLICATION_ID, NEW_JOURNAL_ID, EMAIL)

        // Then
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        assertEquals(PublicationStatus.SUBMITTED, captor.firstValue.status)
        assertEquals(NEW_JOURNAL_ID, captor.firstValue.journalId)
        assertEquals(PublicationStatus.SUBMITTED, result.status)
        assertEquals(NEW_JOURNAL_ID, result.journalId)
    }

    @Test
    fun `should let an associated internal author resubmit the publication`() {
        // Given: the editor is a co-author, not the creator.
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(
                existingPublication(
                    createdBy = OWNER_ID,
                    status = PublicationStatus.REJECTED,
                    authors =
                        listOf(
                            PublicationAuthor.InternalAuthor(OWNER_ID),
                            PublicationAuthor.InternalAuthor(USER_ID),
                        ),
                )
            )
        given(journalFacade.existsById(NEW_JOURNAL_ID)).willReturn(true)
        given(userFacade.getUsersByIds(listOf(OWNER_ID, USER_ID))).willReturn(emptyList())
        givenSaveReturnsArgument()
        givenJournalLookup(NEW_JOURNAL_ID, "Science")

        // When
        resubmitPublicationUseCase.execute(PUBLICATION_ID, NEW_JOURNAL_ID, EMAIL)

        // Then
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        assertEquals(PublicationStatus.SUBMITTED, captor.firstValue.status)
        assertEquals(NEW_JOURNAL_ID, captor.firstValue.journalId)
    }

    @Test
    fun `should throw InvalidPublicationStatusTransitionException when the publication is not rejected`() {
        // Given: only REJECTED publications can be resubmitted.
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(existingPublication(createdBy = USER_ID, status = PublicationStatus.SUBMITTED))
        given(journalFacade.existsById(NEW_JOURNAL_ID)).willReturn(true)

        // When / Then
        assertThrows<InvalidPublicationStatusTransitionException> {
            resubmitPublicationUseCase.execute(PUBLICATION_ID, NEW_JOURNAL_ID, EMAIL)
        }
        verify(publicationRepository, never()).save(any())
    }

    @Test
    fun `should throw SameJournalResubmitException when resubmitting to the same journal`() {
        // Given: the target journal equals the current one.
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(existingPublication(createdBy = USER_ID, status = PublicationStatus.REJECTED))
        given(journalFacade.existsById(JOURNAL_ID)).willReturn(true)

        // When / Then
        assertThrows<SameJournalResubmitException> {
            resubmitPublicationUseCase.execute(PUBLICATION_ID, JOURNAL_ID, EMAIL)
        }
        verify(publicationRepository, never()).save(any())
    }

    @Test
    fun `should throw JournalNotFoundForPublicationException when the target journal does not exist`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(existingPublication(createdBy = USER_ID, status = PublicationStatus.REJECTED))
        given(journalFacade.existsById(NEW_JOURNAL_ID)).willReturn(false)

        // When / Then
        assertThrows<JournalNotFoundForPublicationException> {
            resubmitPublicationUseCase.execute(PUBLICATION_ID, NEW_JOURNAL_ID, EMAIL)
        }
        verify(publicationRepository, never()).save(any())
    }

    @Test
    fun `should throw PublicationEditForbiddenException when the user is neither creator nor author`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(OUTSIDER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(
                existingPublication(
                    createdBy = OWNER_ID,
                    status = PublicationStatus.REJECTED,
                    authors = listOf(PublicationAuthor.InternalAuthor(OWNER_ID)),
                )
            )

        // When / Then
        assertThrows<PublicationEditForbiddenException> {
            resubmitPublicationUseCase.execute(PUBLICATION_ID, NEW_JOURNAL_ID, EMAIL)
        }
        verify(publicationRepository, never()).save(any())
    }

    @Test
    fun `should throw PublicationNotFoundException when the publication does not exist`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID)).willReturn(null)

        // When / Then
        assertThrows<PublicationNotFoundException> {
            resubmitPublicationUseCase.execute(PUBLICATION_ID, NEW_JOURNAL_ID, EMAIL)
        }
        verify(publicationRepository, never()).save(any())
    }

    @Test
    fun `should record the previous journal and the comment in the status history on resubmit`() {
        // Given: a REJECTED publication on JOURNAL_ID (its history starts with that entry).
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(existingPublication(createdBy = USER_ID, status = PublicationStatus.REJECTED))
        given(journalFacade.existsById(NEW_JOURNAL_ID)).willReturn(true)
        given(userFacade.getUsersByIds(listOf(USER_ID))).willReturn(listOf(userOpenDto(USER_ID)))
        givenSaveReturnsArgument()
        // Both the previous and the target journals are resolvable by name.
        given(journalFacade.getJournalsByIds(setOf(JOURNAL_ID, NEW_JOURNAL_ID)))
            .willReturn(
                mapOf(
                    JOURNAL_ID to JournalRefDto(JOURNAL_ID, "Nature", null),
                    NEW_JOURNAL_ID to JournalRefDto(NEW_JOURNAL_ID, "Science", null),
                )
            )

        // When
        val result =
            resubmitPublicationUseCase.execute(
                PUBLICATION_ID,
                NEW_JOURNAL_ID,
                EMAIL,
                "Trying a better fit",
            )

        // Then: the history keeps the previous journal and appends the resubmission.
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        val history = captor.firstValue.statusHistory
        assertEquals(2, history.size)
        assertEquals(PublicationStatus.REJECTED, history.first().status)
        assertEquals(JOURNAL_ID, history.first().journalId)
        val last = history.last()
        assertEquals(PublicationStatus.SUBMITTED, last.status)
        assertEquals(NEW_JOURNAL_ID, last.journalId)
        assertEquals("Trying a better fit", last.comment)
        // The returned detail exposes the previous journal name for the rejected entry.
        assertEquals("Nature", result.statusHistory.first().journalName)
        assertEquals("Science", result.statusHistory.last().journalName)
    }

    /** Simula la persistencia asignando id a cada autor, preservando el resto del agregado. */
    private fun givenSaveReturnsArgument() {
        given(publicationRepository.save(any())).willAnswer {
            val p = it.getArgument<Publication>(0)
            Publication(
                id = p.id,
                title = p.title,
                abstractText = p.abstractText,
                doi = p.doi,
                journalId = p.journalId,
                groupId = p.groupId,
                status = p.status,
                createdBy = p.createdBy,
                createdAt = p.createdAt,
                authors =
                    p.authors.map { author ->
                        when (author) {
                            is PublicationAuthor.InternalAuthor -> author.copy(id = UUID.randomUUID())
                            is PublicationAuthor.ExternalAuthor -> author.copy(id = UUID.randomUUID())
                        }
                    },
                statusHistory = p.statusHistory,
            )
        }
    }

    // El reenvío resuelve los nombres del journal actual y de todos los del historial
    // (incluido el anterior, JOURNAL_ID), de ahí que se stubee el conjunto completo.
    private fun givenJournalLookup(journalId: UUID, name: String) {
        given(journalFacade.getJournalsByIds(setOf(JOURNAL_ID, journalId)))
            .willReturn(
                mapOf(
                    JOURNAL_ID to JournalRefDto(JOURNAL_ID, "Nature", null),
                    journalId to JournalRefDto(journalId, name, null),
                )
            )
    }

    private fun existingPublication(
        createdBy: UUID = USER_ID,
        status: PublicationStatus = PublicationStatus.REJECTED,
        authors: List<PublicationAuthor> = listOf(PublicationAuthor.InternalAuthor(createdBy)),
    ) =
        Publication(
            id = PUBLICATION_ID,
            title = "Old title",
            abstractText = "Old abstract",
            doi = null,
            journalId = JOURNAL_ID,
            groupId = GROUP_ID,
            status = status,
            createdBy = createdBy,
            createdAt = CREATED_AT,
            authors = authors,
        )

    private fun userOpenDto(userId: UUID) =
        UserOpenDto(
            userId = userId,
            firstName = "First-$userId",
            lastName = "Last-$userId",
            email = "$userId@test.com",
            profileImageUrl = "https://example.com/$userId.png",
        )
}
