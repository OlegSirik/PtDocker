package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.product.entity.InsuranceCompanyEntity;

import java.util.List;
import java.util.Optional;

public interface InsuranceCompanyRepository extends JpaRepository<InsuranceCompanyEntity, Long> {

    @Query("select e from InsuranceCompanyEntity e where e.tid = :tid and e.id = :id")
    Optional<InsuranceCompanyEntity> findByTidAndId(@Param("tid") Long tid, @Param("id") Long id);

    List<InsuranceCompanyEntity> findByTidOrderByCodeAsc(Long tid);

    boolean existsByTidAndCode(Long tid, String code);
}
