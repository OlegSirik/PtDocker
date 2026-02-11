package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.pt.product.entity.EntityDefEntity;

import java.util.List;
import java.util.Optional;

public interface EntityDefRepository extends JpaRepository<EntityDefEntity, Long> {
    List<EntityDefEntity> findByTidAndSectionId(Long tid, Long sectionId);
    Optional<EntityDefEntity> findBySectionIdAndCode(Long sectionId, String code);
    Optional<EntityDefEntity> findBySectionIdAndName(Long sectionId, String name);
    List<EntityDefEntity> findBySectionId(Long sectionId);
    
}
