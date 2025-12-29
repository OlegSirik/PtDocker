package ru.pt.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.auth.entity.ClientEntity;
import ru.pt.auth.entity.TenantEntity;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

    /**
     * Найти client по ID tenant и client_id
     */
    @Query("SELECT c FROM ClientEntity c WHERE c.tenantEntity.code = :tenantCode AND c.clientId = :authClientId AND c.isDeleted = false")
    Optional<ClientEntity> findByTenantCodeAndAuthClientId(@Param("tenantCode") String tenantCode, @Param("authClientId") String authClientId);
    
    /**
     * Найти все active clients для tenant
     */
    @Query("SELECT c FROM ClientEntity c WHERE c.tenantEntity.code = :tenantCode AND c.isDeleted = false ORDER BY c.name")
    List<ClientEntity> findByTenantCodeActive(@Param("tenantCode") String tenantCode);

    /**
     * Найти client по имени в tenant
     */
    //@Query("SELECT c FROM ClientEntity c WHERE c.tenantEntity.id = :tenantCode AND c.name = :name AND c.isDeleted = false")
    //Optional<ClientEntity> findByTenantAndName(@Param("tenantCode") String tenantCode, @Param("name") String name);

    /**
     * Найти client по clientId
     */
    @Query("SELECT c FROM ClientEntity c WHERE c.id = :Id")
    Optional<ClientEntity> findById(@Param("Id") Long id);

    /**
     * Найти все clients для tenant (включая deleted)
     */
    @Query("SELECT c FROM ClientEntity c WHERE c.tenantEntity.code = :tenantCode ORDER BY c.name")
    List<ClientEntity> findBytenantCode(@Param("tenantCode") String tenantCode);


    /**
     * Найти client по clientId и tenantCode
     */
    @Query("SELECT c FROM ClientEntity c WHERE c.clientId = :clientId AND c.tenantEntity.code = :tenantCode")
    Optional<ClientEntity> findByClientIdandTenantCode(@Param("clientId") String clientId, @Param("tenantCode") String tenantCode);

    /***
     * Найти client по clientId
     */
    @Query("SELECT c FROM ClientEntity c WHERE c.clientId = :clientId")
    Optional<ClientEntity> findByClientId(String clientId);


    
}

