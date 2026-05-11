package ru.pt.db.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pt_installment_templates")
public class InstallmentTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pt_installment_templates_seq")
    @SequenceGenerator(name = "pt_installment_templates_seq", sequenceName = "pt_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private Long tid;

    @Column(name = "installment_type", nullable = false, length = 30)
    private String installmentType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "installment_template", columnDefinition = "jsonb", nullable = false)
    private List<InstallmentTemplateLine> installmentTemplate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public String getInstallmentType() {
        return installmentType;
    }

    public void setInstallmentType(String installmentType) {
        this.installmentType = installmentType;
    }

    public List<InstallmentTemplateLine> getInstallmentTemplate() {
        return installmentTemplate;
    }

    public void setInstallmentTemplate(List<InstallmentTemplateLine> installmentTemplate) {
        this.installmentTemplate = installmentTemplate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
