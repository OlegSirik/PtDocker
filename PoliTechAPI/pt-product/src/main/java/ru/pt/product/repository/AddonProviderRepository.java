package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.pt.product.entity.AddonProviderEntity;

import java.util.List;
import java.util.Optional;

public interface AddonProviderRepository extends JpaRepository<AddonProviderEntity, Long> {

    List<AddonProviderEntity> findByTidOrderByName(Long tid);

    @Query("select p from AddonProviderEntity p where p.tid = :tid and p.id = :id")
    Optional<AddonProviderEntity> findByTidAndId(@Param("tid") Long tid, @Param("id") Long id);
}
