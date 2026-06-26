package es.uib.record.backend.publications.application.usecase.dto

import java.util.UUID

enum class PublicationAuthorType {
    INTERNAL,
    EXTERNAL,
}

/**
 * Autor de una publicación devuelto en las respuestas. [firstName] y [lastName]
 * siempre están presentes (para mostrar); [userId], [email] y [profileImageUrl]
 * solo en autores internos. [authorId] identifica la entrada de autoría.
 */
data class PublicationAuthorDto(
    val authorId: UUID,
    val type: PublicationAuthorType,
    val userId: UUID?,
    val firstName: String,
    val lastName: String,
    val email: String?,
    val profileImageUrl: String?,
)

/** Autor de entrada al crear: interno (userId) o externo (firstName + lastName). */
data class PublicationAuthorInputDto(
    val userId: UUID?,
    val firstName: String?,
    val lastName: String?,
)
