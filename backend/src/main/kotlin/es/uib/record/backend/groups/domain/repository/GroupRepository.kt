package es.uib.record.backend.groups.domain.repository

import es.uib.record.backend.groups.domain.model.Group
import java.util.UUID

interface GroupRepository {
    fun save(group: Group): Group

    fun findByName(name: String): Group?

    fun findAllByMemberId(memberId: UUID): List<Group>

    fun findById(id: UUID): Group?

    fun delete(id: UUID)
}
