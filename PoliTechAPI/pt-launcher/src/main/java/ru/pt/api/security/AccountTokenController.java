package ru.pt.api.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.auth.AccountToken;
import ru.pt.api.service.auth.AccountTokenService;
import ru.pt.auth.model.CreateTokenRequest;
import ru.pt.auth.model.UpdateTokenRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для работы с токенами аккаунтов
 */
@RestController
@RequestMapping("/api/auth/tokens")
public class AccountTokenController {

    private final AccountTokenService accountTokenService;

    public AccountTokenController(AccountTokenService accountTokenService) {
        this.accountTokenService = accountTokenService;
    }

    /**
     * Создать новый токен для пользователя
     * POST /api/auth/tokens
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'TNT_ADMIN')")
    public ResponseEntity<AccountToken> createToken(@RequestBody CreateTokenRequest request) {
        AccountToken token = accountTokenService.createToken(
                request.getUserLogin(),
                request.getClientId(),
                request.getToken()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }

    /**
     * Обновить существующий токен
     * PUT /api/auth/tokens
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'TNT_ADMIN')")
    public ResponseEntity<AccountToken> updateToken(@RequestBody UpdateTokenRequest request) {
        AccountToken token = accountTokenService.updateToken(
                request.getUserLogin(),
                request.getClientId(),
                request.getNewToken()
        );
        return ResponseEntity.ok(token);
    }

    /**
     * Получить токен по userLogin и clientId
     * GET /api/auth/tokens?userLogin=xxx&clientId=123
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'TNT_ADMIN', 'GROUP_ADMIN')")
    public ResponseEntity<AccountToken> getToken(
            @RequestParam String userLogin,
            @RequestParam Long clientId) {
        return accountTokenService.getToken(userLogin, clientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Получить все токены для пользователя
     * GET /api/auth/tokens/user/{userLogin}
     */
    @GetMapping("/user/{userLogin}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'TNT_ADMIN', 'GROUP_ADMIN')")
    public ResponseEntity<List<AccountToken>> getTokensByUserLogin(@PathVariable String userLogin) {
        List<AccountToken> tokens = accountTokenService.getTokensByUserLogin(userLogin);
        return ResponseEntity.ok(tokens);
    }

    /**
     * Удалить токен
     * DELETE /api/auth/tokens?userLogin=xxx&clientId=123
     */
    @DeleteMapping
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'TNT_ADMIN')")
    public ResponseEntity<Map<String, String>> deleteToken(
            @RequestParam String userLogin,
            @RequestParam Long clientId) {
        accountTokenService.deleteToken(userLogin, clientId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Token deleted successfully");
        response.put("userLogin", userLogin);
        response.put("clientId", clientId.toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Проверить существование токена
     * GET /api/auth/tokens/exists?token=xxx&clientId=123
     */
    @GetMapping("/exists")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'TNT_ADMIN', 'GROUP_ADMIN')")
    public ResponseEntity<Map<String, Boolean>> tokenExists(
            @RequestParam String token,
            @RequestParam Long clientId) {
        boolean exists = accountTokenService.tokenExists(token, clientId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);

        return ResponseEntity.ok(response);
    }




}

