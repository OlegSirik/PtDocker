package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.pt.product.entity.ProductVersionEntity;

import java.util.Optional;

public interface ProductVersionRepository extends JpaRepository<ProductVersionEntity, Long> {

    @Query("select pv from ProductVersionEntity pv join ProductEntity p on pv.productId = p.id where pv.productId = :productId and pv.versionNo = :versionNo and p.tId = :tId")
    Optional<ProductVersionEntity> findByProductIdAndVersionNo(@Param("tId") Long tId,
                                                               @Param("productId") Long productId,
                                                               @Param("versionNo") Long versionNo);

    @Modifying(clearAutomatically = true)
    @Query("delete from ProductVersionEntity pv where pv.productId = :productId and pv.versionNo = :versionNo and pv.tid = :tId")
    Long deleteByProductIdAndVersionNo(@Param("tId") Long tId,
                                      @Param("productId") Long productId,
                                      @Param("versionNo") Long versionNo);

    @Query(value = "SELECT nextval('pt_seq')", nativeQuery = true)
    Long nextId();
}
