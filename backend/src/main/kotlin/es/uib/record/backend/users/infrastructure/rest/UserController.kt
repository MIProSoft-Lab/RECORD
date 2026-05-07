package es.uib.record.backend.users.infrastructure.rest

import es.uib.record.backend.api.UsersApi
import es.uib.record.backend.model.UserResponse
import es.uib.record.backend.model.UserUpdateRequest
import es.uib.record.backend.users.application.usecase.GetUserByEmailUseCase
import es.uib.record.backend.users.application.usecase.UpdateCurrentUserByEmailUseCase
import es.uib.record.backend.users.infrastructure.mapper.toDto
import es.uib.record.backend.users.infrastructure.mapper.toResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val getUserByEmailUseCase: GetUserByEmailUseCase,
    private val updateCurrentUserByEmailUseCase: UpdateCurrentUserByEmailUseCase
) : UsersApi {
    override fun getCurrentUser(): ResponseEntity<UserResponse> {
        val authentication = SecurityContextHolder.getContext().authentication
        val email = authentication.name

        val user = this.getUserByEmailUseCase.execute(email)

        return ResponseEntity.ok(user.toResponse())
    }

    override fun updateCurrentUser(userUpdateRequest: UserUpdateRequest): ResponseEntity<UserResponse> {
        val authentication = SecurityContextHolder.getContext().authentication
        val email = authentication.name

        val user = this.updateCurrentUserByEmailUseCase.execute(email, userUpdateRequest.toDto())

        return ResponseEntity.ok(user.toResponse())
    }
}