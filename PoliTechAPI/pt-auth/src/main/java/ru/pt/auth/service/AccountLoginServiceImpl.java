package ru.pt.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.pt.api.dto.auth.AccountLogin;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.exception.UnauthorizedException;
import ru.pt.api.service.auth.AccountLoginService;
import ru.pt.auth.entity.AccountEntity;
import ru.pt.auth.entity.AccountLoginEntity;
import ru.pt.auth.entity.LoginEntity;
import ru.pt.auth.repository.AccountLoginRepository;
import ru.pt.auth.repository.AccountRepository;
import ru.pt.auth.repository.LoginRepository;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.auth.security.permitions.AuthZ;
import ru.pt.auth.security.permitions.AuthorizationService;
import ru.pt.auth.utils.AccountLoginMapper;

import java.util.List;

/**
 * Сервис для работы с привязками пользователей к аккаунтам (таблица acc_account_logins)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountLoginServiceImpl implements AccountLoginService {

    private final AccountLoginRepository accountLoginRepository;
    private final AccountRepository accountRepository;
    private final LoginRepository loginRepository;
    private final AccountLoginMapper accountLoginMapper;
    private final AuthorizationService authService;
    private final SecurityContextHelper securityContextHelper;

    @Override
    @Transactional
    public AccountLogin createLogin(Long accountId, AccountLogin login) {
        if (login == null || login.getUserLogin() == null || login.getUserLogin().trim().isEmpty()) {
            throw new BadRequestException("Login cannot be null or empty");
        }

        String userLogin = login.getUserLogin().trim();

        // Authorization check
        authService.check(
            getCurrentUser(),
            AuthZ.ResourceType.LOGIN,
            userLogin,
            accountId,
            AuthZ.Action.CREATE
        );

        // Check if login already exists for this account
        if (loginExists(accountId, userLogin)) {
            throw new BadRequestException("Нарушение уникальности. Такой Login уже привязан к аккаунту");
        }

        // Get account entity
        AccountEntity accountEntity = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        // Check if login exists in acc_logins, create if not
        LoginEntity loginEntity = loginRepository.findByTenantCodeAndUserLogin(
                accountEntity.getTenant().getCode(), 
                userLogin
        ).orElseGet(() -> {
            // Create new login in acc_logins
            LoginEntity newLogin = new LoginEntity();
            newLogin.setTenant(accountEntity.getTenant());
            newLogin.setUserLogin(userLogin);
            newLogin.setFullName(userLogin); // Default to userLogin if no fullName provided
            newLogin.setIsDeleted(false);
            return loginRepository.save(newLogin);
        });

        // Create account login binding
        AccountLoginEntity accountLoginEntity = new AccountLoginEntity();
        accountLoginEntity.setTenant(accountEntity.getTenant());
        accountLoginEntity.setClient(accountEntity.getClient());
        accountLoginEntity.setAccount(accountEntity);
        accountLoginEntity.setUserLogin(userLogin);
        accountLoginEntity.setLogin(loginEntity);
        accountLoginEntity.setDefault(Boolean.TRUE.equals(login.getIsDefault()));

        AccountLoginEntity savedEntity = accountLoginRepository.save(accountLoginEntity);
        return accountLoginMapper.toDto(savedEntity);
    }

    @Override
    public List<AccountLogin> getLoginsByAccountId(Long accountId) {
        // Authorization check
        authService.check(
            getCurrentUser(),
            AuthZ.ResourceType.LOGIN,
            null,
            accountId,
            AuthZ.Action.LIST
        );

        return accountLoginRepository.findByAccountEntityId(accountId)
                .stream()
                .map(accountLoginMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteLogin(Long accountId, String userLogin) {
        if (userLogin == null || userLogin.trim().isEmpty()) {
            throw new BadRequestException("Login cannot be null or empty");
        }

        // Authorization check
        authService.check(
            getCurrentUser(),
            AuthZ.ResourceType.LOGIN,
            userLogin,
            accountId,
            AuthZ.Action.DELETE
        );

        AccountLoginEntity accountLoginEntity = accountLoginRepository
                .findByUserLoginAndAccountId(userLogin, accountId)
                .orElseThrow(() -> new NotFoundException("Login binding not found"));

        // Delete only from acc_account_logins
        accountLoginRepository.delete(accountLoginEntity);
    }

    @Override
    public boolean loginExists(Long accountId, String userLogin) {
        return accountLoginRepository.findByUserLoginAndAccountId(userLogin, accountId).isPresent();
    }

    private UserDetailsImpl getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
    }
}
