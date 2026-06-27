package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.journals.open.JournalFacade
import es.uib.record.backend.journals.open.JournalRefDto
import es.uib.record.backend.publications.application.usecase.dto.PublicationAuthorInputDto
import es.uib.record.backend.publications.application.usecase.dto.UpdatePublicationRequestDto
import es.uib.record.backend.publications.domain.exception.AuthorUserNotFoundException
import es.uib.record.backend.publications.domain.exception.DoiNotAllowedException
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
class UpdatePublicationUseCaseTest {

    companion object {
        private val PUBLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000000")
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val JOURNAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000003")
        private val COAUTHOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000004")
        private val OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000005")
        private val OUTSIDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000006")
        private val CREATED_AT = Instant.parse("2024-01-01T00:00:00Z")
        private const val EMAIL = "user@test.com"
        private const val NEW_TITLE = "Updated title"
    }

    @Mock private lateinit var publicationRepository: PublicationRepository

    @Mock private lateinit var userFacade: UserFacade

    @Mock private lateinit var journalFacade: JournalFacade

    @InjectMocks private lateinit var updatePublicationUseCase: UpdatePublicationUseCase

    @Test
    fun `should let the creator update the title and abstract`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(existingPublication(createdBy = USER_ID))
        givenSaveReturnsArgument()
        givenJournalLookup("Nature")
        val dto = updateDto(title = NEW_TITLE, abstractText = "Updated abstract")

        // When
        val result = updatePublicationUseCase.execute(PUBLICATION_ID, dto, EMAIL)

        // Then
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        val saved = captor.firstValue
        assertEquals(NEW_TITLE, saved.title)
        assertEquals("Updated abstract", saved.abstractText)
        assertEquals(NEW_TITLE, result.title)
    }

    @Test
    fun `should let an associated internal author update the publication`() {
        // Given: the editor is a co-author, not the creator.
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(
                existingPublication(
                    createdBy = OWNER_ID,
                    authors =
                        listOf(
                            PublicationAuthor.InternalAuthor(OWNER_ID),
                            PublicationAuthor.InternalAuthor(USER_ID),
                        ),
                )
            )
        givenSaveReturnsArgument()
        givenJournalLookup("Nature")

        // When
        updatePublicationUseCase.execute(PUBLICATION_ID, updateDto(), EMAIL)

        // Then: the creator is preserved as author.
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        val saved = captor.firstValue
        assertEquals(OWNER_ID, saved.createdBy)
        assertEquals(listOf(PublicationAuthor.InternalAuthor(OWNER_ID)), saved.authors)
    }

    @Test
    fun `should throw PublicationEditForbiddenException when the user is neither creator nor author`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(OUTSIDER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(
                existingPublication(
                    createdBy = OWNER_ID,
                    authors = listOf(PublicationAuthor.InternalAuthor(OWNER_ID)),
                )
            )

        // When / Then
        assertThrows<PublicationEditForbiddenException> {
            updatePublicationUseCase.execute(PUBLICATION_ID, updateDto(), EMAIL)
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
            updatePublicationUseCase.execute(PUBLICATION_ID, updateDto(), EMAIL)
        }
        verify(publicationRepository, never()).save(any())
    }

    @Test
    fun `should not change the journal, group, status, creator nor creation date`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(existingPublication(createdBy = USER_ID, status = PublicationStatus.SUBMITTED))
        givenSaveReturnsArgument()
        givenJournalLookup("Nature")

        // When
        updatePublicationUseCase.execute(PUBLICATION_ID, updateDto(title = NEW_TITLE), EMAIL)

        // Then
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        val saved = captor.firstValue
        assertEquals(JOURNAL_ID, saved.journalId)
        assertEquals(GROUP_ID, saved.groupId)
        assertEquals(PublicationStatus.SUBMITTED, saved.status)
        assertEquals(USER_ID, saved.createdBy)
        assertEquals(CREATED_AT, saved.createdAt)
    }

    @Test
    fun `should update the DOI when the publication is published`() {
        // Given: a published publication can set or change its DOI when editing.
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(
                existingPublication(
                    createdBy = USER_ID,
                    status = PublicationStatus.PUBLISHED,
                    doi = "10.1000/old",
                )
            )
        givenSaveReturnsArgument()
        givenJournalLookup("Nature")

        // When
        updatePublicationUseCase.execute(PUBLICATION_ID, updateDto(doi = "10.1000/new"), EMAIL)

        // Then
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        assertEquals("10.1000/new", captor.firstValue.doi)
        assertEquals(PublicationStatus.PUBLISHED, captor.firstValue.status)
    }

    @Test
    fun `should reject a DOI when the publication is not published`() {
        // Given: a non-published publication cannot have a DOI.
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(existingPublication(createdBy = USER_ID, status = PublicationStatus.SUBMITTED))

        // When / Then
        assertThrows<DoiNotAllowedException> {
            updatePublicationUseCase.execute(PUBLICATION_ID, updateDto(doi = "10.1000/xyz123"), EMAIL)
        }
        verify(publicationRepository, never()).save(any())
    }

    @Test
    fun `should replace the authors respecting order and keeping the creator first`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(existingPublication(createdBy = USER_ID))
        given(userFacade.getUsersByIds(listOf(COAUTHOR_ID)))
            .willReturn(listOf(userOpenDto(COAUTHOR_ID)))
        givenSaveReturnsArgument()
        givenJournalLookup("Nature")
        val dto =
            updateDto(authors = listOf(externalInput("Jane", "Doe"), internalInput(COAUTHOR_ID)))

        // When
        updatePublicationUseCase.execute(PUBLICATION_ID, dto, EMAIL)

        // Then
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        assertEquals(
            listOf(
                PublicationAuthor.InternalAuthor(USER_ID),
                PublicationAuthor.ExternalAuthor("Jane", "Doe"),
                PublicationAuthor.InternalAuthor(COAUTHOR_ID),
            ),
            captor.firstValue.authors,
        )
    }

    @Test
    fun `should keep the creator as author even if removed from the submitted list`() {
        // Given: the new author list does not include the creator.
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(existingPublication(createdBy = USER_ID))
        given(userFacade.getUsersByIds(listOf(COAUTHOR_ID)))
            .willReturn(listOf(userOpenDto(COAUTHOR_ID)))
        givenSaveReturnsArgument()
        givenJournalLookup("Nature")
        val dto = updateDto(authors = listOf(internalInput(COAUTHOR_ID)))

        // When
        updatePublicationUseCase.execute(PUBLICATION_ID, dto, EMAIL)

        // Then
        val captor = argumentCaptor<Publication>()
        verify(publicationRepository).save(captor.capture())
        assertEquals(
            listOf(
                PublicationAuthor.InternalAuthor(USER_ID),
                PublicationAuthor.InternalAuthor(COAUTHOR_ID),
            ),
            captor.firstValue.authors,
        )
    }

    @Test
    fun `should throw AuthorUserNotFoundException when an internal co-author does not exist`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(existingPublication(createdBy = USER_ID))
        given(userFacade.getUsersByIds(listOf(COAUTHOR_ID))).willReturn(emptyList())
        val dto = updateDto(authors = listOf(internalInput(COAUTHOR_ID)))

        // When / Then
        assertThrows<AuthorUserNotFoundException> {
            updatePublicationUseCase.execute(PUBLICATION_ID, dto, EMAIL)
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
        doi: String? = null,
        authors: List<PublicationAuthor> = listOf(PublicationAuthor.InternalAuthor(createdBy)),
    ) =
        Publication(
            id = PUBLICATION_ID,
            title = "Old title",
            abstractText = "Old abstract",
            doi = doi,
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

    private fun internalInput(userId: UUID) =
        PublicationAuthorInputDto(userId = userId, firstName = null, lastName = null)

    private fun externalInput(firstName: String, lastName: String) =
        PublicationAuthorInputDto(userId = null, firstName = firstName, lastName = lastName)

    private fun updateDto(
        title: String = NEW_TITLE,
        abstractText: String? = "Updated abstract",
        doi: String? = null,
        authors: List<PublicationAuthorInputDto> = emptyList(),
    ) =
        UpdatePublicationRequestDto(
            title = title,
            abstractText = abstractText,
            doi = doi,
            authors = authors,
        )
}
