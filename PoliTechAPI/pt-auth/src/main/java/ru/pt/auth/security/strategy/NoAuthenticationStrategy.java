package ru.pt.auth.security.strategy;

import jakarta.servlet.http.HttpServletRequest;
import ru.pt.auth.model.AuthType;
import ru.pt.auth.entity.AccountEntity;
import ru.pt.auth.repository.AccountRepository;
import ru.pt.auth.security.context.RequestContext;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import ru.pt.api.dto.exception.NotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

@Component
public class NoAuthenticationStrategy implements IdentitySourceStrategy {

    private final RequestContext requestContext;
    private final AccountRepository accountRepository;

    public NoAuthenticationStrategy(RequestContext requestContext, AccountRepository accountRepository) {
        this.requestContext = requestContext;
        this.accountRepository = accountRepository;
    }

    @Override
    public boolean supports(AuthType type) {
        return type == AuthType.NONE;
    }

    @Override
    public void resolveIdentity(HttpServletRequest request) {
        String accountId = request.getHeader("X-Account-Id");
        if (accountId == null) {
            throw new BadCredentialsException("Account ID is required");
        }
        //requestContext.setAccountId(accountId);
        AccountEntity accountEntity = accountRepository.findById(Long.parseLong(accountId))
            .orElseThrow(() -> new NotFoundException("Account not found"));

        requestContext.setClient(accountEntity.getClient().getClientId());
        requestContext.setAccount(accountId);
        requestContext.setLogin(accountEntity.getAccountLogins().get(0).getUserLogin());
        requestContext.setTenant(accountEntity.getTenant().getCode());
    }
}
