package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.pt.product.entity.ContractModelEntity;

import java.util.List;
import java.util.Optional;

public interface ContractModelRepository extends JpaRepository<ContractModelEntity, Long> {
    List<ContractModelEntity> findByTid(Long tid);
    Optional<ContractModelEntity> findByTidAndCode(Long tid, String code);
    Optional<ContractModelEntity> findByTidAndName(Long tid, String name);
}
