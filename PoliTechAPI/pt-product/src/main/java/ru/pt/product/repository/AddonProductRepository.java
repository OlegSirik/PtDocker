package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.pt.product.entity.AddonProductEntity;

import java.util.List;
import java.util.Optional;

public interface AddonProductRepository extends JpaRepository<AddonProductEntity, Long> {

    @Query("select ap from AddonProductEntity ap where ap.tid = :tid and ap.productId = :productId")
    List<AddonProductEntity> findByTidAndProductId(@Param("tid") Long tid, @Param("productId") Integer productId);

    @Query("select ap from AddonProductEntity ap where ap.tid = :tid and ap.productId = :productId and ap.addonId = :addonId")
    Optional<AddonProductEntity> findByTidAndProductIdAndAddonId(
            @Param("tid") Long tid, @Param("productId") Integer productId, @Param("addonId") Long addonId);

    @Query("select ap from AddonProductEntity ap where ap.tid = :tid and ap.addonId = :addonId")
    List<AddonProductEntity> findByTidAndAddonId(@Param("tid") Long tid, @Param("addonId") Long addonId);
}
