package es.uib.record.backend.publications.application.usecase.dto

data class UpdatePublicationRequestDto(
    val title: String,
    val abstractText: String?,
    val doi: String?,
    val authors: List<PublicationAuthorInputDto> = emptyList(),
)
