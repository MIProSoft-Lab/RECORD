package es.uib.record.backend.publications.infrastructure.persistence.repository

import es.uib.record.backend.publications.domain.model.PublicationStatus
import es.uib.record.backend.publications.infrastructure.persistence.entity.PublicationEntity
import java.time.Instant
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SpringDataJpaPublicationRepository : JpaRepository<PublicationEntity, UUID> {
    fun findAllByCreatedByOrderByCreatedAtDesc(createdBy: UUID): List<PublicationEntity>

    fun findAllByAuthors_UserIdOrderByCreatedAtDesc(userId: UUID): List<PublicationEntity>

    /**
     * IDs de las publicaciones en las que [userId] figura como autor, paginadas y ordenadas por
     * fecha de creación descendente, aplicando los filtros opcionales.
     *
     * [titlePattern] llega ya en minúsculas y con comodines (`%texto%`), o `null`, y se usa solo
     * como operando de `LIKE` (no envuelto en `LOWER(...)`) para que Postgres infiera el tipo
     * `text` del parámetro incluso cuando es `null`.
     *
     * El filtro de "días en estado" detecta publicaciones estancadas: exige que NO exista ningún
     * cambio de estado posterior a [staleBefore] (equivalente a que el último cambio sea anterior o
     * igual a él). [staleBefore] nunca es `null`: cuando no hay filtro, el adapter pasa un instante
     * muy lejano en el futuro, de modo que la condición siempre se cumple. Se evita así un
     * `:staleBefore IS NULL` suelto, cuyo tipo Postgres no puede inferir al estar su único uso
     * tipado dentro de la subconsulta (a diferencia del resto de filtros anulables, cuya
     * comparación tipada vive en el mismo ámbito que el `IS NULL`). Los estados finales (REJECTED,
     * PUBLISHED) se excluyen con literales de enum —no con un parámetro de colección, cuyo tipo
     * Postgres tampoco puede inferir— activados por el flag [excludeFinalStatuses].
     */
    @Query(
        value =
            "SELECT p.id FROM PublicationEntity p JOIN p.authors author " +
                "WHERE author.userId = :userId " +
                "AND (:titlePattern IS NULL OR LOWER(p.title) LIKE :titlePattern) " +
                "AND (:status IS NULL OR p.status = :status) " +
                "AND (:journalId IS NULL OR p.journalId = :journalId) " +
                "AND (:onlyMainAuthor = false OR author.position = 0) " +
                "AND NOT EXISTS (" +
                "  SELECT 1 FROM PublicationEntity pp JOIN pp.statusHistory h WHERE pp.id = p.id AND h.changedAt > :staleBefore) " +
                "AND (:excludeFinalStatuses = false OR p.status NOT IN (" +
                "  es.uib.record.backend.publications.domain.model.PublicationStatus.REJECTED, " +
                "  es.uib.record.backend.publications.domain.model.PublicationStatus.PUBLISHED)) " +
                "ORDER BY p.createdAt DESC",
        countQuery =
            "SELECT COUNT(p.id) FROM PublicationEntity p JOIN p.authors author " +
                "WHERE author.userId = :userId " +
                "AND (:titlePattern IS NULL OR LOWER(p.title) LIKE :titlePattern) " +
                "AND (:status IS NULL OR p.status = :status) " +
                "AND (:journalId IS NULL OR p.journalId = :journalId) " +
                "AND (:onlyMainAuthor = false OR author.position = 0) " +
                "AND NOT EXISTS (" +
                "  SELECT 1 FROM PublicationEntity pp JOIN pp.statusHistory h WHERE pp.id = p.id AND h.changedAt > :staleBefore) " +
                "AND (:excludeFinalStatuses = false OR p.status NOT IN (" +
                "  es.uib.record.backend.publications.domain.model.PublicationStatus.REJECTED, " +
                "  es.uib.record.backend.publications.domain.model.PublicationStatus.PUBLISHED))",
    )
    fun searchPublicationIds(
        @Param("userId") userId: UUID,
        @Param("titlePattern") titlePattern: String?,
        @Param("status") status: PublicationStatus?,
        @Param("journalId") journalId: UUID?,
        @Param("staleBefore") staleBefore: Instant,
        @Param("excludeFinalStatuses") excludeFinalStatuses: Boolean,
        @Param("onlyMainAuthor") onlyMainAuthor: Boolean,
        pageable: Pageable,
    ): Page<UUID>

    /**
     * IDs de las publicaciones del grupo [groupId] (todas), paginadas y ordenadas por fecha de
     * creación descendente, aplicando los mismos filtros opcionales que [searchPublicationIds].
     */
    @Query(
        value =
            "SELECT p.id FROM PublicationEntity p " +
                "WHERE p.groupId = :groupId " +
                "AND (:titlePattern IS NULL OR LOWER(p.title) LIKE :titlePattern) " +
                "AND (:status IS NULL OR p.status = :status) " +
                "AND (:journalId IS NULL OR p.journalId = :journalId) " +
                "AND NOT EXISTS (" +
                "  SELECT 1 FROM PublicationEntity pp JOIN pp.statusHistory h WHERE pp.id = p.id AND h.changedAt > :staleBefore) " +
                "AND (:excludeFinalStatuses = false OR p.status NOT IN (" +
                "  es.uib.record.backend.publications.domain.model.PublicationStatus.REJECTED, " +
                "  es.uib.record.backend.publications.domain.model.PublicationStatus.PUBLISHED)) " +
                "ORDER BY p.createdAt DESC",
        countQuery =
            "SELECT COUNT(p.id) FROM PublicationEntity p " +
                "WHERE p.groupId = :groupId " +
                "AND (:titlePattern IS NULL OR LOWER(p.title) LIKE :titlePattern) " +
                "AND (:status IS NULL OR p.status = :status) " +
                "AND (:journalId IS NULL OR p.journalId = :journalId) " +
                "AND NOT EXISTS (" +
                "  SELECT 1 FROM PublicationEntity pp JOIN pp.statusHistory h WHERE pp.id = p.id AND h.changedAt > :staleBefore) " +
                "AND (:excludeFinalStatuses = false OR p.status NOT IN (" +
                "  es.uib.record.backend.publications.domain.model.PublicationStatus.REJECTED, " +
                "  es.uib.record.backend.publications.domain.model.PublicationStatus.PUBLISHED))",
    )
    fun searchGroupPublicationIds(
        @Param("groupId") groupId: UUID,
        @Param("titlePattern") titlePattern: String?,
        @Param("status") status: PublicationStatus?,
        @Param("journalId") journalId: UUID?,
        @Param("staleBefore") staleBefore: Instant,
        @Param("excludeFinalStatuses") excludeFinalStatuses: Boolean,
        pageable: Pageable,
    ): Page<UUID>

    /**
     * Igual que [searchGroupPublicationIds] pero acotando a las publicaciones del grupo en las que
     * alguno de los usuarios de [authorIds] figura como autor (creador o co-autor). Se usa un
     * `EXISTS` correlacionado en lugar de un `JOIN` para no duplicar publicaciones con varios
     * autores coincidentes (evita un `DISTINCT` incompatible con el `ORDER BY` en Postgres).
     */
    @Query(
        value =
            "SELECT p.id FROM PublicationEntity p " +
                "WHERE p.groupId = :groupId " +
                "AND EXISTS (" +
                "  SELECT 1 FROM PublicationEntity pa JOIN pa.authors a WHERE pa.id = p.id AND a.userId IN :authorIds) " +
                "AND (:titlePattern IS NULL OR LOWER(p.title) LIKE :titlePattern) " +
                "AND (:status IS NULL OR p.status = :status) " +
                "AND (:journalId IS NULL OR p.journalId = :journalId) " +
                "AND NOT EXISTS (" +
                "  SELECT 1 FROM PublicationEntity pp JOIN pp.statusHistory h WHERE pp.id = p.id AND h.changedAt > :staleBefore) " +
                "AND (:excludeFinalStatuses = false OR p.status NOT IN (" +
                "  es.uib.record.backend.publications.domain.model.PublicationStatus.REJECTED, " +
                "  es.uib.record.backend.publications.domain.model.PublicationStatus.PUBLISHED)) " +
                "ORDER BY p.createdAt DESC",
        countQuery =
            "SELECT COUNT(p.id) FROM PublicationEntity p " +
                "WHERE p.groupId = :groupId " +
                "AND EXISTS (" +
                "  SELECT 1 FROM PublicationEntity pa JOIN pa.authors a WHERE pa.id = p.id AND a.userId IN :authorIds) " +
                "AND (:titlePattern IS NULL OR LOWER(p.title) LIKE :titlePattern) " +
                "AND (:status IS NULL OR p.status = :status) " +
                "AND (:journalId IS NULL OR p.journalId = :journalId) " +
                "AND NOT EXISTS (" +
                "  SELECT 1 FROM PublicationEntity pp JOIN pp.statusHistory h WHERE pp.id = p.id AND h.changedAt > :staleBefore) " +
                "AND (:excludeFinalStatuses = false OR p.status NOT IN (" +
                "  es.uib.record.backend.publications.domain.model.PublicationStatus.REJECTED, " +
                "  es.uib.record.backend.publications.domain.model.PublicationStatus.PUBLISHED))",
    )
    fun searchGroupPublicationIdsByAuthors(
        @Param("groupId") groupId: UUID,
        @Param("authorIds") authorIds: List<UUID>,
        @Param("titlePattern") titlePattern: String?,
        @Param("status") status: PublicationStatus?,
        @Param("journalId") journalId: UUID?,
        @Param("staleBefore") staleBefore: Instant,
        @Param("excludeFinalStatuses") excludeFinalStatuses: Boolean,
        pageable: Pageable,
    ): Page<UUID>
}
