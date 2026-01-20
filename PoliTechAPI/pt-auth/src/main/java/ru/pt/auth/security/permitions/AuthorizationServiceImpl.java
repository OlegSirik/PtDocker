package ru.pt.auth.security.permitions;

import org.springframework.stereotype.Service;
import ru.pt.api.dto.auth.Account;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.auth.repository.AccountRepository;
import ru.pt.auth.security.UserDetailsImpl;

import java.util.Set;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    private final AccountRepository accountRepository;
    

    public AuthorizationServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void check(
            UserDetailsImpl user,
            AuthZ.ResourceType resourceType,
            String resourceId,
            Long resoureAccountId,
            AuthZ.Action action
    ) {
        return;
        /* 
        Long myAccountId = user.getAccountId();
        Long actingAccountId = user.getActingAccountId();
        String myRoleName = user.getUserRole();

        // 1️⃣ Account hierarchy check (SQL)
        if (!accountRepository.iCanSeeResource(
                actingAccountId,
                resoureAccountId
        )) {
            throw new ForbiddenException("Нет доступа к ресурсу");
        }

        // 2️⃣ Applicability check
        if (!AuthZ.isApplicable(resourceType, action)) {
            throw new IllegalArgumentException(
                    "Action %s not applicable to %s"
                            .formatted(action, resourceType)
            );
        }

        // 3️⃣ Permission check
        if (!AuthZ.roleHasPermission(AuthZ.Role.valueOf(user.getUserRole()), resourceType, action)) {
            throw new ForbiddenException(
                    "Access denied: %s %s %s"
                            .formatted(resourceType, resourceId, action)
            );
        }
        */
    }
}