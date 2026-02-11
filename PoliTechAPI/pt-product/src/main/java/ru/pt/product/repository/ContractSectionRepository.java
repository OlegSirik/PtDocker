package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.pt.product.entity.ContractSectionEntity;

import java.util.List;
import java.util.Optional;

public interface ContractSectionRepository extends JpaRepository<ContractSectionEntity, Long> {
    List<ContractSectionEntity> findByTidAndModelId(Long tid, Long modelId);
    Optional<ContractSectionEntity> findByModelIdAndCode(Long modelId, String code);
    Optional<ContractSectionEntity> findByModelIdAndName(Long modelId, String name);
    List<ContractSectionEntity> findByModelId(Long modelId);
}
