package ru.pt.product.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "acc_accounts")
public class AccountEntity {
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
    @JoinColumn(name = "parent_id")
    private AccountEntity parent;

    @OneToMany(mappedBy = "parent")
    private List<AccountEntity> children = new ArrayList<>();

    @Column(name = "node_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private AccountNodeType nodeType;

    @Column(name = "name", length = 250)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "accountEntity", cascade = CascadeType.ALL)
    private List<ProductRoleEntity> productRoleEntities = new ArrayList<>();

    @OneToMany(mappedBy = "accountEntity", cascade = CascadeType.ALL)
    private List<AccountLoginEntity> accountLoginEntities = new ArrayList<>();

    @OneToMany(mappedBy = "accountEntity", cascade = CascadeType.ALL)
    private List<AccountTokenEntity> accountTokenEntities = new ArrayList<>();
}
