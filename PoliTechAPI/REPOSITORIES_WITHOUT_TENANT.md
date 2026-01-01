# Repositories without tenant/tid

## Репозитории, которые НЕ используют tenant/tid в запросах:

### 1. ProductRepository
- **Entity**: `ProductEntity`
- **Table**: `pt_products`
- **Status**: ❌ Нет tid в таблице и репозитории
- **Methods**: `findByIdAndIsDeletedFalse`, `findByCodeAndIsDeletedFalse`, `listActiveSummaries`, `findProductIdEntityByAccountId`

### 2. ProductVersionRepository
- **Entity**: `ProductVersionEntity`
- **Table**: `pt_product_versions`
- **Status**: ❌ Нет tid в таблице и репозитории
- **Methods**: `findByProductIdAndVersionNo`, `deleteByProductIdAndVersionNo`

### 3. LobRepository
- **Entity**: `LobEntity`
- **Table**: `pt_lobs`
- **Status**: ❌ Нет tid в таблице и репозитории
- **Methods**: `findByCodeAndIsDeletedFalse`, `listActiveSummaries`, `nextLobId`, `findByIdAndIsDeletedFalse`

### 4. CalculatorRepository
- **Entity**: `CalculatorEntity`
- **Table**: `pt_calculators`
- **Status**: ❌ Нет tid в таблице и репозитории
- **Methods**: `findByKeys`

### 5. CoefficientDataRepository
- **Entity**: `CoefficientDataEntity`
- **Table**: `coefficient_data`
- **Status**: ❌ Нет tid в таблице и репозитории
- **Methods**: `findAllByCalcAndCode`, `deleteAllByCalcAndCode`

### 6. PolicyRepository
- **Entity**: `PolicyEntity`
- **Table**: `policy_data`
- **Status**: ❌ Нет tid в таблице и репозитории
- **Methods**: Только стандартные методы JpaRepository

### 7. PolicyIndexRepository
- **Entity**: `PolicyIndexEntity`
- **Table**: `policy_index`
- **Status**: ⚠️ Нет tid в таблице, но есть `user_account_id` и `client_account_id` (косвенная связь через accounts)
- **Methods**: `findPolicyIndexEntityByPolicyNumber`, `findAllByClientAccountIdAndUserAccountId`, `findByPolicyNumber`, `findByPaymentOrderId`, `findPoliciesByAccountIdRecursive`

### 8. NumberGeneratorRepository
- **Entity**: `NumberGeneratorEntity`
- **Table**: `pt_number_generators`
- **Status**: ❌ Нет tid в таблице и репозитории
- **Methods**: `findByProductCode`

### 9. FileRepository ⚠️
- **Entity**: `FileEntity` (имеет поле `tid`)
- **Table**: `pt_files` (имеет колонку `tid`)
- **Status**: ⚠️ tid добавлен в таблицу (V21), но НЕ используется в @Query методах
- **Methods**: `findActiveById`, `listSummaries`, `findActiveByFileTypeAndProductCodeAndPackageCode` - все БЕЗ фильтрации по tid

## Репозитории, которые ИСПОЛЬЗУЮТ tenant/tid:

### ✅ AccountRepository - использует tenant
### ✅ AccountLoginRepository - использует tenant
### ✅ AccountTokenRepository - использует tenant
### ✅ ClientRepository - использует tenant
### ✅ LoginRepository - использует tenant
### ✅ ProductRoleRepository - использует tenant
### ✅ TenantRepository - сам tenant

## Рекомендации:

Репозитории без tenant/tid могут быть проблемой для мультитенантности, если данные должны быть изолированы по tenant. 

### Критические (нет tid в таблице):
- `pt_products` → добавить tid
- `pt_product_versions` → добавить tid
- `pt_lobs` → добавить tid
- `pt_calculators` → добавить tid
- `coefficient_data` → добавить tid
- `pt_number_generators` → добавить tid
- `policy_data` и `policy_index` → добавить tid или использовать фильтрацию через account_id

### Требует исправления (tid есть, но не используется):
- `FileRepository` → добавить фильтрацию по tid во все методы с @Query
