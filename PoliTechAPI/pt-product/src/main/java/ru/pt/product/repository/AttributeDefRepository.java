package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.pt.product.entity.AttributeDefEntity;

import java.util.List;
import java.util.Optional;

public interface AttributeDefRepository extends JpaRepository<AttributeDefEntity, Long> {
    List<AttributeDefEntity> findByTidAndEntityId(Long tid, Long entityId);
    Optional<AttributeDefEntity> findByEntityIdAndCode(Long entityId, String code);
    Optional<AttributeDefEntity> findByEntityIdAndName(Long entityId, String name);
    List<AttributeDefEntity> findByEntityId(Long entityId);

    /**
     * Find all attribute definitions for a given tenant and contract model code.
     *
     * Equivalent SQL:
     * <pre>
     *   select t1.*
     *   from mt_attribute_def t1
     *   join mt_entity_def t2 on t1.entity_id = t2.id
     *   join mt_contract_section t3 on t2.section_id = t3.id
     *   join mt_contract_model t4 on t3.model_id = t4.id
     *   where t4.tid = :tid and t4.code = :modelCode
     * </pre>
     */
    @Query(value = """
            select t1.*
            from mt_attribute_def t1
            join mt_entity_def t2 on t1.entity_id = t2.id
            join mt_contract_section t3 on t2.section_id = t3.id
            join mt_contract_model t4 on t3.model_id = t4.id
            where t4.tid = :tid and t4.code = :modelCode
            """, nativeQuery = true)
    List<AttributeDefEntity> findByTenantAndModelCode(@Param("tid") Long tid,
                                                      @Param("modelCode") String modelCode);
}
