package ru.pt.auth.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "acc_accounts")
public class AccountEntity {
    @Id
    //@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_seq")
    //@SequenceGenerator(name = "account_seq", sequenceName = "account_seq", allocationSize = 1)
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

    // constructors, getters, setters
    public AccountEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TenantEntity getTenant() {
        return tenantEntity;
    }

    public void setTenant(TenantEntity tenantEntity) {
        this.tenantEntity = tenantEntity;
    }

    public ClientEntity getClient() {
        return clientEntity;
    }

    public void setClient(ClientEntity clientEntity) {
        this.clientEntity = clientEntity;
    }

    public AccountEntity getParent() {
        return parent;
    }

    public void setParent(AccountEntity parent) {
        this.parent = parent;
    }

    public List<AccountEntity> getChildren() {
        return children;
    }

    public void setChildren(List<AccountEntity> children) {
        this.children = children;
    }

    public AccountNodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(AccountNodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<ProductRoleEntity> getProductRoles() {
        return productRoleEntities;
    }

    public void setProductRoles(List<ProductRoleEntity> productRoleEntities) {
        this.productRoleEntities = productRoleEntities;
    }

    public List<AccountLoginEntity> getAccountLogins() {
        return accountLoginEntities;
    }

    public void setAccountLogins(List<AccountLoginEntity> accountLoginEntities) {
        this.accountLoginEntities = accountLoginEntities;
    }

    public List<AccountTokenEntity> getAccountTokens() {
        return accountTokenEntities;
    }

    public void setAccountTokens(List<AccountTokenEntity> accountTokenEntities) {
        this.accountTokenEntities = accountTokenEntities;
    }

    // --- Конструктор для фабрик ---
    private AccountEntity(Long id,TenantEntity tenant, ClientEntity client, AccountEntity parent,
        AccountNodeType nodeType, String name) {
        this.id = id;
        this.tenantEntity = tenant;
        this.clientEntity = client;
        this.parent = parent;
        this.nodeType = nodeType;
        this.name = name;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.productRoleEntities = new ArrayList<>();
        this.accountLoginEntities = new ArrayList<>();
        this.accountTokenEntities = new ArrayList<>();
    }


    /**
    * Создаёт корневой аккаунт тенанта
    */
    public static AccountEntity tenantAccount(TenantEntity tenant) {
        return new AccountEntity(tenant.getId(), tenant, null, null, AccountNodeType.TENANT, tenant.getName());
    }

    /**
    * Создаёт аккаунт клиента под тенантом
    */
    public static AccountEntity clientAccount(ClientEntity client, AccountEntity parentAccount) {
        if (parentAccount.getNodeType() != AccountNodeType.TENANT) {
            throw new IllegalArgumentException(
                "Parent account must be of type TENANT, but was " + parentAccount.getNodeType()
            );
        }
        return new AccountEntity(
            client.getId(),
            parentAccount.getTenant(),
            client,          // клиент копируем из параметра
            parentAccount,
            AccountNodeType.CLIENT,
            client.getName()
        );
    }

    /**
    * Создаёт группу под клиентом
    */
    public static AccountEntity groupAccount(AccountEntity parentAccount, String name) {
    if (parentAccount.getNodeType() != AccountNodeType.CLIENT) {
            throw new IllegalArgumentException(
                "Parent account must be of type CLIENT, but was " + parentAccount.getNodeType()
            );
        }
        return new AccountEntity(
            null,
            parentAccount.getTenant(),
            parentAccount.getClient(),  // копируем клиента с родителя
            parentAccount,
            AccountNodeType.GROUP,
            name
        );
    }

    /**
    * Создаёт стандартный аккаунт под клиентом (например, default)
    */
    public static AccountEntity defaultClientAccount(AccountEntity parentAccount) {
        if (parentAccount.getNodeType() != AccountNodeType.CLIENT) {
            throw new IllegalArgumentException(
                "Parent account must be of type CLIENT, but was " + parentAccount.getNodeType()
            );
        }

        return new AccountEntity(
            null,
            parentAccount.getTenant(),
            parentAccount.getClient(),
            parentAccount,
            AccountNodeType.ACCOUNT,
            "Default account for client"
        );
    }

    public static AccountEntity sysAdminAccount(AccountEntity parentAccount) {
        if (parentAccount.getNodeType() != AccountNodeType.CLIENT) {
            throw new IllegalArgumentException(
                "Parent account must be of type CLIENT, but was " + parentAccount.getNodeType()
            );
        }
        return new AccountEntity(
            null,
            parentAccount.getTenant(),
            parentAccount.getClient(),
            parentAccount,
            AccountNodeType.SYS_ADMIN,
        "SYS_ADMIN"
        );
    }

    public static AccountEntity tntAdminAccount(AccountEntity parentAccount) {
        if (parentAccount.getNodeType() != AccountNodeType.CLIENT) {
            throw new IllegalArgumentException(
                "Parent account must be of type CLIENT, but was " + parentAccount.getNodeType()
            );
        }
        return new AccountEntity(
            null,
            parentAccount.getTenant(),
            parentAccount.getClient(),
            parentAccount,
            AccountNodeType.TNT_ADMIN,
            "TNT_ADMIN"
        );
    }

    public static AccountEntity productAdminAccount(AccountEntity parentAccount) {
        if (parentAccount.getNodeType() != AccountNodeType.CLIENT) {
            throw new IllegalArgumentException(
                "Parent account must be of type CLIENT, but was " + parentAccount.getNodeType()
            );
        }
        return new AccountEntity(
            null,
            parentAccount.getTenant(),
            parentAccount.getClient(),
            parentAccount,
            AccountNodeType.PRODUCT_ADMIN,
            "PRODUCT_ADMIN"
        );
    }

    public static AccountEntity groupAdminAccount(AccountEntity parentAccount) {
        if (parentAccount.getNodeType() != AccountNodeType.CLIENT) {
            throw new IllegalArgumentException(
                "Parent account must be of type CLIENT, but was " + parentAccount.getNodeType()
            );
        }
        return new AccountEntity(
            null,
            parentAccount.getTenant(),
            parentAccount.getClient(),
            parentAccount,
            AccountNodeType.GROUP_ADMIN,
            "GROUP_ADMIN"
        );
    }

}