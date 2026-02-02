package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.pt.product.entity.MetadataEntity;

import java.util.List;

/**
 * Repository for pt_metadata table
 */
@Repository
public interface MetadataRepository extends JpaRepository<MetadataEntity, String> {

    /**
     * Get all metadata records ordered by nr
     */
    @Query("SELECT m FROM MetadataEntity m ORDER BY m.nr")
    List<MetadataEntity> findAllOrderByNr();
}
