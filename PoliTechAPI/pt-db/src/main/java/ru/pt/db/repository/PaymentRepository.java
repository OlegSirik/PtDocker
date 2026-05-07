package ru.pt.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.pt.db.entity.PaymentEntity;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
}

