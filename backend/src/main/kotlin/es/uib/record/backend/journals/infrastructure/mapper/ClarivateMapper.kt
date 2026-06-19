package es.uib.record.backend.journals.infrastructure.mapper

import es.uib.record.backend.journals.domain.model.Quartile
import java.math.BigDecimal

/** Parsea el JIF de Clarivate (p. ej. "1.8"), tolerando null, vacío o "N/A". */
fun parseImpactFactor(raw: String?): BigDecimal? {
    val value = raw?.trim() ?: return null
    if (value.isEmpty() || value.equals("N/A", ignoreCase = true)) return null
    return value.toBigDecimalOrNull()
}

/** Parsea el cuartil de Clarivate (p. ej. "Q3"), devolviendo null si no es un valor reconocido. */
fun parseQuartile(raw: String?): Quartile? {
    val value = raw?.trim()?.uppercase() ?: return null
    return Quartile.entries.firstOrNull { it.name == value }
}
