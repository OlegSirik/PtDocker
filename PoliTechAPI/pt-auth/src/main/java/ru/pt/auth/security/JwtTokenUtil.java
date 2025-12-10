package ru.pt.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ru.pt.auth.entity.AccountEntity;
import ru.pt.auth.entity.AccountLoginEntity;
import ru.pt.auth.repository.AccountLoginRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Утилита для работы с JWT токенами.
 * Парсит и валидирует JWT токен без проверки подписи (так как токен приходит извне).
 * Также создает новые JWT токены на основе данных пользователя.
 */
@Component
public class JwtTokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);
    private static final long TOKEN_VALIDITY = 24 * 60 * 60; // 24 часа в секундах
    private static final long REFRESH_TOKEN_VALIDITY = 30 * 24 * 60 * 60; // 30 дней в секундах

    @Value("${jwt.secret:defaultSecretKeyThatShouldBeChangedInProduction1234567890}")
    private String jwtSecret;

    private final AccountLoginRepository accountLoginRepository;
    private final ObjectMapper objectMapper;

    public JwtTokenUtil(AccountLoginRepository accountLoginRepository) {
        this.accountLoginRepository = accountLoginRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Создает JWT токен для пользователя
     *
     * @param userLogin логин пользователя
     * @param clientId идентификатор клиента (может быть null)
     * @return JWT токен или null если пользователь не найден
     */
    @Transactional(readOnly = true)
    public String createToken(String userLogin, Long clientId) {
        try {
            // Найти пользователя в БД
            List<AccountLoginEntity> accountLogins = accountLoginRepository.findByUserLogin(userLogin);

            if (accountLogins.isEmpty()) {
                logger.error("User not found: {}", userLogin);
                return null;
            }

            // Если clientId указан, найти соответствующий AccountLogin
            AccountLoginEntity accountLogin;
            if (clientId != null) {
                accountLogin = accountLogins.stream()
                        .filter(al -> al.getClient() != null && al.getClient().getId().equals(clientId))
                        .findFirst()
                        .orElse(null);

                if (accountLogin == null) {
                    logger.error("User {} not associated with client ID {}", userLogin, clientId);
                    return null;
                }
            } else {
                // Если clientId не указан, берем первый или дефолтный
                accountLogin = accountLogins.stream()
                        .filter(al -> Boolean.TRUE.equals(al.getDefault()))
                        .findFirst()
                        .orElseGet(() -> accountLogins.get(0));
            }

            return generateToken(accountLogin, TOKEN_VALIDITY);

        } catch (Exception e) {
            logger.error("Failed to create token for user: {}", userLogin, e);
            return null;
        }
    }

    /**
     * Создает refresh токен для пользователя (с увеличенным сроком действия)
     *
     * @param userLogin логин пользователя
     * @param clientId идентификатор клиента (может быть null)
     * @return JWT refresh токен или null если пользователь не найден
     */
    @Transactional(readOnly = true)
    public String refreshToken(String userLogin, Long clientId) {
        try {
            // Найти пользователя в БД
            List<AccountLoginEntity> accountLogins = accountLoginRepository.findByUserLogin(userLogin);

            if (accountLogins.isEmpty()) {
                logger.error("User not found: {}", userLogin);
                return null;
            }

            // Если clientId указан, найти соответствующий AccountLogin
            AccountLoginEntity accountLogin;
            if (clientId != null) {
                accountLogin = accountLogins.stream()
                        .filter(al -> al.getClient() != null && al.getClient().getId().equals(clientId))
                        .findFirst()
                        .orElse(null);

                if (accountLogin == null) {
                    logger.error("User {} not associated with client ID {}", userLogin, clientId);
                    return null;
                }
            } else {
                // Если clientId не указан, берем первый или дефолтный
                accountLogin = accountLogins.stream()
                        .filter(al -> Boolean.TRUE.equals(al.getDefault()))
                        .findFirst()
                        .orElseGet(() -> accountLogins.get(0));
            }

            return generateToken(accountLogin, REFRESH_TOKEN_VALIDITY);

        } catch (Exception e) {
            logger.error("Failed to create refresh token for user: {}", userLogin, e);
            return null;
        }
    }

    /**
     * Генерирует JWT токен на основе данных пользователя
     */
    private String generateToken(AccountLoginEntity accountLogin, long validityInSeconds) {
        try {
            // Header
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");
            String headerJson = objectMapper.writeValueAsString(header);
            String headerBase64 = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));

            // Payload
            long nowSeconds = System.currentTimeMillis() / 1000;
            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", accountLogin.getUserLogin());
            payload.put("iat", nowSeconds);
            payload.put("exp", nowSeconds + validityInSeconds);
            payload.put("role", accountLogin.getUserRole());

            // Добавляем дополнительные данные
            if (accountLogin.getTenant() != null) {
                payload.put("tenantCode", accountLogin.getTenant().getId());
                payload.put("tenantName", accountLogin.getTenant().getName());
            }

            if (accountLogin.getClient() != null) {
                payload.put("clientId", accountLogin.getClient().getId());
                payload.put("clientName", accountLogin.getClient().getName());
            }

            if (accountLogin.getAccount() != null) {
                Long accountId = accountLogin.getAccount().getId();
                String accId = accountId.toString();
                if (accountId != null) {
                    payload.put("accountId", accountId);
                }
                payload.put("accountId", accountLogin.getAccount().getId());
                String accountName = accountLogin.getAccount().getName();
                if (accountName != null) {
                    payload.put("accountName", accountName);
                }
                if (accountLogin.getAccount().getNodeType() != null) {
                    payload.put("accountType", accountLogin.getAccount().getNodeType().name());
                }
            }

            String payloadJson = objectMapper.writeValueAsString(payload);
            String payloadBase64 = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));

            // Signature
            String dataToSign = headerBase64 + "." + payloadBase64;
            String signature = hmacSha256(dataToSign, jwtSecret);

            // Complete token
            String token = dataToSign + "." + signature;

            logger.debug("Generated JWT token for user: {}, role: {}",
                    accountLogin.getUserLogin(), accountLogin.getUserRole());

            return token;

        } catch (Exception e) {
            logger.error("Failed to generate JWT token", e);
            return null;
        }
    }

    /**
     * Base64 URL encoding (без padding)
     */
    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(data);
    }

    /**
     * HMAC SHA256 подпись
     */
    private String hmacSha256(String data, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncode(signatureBytes);
    }

    /**
     * Извлекает логин (subject) из JWT токена без проверки подписи
     */
    public String getUsernameFromToken(String token) {
        try {
            // Парсим токен без валидации подписи
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                logger.error("Invalid JWT token format");
                return null;
            }

            // Декодируем payload (вторая часть токена)
            String payload = new String(
                java.util.Base64.getUrlDecoder().decode(parts[1]),
                StandardCharsets.UTF_8
            );

            /// Используем Jackson для парсинга JSON payload и извлечения sub
            ObjectMapper objectMapper = new ObjectMapper();
            String username = objectMapper.readTree(payload).path("sub").asText(null);

            logger.debug("Extracted username from JWT: {}", username);
            return username;

        } catch (Exception e) {
            logger.error("Failed to parse JWT token", e);
            return null;
        }
    }

    /**
     * Извлекает claim из JWT токена
     */
    public String getClaimFromToken(String token, String claimName) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String payload = new String(
                java.util.Base64.getUrlDecoder().decode(parts[1]),
                StandardCharsets.UTF_8
            );

            return extractClaimFromPayload(payload, claimName);

        } catch (Exception e) {
            logger.error("Failed to extract claim {} from JWT token", claimName, e);
            return null;
        }
    }

    /**
     * Проверяет, не истек ли токен
     */
    public boolean isTokenExpired(String token) {
        try {
            String expStr = getClaimFromToken(token, "exp");
            if (expStr == null) {
                return true; // Если нет exp, считаем истекшим
            }

            long exp = Long.parseLong(expStr);
            return exp < (System.currentTimeMillis() / 1000);

        } catch (Exception e) {
            logger.error("Failed to check token expiration", e);
            return true;
        }
    }

    /**
     * Валидирует токен (проверяет формат и срок действия)
     */
    public boolean validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }

            String username = getUsernameFromToken(token);
            if (username == null || username.trim().isEmpty()) {
                logger.error("Token validation failed: username is null or empty");
                return false;
            }

            if (isTokenExpired(token)) {
                logger.error("Token validation failed: token is expired");
                return false;
            }

            return true;

        } catch (Exception e) {
            logger.error("Token validation failed", e);
            return false;
        }
    }

    /**
     * Извлекает subject (username) из JSON payload
     */
    private String extractSubFromPayload(String payload) {
        return extractClaimFromPayload(payload, "sub");
    }

    /**
     * Простой парсинг JSON для извлечения claim
     * Для production рекомендуется использовать Jackson
     */
    private String extractClaimFromPayload(String payload, String claimName) {
        try {
            String searchKey = "\"" + claimName + "\"";
            int startIndex = payload.indexOf(searchKey);
            if (startIndex == -1) {
                return null;
            }

            int colonIndex = payload.indexOf(":", startIndex);
            if (colonIndex == -1) {
                return null;
            }

            int valueStart = colonIndex + 1;
            // Пропускаем пробелы
            while (valueStart < payload.length() && Character.isWhitespace(payload.charAt(valueStart))) {
                valueStart++;
            }

            // Проверяем тип значения
            if (payload.charAt(valueStart) == '"') {
                // Строковое значение
                int valueEnd = payload.indexOf('"', valueStart + 1);
                if (valueEnd == -1) {
                    return null;
                }
                return payload.substring(valueStart + 1, valueEnd);
            } else {
                // Числовое значение или boolean
                int valueEnd = valueStart;
                while (valueEnd < payload.length() &&
                       !Character.isWhitespace(payload.charAt(valueEnd)) &&
                       payload.charAt(valueEnd) != ',' &&
                       payload.charAt(valueEnd) != '}') {
                    valueEnd++;
                }
                return payload.substring(valueStart, valueEnd);
            }

        } catch (Exception e) {
            logger.error("Failed to extract claim from payload", e);
            return null;
        }
    }
}
