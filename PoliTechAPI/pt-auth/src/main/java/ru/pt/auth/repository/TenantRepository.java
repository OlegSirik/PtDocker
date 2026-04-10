package ru.pt.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.auth.entity.TenantEntity;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<TenantEntity, Long> {

    /**
     * Найти tenant по коду
     */
    @Query("SELECT t FROM TenantEntity t WHERE t.code = :code AND t.recordStatus = 'ACTIVE'")
    Optional<TenantEntity> findByCode(@Param("code") String code);

    /**
     * Найти tenant по имени
     */
    @Query("SELECT t FROM TenantEntity t WHERE t.name = :name AND t.recordStatus = 'ACTIVE'")
    Optional<TenantEntity> findByName(@Param("name") String name);

}

