package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.pt.product.entity.ProductTestEntity;
import ru.pt.product.entity.ProductTestId;

import java.util.Optional;

public interface ProductTestRepository extends JpaRepository<ProductTestEntity, ProductTestId> {

    Optional<ProductTestEntity> findByProductIdAndVersionNo(Integer productId, Integer versionNo);
}
