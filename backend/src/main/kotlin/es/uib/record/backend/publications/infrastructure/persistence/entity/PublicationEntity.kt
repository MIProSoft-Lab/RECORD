package es.uib.record.backend.publications.infrastructure.persistence.entity

import es.uib.record.backend.publications.domain.model.PublicationStatus
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "publications")
class PublicationEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
    @Column(nullable = false) var title: String,
    @Column(name = "abstract", columnDefinition = "text") var abstractText: String? = null,
    var doi: String? = null,
    @Column(name = "journal_id", nullable = false) var journalId: UUID,
    @Column(name = "group_id", nullable = false) var groupId: UUID,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var status: PublicationStatus,
    @Column(nullable = false, name = "created_by") var createdBy: UUID,
    @Column(nullable = false, name = "created_at", updatable = false)
    var createdAt: Instant = Instant.now(),
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "publication_id", nullable = false)
    @OrderBy("position ASC")
    var authors: MutableList<PublicationAuthorEntity> = mutableListOf(),
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "publication_id", nullable = false)
    @OrderBy("position ASC")
    var statusHistory: MutableList<PublicationStatusHistoryEntity> = mutableListOf(),
)
