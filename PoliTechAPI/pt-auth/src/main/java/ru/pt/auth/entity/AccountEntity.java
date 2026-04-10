package ru.pt.auth.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "acc_accounts")
public class AccountEntity {
    @Id
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

    @Column(name = "id_path", length = 300)
    private String idPath;

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

    public String getIdPath() {
        return idPath;
    }

    public void setIdPath(String idPath) {
        this.idPath = idPath;
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
     * Creates a child account under the given parent. Uses parent's tenant and client.
     */
    private static AccountEntity newChildAccount(AccountEntity parent, AccountNodeType nodeType, String name) {
        String displayName = (name != null && !name.isBlank()) ? name : nodeType.name();
        return new AccountEntity(null, parent.getTenant(), parent.getClient(), parent, nodeType, displayName);
    }

    /**
     * Matrix of allowed child account types per parent type.
     * Defines which account types can be created under which parent.
     */
    private static final Map<AccountNodeType, Set<AccountNodeType>> ALLOWED_CHILD_TYPES = Map.of(
        AccountNodeType.TENANT, Set.of(AccountNodeType.CLIENT),
        AccountNodeType.CLIENT, Set.of(AccountNodeType.GROUP, AccountNodeType.ACCOUNT, AccountNodeType.SYS_ADMIN,
            AccountNodeType.TNT_ADMIN, AccountNodeType.GROUP_ADMIN, AccountNodeType.PRODUCT_ADMIN),
        AccountNodeType.GROUP, Set.of(AccountNodeType.GROUP, AccountNodeType.ACCOUNT, AccountNodeType.GROUP_ADMIN),
        AccountNodeType.ACCOUNT, Set.of(AccountNodeType.SUB),
        AccountNodeType.SUB, Collections.emptySet(),
        AccountNodeType.SYS_ADMIN, Collections.emptySet(),
        AccountNodeType.TNT_ADMIN, Collections.emptySet(),
        AccountNodeType.CLIENT_ADMIN, Collections.emptySet(),
        AccountNodeType.GROUP_ADMIN, Collections.emptySet(),
        AccountNodeType.PRODUCT_ADMIN, Collections.emptySet()
    );

    public static boolean nodeTypeHasChildren(AccountNodeType nodeType) {
        if (nodeType == AccountNodeType.ACCOUNT) return false;
        Set<AccountNodeType> allowed = ALLOWED_CHILD_TYPES.getOrDefault(nodeType, Collections.emptySet());
        return !allowed.isEmpty();
    }
    /**
     * Creates an account of the given type under the parent account.
     * Validates hierarchy via ALLOWED_CHILD_TYPES, then creates the entity.
     */
    public static AccountEntity createAccount(AccountEntity parentAccount, String name, AccountNodeType childType) {
        AccountNodeType parentType = parentAccount.getNodeType();
        Set<AccountNodeType> allowed = ALLOWED_CHILD_TYPES.getOrDefault(parentType, Collections.emptySet());
        if (!allowed.contains(childType)) {
            throw new IllegalArgumentException(
                "Account type " + childType + " cannot be created under " + parentType);
        }
        return switch (childType) {
            case TENANT -> throw new IllegalArgumentException("TENANT cannot be created as child; use tenantAccount(TenantEntity)");
            case CLIENT -> throw new IllegalArgumentException("Use clientAccount(ClientEntity, AccountEntity) for CLIENT");
            default -> newChildAccount(parentAccount, childType, name);
        };
    }

}