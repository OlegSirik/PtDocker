package ru.pt.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.auth.entity.LoginEntity;

import java.util.List;
import java.util.Optional;

public interface LoginRepository extends JpaRepository<LoginEntity, Long> {

    @Query("SELECT l FROM LoginEntity l WHERE l.userLogin = :userLogin")
    Optional<LoginEntity> findByUserLogin(@Param("userLogin") String userLogin);

    /**
     * Поиск логина по userLogin и tenant ID
     */
    @Query("SELECT l FROM LoginEntity l WHERE l.tenantEntity.id = :tenantId AND l.userLogin = :userLogin")
    Optional<LoginEntity> findByTenantIdAndUserLogin(@Param("tenantId") Long tenantId, @Param("userLogin") String userLogin);

    /**
     * Получить все логины для тенанта
     */
    @Query("SELECT l FROM LoginEntity l WHERE l.tenantEntity.id = :tenantId ORDER BY l.createdAt DESC")
    List<LoginEntity> findByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Получить логин по ID и tenant ID
     */
    @Query("SELECT l FROM LoginEntity l WHERE l.id = :id AND l.tenantEntity.id = :tenantId")
    Optional<LoginEntity> findByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    /**
     * Проверка существования логина для тенанта
     */
    @Query("SELECT COUNT(l) > 0 FROM LoginEntity l WHERE l.tenantEntity.id = :tenantId AND l.userLogin = :userLogin")
    boolean existsByTenantIdAndUserLogin(@Param("tenantId") Long tenantId, @Param("userLogin") String userLogin);

}
