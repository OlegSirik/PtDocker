package ru.pt.files.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.files.entity.FileEntity;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findByPublicId(String publicId);

    @Query("select f from FileEntity f where f.tid = :tId and f.id = :id")
    Optional<FileEntity> findActiveById(@Param("tId") Long tId, @Param("id") Long id);

    @Query("select f from FileEntity f where f.tid = :tId order by f.id")
    List<FileEntity> listByTenant(@Param("tId") Long tId);
}