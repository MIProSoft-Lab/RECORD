package es.uib.record.backend.auth.infrastructure.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataJpaTokenRepository : JpaRepository<TokenEntity, UUID> {
    fun findByToken(token: String): TokenEntity?
}
