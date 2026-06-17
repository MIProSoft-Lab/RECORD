package es.uib.record.backend.auth.infrastructure.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SpringDataJpaTokenRepository : JpaRepository<TokenEntity, UUID> {
    fun findByToken(token: String): TokenEntity?

    @Modifying
    @Query("UPDATE TokenEntity t SET t.revoked = true WHERE t.userId = :userId")
    fun revokeAllByUserId(@Param("userId") userId: UUID)
}
