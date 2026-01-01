package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.product.entity.ProductEntity;
import ru.pt.product.entity.ProductVersionEntity;

import java.util.Optional;

public interface ProductVersionRepository extends JpaRepository<ProductVersionEntity, Integer> {

    @Query("select pv from ProductVersionEntity pv join ProductEntity p on pv.productId = p.id where pv.productId = :productId and pv.versionNo = :versionNo and p.tId = :tId")
    Optional<ProductVersionEntity> findByProductIdAndVersionNo(@Param("tId") Long tId,
                                                               @Param("productId") Integer productId,
                                                               @Param("versionNo") Integer versionNo);

    @Modifying
    @Query("delete from ProductVersionEntity pv where pv.productId = :productId and pv.versionNo = :versionNo and exists (select 1 from ProductEntity p where p.id = pv.productId and p.tId = :tId)")
    int deleteByProductIdAndVersionNo(@Param("tId") Long tId,
                                      @Param("productId") Integer productId,
                                      @Param("versionNo") Integer versionNo);
}
