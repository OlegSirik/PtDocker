package ru.pt.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.pt.db.entity.RefDictEntity;

import java.util.List;

public interface RefDictRepository extends JpaRepository<RefDictEntity, RefDictEntity.RefDictId> {

    List<RefDictEntity> findByTidOrderByCodeAsc(Long tid);

    boolean existsByTidAndCode(Long tid, String code);
}
