package es.uib.record.backend.users.infrastructure.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SpringDataJpaUserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?

    @Query(
        """
        SELECT u FROM UserEntity u
        WHERE u.deactivatedAt IS NULL
          AND (
            LOWER(u.email)     LIKE LOWER(CONCAT('%', :query, '%'))
         OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%'))
         OR LOWER(u.lastName)  LIKE LOWER(CONCAT('%', :query, '%'))
         OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :query, '%'))
          )
        ORDER BY u.firstName, u.lastName
        """
    )
    fun findByEmailOrName(@Param("query") query: String): List<UserEntity>

    @Query("SELECT u FROM UserEntity u WHERE u.id IN :ids AND u.deactivatedAt IS NULL")
    fun findAllByIdsActive(@Param("ids") ids: Collection<UUID>): List<UserEntity>
}
