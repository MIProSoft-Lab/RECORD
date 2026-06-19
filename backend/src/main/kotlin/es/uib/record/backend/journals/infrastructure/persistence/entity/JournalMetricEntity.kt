package es.uib.record.backend.journals.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "journal_metrics")
class JournalMetricEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
    @Column(nullable = false, name = "journal_id") var journalId: UUID,
    @Column(nullable = false, name = "report_year") var year: Int,
    @Column(name = "impact_factor") var impactFactor: BigDecimal? = null,
    @Column(nullable = false, name = "created_at", updatable = false)
    var createdAt: Instant = Instant.now(),
)
