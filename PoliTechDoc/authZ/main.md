# Система авторизации

### Назначение

Система авторизации обеспечивает управление доступом к бизнес-функциям на основе иерархической модели прав доступа с поддержкой наследования и переопределения привилегий.

#### Архитектурные компоненты

* **Authentication Service** - проверка подлинности токенов
* **Authorization Engine** - механизм проверки прав доступа
* **Role Registry** - реестр ролей и привилегий
* **Product Privilege Manager** - менеджер продуктовых прав

### Процесс авторизации

#### Входные данные

json

```
{
  "token": "JWT-токен",
  "required_permission": "запрашиваемое действие"
}
```

#### Алгоритм обработки

1. **Валидация токена** → Извлечение claims:
   * `login` (email)
   * `client_id` (идентификатор клиентского приложения)
2. **Определение контекста доступа**:
   * Поиск `account_id` по login и client\_id
   * Если multiple accounts → выбор account\_id = 0 (по умолчанию)

#### Схема данных токена

java

```
public class TokenClaims {
    private String login;          // Электронная почта
    private String clientId;       // Идентификатор приложения
    private Long accountId;        // Опционально: явно выбранный аккаунт
}
```

### Модель ролей и привилегий

#### Матрица соответствия типов аккаунтов и ролей

| Тип аккаунта | Системная роль | Уровень доступа |
| ------------ | -------------- | --------------- |
| ROOT         | SYS\_ADMIN     | Системный       |
| CLIENT       | CLIENT\_ADMIN  | Клиентский      |
| GROUP        | GROUP\_ADMIN   | Групповой       |
| ACCOUNT      | SALES          | Операционный    |
| SUB          | SALES          | Операционный    |

### Механизм наследования прав

#### Иерархическая модель



```
ROOT (SYS_ADMIN)
│
└── CLIENT (CLIENT_ADMIN)
    │
    └── GROUP (GROUP_ADMIN)
        │
        └── ACCOUNT (SALES)
            │
            └── SUB (SALES)
```

#### Приоритетность прав

1. **Локальные права** (назначенные непосредственно на account\_id)
2. **Унаследованные права** (от родительских узлов)
3. **Системные права по роли** (базовый набор)

### Модель видимости данных

#### Правила доступа к договорам

**Для роли SALES:**



```
-- Видимость ограничена портфелем пользователя
SELECT * FROM contracts 
WHERE account_id = :current_account_id;
```

**Для ролей ADMIN:**



```
-- Видимость распространяется на всю ветку иерархии
WITH RECURSIVE account_tree AS (
    SELECT id FROM accounts WHERE id = :current_account_id
    UNION ALL
    SELECT a.id FROM accounts a
    INNER JOIN account_tree at ON a.parent_id = at.id
)
SELECT * FROM contracts 
WHERE account_id IN (SELECT id FROM account_tree);
```

#### Матрица возможностей

| Роль          | Просмотр договоров   | Продажи                     | Администрирование   |
| ------------- | -------------------- | --------------------------- | ------------------- |
| SYS\_ADMIN    | Вся система          | Только с явными правами     | Полный доступ       |
| CLIENT\_ADMIN | Ветка клиента        | Только с явными правами     | Управление клиентом |
| GROUP\_ADMIN  | Ветка группы         | Только с явными правами     | Управление группой  |
| SALES         | Только свой портфель | Согласно продуктовым правам | Нет                 |

### Реализация типа SUB

#### Назначение

Тип `SUB` предназначен для гранулярного управления правами внутри существующего портфеля договоров.

#### Use Case: Ограничение прав пролонгации

```
Сценарий: "Только пролонгация ОСАГО"
- Родительский узел: ACCOUNT (полные права продаж)
- Дочерний узел: SUB (наследует видимость договоров)
- Права доступа: 
  calculate: false
  sell: false
  prolong: true
  additional_agreement: false
```

### API спецификация

#### Эндпоинт проверки прав доступа

```
POST /api/authorization/check
Authorization: Bearer {token}
Content-Type: application/json

{
  "resource_type": "contract",
  "action": "prolong",
  "product_code": "OSAGO",
  "target_account_id": 12345
}
```

Response:

```
{
  "authorized": true,
  "role": "SALES",
  "effective_permissions": ["prolong"],
  "visibility_scope": "PORTFOLIO"
}
```

#### Эндпоинт получения контекста пользователя

```
GET /api/authorization/context
Authorization: Bearer {token}
```

Response:

```json
{
  "user_id": 789,
  "login": "user@company.com",
  "current_account": {
    "id": 12345,
    "type": "SALES",
    "name": "Sales Portfolio OСAGO"
  },
  "available_accounts": [
    {"id": 12345, "type": "SALES", "name": "Sales Portfolio"},
    {"id": 0, "type": "SYS_ADMIN", "name": "System Administration"}
  ],
  "effective_role": "SALES",
  "product_privileges": {
    "OSAGO": ["calculate", "prolong"],
    "KASKO": ["calculate"]
  }
}
```

### Безопасность

#### Принцип минимальных привилегий

* Права наследуются, но могут быть ограничены на любом уровне
* Явный запрет перекрывает наследуемые разрешения
* ADMIN-роли требуют явной выдачи прав для операций продаж

#### Аудит доступа

```
CREATE TABLE access_audit (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP DEFAULT NOW(),
    success BOOLEAN NOT NULL
);
```

###
