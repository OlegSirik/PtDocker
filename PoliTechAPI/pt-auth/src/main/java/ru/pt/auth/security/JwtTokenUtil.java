package ru.pt.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Утилита для работы с JWT токенами.
 * Парсит и валидирует JWT токен без проверки подписи (так как токен приходит извне).
 */
@Component
public class JwtTokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${jwt.secret:defaultSecretKeyThatShouldBeChangedInProduction1234567890}")
    private String jwtSecret;

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

