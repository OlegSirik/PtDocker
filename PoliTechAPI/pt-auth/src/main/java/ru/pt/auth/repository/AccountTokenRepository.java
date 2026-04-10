package ru.pt.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.auth.entity.AccountTokenEntity;

import java.util.List;
import java.util.Optional;

public interface AccountTokenRepository extends JpaRepository<AccountTokenEntity, Long> {
/* 
    @Query("SELECT at FROM AccountTokenEntity at " +
           "JOIN at.accountEntity acc " +
           "JOIN AccountLoginEntity al ON al.accountEntity.id = acc.id " +
           "WHERE al.userLogin = :userLogin AND at.clientEntity.id = :clientId")
    Optional<AccountTokenEntity> findByUserLoginAndClientId(@Param("userLogin") String userLogin,
                                                             @Param("clientId") Long clientId);

    @Query("SELECT at FROM AccountTokenEntity at " +
           "JOIN at.accountEntity acc " +
           "JOIN AccountLoginEntity al ON al.accountEntity.id = acc.id " +
           "WHERE al.userLogin = :userLogin")
    List<AccountTokenEntity> findByUserLogin(@Param("userLogin") String userLogin);
*/
    @Query("SELECT at FROM AccountTokenEntity at WHERE at.token = :token AND at.clientId = :clientId")
    Optional<AccountTokenEntity> findByTokenAndClientId(@Param("token") String token,
                                                        @Param("clientId") Long clientId);

    @Query("SELECT at FROM AccountTokenEntity at WHERE at.token = :token AND at.accountId = :accountId")
    Optional<AccountTokenEntity> findByTokenAndAccountId(@Param("token") String token,
                                                         @Param("accountId") Long accountId);

    @Query("SELECT at FROM AccountTokenEntity at JOIN TenantEntity t ON at.tid = t.id WHERE at.token = :token AND t.code = :tenantCode")
    Optional<AccountTokenEntity> findByTokenAndTenantCode(@Param("token") String token,
                                                         @Param("tenantCode") String tenantCode);

    @Query("SELECT at FROM AccountTokenEntity at JOIN TenantEntity t ON at.tid = t.id WHERE at.token = :token AND t.code = :tenantCode")
    Optional<AccountTokenEntity> findByTokenAndTenantCodeWithClientAndAccount(
            @Param("token") String token,
            @Param("tenantCode") String tenantCode);

    @Query("SELECT at FROM AccountTokenEntity at " +
           "JOIN TenantEntity t ON at.tid = t.id " +
           "JOIN ClientEntity c ON at.clientId = c.id " +
           "WHERE t.code = :tenantCode AND c.authClientId = :clientAuthId AND at.accountId = :accountId")
    Optional<AccountTokenEntity> findByAll4Fields(
              @Param("tenantCode") String tenantCode,
              @Param("clientAuthId") String clientAuthId,
              @Param("accountId") Long accountId);

    @Query("SELECT at FROM AccountTokenEntity at WHERE at.accountId = :accountId")
    Optional<List<AccountTokenEntity>> findByAccountId(@Param("accountId") Long accountId);
}
