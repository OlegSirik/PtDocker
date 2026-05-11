package ru.pt.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.pt.db.entity.InstallmentTemplateEntity;

import java.util.Optional;

public interface InstallmentTemplateRepository extends JpaRepository<InstallmentTemplateEntity, Long> {

    Optional<InstallmentTemplateEntity> findByTidAndInstallmentTypeIgnoreCase(Long tid, String installmentType);
}
