package ru.pt.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.pt.db.entity.RefDataEntity;

import java.util.List;

public interface RefDataRepository extends JpaRepository<RefDataEntity, RefDataEntity.RefDataId> {

    List<RefDataEntity> findByTidAndRefCodeOrderByMdCodeAsc(Long tid, String refCode);

    boolean existsByTidAndRefCodeAndMdCode(Long tid, String refCode, String mdCode);
}
