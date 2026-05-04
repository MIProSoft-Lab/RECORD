package es.uib.record.backend.users.infrastructure.mapper

import es.uib.record.backend.model.UserResponse
import es.uib.record.backend.users.domain.User
import es.uib.record.backend.users.infrastructure.persistence.UserEntity

fun User.toEntity() = UserEntity(
    this.id,
    this.email,
    this.password,
    this.firstName,
    this.lastName,
    this.pushNotifications,
    this.createdAt
)

fun UserEntity.toDomain() = User(
    this.id,
    this.email,
    this.password,
    this.firstName,
    this.lastName,
    this.pushNotifications,
    this.createdAt
)

fun User.toResponse() = UserResponse(
    this.id!!,
    this.firstName,
    this.lastName,
    this.email
)