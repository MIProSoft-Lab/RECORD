package es.uib.record.backend.groups.domain

import java.util.UUID

interface GroupRepository {
    fun save(group: Group): Group
    fun findByName(name: String): Group?
    fun findAllByMemberId(memberId: UUID): List<Group>
}