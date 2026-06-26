package es.uib.record.backend.users.infrastructure.mapper

import es.uib.record.backend.model.UserProfileImageSignatureResponse
import es.uib.record.backend.model.UserResponse
import es.uib.record.backend.model.UserSummaryResponse
import es.uib.record.backend.model.UserUpdateRequest
import es.uib.record.backend.users.application.usecase.dto.UserProfileImageSignatureResponseDto
import es.uib.record.backend.users.application.usecase.dto.UserUpdateRequestDto
import es.uib.record.backend.users.domain.User
import es.uib.record.backend.users.infrastructure.persistence.UserEntity
import es.uib.record.backend.users.open.UserOpenDto

fun User.toEntity() =
    UserEntity(
        this.id,
        this.email,
        this.password,
        this.firstName,
        this.lastName,
        this.pushNotifications,
        this.profileImageUrl,
        this.createdAt,
        this.deactivatedAt,
    )

fun UserEntity.toDomain() =
    User(
        this.id,
        this.email,
        this.password,
        this.firstName,
        this.lastName,
        this.pushNotifications,
        this.profileImageUrl,
        this.createdAt,
        this.deactivatedAt,
    )

fun User.toResponse() =
    UserResponse(
        this.id!!,
        this.firstName,
        this.lastName,
        this.email,
        this.profileImageUrl,
        this.pushNotifications,
    )

fun UserUpdateRequest.toDto() =
    UserUpdateRequestDto(this.firstName, this.lastName, this.profileImageUrl)

fun UserProfileImageSignatureResponseDto.toResponse() =
    UserProfileImageSignatureResponse(
        this.signature,
        this.timestamp,
        this.apiKey,
        this.cloudName,
        this.transformation,
        this.folder,
    )

fun User.toOpenDto() =
    UserOpenDto(this.id!!, this.firstName, this.lastName, this.email, this.profileImageUrl)

fun User.toSummaryResponse() =
    UserSummaryResponse(
        this.id!!,
        this.firstName,
        this.lastName,
        this.email,
        this.profileImageUrl,
    )
