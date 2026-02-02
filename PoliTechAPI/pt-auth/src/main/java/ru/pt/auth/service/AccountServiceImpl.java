package ru.pt.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.pt.api.dto.auth.Account;
import ru.pt.api.dto.auth.ProductRole;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.exception.UnauthorizedException;
import ru.pt.api.service.auth.AccountService;
import ru.pt.auth.entity.AccountEntity;
import ru.pt.auth.entity.ProductRoleEntity;
import ru.pt.auth.repository.AccountRepository;
import ru.pt.auth.repository.ProductRoleRepository;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.auth.security.permitions.AuthorizationService;
import ru.pt.auth.security.permitions.AuthZ;
import ru.pt.auth.utils.AccountMapper;
import ru.pt.auth.utils.ProductRoleMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic service for account management.
 * Contains authorization checks before operations.
 * Uses AccountDataService for pure data access.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ProductRoleRepository productRoleRepository;
    private final AccountMapper accountMapper;
    private final ProductRoleMapper productRoleMapper;
    private final AuthorizationService authService;
    private final SecurityContextHelper securityContextHelper;
    private final AccountDataService accountDataService;
    

    @Override
    public Account getAccountById(Long id) {

        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT, 
            id.toString(),   // resourceId 
            id,  // resourceAccountid
            AuthZ.Action.VIEW);

        return accountRepository.findById(id)
                .map(accountMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }

    @Override
    @Transactional
    public Account createGroup(String name, Long parentId) {
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT, 
            parentId.toString(),
            parentId,
            AuthZ.Action.CREATE);

        AccountEntity parentAccount = accountRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent account not found: " + parentId));

        AccountEntity account = AccountEntity.groupAccount(parentAccount, name);
        account.setId(accountRepository.getNextAccountId());

        AccountEntity savedAccount = accountRepository.save(account);
        return accountMapper.toDto(savedAccount);
    }

    @Override
    public List<Account> getGroups(Long parentId) {
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT, 
            parentId.toString(),
            parentId,
            AuthZ.Action.VIEW);

        return accountDataService.getGroups(parentId);
    } 

    @Override
    @Transactional
    public Account createAccount(String name, Long parentId) {
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT, 
            parentId.toString(),
            parentId,
            AuthZ.Action.CREATE);

        AccountEntity parentAccount = accountRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent account not found: " + parentId));

        AccountEntity account = AccountEntity.accountAccount(parentAccount, name);
        account.setId(accountRepository.getNextAccountId());

        AccountEntity savedAccount = accountRepository.save(account);
        return accountMapper.toDto(savedAccount);
    }

    @Override
    public List<Account> getAccounts(Long parentId) {
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT, 
            parentId.toString(),
            parentId,
            AuthZ.Action.VIEW);

        return accountDataService.getAccounts(parentId);
    } 

    @Override
    @Transactional
    public Account createSubaccount(String name, Long parentId) {
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT, 
            parentId.toString(),
            parentId,
            AuthZ.Action.CREATE);

        AccountEntity parentAccount = accountRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent account not found: " + parentId));

        AccountEntity account = AccountEntity.subaccountAccount(parentAccount, name);
        account.setId(accountRepository.getNextAccountId());

        AccountEntity savedAccount = accountRepository.save(account);
        return accountMapper.toDto(savedAccount);
    }

    @Override
    public List<Account> getSubaccounts(Long parentId) {
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT, 
            parentId.toString(),
            parentId,
            AuthZ.Action.VIEW);

        return accountDataService.getSubaccounts(parentId);
    } 

    /********************************** */
    @Override
    public ProductRole getProductRole(Long accountId, Long productId) {
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT, 
            accountId.toString(),
            accountId,
            AuthZ.Action.PERMISSION);

        return accountDataService.getProductRole(accountId, productId);
    }

    @Override
    @Transactional
    public ProductRole grantProduct(Long accountId, ProductRole productRole) {

        // проверить доступ к account
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT, 
            accountId.toString(),
            accountId,
            AuthZ.Action.PERMISSION);

        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        // Find existing product role for this account and roleProductId
        ProductRoleEntity existingRole = productRoleRepository.findByAccountIdAndProductId(accountId, productRole.roleProductId());

        // Если это унаследованаая роль, то accountId != productRole.accountId
        
        if (existingRole != null) {
            // Update only can_* columns
            existingRole.setCanRead(productRole.canRead());
            existingRole.setCanQuote(productRole.canQuote());
            existingRole.setCanPolicy(productRole.canPolicy());
            existingRole.setCanAddendum(productRole.canAddendum());
            existingRole.setCanCancel(productRole.canCancel());
            existingRole.setCanProlongate(productRole.canProlongate());
            productRoleRepository.save(existingRole);
        } else {
            // Create new entity with accountId for both account and roleAccount
            ProductRoleEntity newRole = productRoleMapper.toEntity(productRole);
            newRole.setTenant(account.getTenant());
            newRole.setClient(account.getClient());
            newRole.setAccount(account);
            newRole.setRoleAccount(account);
            newRole.setId(null);
            productRoleRepository.save(newRole);
        }

        // Return complete data with product name from joined query
        return accountDataService.getProductRole(accountId, productRole.roleProductId());
    }

    @Override
    @Transactional
    public void revokeProduct(Long accountId, Long productId) {

        // проверить доступ к account
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT, 
            accountId.toString(),
            accountId,
            AuthZ.Action.PERMISSION);

        // Find and delete the product role directly
        ProductRoleEntity roleToRemove = productRoleRepository.findByAccountIdAndProductId(accountId, productId);

        if (roleToRemove != null) {
            productRoleRepository.delete(roleToRemove);
        }
    }
    
    @Override
    public List<ProductRole> getProductRolesByAccountId(Long accountId) {
        // проверить доступ к account
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT, 
            accountId.toString(),
            accountId,
            AuthZ.Action.PERMISSION);

        return accountDataService.getProductRolesByAccountIdRaw(accountId);
    }

    @Override
    public List<Account> getAllMyAccounts(String tenantCode, Long clientId, String userLogin) {
        return accountRepository.findByTenantCodeAndClientIdAndUserLogin(tenantCode, clientId, userLogin)
            .stream()
            .map(entity -> {
                Account account = accountMapper.toDto(entity);
                return new Account(
                    account.id(),
                    null,
                    null,
                    null,
                    account.nodeType(),
                    account.name()
                );
            })
            .collect(Collectors.toList());
    }

    private UserDetailsImpl getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
    }

    @Override
    public List<Account> getPathToRoot(Long accountId) {
        UserDetailsImpl currentUser = getCurrentUser();
        Long actingAccountId = currentUser.getActingAccountId();
        
        authService.check(
            currentUser, 
            AuthZ.ResourceType.ACCOUNT, 
            accountId.toString(),
            accountId,
            AuthZ.Action.VIEW);

        List<Account> fullList = accountDataService.getPathToRoot(accountId);
        
        List<Account> returnList = new ArrayList<>();
        boolean startCopying = false;
        
        for (Account account : fullList) {

            if (account.id().equals(actingAccountId)) {
                startCopying = true;
            }
            if (startCopying) { returnList.add(account); }

        }
        
        return returnList;
    }
}
