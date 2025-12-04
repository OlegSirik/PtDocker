package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pt.product.entity.ProductRoleEntity;

@Repository
public interface ProductRoleRepository extends JpaRepository<ProductRoleEntity, Integer> {
}
