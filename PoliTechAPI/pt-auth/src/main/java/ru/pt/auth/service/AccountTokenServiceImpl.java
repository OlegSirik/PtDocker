package ru.pt.auth.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.auth.AccountToken;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.service.auth.AccountTokenService;
import ru.pt.auth.entity.AccountLoginEntity;
import ru.pt.auth.entity.AccountTokenEntity;
import ru.pt.auth.repository.AccountLoginRepository;
import ru.pt.auth.repository.AccountTokenRepository;
import ru.pt.auth.utils.AccountTokenMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AccountTokenServiceImpl implements AccountTokenService {

    private final AccountTokenRepository accountTokenRepository;
    private final AccountLoginRepository accountLoginRepository;
    private final AccountTokenMapper accountTokenMapper;

    public AccountTokenServiceImpl(AccountTokenRepository accountTokenRepository,
                                   AccountLoginRepository accountLoginRepository,
                                   AccountTokenMapper accountTokenMapper) {
        this.accountTokenRepository = accountTokenRepository;
        this.accountLoginRepository = accountLoginRepository;
        this.accountTokenMapper = accountTokenMapper;
    }

    @Override
    @Transactional
    public AccountToken createToken(String userLogin, Long clientId, String token) {
        if (userLogin == null || userLogin.trim().isEmpty()) {
            throw new BadRequestException("User login cannot be null or empty");
        }
        if (clientId == null) {
            throw new BadRequestException("Client ID cannot be null");
        }
        if (token == null || token.trim().isEmpty()) {
            throw new BadRequestException("Token cannot be null or empty");
        }

        // Проверить, существует ли уже токен для этого пользователя и клиента
        Optional<AccountTokenEntity> existingToken = accountTokenRepository
                .findByUserLoginAndClientId(userLogin, clientId);

        if (existingToken.isPresent()) {
            throw new BadRequestException("Token already exists for user '" + userLogin +
                                        "' and client ID " + clientId + ". Use update instead.");
        }

        // Найти AccountLogin для получения связанных данных
        List<AccountLoginEntity> accountLogins = accountLoginRepository.findByUserLogin(userLogin);

        if (accountLogins.isEmpty()) {
            throw new NotFoundException("User login '" + userLogin + "' not found");
        }

        // Найти соответствующий AccountLogin с нужным clientId
        AccountLoginEntity accountLogin = accountLogins.stream()
                .filter(al -> al.getClient() != null && al.getClient().getId().equals(clientId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("User '" + userLogin +
                                                        "' not associated with client ID " + clientId));

        // Создать новый токен
        AccountTokenEntity tokenEntity = new AccountTokenEntity();
        tokenEntity.setToken(token);
        tokenEntity.setTenant(accountLogin.getTenant());
        tokenEntity.setClient(accountLogin.getClient());
        tokenEntity.setAccount(accountLogin.getAccount());

        AccountTokenEntity savedToken = accountTokenRepository.save(tokenEntity);
        return accountTokenMapper.toDto(savedToken);
    }

    @Override
    @Transactional
    public AccountToken updateToken(String userLogin, Long clientId, String newToken) {
        if (userLogin == null || userLogin.trim().isEmpty()) {
            throw new BadRequestException("User login cannot be null or empty");
        }
        if (clientId == null) {
            throw new BadRequestException("Client ID cannot be null");
        }
        if (newToken == null || newToken.trim().isEmpty()) {
            throw new BadRequestException("Token cannot be null or empty");
        }

        // Найти существующий токен
        AccountTokenEntity tokenEntity = accountTokenRepository
                .findByUserLoginAndClientId(userLogin, clientId)
                .orElseThrow(() -> new NotFoundException("Token not found for user '" + userLogin +
                                                        "' and client ID " + clientId));

        // Обновить токен
        tokenEntity.setToken(newToken);
        AccountTokenEntity updatedToken = accountTokenRepository.save(tokenEntity);

        return accountTokenMapper.toDto(updatedToken);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AccountToken> getToken(String userLogin, Long clientId) {
        return accountTokenRepository.findByUserLoginAndClientId(userLogin, clientId)
                .map(accountTokenMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountToken> getTokensByUserLogin(String userLogin) {
        return accountTokenRepository.findByUserLogin(userLogin)
                .stream()
                .map(accountTokenMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteToken(String userLogin, Long clientId) {
        AccountTokenEntity tokenEntity = accountTokenRepository
                .findByUserLoginAndClientId(userLogin, clientId)
                .orElseThrow(() -> new NotFoundException("Token not found for user '" + userLogin +
                                                        "' and client ID " + clientId));

        accountTokenRepository.delete(tokenEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tokenExists(String token, Long clientId) {
        return accountTokenRepository.findByTokenAndClientId(token, clientId).isPresent();
    }
}
