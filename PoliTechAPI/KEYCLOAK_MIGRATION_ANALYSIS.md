# Миграция на Keycloak - Анализ и план

## 📊 Текущее состояние

### Компоненты JWT аутентификации

1. **JwtTokenUtil** - генерация и валидация токенов (HMAC-SHA256)
2. **JwtAuthenticationFilter** - фильтр для проверки JWT из заголовка
3. **UserDetailsServiceImpl** - загрузка пользователя из БД
4. **SecurityConfig** - конфигурация Spring Security
5. **AuthenticationController** - endpoint'ы для получения токенов
6. **AdminUserManagementService** - создание пользователей

### Текущая архитектура

```
Запрос → JwtAuthenticationFilter → JwtTokenUtil.validateToken()
    → UserDetailsServiceImpl.loadUserByUsername()
    → SecurityContextHolder.setAuthentication()
```

---

## 🎯 Целевая архитектура с Keycloak

### Что делает Keycloak

1. **Управление пользователями** - создание, удаление, редактирование
2. **Аутентификация** - проверка credentials (username/password, OAuth2, SAML, etc.)
3. **Генерация токенов** - JWT токены с подписью RSA (не HMAC)
4. **Валидация токенов** - проверка подписи через публичный ключ
5. **Управление ролями** - роли и группы в Keycloak
6. **Single Sign-On (SSO)** - единая точка входа

### Новая архитектура

```
Запрос → KeycloakAuthenticationFilter → KeycloakJwtConverter
    → Проверка токена через публичный ключ Keycloak
    → SecurityContextHolder.setAuthentication()
```

---

## 📋 Объем изменений

### 🔴 ВЫСОКИЙ ПРИОРИТЕТ - Обязательные изменения

#### 1. Добавление зависимостей (build.gradle.kts)

**Файл:** `PoliTechAPI/build.gradle.kts` или `pt-auth/build.gradle.kts`

```kotlin
dependencies {
    // Keycloak Spring Boot Adapter
    implementation("org.keycloak:keycloak-spring-boot-starter:23.0.0")
    implementation("org.keycloak:keycloak-admin-client:23.0.0")
    
    // Или Spring Security OAuth2 Resource Server (рекомендуется)
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.security:spring-security-oauth2-jose")
}
```

**Оценка:** 5 минут

---

#### 2. Конфигурация Keycloak (application.yml)

**Файл:** `PoliTechAPI/pt-launcher/src/main/resources/application.yml`

```yaml
keycloak:
  realm: politech
  auth-server-url: http://localhost:8180/auth
  ssl-required: external
  resource: politech-api
  credentials:
    secret: ${KEYCLOAK_CLIENT_SECRET}
  use-resource-role-mappings: true
  
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8180/auth/realms/politech
          jwk-set-uri: http://localhost:8180/auth/realms/politech/protocol/openid-connect/certs
```

**Оценка:** 10 минут

---

#### 3. Замена SecurityConfig

**Файл:** `pt-auth/src/main/java/ru/pt/auth/configuration/SecurityConfig.java`

**ДО (текущее):**
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .addFilterBefore(jwtAuthenticationFilter, ...);
        return http.build();
    }
}
```

**ПОСЛЕ (с Keycloak):**
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**", "/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return converter;
    }
}
```

**Оценка:** 30 минут

---

#### 4. Создание KeycloakRoleConverter

**Новый файл:** `pt-auth/src/main/java/ru/pt/auth/security/KeycloakRoleConverter.java`

```java
@Component
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        // Извлечь роли из Keycloak токена
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return Collections.emptyList();
        }
        
        List<String> roles = (List<String>) realmAccess.get("roles");
        if (roles == null) {
            return Collections.emptyList();
        }
        
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());
    }
}
```

**Оценка:** 20 минут

---

#### 5. Удаление/замена JwtTokenUtil

**Файл:** `pt-auth/src/main/java/ru/pt/auth/security/JwtTokenUtil.java`

**Варианты:**

**Вариант А: Полное удаление**
- Удалить файл JwtTokenUtil.java
- Keycloak сам генерирует токены через `/token` endpoint

**Вариант Б: Частичная замена**
- Оставить только методы парсинга для совместимости
- Убрать методы генерации (`createToken`, `refreshToken`)
- Добавить валидацию через публичный ключ Keycloak

**Оценка:** 1-2 часа

---

#### 6. Замена JwtAuthenticationFilter

**Файл:** `pt-auth/src/main/java/ru/pt/auth/security/JwtAuthenticationFilter.java`

**Статус:** УДАЛИТЬ полностью

Spring Security OAuth2 Resource Server предоставляет свой фильтр `BearerTokenAuthenticationFilter`.

**Оценка:** 5 минут (просто удалить)

---

#### 7. Обновление UserDetailsServiceImpl

**Файл:** `pt-auth/src/main/java/ru/pt/auth/security/UserDetailsServiceImpl.java`

**Изменения:**
- Keycloak управляет пользователями
- UserDetailsService больше не нужен для аутентификации
- Можно оставить для загрузки доп. данных из локальной БД

**Оценка:** 30 минут (рефакторинг)

---

#### 8. Обновление AuthenticationController

**Файл:** `pt-auth/src/main/java/ru/pt/auth/controller/AuthenticationController.java`

**Изменения:**

**ДО:**
```java
@PostMapping("/token")
public ResponseEntity<TokenResponse> generateToken(@RequestBody TokenRequest request) {
    String token = jwtTokenUtil.createToken(request.getUserLogin(), request.getClientId());
    return ResponseEntity.ok(new TokenResponse(token));
}
```

**ПОСЛЕ (проксирование на Keycloak):**
```java
@PostMapping("/token")
public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
    // Проксировать запрос на Keycloak
    String keycloakUrl = keycloakProperties.getAuthServerUrl() + 
        "/realms/" + keycloakProperties.getRealm() + "/protocol/openid-connect/token";
    
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("grant_type", "password");
    formData.add("client_id", keycloakProperties.getResource());
    formData.add("client_secret", keycloakProperties.getClientSecret());
    formData.add("username", request.getUsername());
    formData.add("password", request.getPassword());
    
    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
    ResponseEntity<Map> response = restTemplate.postForEntity(keycloakUrl, entity, Map.class);
    
    return ResponseEntity.ok(new TokenResponse(
        response.getBody().get("access_token"),
        response.getBody().get("refresh_token")
    ));
}

@PostMapping("/refresh")
public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest request) {
    // Аналогично, но с grant_type=refresh_token
}
```

**Оценка:** 1 час

---

### 🟡 СРЕДНИЙ ПРИОРИТЕТ - Миграция пользователей

#### 9. AdminUserManagementService - интеграция с Keycloak

**Файл:** `pt-auth/src/main/java/ru/pt/auth/service/AdminUserManagementService.java`

**Изменения:**
- Создание пользователей через Keycloak Admin API
- Синхронизация ролей Keycloak ↔ локальная БД
- Управление группами в Keycloak

**ДО:**
```java
public AccountLoginEntity createTntAdmin(Long tenantId, String userLogin, String userName) {
    LoginEntity login = new LoginEntity();
    login.setUserLogin(userLogin);
    loginRepository.save(login);
    // ...
}
```

**ПОСЛЕ:**
```java
public AccountLoginEntity createTntAdmin(Long tenantId, String userLogin, String userName, String password) {
    // 1. Создать пользователя в Keycloak
    UserRepresentation keycloakUser = new UserRepresentation();
    keycloakUser.setUsername(userLogin);
    keycloakUser.setEnabled(true);
    
    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(password);
    keycloakUser.setCredentials(Arrays.asList(credential));
    
    UsersResource usersResource = keycloak.realm(realm).users();
    Response response = usersResource.create(keycloakUser);
    String userId = extractUserId(response);
    
    // 2. Назначить роль TNT_ADMIN в Keycloak
    RoleRepresentation role = keycloak.realm(realm).roles().get("TNT_ADMIN").toRepresentation();
    usersResource.get(userId).roles().realmLevel().add(Arrays.asList(role));
    
    // 3. Сохранить в локальной БД для доп. данных
    LoginEntity login = new LoginEntity();
    login.setUserLogin(userLogin);
    login.setKeycloakUserId(userId); // Новое поле!
    loginRepository.save(login);
    // ...
}
```

**Оценка:** 3-4 часа

---

#### 10. Миграция существующих пользователей

**Новый скрипт:** `scripts/migrate_users_to_keycloak.sh`

```bash
#!/bin/bash
# Экспорт пользователей из БД и создание в Keycloak

# 1. Получить всех пользователей из БД
# 2. Для каждого пользователя:
#    - Создать в Keycloak через Admin API
#    - Назначить роли
#    - Обновить acc_logins с keycloak_user_id
```

**Оценка:** 2-3 часа

---

### 🟢 НИЗКИЙ ПРИОРИТЕТ - Дополнительно

#### 11. Обновление тестов

**Файлы:** Все тесты, использующие JWT

**Изменения:**
- Mock'и для Keycloak токенов
- Использование тестовых токенов с правильной структурой

**Оценка:** 2-3 часа

---

#### 12. Документация

**Файлы:**
- `KEYCLOAK_INTEGRATION.md` - новый файл
- `ADMIN_USER_MANAGEMENT_API.md` - обновление
- Postman коллекция - обновление endpoints

**Оценка:** 2 часа

---

#### 13. Docker Compose для Keycloak

**Файл:** `docker-compose.yml`

```yaml
services:
  keycloak:
    image: quay.io/keycloak/keycloak:23.0
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
      KC_DB_USERNAME: keycloak
      KC_DB_PASSWORD: keycloak
    ports:
      - "8180:8080"
    command: start-dev
    depends_on:
      - postgres
```

**Оценка:** 30 минут

---

## 📊 Итоговая оценка

### Время на миграцию

| Компонент | Время | Сложность |
|-----------|-------|-----------|
| Зависимости + конфигурация | 30 мин | 🟢 Низкая |
| SecurityConfig | 30 мин | 🟡 Средняя |
| KeycloakRoleConverter | 20 мин | 🟢 Низкая |
| Удаление JwtTokenUtil | 1-2 часа | 🟡 Средняя |
| Удаление JwtAuthenticationFilter | 5 мин | 🟢 Низкая |
| UserDetailsServiceImpl рефакторинг | 30 мин | 🟡 Средняя |
| AuthenticationController | 1 час | 🟡 Средняя |
| AdminUserManagementService | 3-4 часа | 🔴 Высокая |
| Миграция пользователей | 2-3 часа | 🔴 Высокая |
| Тесты | 2-3 часа | 🟡 Средняя |
| Документация | 2 часа | 🟢 Низкая |
| Docker Compose | 30 мин | 🟢 Низкая |

**ИТОГО: 13-17 часов** (2-3 рабочих дня)

---

## ⚖️ Плюсы и минусы

### ✅ Плюсы Keycloak

1. **Централизованное управление пользователями**
   - Единая точка управления
   - Web UI для администрирования
   - Готовые API для CRUD операций

2. **Безопасность**
   - Проверенная реализация OAuth2/OpenID Connect
   - RSA подпись токенов (безопаснее HMAC)
   - Защита от bruteforce, account lockout
   - Двухфакторная аутентификация (2FA)

3. **Функциональность**
   - Single Sign-On (SSO)
   - Social Login (Google, Facebook, etc.)
   - SAML, LDAP интеграция
   - User Federation
   - Identity Brokering

4. **Стандарты**
   - OAuth2, OpenID Connect, SAML 2.0
   - JWT стандартной структуры
   - Совместимость с другими системами

5. **Масштабируемость**
   - Кластеризация
   - High Availability
   - Session replication

### ❌ Минусы Keycloak

1. **Сложность**
   - Дополнительный сервис для развертывания
   - Больше инфраструктуры (БД для Keycloak)
   - Кривая обучения для команды

2. **Производительность**
   - Дополнительный network hop для проверки токенов
   - Больше памяти и CPU (еще один JVM процесс)

3. **Зависимость**
   - Зависимость от внешнего сервиса
   - Если Keycloak недоступен → невозможно войти

4. **Кастомизация**
   - Сложнее кастомизировать логику аутентификации
   - Нужны Keycloak SPI для расширений

5. **Миграция**
   - Нужно мигрировать существующих пользователей
   - Обновить все клиенты (фронтенд, мобильные приложения)

---

## 🎯 Рекомендации

### Когда стоит мигрировать на Keycloak

✅ **ДА, если:**
- Планируется SSO между несколькими приложениями
- Нужна интеграция с LDAP/Active Directory
- Требуется Social Login
- Команда готова поддерживать дополнительную инфраструктуру
- Нужны enterprise-фичи (2FA, User Federation)

❌ **НЕТ, если:**
- Простое приложение с базовой аутентификацией
- Текущее решение работает хорошо
- Нет ресурсов на миграцию и поддержку
- Performance critical (каждая миллисекунда важна)

### Альтернативы Keycloak

1. **Auth0** - managed service, проще, но платный
2. **Okta** - enterprise solution, платный
3. **AWS Cognito** - если в AWS
4. **Custom JWT + Spring Security** - текущее решение ✅
5. **ORY Hydra** - open source, легковеснее Keycloak

---

## 📋 План миграции (поэтапный)

### Фаза 1: Подготовка (1 день)
1. Развернуть Keycloak локально
2. Создать realm, client, тестовых пользователей
3. Настроить конфигурацию
4. Добавить зависимости

### Фаза 2: Базовая интеграция (1 день)
5. Заменить SecurityConfig
6. Создать KeycloakRoleConverter
7. Удалить JwtAuthenticationFilter
8. Обновить AuthenticationController

### Фаза 3: Миграция пользователей (1 день)
9. Написать скрипт миграции
10. Мигрировать тестовых пользователей
11. Обновить AdminUserManagementService

### Фаза 4: Тестирование и документация (1 день)
12. Обновить тесты
13. Написать документацию
14. Обновить Postman коллекцию

---

## 💡 Выводы

### Объем изменений: СРЕДНИЙ-ВЫСОКИЙ

- **Файлов для изменения:** ~10-15
- **Новых файлов:** ~5-7
- **Удаляемых файлов:** ~2-3
- **Время:** 13-17 часов (2-3 рабочих дня)
- **Сложность:** 🟡 Средняя (есть challenging моменты)

### Ключевые изменения

1. ✅ **Просто:** Конфигурация, зависимости
2. 🟡 **Средне:** SecurityConfig, AuthenticationController
3. 🔴 **Сложно:** AdminUserManagementService, миграция пользователей

### Мое мнение

Если у вас:
- Простое приложение
- Текущее решение работает
- Нет планов на SSO/Social Login

**→ Оставайтесь на текущем решении (Custom JWT)** ✅

Если нужны:
- SSO
- Enterprise функции
- Долгосрочное развитие

**→ Стоит мигрировать на Keycloak** 🚀

---

**Хотите, чтобы я создал пошаговый план миграции с примерами кода для каждого компонента?**

