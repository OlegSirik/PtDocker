package ru.pt.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.pt.api.dto.auth.AccountToken;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.exception.UnauthorizedException;
import ru.pt.api.service.auth.AccountTokenService;
import ru.pt.auth.entity.AccountEntity;
import ru.pt.auth.entity.AccountTokenEntity;
import ru.pt.auth.repository.AccountRepository;
import ru.pt.auth.repository.AccountTokenRepository;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.auth.security.permitions.AuthZ;
import ru.pt.auth.security.permitions.AuthorizationService;
import ru.pt.auth.utils.AccountTokenMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountTokenServiceImpl implements AccountTokenService {

    private final AccountTokenRepository accountTokenRepository;
    private final AccountRepository accountRepository;
    private final AccountTokenMapper accountTokenMapper;
    private final AuthorizationService authService;
    private final SecurityContextHelper securityContextHelper;

    @Override
    @Transactional
    public AccountToken createToken(Long accountId, String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new BadRequestException("Token cannot be null or empty");
        }


        authService.check(
            getCurrentUser(),
            AuthZ.ResourceType.TOKEN,
            token,
            accountId,
            AuthZ.Action.CREATE
        );

        AccountEntity clientAccount = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        if (tokenExists(clientAccount.getClient().getId(), token)) {
                    throw new BadRequestException("Нарушение уникальности. Такой Token уже есть");
                }
        
        AccountTokenEntity tokenEntity = new AccountTokenEntity();
        tokenEntity.setToken(token);
        tokenEntity.setTenant(clientAccount.getTenant());
        tokenEntity.setClient(clientAccount.getClient());
        tokenEntity.setAccount(clientAccount);

        AccountTokenEntity savedToken = accountTokenRepository.save(tokenEntity);
        return accountTokenMapper.toDto(savedToken);
    }

    @Override
    public List<AccountToken> getTokens(Long accountId) {
        authService.check(
            getCurrentUser(),
            AuthZ.ResourceType.TOKEN,
            null,
            accountId,
            AuthZ.Action.LIST
        );

        return accountTokenRepository.findByAccountId(accountId)
                .map(accountTokenMapper::toDto)
                .orElse(List.of());
    }

    @Override
    @Transactional
    public void deleteToken(Long accountId, String token) {
        authService.check(
            getCurrentUser(),
            AuthZ.ResourceType.TOKEN,
            token,
            accountId,
            AuthZ.Action.DELETE
        );

        AccountTokenEntity tokenEntity = accountTokenRepository.findByTokenAndAccountId(token, accountId)
                .orElseThrow(() -> new NotFoundException("Token not found"));

        accountTokenRepository.delete(tokenEntity);
    }

    @Override
    public boolean tokenExists(Long clientId, String token) {
        return accountTokenRepository.findByTokenAndClientId(token, clientId).isPresent();
    }

    private UserDetailsImpl getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
    }
}
