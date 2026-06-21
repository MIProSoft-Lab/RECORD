package es.uib.record.backend.groups.application.usecase.group.dto

import java.util.UUID

data class GroupJournalInterestResponseDto(
    val id: UUID,
    val name: String,
    val issn: String?,
    val eIssn: String?,
    val publisherName: String?,
    val year: Int?,
    val categories: List<GroupJournalInterestCategoryDto>,
    val favoriteCount: Int,
    val members: List<GroupJournalInterestMemberDto>,
)

data class GroupJournalInterestCategoryDto(
    val categoryId: UUID,
    val categoryName: String,
    val edition: String?,
    val quartile: String,
    val impactFactor: Double?,
)

data class GroupJournalInterestMemberDto(
    val userId: UUID,
    val firstName: String,
    val lastName: String,
    val profileImageUrl: String,
)
