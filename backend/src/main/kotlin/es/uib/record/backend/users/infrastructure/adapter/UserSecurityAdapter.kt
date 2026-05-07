package es.uib.record.backend.users.infrastructure.adapter

import es.uib.record.backend.users.application.usecase.GetUserByEmailUseCase
import es.uib.record.backend.users.domain.exception.UserNotFoundException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserSecurityAdapter(
    private val getUserByEmailUseCase: GetUserByEmailUseCase
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        try {
            val user = this.getUserByEmailUseCase.execute(email)

            return org.springframework.security.core.userdetails.User.builder()
                .username(user.email)
                .password(user.password)
                .build()
        } catch (_: UserNotFoundException) {
            throw BadCredentialsException("Invalid username or password")
        }
    }
}