package ru.pt.numbers.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.numbers.entity.NumberGeneratorEntity;

import java.util.Optional;

public interface NumberGeneratorRepository extends JpaRepository<NumberGeneratorEntity, Integer>, NumberGeneratorRepositoryCustom {

    @Query("select ng from NumberGeneratorEntity ng where ng.tid = :tId and ng.productCode = :productCode")
    Optional<NumberGeneratorEntity> findByProductCode(@Param("tId") Long tId, @Param("productCode") String productCode);

    @Query("select ng from NumberGeneratorEntity ng where ng.tid = :tId and ng.id = :id")
    Optional<NumberGeneratorEntity> findByTidAndId(@Param("tId") Long tId, @Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ng from NumberGeneratorEntity ng where ng.tid = :tId and ng.id = :id")
    Optional<NumberGeneratorEntity> findByTidAndIdForUpdate(@Param("tId") Long tId, @Param("id") Integer id);
}