package ru.pt.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.auth.entity.AccountLoginEntity;

import java.util.List;
import java.util.Optional;

public interface AccountLoginRepository extends JpaRepository<AccountLoginEntity, Long> {

    @Query("SELECT al FROM AccountLoginEntity al WHERE al.clientEntity.name = :client AND al.loginEntity.userLogin = :login ORDER BY al.id")
    List<AccountLoginEntity> findByClientAndLogin(@Param("client") String client, @Param("login") String login);

    @Query("SELECT al FROM AccountLoginEntity al WHERE al.userLogin = :userLogin ORDER BY al.id")
    List<AccountLoginEntity> findByUserLogin(@Param("userLogin") String userLogin);

    @Query("SELECT al FROM AccountLoginEntity al WHERE al.userLogin = :userLogin AND al.accountEntity.id = :accountId")
    Optional<AccountLoginEntity> findByUserLoginAndAccountId(@Param("userLogin") String userLogin,
                                                              @Param("accountId") Long accountId);

    /**
     * Проверка существования записи для пользователя и клиента
     */
    @Query("SELECT COUNT(al) > 0 FROM AccountLoginEntity al WHERE al.userLogin = :userLogin AND al.clientEntity.id = :clientId")
    boolean existsByUserLoginAndClientId(@Param("userLogin") String userLogin, @Param("clientId") Long clientId);

}
