package ru.pt.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.db.entity.PaymentAllocationEntity;

import java.math.BigDecimal;

public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocationEntity, Long> {
    @Query("""
            select coalesce(sum(a.allocatedAmount), 0)
            from PaymentAllocationEntity a
            where a.tid = :tid and a.installmentId = :installmentId
            """)
    BigDecimal getAllocatedAmount(@Param("tid") Long tid, @Param("installmentId") Long installmentId);
}

