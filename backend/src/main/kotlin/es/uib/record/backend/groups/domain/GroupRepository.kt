package es.uib.record.backend.groups.domain

interface GroupRepository {
    fun save(group: Group): Group
    fun findByName(name: String): Group?
}