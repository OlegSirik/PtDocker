package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.pt.product.entity.AttributeDefEntity;

import java.util.List;
import java.util.Optional;

/**
 * Доступ к строкам {@code mt_attribute_def} (дерево атрибутов схемы / LobVar в БД).
 * Бизнес-логика остаётся в {@link ru.pt.api.service.schema.SchemaService}.
 */
public interface AttributeDefRepository extends JpaRepository<AttributeDefEntity, Long> {


    List<AttributeDefEntity> findByTenantIdAndDocumentId(Long tenantId, String documentId);
    List<AttributeDefEntity> findByTenantId(Long tenantId);
    
    Optional<AttributeDefEntity> findByTenantIdAndId(Long tenantId, Long id);

    boolean existsByTenantIdAndDocumentIdAndVarCode(Long tenantId, String documentId, String varCode);

    boolean existsByIdAndTenantIdAndDocumentId(Long id, Long tenantId, String documentId);

    /** {@code modelCode} — то же, что {@code document_id} в {@code mt_attribute_def}. */
    @Query("SELECT a FROM AttributeDefEntity a WHERE a.tenantId = :tenantId AND a.documentId = :modelCode")
    List<AttributeDefEntity> findByTenantAndModelCode(@Param("tenantId") Long tenantId, @Param("modelCode") String modelCode);

    /** Следующее значение последовательности {@code mt_attribute_def_seq} (аналог {@code nextval}). */
    @Query(value = "SELECT nextval('mt_attribute_def_seq')", nativeQuery = true)
    Long nextId();

}
