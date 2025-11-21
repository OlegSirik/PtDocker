package ru.pt.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.auth.entity.ClientEntity;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

    /**
     * Найти client по ID tenant и client_id
     */
    @Query("SELECT c FROM ClientEntity c WHERE c.tenantEntity.id = :tenantId AND c.clientId = :clientId AND c.isDeleted = false")
    Optional<ClientEntity> findByTenantAndClientId(@Param("tenantId") Long tenantId, @Param("clientId") String clientId);

    /**
     * Найти все active clients для tenant
     */
    @Query("SELECT c FROM ClientEntity c WHERE c.tenantEntity.id = :tenantId AND c.isDeleted = false ORDER BY c.name")
    List<ClientEntity> findByTenantIdActive(@Param("tenantId") Long tenantId);

    /**
     * Найти client по имени в tenant
     */
    @Query("SELECT c FROM ClientEntity c WHERE c.tenantEntity.id = :tenantId AND c.name = :name AND c.isDeleted = false")
    Optional<ClientEntity> findByTenantAndName(@Param("tenantId") Long tenantId, @Param("name") String name);
}

