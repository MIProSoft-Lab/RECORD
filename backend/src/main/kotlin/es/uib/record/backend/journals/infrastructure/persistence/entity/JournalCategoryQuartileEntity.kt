package es.uib.record.backend.journals.infrastructure.persistence.entity

import es.uib.record.backend.journals.domain.model.Quartile
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "journal_category_quartiles")
class JournalCategoryQuartileEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
    @Column(nullable = false, name = "journal_metric_id") var journalMetricId: UUID,
    @Column(nullable = false, name = "journal_id") var journalId: UUID,
    @Column(nullable = false, name = "category_id") var categoryId: UUID,
    @Column(nullable = false, name = "report_year") var year: Int,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var quartile: Quartile,
    @Column(name = "impact_factor") var impactFactor: BigDecimal? = null,
)
