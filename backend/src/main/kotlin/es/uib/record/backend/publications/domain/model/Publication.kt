package es.uib.record.backend.publications.domain.model

import es.uib.record.backend.publications.domain.exception.DoiNotAllowedException
import java.time.Instant
import java.util.UUID

class Publication(
    val id: UUID? = null,
    val title: String,
    val abstractText: String? = null,
    val doi: String? = null,
    val journalId: UUID,
    val groupId: UUID,
    val status: PublicationStatus = PublicationStatus.PLANNED,
    val createdBy: UUID,
    val createdAt: Instant = Instant.now(),
    authors: List<PublicationAuthor> = emptyList(),
) {
    private val _authors: MutableList<PublicationAuthor> = authors.toMutableList()

    val authors: List<PublicationAuthor>
        get() = _authors.toList()

    init {
        // Invariante: el DOI solo puede tener valor cuando la publicación está PUBLISHED.
        if (!doi.isNullOrBlank() && status != PublicationStatus.PUBLISHED) {
            throw DoiNotAllowedException()
        }
    }

    fun addAuthor(userId: UUID) {
        if (_authors.none { it.userId == userId }) {
            _authors.add(PublicationAuthor(userId))
        }
    }
}
