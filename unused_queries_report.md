# Отчет о неиспользуемых запросах (@Query) в репозиториях

## ClientRepository

### ❌ findByTenantAndName
**Файл:** `pt-auth/src/main/java/ru/pt/auth/repository/ClientRepository.java:29-30`
**Запрос:**
```java
@Query("SELECT c FROM ClientEntity c WHERE c.tenantEntity.id = :tenantCode AND c.name = :name AND c.isDeleted = false")
Optional<ClientEntity> findByTenantAndName(@Param("tenantCode") String tenantCode, @Param("name") String name);
```
**Статус:** Не используется нигде в коде

### ⚠️ findById
**Файл:** `pt-auth/src/main/java/ru/pt/auth/repository/ClientRepository.java:35-36`
**Запрос:**
```java
@Query("SELECT c FROM ClientEntity c WHERE c.id = :Id")
Optional<ClientEntity> findById(@Param("Id") Long id);
```
**Статус:** Переопределение стандартного метода JpaRepository. Используется стандартный `findById` из JpaRepository, а не этот с @Query

---

## AccountLoginRepository

### ❌ existsByUserLoginAndClientId
**Файл:** `pt-auth/src/main/java/ru/pt/auth/repository/AccountLoginRepository.java:39-40`
**Запрос:**
```java
@Query("SELECT COUNT(al) > 0 FROM AccountLoginEntity al WHERE al.userLogin = :userLogin AND al.clientEntity.id = :clientId")
boolean existsByUserLoginAndClientId(@Param("userLogin") String userLogin, @Param("clientId") Long clientId);
```
**Статус:** Закомментирован в `LoginManagementService.java:120`, не используется

### ❌ findByTenantAndUserRole
**Файл:** `pt-auth/src/main/java/ru/pt/auth/repository/AccountLoginRepository.java:42-43`
**Запрос:**
```java
@Query("SELECT al FROM AccountLoginEntity al WHERE al.tenantEntity.code = :tenantCode AND al.userRole = :userRole")
List<AccountLoginEntity> findByTenantAndUserRole(@Param("tenantCode") String tenantCode, @Param("userRole") String userRole);
```
**Статус:** Не используется (используется только `findByTenantAndUserRoleFull`)

---

## AccountRepository

### ❌ findByIdWithChildren
**Файл:** `pt-auth/src/main/java/ru/pt/auth/repository/AccountRepository.java:16-17`
**Запрос:**
```java
@Query("SELECT a FROM AccountEntity a LEFT JOIN FETCH a.children WHERE a.id = :id")
Optional<AccountEntity> findByIdWithChildren(Long id);
```
**Статус:** Не используется нигде в коде

### ❌ findByIdWithParent
**Файл:** `pt-auth/src/main/java/ru/pt/auth/repository/AccountRepository.java:22-23`
**Запрос:**
```java
@Query("SELECT a FROM AccountEntity a LEFT JOIN FETCH a.parent WHERE a.id = :id")
Optional<AccountEntity> findByIdWithParent(Long id);
```
**Статус:** Не используется нигде в коде

### ❌ findRootAccounts
**Файл:** `pt-auth/src/main/java/ru/pt/auth/repository/AccountRepository.java:28-29`
**Запрос:**
```java
@Query("SELECT a FROM AccountEntity a WHERE a.parent.id IS NULL ORDER BY a.name")
List<AccountEntity> findRootAccounts();
```
**Статус:** Не используется нигде в коде

### ❌ findByNameContainingIgnoreCase
**Файл:** `pt-auth/src/main/java/ru/pt/auth/repository/AccountRepository.java:46-47`
**Запрос:**
```java
@Query("SELECT a FROM AccountEntity a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY a.name")
List<AccountEntity> findByNameContainingIgnoreCase(String name);
```
**Статус:** Не используется нигде в коде

---

## ProductRepository

### ❌ listActive
**Файл:** `pt-product/src/main/java/ru/pt/product/repository/ProductRepository.java:16-17`
**Запрос:**
```java
@Query("select p from ProductEntity p where p.isDeleted = false order by p.code")
List<ProductEntity> listActive();
```
**Статус:** Не используется (используется только `listActiveSummaries`)

### ❌ getNextProductId
**Файл:** `pt-product/src/main/java/ru/pt/product/repository/ProductRepository.java:28-29`
**Запрос:**
```java
@Query(value = "SELECT nextval('pt_seq')", nativeQuery = true)
Integer getNextProductId();
```
**Статус:** Не используется нигде в коде

---

## TenantRepository

### ❌ findAllActive
**Файл:** `pt-auth/src/main/java/ru/pt/auth/repository/TenantRepository.java:28-29`
**Запрос:**
```java
@Query("SELECT t FROM TenantEntity t WHERE t.isDeleted = false ORDER BY t.name")
List<TenantEntity> findAllActive();
```
**Статус:** Не используется нигде в коде

### ❌ findAllWithDeleted
**Файл:** `pt-auth/src/main/java/ru/pt/auth/repository/TenantRepository.java:34-35`
**Запрос:**
```java
@Query("SELECT t FROM TenantEntity t ORDER BY t.name")
List<TenantEntity> findAllWithDeleted();
```
**Статус:** Не используется нигде в коде

---

## FileRepository

### ❌ findActiveByProductandFileType
**Файл:** `pt-files/src/main/java/ru/pt/files/repository/FileRepository.java:16-17`
**Запрос:**
```java
@Query("select f from FileEntity f where f.productCode = :productCode and f.fileType = :fileType and f.deleted = false")
FileEntity findActiveByProductandFileType(@Param("productCode") String productCode, @Param("fileType") String fileType);
```
**Статус:** Не используется нигде в коде

---

## ProductRoleRepository

### ❌ findByRoleProductId
**Файл:** `pt-auth/src/main/java/ru/pt/auth/repository/ProductRoleRepository.java:23-24`
**Запрос:**
```java
@Query("SELECT pr FROM ProductRoleEntity pr WHERE pr.roleProductId = :roleProductId ORDER BY pr.accountEntity.id")
List<ProductRoleEntity> findByRoleProductId(@Param("roleProductId") Long roleProductId);
```
**Статус:** Не используется нигде в коде

### ❌ findByRoleAccountId
**Файл:** `pt-auth/src/main/java/ru/pt/auth/repository/ProductRoleRepository.java:29-30`
**Запрос:**
```java
@Query("SELECT pr FROM ProductRoleEntity pr WHERE pr.roleAccountEntity.id = :roleAccountId ORDER BY pr.accountEntity.id, pr.roleAccountEntity.id")
List<ProductRoleEntity> findByRoleAccountId(@Param("roleAccountId") Long roleAccountId);
```
**Статус:** Не используется нигде в коде

### ❌ findByAccountIdAndCanReadTrue
**Файл:** `pt-auth/src/main/java/ru/pt/auth/repository/ProductRoleRepository.java:35-36`
**Запрос:**
```java
@Query("SELECT pr FROM ProductRoleEntity pr WHERE pr.accountEntity.id = :accountId AND pr.canRead = true ORDER BY pr.roleAccountEntity.id")
List<ProductRoleEntity> findByAccountIdAndCanReadTrue(@Param("accountId") Long accountId);
```
**Статус:** Не используется нигде в коде

### ❌ findByAccountIdAndCanQuoteTrue
**Файл:** `pt-auth/src/main/java/ru/pt/auth/repository/ProductRoleRepository.java:41-42`
**Запрос:**
```java
@Query("SELECT pr FROM ProductRoleEntity pr WHERE pr.accountEntity.id = :accountId AND pr.canQuote = true ORDER BY pr.roleAccountEntity.id")
List<ProductRoleEntity> findByAccountIdAndCanQuoteTrue(@Param("accountId") Long accountId);
```
**Статус:** Не используется нигде в коде

### ❌ findByAccountIdAndCanPolicyTrue
**Файл:** `pt-auth/src/main/java/ru/pt/auth/repository/ProductRoleRepository.java:47-48`
**Запрос:**
```java
@Query("SELECT pr FROM ProductRoleEntity pr WHERE pr.accountEntity.id = :accountId AND pr.canPolicy = true ORDER BY pr.roleAccountEntity.id")
List<ProductRoleEntity> findByAccountIdAndCanPolicyTrue(@Param("accountId") Long accountId);
```
**Статус:** Не используется нигде в коде

### ❌ existsByAccountIdAndRoleProductId
**Файл:** `pt-auth/src/main/java/ru/pt/auth/repository/ProductRoleRepository.java:53-54`
**Запрос:**
```java
@Query("SELECT COUNT(pr) > 0 FROM ProductRoleEntity pr WHERE pr.accountEntity.id = :accountId AND pr.roleProductId = :roleProductId")
boolean existsByAccountIdAndRoleProductId(@Param("accountId") Long accountId, @Param("roleProductId") Long roleProductId);
```
**Статус:** Не используется нигде в коде

### ❌ getNextProductRoleId
**Файл:** `pt-auth/src/main/java/ru/pt/auth/repository/ProductRoleRepository.java:59-60`
**Запрос:**
```java
@Query(value = "SELECT nextval('account_seq')", nativeQuery = true)
Long getNextProductRoleId();
```
**Статус:** Не используется нигде в коде

---

## Итого

**Всего неиспользуемых методов с @Query: 21**

### По репозиториям:
- **ClientRepository**: 2 метода (1 неиспользуемый + 1 переопределение стандартного)
- **AccountLoginRepository**: 2 метода
- **AccountRepository**: 4 метода
- **ProductRepository**: 2 метода
- **TenantRepository**: 2 метода
- **FileRepository**: 1 метод
- **ProductRoleRepository**: 7 методов

### Рекомендации:
1. Удалить неиспользуемые методы для упрощения кода
2. Проверить `ClientRepository.findById` - возможно, стоит удалить @Query и использовать стандартный метод JpaRepository
3. Проверить закомментированный `existsByUserLoginAndClientId` - удалить, если не планируется использование
