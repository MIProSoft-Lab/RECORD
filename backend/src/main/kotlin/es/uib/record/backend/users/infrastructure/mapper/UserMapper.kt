package es.uib.record.backend.users.infrastructure.mapper

import es.uib.record.backend.model.UserProfileImageSignatureResponse
import es.uib.record.backend.model.UserResponse
import es.uib.record.backend.model.UserUpdateRequest
import es.uib.record.backend.users.application.usecase.dto.UserProfileImageSignatureResponseDto
import es.uib.record.backend.users.application.usecase.dto.UserUpdateRequestDto
import es.uib.record.backend.users.domain.User
import es.uib.record.backend.users.infrastructure.persistence.UserEntity

fun User.toEntity() = UserEntity(
    this.id,
    this.email,
    this.password,
    this.firstName,
    this.lastName,
    this.pushNotifications,
    this.profileImageUrl,
    this.createdAt
)

fun UserEntity.toDomain() = User(
    this.id,
    this.email,
    this.password,
    this.firstName,
    this.lastName,
    this.pushNotifications,
    this.profileImageUrl,
    this.createdAt
)

fun User.toResponse() = UserResponse(
    this.id!!,
    this.firstName,
    this.lastName,
    this.email,
    this.profileImageUrl
)

fun UserUpdateRequest.toDto() = UserUpdateRequestDto(
    this.firstName,
    this.lastName,
    this.profileImageUrl
)

fun UserProfileImageSignatureResponseDto.toResponse() = UserProfileImageSignatureResponse(
    this.signature,
    this.timestamp,
    this.apiKey,
    this.cloudName,
    this.transformation,
    this.folder
)