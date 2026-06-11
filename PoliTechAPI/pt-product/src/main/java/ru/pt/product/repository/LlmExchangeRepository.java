package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.pt.product.entity.LlmExchangeEntity;

public interface LlmExchangeRepository extends JpaRepository<LlmExchangeEntity, Long> {
}
