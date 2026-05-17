package es.uib.record.backend.groups.infrastructure.persistence.entity

import es.uib.record.backend.groups.domain.Group
import es.uib.record.backend.groups.domain.GroupRepository
import es.uib.record.backend.groups.infrastructure.mapper.toDomain
import es.uib.record.backend.groups.infrastructure.mapper.toEntity
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class GroupRepositoryAdapter(
    private val springDataJpaGroupRepository: SpringDataJpaGroupRepository
) : GroupRepository {

    override fun save(group: Group): Group {
        val groupEntity = springDataJpaGroupRepository.save(group.toEntity())
        return groupEntity.toDomain()
    }

    override fun findByName(name: String): Group? {
        return this.springDataJpaGroupRepository.findByName(name)?.toDomain()
    }

    override fun findAllByMemberId(memberId: UUID): List<Group> {
        return this.springDataJpaGroupRepository.findAllByMembersUserId(memberId).map { it.toDomain() }
    }
}