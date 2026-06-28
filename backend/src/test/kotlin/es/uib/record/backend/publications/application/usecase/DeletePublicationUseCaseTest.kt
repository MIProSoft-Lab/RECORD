package es.uib.record.backend.publications.application.usecase

import es.uib.record.backend.publications.domain.exception.PublicationDeleteForbiddenException
import es.uib.record.backend.publications.domain.exception.PublicationNotFoundException
import es.uib.record.backend.publications.domain.model.Publication
import es.uib.record.backend.publications.domain.model.PublicationAuthor
import es.uib.record.backend.publications.domain.model.PublicationStatus
import es.uib.record.backend.publications.domain.repository.PublicationRepository
import es.uib.record.backend.users.open.UserFacade
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class DeletePublicationUseCaseTest {

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

    @InjectMocks private lateinit var deletePublicationUseCase: DeletePublicationUseCase

    @Test
    fun `should let the creator delete the publication`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID))
            .willReturn(existingPublication(createdBy = USER_ID))

        // When
        deletePublicationUseCase.execute(PUBLICATION_ID, EMAIL)

        // Then
        verify(publicationRepository).delete(PUBLICATION_ID)
    }

    @Test
    fun `should let an associated internal author delete the publication`() {
        // Given: the user is a co-author, not the creator.
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

        // When
        deletePublicationUseCase.execute(PUBLICATION_ID, EMAIL)

        // Then
        verify(publicationRepository).delete(PUBLICATION_ID)
    }

    @Test
    fun `should throw PublicationDeleteForbiddenException when the user is neither creator nor author`() {
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
        assertThrows<PublicationDeleteForbiddenException> {
            deletePublicationUseCase.execute(PUBLICATION_ID, EMAIL)
        }
        verify(publicationRepository, never()).delete(any())
    }

    @Test
    fun `should throw PublicationNotFoundException when the publication does not exist`() {
        // Given
        given(userFacade.getUserIdByEmail(EMAIL)).willReturn(USER_ID)
        given(publicationRepository.findById(PUBLICATION_ID)).willReturn(null)

        // When / Then
        assertThrows<PublicationNotFoundException> {
            deletePublicationUseCase.execute(PUBLICATION_ID, EMAIL)
        }
        verify(publicationRepository, never()).delete(any())
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
}
