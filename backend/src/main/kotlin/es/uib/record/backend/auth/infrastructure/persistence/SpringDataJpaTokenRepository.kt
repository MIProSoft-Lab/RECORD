package es.uib.record.backend.auth.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpringDataJpaTokenRepository : JpaRepository<TokenEntity, UUID> {
    fun findByToken(token: String): TokenEntity?
}