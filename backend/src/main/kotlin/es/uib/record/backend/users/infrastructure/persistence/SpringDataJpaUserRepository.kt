package es.uib.record.backend.users.infrastructure.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataJpaUserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?
}
