package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.product.entity.PolicyFormEntity;

import java.util.List;
import java.util.Optional;

public interface PolicyFormRepository extends JpaRepository<PolicyFormEntity, Long> {

    @Query("""
            select f from PolicyFormEntity f
            where f.tid = :tid and f.recordStatus = 'ACTIVE'
            order by f.title
            """)
    List<PolicyFormEntity> findActiveByTid(@Param("tid") Long tid);

    @Query("""
            select f from PolicyFormEntity f
            where f.tid = :tid and f.pageId = :pageId and f.recordStatus = 'ACTIVE'
            """)
    Optional<PolicyFormEntity> findActiveByTidAndPageId(
            @Param("tid") Long tid, @Param("pageId") String pageId);
}
