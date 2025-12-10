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
    @Query("SELECT l FROM LoginEntity l WHERE l.tenantEntity.code = :tenantCode AND l.userLogin = :userLogin")
    Optional<LoginEntity> findByTenantCodeAndUserLogin(@Param("tenantCode") String tenantCode, @Param("userLogin") String userLogin);

    /**
     * Получить все логины для тенанта
     */
    @Query("SELECT l FROM LoginEntity l WHERE l.tenantEntity.code = :tenantCode ORDER BY l.createdAt DESC")
    List<LoginEntity> findByTenantCode(@Param("tenantCode") String tenantCode);

    /**
     * Получить логин по ID и tenant ID
     */
    @Query("SELECT l FROM LoginEntity l WHERE l.id = :id AND l.tenantEntity.code = :tenantCode")
    Optional<LoginEntity> findByIdAndTenantCode(@Param("id") Long id, @Param("tenantCode") String tenantCode);

    /**
     * Проверка существования логина для тенанта
     */
    @Query("SELECT COUNT(l) > 0 FROM LoginEntity l WHERE l.tenantEntity.code = :tenantCode AND l.userLogin = :userLogin")
    boolean existsByTenantCodeAndUserLogin(@Param("tenantCode") String tenantCode, @Param("userLogin") String userLogin);

}
