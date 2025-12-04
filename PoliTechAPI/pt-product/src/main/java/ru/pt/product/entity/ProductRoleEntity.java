package ru.pt.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "acc_product_roles",
        uniqueConstraints = @UniqueConstraint(name = "acc_product_roles_uk",
                columnNames = {"account_id", "role_product_id", "role_account_id"}))
public class ProductRoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_seq")
    @SequenceGenerator(name = "account_seq", sequenceName = "account_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tid", nullable = false)
    private TenantEntity tenantEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private ClientEntity clientEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity accountEntity;

    @Column(name = "role_product_id", nullable = false)
    private Long roleProductId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_account_id", nullable = false)
    private AccountEntity roleAccountEntity;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "can_read")
    private Boolean canRead = false;

    @Column(name = "can_quote")
    private Boolean canQuote = false;

    @Column(name = "can_policy")
    private Boolean canPolicy = false;

    @Column(name = "can_addendum")
    private Boolean canAddendum = false;

    @Column(name = "can_cancel")
    private Boolean canCancel = false;

    @Column(name = "can_prolongate")
    private Boolean canProlongate = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
