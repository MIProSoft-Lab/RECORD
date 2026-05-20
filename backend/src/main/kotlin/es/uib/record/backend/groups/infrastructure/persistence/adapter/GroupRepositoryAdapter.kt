package es.uib.record.backend.groups.infrastructure.persistence.adapter

import es.uib.record.backend.groups.domain.model.Group
import es.uib.record.backend.groups.domain.repository.GroupRepository
import es.uib.record.backend.groups.infrastructure.mapper.toDomain
import es.uib.record.backend.groups.infrastructure.mapper.toEntity
import es.uib.record.backend.groups.infrastructure.persistence.repository.SpringDataJpaGroupRepository
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

    override fun findById(id: UUID): Group? {
        return this.springDataJpaGroupRepository.findById(id).orElse(null)?.toDomain()
    }
}