# Tenant ID Audit Report

## Entities WITHOUT Tenant ID (tid/tId) Field

### ❌ Critical - Missing Tenant ID Field

1. **ProductVersionEntity** (`pt-product`)
   - Table: `pt_product_versions`
   - Status: ❌ NO `tid` field
   - Note: References `ProductEntity` via `productId`, but doesn't have direct tenant ID

2. **NumberGeneratorEntity** (`pt-numbers`)
   - Table: `pt_number_generators`
   - Status: ❌ NO `tid` field
   - Fields: id, product_code, mask, reset_policy, max_value, last_reset, current_value, xor_mask

3. **PolicyEntity** (`pt-db`)
   - Table: `policy_data`
   - Status: ❌ NO `tid` field
   - Fields: id (UUID), policy (JSONB)

4. **PolicyIndexEntity** (`pt-db`)
   - Table: `policy_index`
   - Status: ⚠️ NO direct `tid` field
   - Has: `user_account_id`, `client_account_id` (indirect tenant relationship via accounts)

### ✅ Entities WITH Tenant ID Field

1. **ProductEntity** - ✅ Has `tId` (Long)
2. **LobEntity** - ✅ Has `tId` (Long)
3. **CalculatorEntity** - ✅ Has `tId` (Long)
4. **CoefficientDataEntity** - ✅ Has `tId` (Long)
5. **FileEntity** - ✅ Has `tid` (Long)

---

## Repositories WITHOUT Tenant Filtering

### ❌ Critical - No Tenant Filtering in Queries

1. **ProductVersionRepository**
   - Status: ⚠️ PARTIALLY FIXED
   - Methods: `findByProductIdAndVersionNo`, `deleteByProductIdAndVersionNo`
   - Note: Recently updated to join with ProductEntity for tenant filtering

2. **NumberGeneratorRepository**
   - Status: ⚠️ PARTIALLY FIXED
   - Method: `findByProductCode` - has `tId` parameter but entity doesn't have field
   - Issue: Entity `NumberGeneratorEntity` doesn't have `tid` field, but repository query expects it

3. **PolicyRepository**
   - Status: ❌ NO tenant filtering
   - Methods: Only standard JpaRepository methods
   - Issue: `PolicyEntity` doesn't have tenant ID field

4. **PolicyIndexRepository**
   - Status: ⚠️ Uses account-based filtering instead of direct tenant
   - Methods: Filter by `user_account_id` and `client_account_id`
   - Note: Indirect tenant relationship through accounts

### ✅ Repositories WITH Tenant Filtering

1. **ProductRepository** - ✅ All queries filter by `tId`
2. **LobRepository** - ✅ All queries filter by `tId`
3. **CalculatorRepository** - ✅ Query filters by `tId`
4. **CoefficientDataRepository** - ✅ Queries filter by `tId`
5. **FileRepository** - ✅ All queries filter by `tId` (recently fixed)

---

## Save Operations WITHOUT Setting Tenant ID

### ❌ Critical - Missing Tenant ID on Save

1. **FileServiceImpl.createMeta()** (Line 36-43)
   ```java
   FileEntity e = new FileEntity();
   e.setFileType(fileType);
   e.setFileDesc(fileDesc);
   e.setProductCode(productCode);
   e.setPackageCode(packageCode);
   e.setDeleted(false);
   // ❌ MISSING: e.setTid(getCurrentTenantId());
   var saved = fileRepository.save(e);
   ```

2. **FileServiceImpl.uploadBody()** (Line 56-60)
   - Updates existing entity, but should verify tenant matches
   - Uses `findActiveById()` which requires tenant ID parameter

3. **FileServiceImpl.softDelete()** (Line 102-106)
   - Updates existing entity, tenant should be verified via repository query

4. **ProductServiceImpl.create()** - ProductVersionEntity save (Line 133-171)
   ```java
   ProductVersionEntity pv = new ProductVersionEntity();
   pv.setProductId(saved.getId());
   pv.setVersionNo(productVersionModel.getVersionNo());
   // ❌ MISSING: ProductVersionEntity doesn't have tid field
   productVersionRepository.save(pv);
   ```
   - Note: `ProductVersionEntity` doesn't have tenant ID field

5. **ProductServiceImpl.createVersionFrom()** (Line 231-239)
   ```java
   ProductVersionEntity pv = new ProductVersionEntity();
   pv.setProductId(id);
   pv.setVersionNo(newVersion);
   // ❌ MISSING: ProductVersionEntity doesn't have tid field
   productVersionRepository.save(pv);
   ```

6. **ProductServiceImpl.updateVersion()** (Line 258-269)
   - Updates existing ProductVersionEntity, tenant verified via repository query

7. **DatabaseNumberGeneratorService.create()** (Line 91-93)
   ```java
   var entity = mapper.toEntity(numberGeneratorDescription);
   // ❌ MISSING: NumberGeneratorEntity doesn't have tid field
   repository.save(entity);
   ```

8. **DatabaseNumberGeneratorService.update()** (Line 97-108)
   - Updates existing entity, but entity doesn't have tenant ID field

9. **DatabaseNumberGeneratorService.getNext()** (Line 111-138)
   - Updates existing entity, but entity doesn't have tenant ID field

10. **DbStorageService.save()** - PolicyEntity and PolicyIndexEntity (Line 47-66)
    ```java
    var entity = policyMapper.policyEntityFromDTO(policy, userData);
    // ❌ MISSING: PolicyEntity doesn't have tid field
    policyRepository.save(entity);
    
    var index = policyMapper.policyIndexFromDTO(policy, userData);
    // ⚠️ PolicyIndexEntity uses account IDs, not direct tenant
    policyIndexRepository.save(index);
    ```

11. **DbStorageService.save()** - Overload (Line 71-84)
    ```java
    var entity = new PolicyEntity();
    entity.setId(uuid);
    entity.setPolicy(policy);
    // ❌ MISSING: PolicyEntity doesn't have tid field
    policyRepository.save(entity);
    ```

12. **DbStorageService.update()** (Line 97-102)
    ```java
    PolicyEntity policyEntity = new PolicyEntity();
    policyEntity.setPolicy(policyData.getPolicy());
    policyEntity.setId(policyData.getPolicyId());
    // ❌ MISSING: PolicyEntity doesn't have tid field
    policyRepository.save(policyEntity);
    ```

### ✅ Save Operations WITH Tenant ID Set

1. **ProductServiceImpl.create()** - ProductEntity ✅
   ```java
   product.setTId(getCurrentTenantId());
   ```

2. **LobServiceImpl.create()** ✅
   ```java
   lob.setTid(getCurrentTenantId());
   ```

3. **CalculatorServiceImpl.createCalculatorIfMissing()** ✅
   ```java
   e.setTId(getCurrentTenantId());
   ```

4. **CoefficientServiceImpl.insert()** ✅
   ```java
   entity.setTId(getCurrentTenantId());
   ```

5. **FileServiceImpl.uploadFile()** ✅
   ```java
   entity.setTid(tid);
   ```

---

## Summary and Recommendations

### Immediate Actions Required

1. **Add `tid` field to entities:**
   - `ProductVersionEntity` - Add `tid` column and field
   - `NumberGeneratorEntity` - Add `tid` column and field
   - `PolicyEntity` - Add `tid` column and field (or use account-based filtering)
   - `PolicyIndexEntity` - Consider adding direct `tid` field (currently uses account IDs)

2. **Fix save operations:**
   - `FileServiceImpl.createMeta()` - Add `e.setTid(getCurrentTenantId())`
   - All `ProductVersionEntity` saves - Add tenant ID after adding field to entity
   - All `NumberGeneratorEntity` saves - Add tenant ID after adding field to entity
   - All `PolicyEntity` saves - Add tenant ID after adding field to entity

3. **Update repository queries:**
   - `NumberGeneratorRepository.findByProductCode()` - Entity needs `tid` field first
   - `PolicyRepository` - Add tenant filtering after adding field to entity
   - `PolicyIndexRepository` - Consider adding direct tenant filtering

### Database Migration Required

1. Add `tid` column to `pt_product_versions` table
2. Add `tid` column to `pt_number_generators` table
3. Add `tid` column to `policy_data` table
4. Consider adding `tid` column to `policy_index` table (or keep account-based filtering)

### Files Requiring Updates

1. `PoliTechAPI/pt-files/src/main/java/ru/pt/files/service/FileServiceImpl.java`
   - Line 36-43: Add tenant ID to `createMeta()`

2. `PoliTechAPI/pt-product/src/main/java/ru/pt/product/entity/ProductVersionEntity.java`
   - Add `tid` field

3. `PoliTechAPI/pt-numbers/src/main/java/ru/pt/numbers/entity/NumberGeneratorEntity.java`
   - Add `tid` field

4. `PoliTechAPI/pt-db/src/main/java/ru/pt/db/entity/PolicyEntity.java`
   - Add `tid` field

5. `PoliTechAPI/pt-db/src/main/java/ru/pt/db/entity/PolicyIndexEntity.java`
   - Consider adding `tid` field

6. All service classes that save these entities need to set tenant ID

