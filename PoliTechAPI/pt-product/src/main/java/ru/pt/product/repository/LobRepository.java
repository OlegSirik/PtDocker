package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.product.entity.LobEntity;

import java.util.List;
import java.util.Optional;

public interface LobRepository extends JpaRepository<LobEntity, Integer> {

    @Query("select l from LobEntity l where l.tId = :tId and l.code = :code and l.isDeleted = false")
    Optional<LobEntity> findByCode(@Param("tId") Long tId, @Param("code") String code);

    @Query("select l from LobEntity l where l.tId = :tId and l.id = :id and l.isDeleted = false")
    Optional<LobEntity> findById(@Param("tId") Long tId, @Param("id") Integer id);

    @Query("select l.id as id, l.code as code, l.name as name from LobEntity l where l.tId = :tId and l.isDeleted = false order by l.code")
    List<Object[]> listActiveSummaries(@Param("tId") Long tenantId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT nextval('pt_lobs_seq')", nativeQuery = true)
    Long nextLobId();
}
