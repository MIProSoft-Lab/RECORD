package es.uib.record.backend.journals.infrastructure.mapper

import es.uib.record.backend.journals.domain.model.Category
import es.uib.record.backend.journals.domain.model.Journal
import es.uib.record.backend.journals.domain.model.JournalCategoryQuartile
import es.uib.record.backend.journals.domain.model.JournalCategoryQuartileInfo
import es.uib.record.backend.journals.domain.model.JournalMetric
import es.uib.record.backend.journals.domain.model.SyncState
import es.uib.record.backend.journals.infrastructure.persistence.entity.CategoryEntity
import es.uib.record.backend.journals.infrastructure.persistence.entity.JournalCategoryQuartileEntity
import es.uib.record.backend.journals.infrastructure.persistence.entity.JournalEntity
import es.uib.record.backend.journals.infrastructure.persistence.entity.JournalMetricEntity
import es.uib.record.backend.journals.infrastructure.persistence.entity.JournalSyncStateEntity
import es.uib.record.backend.journals.infrastructure.persistence.repository.JournalCategoryQuartileView

fun Journal.toEntity() =
    JournalEntity(
        id = this.id,
        clarivateId = this.clarivateId,
        name = this.name,
        issn = this.issn,
        eIssn = this.eIssn,
        publisherName = this.publisherName,
        publisherCountry = this.publisherCountry,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        lastSyncedAt = this.lastSyncedAt,
    )

fun JournalEntity.toDomain() =
    Journal(
        id = this.id,
        clarivateId = this.clarivateId,
        name = this.name,
        issn = this.issn,
        eIssn = this.eIssn,
        publisherName = this.publisherName,
        publisherCountry = this.publisherCountry,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        lastSyncedAt = this.lastSyncedAt,
    )

fun Category.toEntity() = CategoryEntity(id = this.id, name = this.name, edition = this.edition)

fun CategoryEntity.toDomain() = Category(id = this.id, name = this.name, edition = this.edition)

fun JournalMetric.toEntity() =
    JournalMetricEntity(
        id = this.id,
        journalId = this.journalId,
        year = this.year,
        impactFactor = this.impactFactor,
        createdAt = this.createdAt,
    )

fun JournalMetricEntity.toDomain() =
    JournalMetric(
        id = this.id,
        journalId = this.journalId,
        year = this.year,
        impactFactor = this.impactFactor,
        createdAt = this.createdAt,
    )

fun JournalCategoryQuartileView.toInfo() =
    JournalCategoryQuartileInfo(
        categoryId = this.categoryId,
        categoryName = this.categoryName,
        edition = this.edition,
        year = this.year,
        quartile = this.quartile,
        impactFactor = this.impactFactor,
    )

fun JournalCategoryQuartile.toEntity() =
    JournalCategoryQuartileEntity(
        id = this.id,
        journalMetricId = this.journalMetricId,
        journalId = this.journalId,
        categoryId = this.categoryId,
        year = this.year,
        quartile = this.quartile,
        impactFactor = this.impactFactor,
    )

fun SyncState.toEntity() =
    JournalSyncStateEntity(
        id = this.id,
        clarivateLastUpdated = this.clarivateLastUpdated,
        status = this.status,
        runStartedAt = this.runStartedAt,
        runFinishedAt = this.runFinishedAt,
        processedCount = this.processedCount,
        totalCount = this.totalCount,
        failedCount = this.failedCount,
    )

fun JournalSyncStateEntity.toDomain() =
    SyncState(
        id = this.id,
        clarivateLastUpdated = this.clarivateLastUpdated,
        status = this.status,
        runStartedAt = this.runStartedAt,
        runFinishedAt = this.runFinishedAt,
        processedCount = this.processedCount,
        totalCount = this.totalCount,
        failedCount = this.failedCount,
    )
