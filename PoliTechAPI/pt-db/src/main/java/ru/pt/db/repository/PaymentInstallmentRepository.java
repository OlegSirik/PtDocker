package ru.pt.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.pt.db.entity.PaymentInstallmentEntity;

import java.util.List;

public interface PaymentInstallmentRepository extends JpaRepository<PaymentInstallmentEntity, Long> {
    List<PaymentInstallmentEntity> findByTidAndPolicyIdOrderByInstallmentNrAsc(Long tid, Long policyId);
}

