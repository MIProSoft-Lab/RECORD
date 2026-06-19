package es.uib.record.backend.journals.infrastructure.mapper

import es.uib.record.backend.journals.domain.model.Quartile
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ClarivateMapperTest {

    @Test
    fun `parseImpactFactor parses a valid decimal string`() {
        assertEquals(BigDecimal("1.8"), parseImpactFactor("1.8"))
        assertEquals(BigDecimal("0.41"), parseImpactFactor("0.41"))
    }

    @Test
    fun `parseImpactFactor returns null for null, blank or non-numeric values`() {
        assertNull(parseImpactFactor(null))
        assertNull(parseImpactFactor(""))
        assertNull(parseImpactFactor("   "))
        assertNull(parseImpactFactor("N/A"))
        assertNull(parseImpactFactor("not-a-number"))
    }

    @Test
    fun `parseQuartile parses recognised quartile values case-insensitively`() {
        assertEquals(Quartile.Q1, parseQuartile("Q1"))
        assertEquals(Quartile.Q2, parseQuartile(" q2 "))
        assertEquals(Quartile.Q3, parseQuartile("Q3"))
        assertEquals(Quartile.Q4, parseQuartile("q4"))
    }

    @Test
    fun `parseQuartile returns null for null or unknown values`() {
        assertNull(parseQuartile(null))
        assertNull(parseQuartile(""))
        assertNull(parseQuartile("Q5"))
        assertNull(parseQuartile("foo"))
    }
}
