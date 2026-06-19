package es.uib.record.backend.journals.infrastructure.mapper

import es.uib.record.backend.journals.domain.model.Category
import es.uib.record.backend.journals.domain.model.JournalCategoryQuartileInfo
import es.uib.record.backend.journals.domain.model.JournalDetail
import es.uib.record.backend.journals.domain.model.JournalMetric
import es.uib.record.backend.journals.domain.model.JournalSearchItem
import es.uib.record.backend.model.CategoryResponse
import es.uib.record.backend.model.JournalCategoryQuartileDetail
import es.uib.record.backend.model.JournalCategoryQuartileSummary
import es.uib.record.backend.model.JournalDetailResponse
import es.uib.record.backend.model.JournalMetricResponse
import es.uib.record.backend.model.JournalSearchPageResponse
import es.uib.record.backend.model.JournalSummaryResponse
import es.uib.record.backend.shared.domain.PageResult
import es.uib.record.backend.model.Quartile as ApiQuartile

fun PageResult<JournalSearchItem>.toResponse() =
    JournalSearchPageResponse(
        content = this.items.map { it.toSummaryResponse() },
        totalElements = this.totalElements,
        totalPages = this.totalPages,
        page = this.page,
        propertySize = this.size,
    )

fun JournalSearchItem.toSummaryResponse() =
    JournalSummaryResponse(
        id = this.journal.id!!,
        name = this.journal.name,
        categories = this.categories.map { it.toSummary() },
        issn = this.journal.issn,
        eIssn = this.journal.eIssn,
        publisherName = this.journal.publisherName,
        year = this.year,
    )

fun JournalCategoryQuartileInfo.toSummary() =
    JournalCategoryQuartileSummary(
        categoryId = this.categoryId,
        categoryName = this.categoryName,
        quartile = ApiQuartile.valueOf(this.quartile.name),
        edition = this.edition,
        impactFactor = this.impactFactor?.toDouble(),
    )

fun Category.toResponse() = CategoryResponse(id = this.id!!, name = this.name, edition = this.edition)

fun JournalDetail.toResponse() =
    JournalDetailResponse(
        id = this.journal.id!!,
        name = this.journal.name,
        metrics = this.metrics.map { it.toResponse() },
        categoryQuartiles = this.categoryQuartiles.map { it.toDetail() },
        issn = this.journal.issn,
        eIssn = this.journal.eIssn,
        publisherName = this.journal.publisherName,
        publisherCountry = this.journal.publisherCountry,
    )

fun JournalMetric.toResponse() =
    JournalMetricResponse(year = this.year, impactFactor = this.impactFactor?.toDouble())

fun JournalCategoryQuartileInfo.toDetail() =
    JournalCategoryQuartileDetail(
        year = this.year,
        categoryName = this.categoryName,
        quartile = ApiQuartile.valueOf(this.quartile.name),
        edition = this.edition,
        impactFactor = this.impactFactor?.toDouble(),
    )
