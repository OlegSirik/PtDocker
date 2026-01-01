package ru.pt.calculator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.calculator.entity.CoefficientDataEntity;

import java.util.List;

public interface CoefficientDataRepository extends JpaRepository<CoefficientDataEntity, Integer> {

    @Query("select c from CoefficientDataEntity c where c.tId = :tId and c.calculatorId = :calculatorId and c.coefficientCode = :code order by c.col1, c.col2, c.col3, c.col4, c.col5")
    List<CoefficientDataEntity> findAllByCalcAndCode(
        @Param("tId") Long tId,
        @Param("calculatorId") Integer calculatorId,
        @Param("code") String code);

    @Modifying
    @Query("delete from CoefficientDataEntity c where c.tId = :tId and c.calculatorId = :calculatorId and c.coefficientCode = :code")
    int deleteAllByCalcAndCode(
        @Param("tId") Long tId,
        @Param("calculatorId") Integer calculatorId,
        @Param("code") String code);
}