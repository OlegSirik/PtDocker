package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.pt.product.entity.AddonPricelistEntity;

import java.util.List;
import java.util.Optional;

public interface AddonPricelistRepository extends JpaRepository<AddonPricelistEntity, Long> {

    List<AddonPricelistEntity> findByTidOrderByCode(Long tid);

    List<AddonPricelistEntity> findByTidAndProviderIdOrderByCode(Long tid, Long providerId);

    @Query("select p from AddonPricelistEntity p where p.tid = :tid and p.id = :id")
    Optional<AddonPricelistEntity> findByTidAndId(@Param("tid") Long tid, @Param("id") Long id);

    @Query("""
        select p from AddonPricelistEntity p
        where p.tid = :tid and p.status = 'ACTIVE' and p.amountFree > 0
        order by p.code, p.price
        """)
    List<AddonPricelistEntity> findAvailableByTid(@Param("tid") Long tid);
}
