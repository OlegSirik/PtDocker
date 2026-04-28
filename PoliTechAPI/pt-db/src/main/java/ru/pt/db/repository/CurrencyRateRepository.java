package ru.pt.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.pt.db.entity.CurrencyRateEntity;

import java.time.LocalDate;
import java.util.List;

public interface CurrencyRateRepository extends JpaRepository<CurrencyRateEntity, Long> {

    List<CurrencyRateEntity> findByFromCurrencyIgnoreCaseAndToCurrencyIgnoreCaseOrderByValidFromDesc(
            String fromCurrency,
            String toCurrency
    );

    @Query("""
            select c
            from CurrencyRateEntity c
            where upper(c.fromCurrency) = upper(:fromCurrency)
              and upper(c.toCurrency) = upper(:toCurrency)
              and (c.validFrom is null or c.validFrom <= :date)
              and (c.validTo is null or c.validTo >= :date)
            order by c.validFrom desc, c.id desc
            """)
    List<CurrencyRateEntity> findByPairAndDate(
            String fromCurrency,
            String toCurrency,
            LocalDate date
    );
}
