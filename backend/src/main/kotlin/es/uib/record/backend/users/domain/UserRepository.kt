package es.uib.record.backend.users.domain

import java.util.UUID

interface UserRepository {
    fun save(user: User): User

    fun findByEmail(email: String): User?

    fun findAllByIds(userIds: List<UUID>): List<User>

    fun searchByEmailOrName(query: String): List<User>
}
