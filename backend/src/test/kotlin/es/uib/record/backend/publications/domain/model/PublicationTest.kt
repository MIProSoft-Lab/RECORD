package es.uib.record.backend.publications.domain.model

import es.uib.record.backend.publications.domain.exception.InvalidPublicationStatusTransitionException
import es.uib.record.backend.publications.domain.exception.SameJournalResubmitException
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PublicationTest {

    companion object {
        private val JOURNAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000002")
        private val NEW_JOURNAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000004")
        private val USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private val GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000003")
    }

    @Test
    fun `resubmit moves a rejected publication to submitted with the new journal`() {
        val publication = publication(status = PublicationStatus.REJECTED)

        val result = publication.resubmit(NEW_JOURNAL_ID)

        assertEquals(PublicationStatus.SUBMITTED, result.status)
        assertEquals(NEW_JOURNAL_ID, result.journalId)
    }

    @Test
    fun `resubmit throws when the publication is not rejected`() {
        val publication = publication(status = PublicationStatus.SUBMITTED)

        assertThrows<InvalidPublicationStatusTransitionException> {
            publication.resubmit(NEW_JOURNAL_ID)
        }
    }

    @Test
    fun `resubmit throws when the target journal is the same as the current one`() {
        val publication = publication(status = PublicationStatus.REJECTED)

        assertThrows<SameJournalResubmitException> { publication.resubmit(JOURNAL_ID) }
    }

    private fun publication(status: PublicationStatus) =
        Publication(
            id = UUID.randomUUID(),
            title = "Title",
            abstractText = null,
            doi = null,
            journalId = JOURNAL_ID,
            groupId = GROUP_ID,
            status = status,
            createdBy = USER_ID,
            authors = listOf(PublicationAuthor.InternalAuthor(USER_ID)),
        )
}
