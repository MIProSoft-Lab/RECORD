package es.uib.record.backend.publications.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * Autor de una publicación. Es interno cuando [userId] tiene valor (referencia débil al usuario,
 * sin foreign key entre dominios) o externo cuando se rellenan [firstName] y [lastName]. [position]
 * preserva el orden de autoría.
 */
@Entity
@Table(name = "publication_authors")
class PublicationAuthorEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
    @Column(name = "user_id") var userId: UUID? = null,
    @Column(name = "first_name") var firstName: String? = null,
    @Column(name = "last_name") var lastName: String? = null,
    @Column(name = "position", nullable = false) var position: Int = 0,
)
