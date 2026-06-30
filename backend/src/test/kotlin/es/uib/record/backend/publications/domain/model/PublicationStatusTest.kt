package es.uib.record.backend.publications.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PublicationStatusTest {

    @Test
    fun `isFinal is true only for terminal statuses`() {
        assertTrue(PublicationStatus.REJECTED.isFinal())
        assertTrue(PublicationStatus.PUBLISHED.isFinal())
    }

    @Test
    fun `isFinal is false for non-terminal statuses`() {
        val nonFinal =
            PublicationStatus.entries.filter {
                it != PublicationStatus.REJECTED && it != PublicationStatus.PUBLISHED
            }
        nonFinal.forEach { assertFalse(it.isFinal(), "$it should not be final") }
    }

    @Test
    fun `the set of final statuses matches REJECTED and PUBLISHED`() {
        assertEquals(
            setOf(PublicationStatus.REJECTED, PublicationStatus.PUBLISHED),
            PublicationStatus.entries.filter { it.isFinal() }.toSet(),
        )
    }
}
