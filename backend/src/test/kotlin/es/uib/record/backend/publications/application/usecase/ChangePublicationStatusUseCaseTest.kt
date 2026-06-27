package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.journals.open.JournalRefDto
import es.uib.record.backend.publications.domain.exception.InvalidPublicationStatusTransitionException
import es.uib.record.backend.publications.domain.exception.PublicationEditForbiddenException
import es.uib.record.backend.publications.domain.exception.PublicationNotFoundException
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
class ChangePublicationStatusUseCaseTest {

    companion object {
        private val PUBLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000000")
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val JOURNAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005")
        private val OUTSIDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000006")
        private val CREATED_AT = Instant.parse("2024-01-01T00:00:00Z")
        private const val EMAIL = "user@test.com"
    }

    @Mock private lateinit var publicationRepository: PublicationRepository

    @Mock private lateinit var userFacade: UserFacade

    @Mock private lateinit var journalFacade: JournalFacade

    @InjectMocks private lateinit var changePublicationStatusUseCase: ChangePublicationStatusUseCase

    @Test
    fun `should let the creator change to a valid next status`() {
        // Given: a SUBMITTED publication can transition to UNDER_REVIEW.
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(existingPublication(createdBy = USER_ID, status = PublicationStatus.SUBMITTED))
        given(userFacade.getUsersByIds(listOf(USER_ID))).willReturn(listOf(userOpenDto(USER_ID)))
        givenSaveReturnsArgument()
        givenJournalLookup("Nature")

        // When
        val result =
            changePublicationStatusUseCase.execute(
                PUBLICATION_ID,
                PublicationStatus.UNDER_REVIEW,
                EMAIL,
            )

        // Then
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        assertEquals(PublicationStatus.UNDER_REVIEW, captor.firstValue.status)
        assertEquals(PublicationStatus.UNDER_REVIEW, result.status)
    }

    @Test
    fun `should let an associated internal author change the status`() {
        // Given: the editor is a co-author, not the creator.
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(
                existingPublication(
                    createdBy = OWNER_ID,
                    status = PublicationStatus.SUBMITTED,
                    authors =
                        listOf(
                            PublicationAuthor.InternalAuthor(OWNER_ID),
                            PublicationAuthor.InternalAuthor(USER_ID),
                        ),
                )
            )
        given(userFacade.getUsersByIds(listOf(OWNER_ID, USER_ID))).willReturn(emptyList())
        givenSaveReturnsArgument()
        givenJournalLookup("Nature")

        // When
        changePublicationStatusUseCase.execute(PUBLICATION_ID, PublicationStatus.REJECTED, EMAIL)

        // Then
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        assertEquals(PublicationStatus.REJECTED, captor.firstValue.status)
    }

    @Test
    fun `should throw InvalidPublicationStatusTransitionException for a forbidden transition`() {
        // Given: PLANNED cannot jump straight to PUBLISHED.
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(existingPublication(createdBy = USER_ID, status = PublicationStatus.PLANNED))

        // When / Then
        assertThrows<InvalidPublicationStatusTransitionException> {
            changePublicationStatusUseCase.execute(
                PUBLICATION_ID,
                PublicationStatus.PUBLISHED,
                EMAIL,
            )
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
                    status = PublicationStatus.SUBMITTED,
                    authors = listOf(PublicationAuthor.InternalAuthor(OWNER_ID)),
                )
            )

        // When / Then
        assertThrows<PublicationEditForbiddenException> {
            changePublicationStatusUseCase.execute(
                PUBLICATION_ID,
                PublicationStatus.UNDER_REVIEW,
                EMAIL,
            )
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
            changePublicationStatusUseCase.execute(
                PUBLICATION_ID,
                PublicationStatus.UNDER_REVIEW,
                EMAIL,
            )
        }
        verify(publicationRepository, never()).save(any())
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
            )
        }
    }

    private fun givenJournalLookup(name: String?) {
        val result =
            if (name == null) emptyMap()
            else mapOf(JOURNAL_ID to JournalRefDto(JOURNAL_ID, name, null))
        given(journalFacade.getJournalsByIds(setOf(JOURNAL_ID))).willReturn(result)
    }

    private fun existingPublication(
        createdBy: UUID = USER_ID,
        status: PublicationStatus = PublicationStatus.SUBMITTED,
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
