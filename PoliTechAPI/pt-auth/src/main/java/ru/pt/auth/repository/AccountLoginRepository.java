package ru.pt.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.Tuple;
import ru.pt.auth.entity.AccountLoginEntity;

import java.util.List;
import java.util.Optional;

public interface AccountLoginRepository extends JpaRepository<AccountLoginEntity, Long> {

   @Query("SELECT al FROM AccountLoginEntity al " +
          "WHERE al.tenantEntity.code = :tenantCode " +
          "AND al.clientEntity.clientId = :authClientId " +
          "AND al.userLogin = :userLogin " +
          "AND al.loginEntity.isDeleted = false " +
          "ORDER BY al.isDefault DESC, al.id")
    List<AccountLoginEntity> findByTenantCodeAndAuthClientIdAndLogin(@Param("tenantCode") String tenantCode, @Param("authClientId") String authClientId, @Param("userLogin") String userLogin);
         
    @Query("SELECT al FROM AccountLoginEntity al " +
    "WHERE al.tenantEntity.code = :tenantCode " +
    "AND al.clientEntity.clientId = :authClientId " +
    "AND al.accountEntity.id = :accountId " +
    "AND al.loginEntity.isDeleted = false " +
    "ORDER BY al.isDefault DESC, al.id")
    List<AccountLoginEntity> findByTenantCodeAndAuthClientIdAndAccountId(@Param("tenantCode") String tenantCode, @Param("authClientId") String authClientId, @Param("accountId") Long accountId);


    @Query("SELECT al FROM AccountLoginEntity al WHERE al.clientEntity.clientId = :client AND al.loginEntity.userLogin = :login ORDER BY al.id")
    List<AccountLoginEntity> findByClientAndLogin(@Param("client") String client, @Param("login") String login);

    @Query("SELECT al FROM AccountLoginEntity al WHERE al.userLogin = :userLogin ORDER BY al.id")
    List<AccountLoginEntity> findByUserLogin(@Param("userLogin") String userLogin);

    @Query("SELECT al FROM AccountLoginEntity al WHERE al.tenantEntity.code = :tenantCode AND al.clientEntity.clientId = :clientId AND al.userLogin = :userLogin ORDER BY al.id")
    List<AccountLoginEntity> findByTenantCodeAndClientIdAndUserLogin(@Param("tenantCode") String tenantCode, @Param("clientId") String clientId, @Param("userLogin") String userLogin);

    @Query("SELECT al FROM AccountLoginEntity al WHERE al.userLogin = :userLogin AND al.accountEntity.id = :accountId")
    Optional<AccountLoginEntity> findByUserLoginAndAccountId(@Param("userLogin") String userLogin,
                                                              @Param("accountId") Long accountId);

    /**
     * Проверка существования записи для пользователя и клиента
     */
    //@Query("SELECT COUNT(al) > 0 FROM AccountLoginEntity al WHERE al.userLogin = :userLogin AND al.clientEntity.id = :clientId")
    //boolean existsByUserLoginAndClientId(@Param("userLogin") String userLogin, @Param("clientId") Long clientId);

    //@Query("SELECT al FROM AccountLoginEntity al WHERE al.tenantEntity.code = :tenantCode AND al.userRole = :userRole")
    //List<AccountLoginEntity> findByTenantAndUserRole(@Param("tenantCode") String tenantCode, @Param("userRole") String userRole);

    @Query(value = "SELECT " +
            "al.id AS id, " +
            "al.tid AS tid, " +
            "al.client_id AS clientId, " +
            "al.account_id AS accountId, " +
            "al.user_login AS userLogin, " +
            "al.user_role AS userRole, " +
            "lg.full_name AS fullName, " +
            "lg.position AS position " +
            "FROM acc_account_logins al " +
            "JOIN acc_logins lg ON al.tid = lg.tid AND al.user_login = lg.user_login and lg.is_deleted = false " +
            "WHERE al.tid = (SELECT t.id FROM acc_tenants t WHERE t.code = :tenantCode) " +
            "AND al.user_role = :userRole",
           nativeQuery = true)
    List<Tuple> findByTenantAndUserRoleFull(@Param("tenantCode") String tenantCode, @Param("userRole") String userRole);

    /**
     * Проверка существования связи между user_login и client_id через таблицы acc_logins и acc_account_logins
     */
    @Query("SELECT al FROM AccountLoginEntity al " +
           "WHERE al.userLogin = :userLogin " +
           "AND al.clientEntity.clientId = :clientId " +
           "AND al.tenantEntity.code = :tenantCode " +
           "AND al.loginEntity.isDeleted = false")
    Optional<AccountLoginEntity> findByUserLoginAndClientIdWithValidation(@Param("userLogin") String userLogin,
                                                                           @Param("clientId") String clientId,
                                                                          @Param("tenantCode") String tenantCode);

    /**
     * Поиск записи по account_id для получения базового логина
     */
    @Query("SELECT al FROM AccountLoginEntity al WHERE al.accountEntity.id = :accountId")
    Optional<AccountLoginEntity> findByAccountId(@Param("accountId") Long accountId);

    /**
     * Проверка существования user_login в БД acc_logins для указанного тенанта
     * с проверкой что запись не удалена
     */
    @Query("SELECT al FROM AccountLoginEntity al " +
           "WHERE al.userLogin = :userLogin " +
           "AND al.tenantEntity.code = :tenantCode " +
           "AND al.loginEntity.isDeleted = false")
    Optional<AccountLoginEntity> findByUserLoginAndTenantCode(@Param("userLogin") String userLogin,
                                                               @Param("tenantCode") String tenantCode);

}