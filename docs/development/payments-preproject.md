# Предпроект: модуль оплаты премии

## 1) Что уже понятно из требований

- Нужен учет графика взносов по полису (`installments`).
- Нужен учет попыток/фактов оплаты (`payments`).
- Нужна аллокация платежа на взнос(ы) (`allocations`).
- Нужны операции сервиса:
  - создание графика взносов,
  - создание платежа,
  - смена статуса платежа с бизнес-правилами.
- Нужны API:
  - `POST /installments/{id}/payments`
  - `POST /payments/{id}` (обновление статуса).

## 2) Предлагаемая область ответственности

- **pt-api**: DTO + интерфейс `PaymentService`.
- **pt-db**: entity/repository/service impl.
- **pt-launcher** или **pt-process**: REST-контроллер (в зависимости от принятой структуры по платежам).
- Миграции: `pt-launcher/src/main/resources/db/migration`.

## 3) Нормализованная модель данных (предложение)

### 3.1 `pmt_installments`

- `id` bigint PK (`pmt_seq`)
- `tid` bigint not null
- `policy_id` bigint not null FK -> `policy_index.id`
- `installment_nr` int not null
- `period_from` date not null
- `period_to` date not null
- `due_date` date null
- `amount` decimal(18,2) not null
- `currency` varchar(3) not null
- `status` varchar(20) not null  
  `UNPAID | PARTIAL | PAID | OVERDUE`
- `created_at` timestamp not null default now()
- `updated_at` timestamp not null default now()

Индексы:
- `(tid, policy_id, installment_nr)` unique
- `(policy_id, installment_nr)` unique
- `(policy_id, status)`
- `(due_date, status)`

### 3.2 `pmt_installment_templates`

- `id` bigint PK (`pmt_seq`)
- `tid` bigint not null
- `installment_type` varchar(30) not null unique
- `installment_template` jsonb not null
- `created_at` timestamp not null default now()
- `updated_at` timestamp not null default now()

`installment_template`:
```json
[
  { "installment_nr": 1, "percent": 50, "period_months": 6 },
  { "installment_nr": 2, "percent": 50, "period_months": 6 }
]
```

### 3.3 `pmt_payments`

- `id` bigint PK (`pmt_seq`)
- `tid` bigint not null
- `amount` decimal(18,2) not null
- `currency` varchar(3) not null
- `payment_method` varchar(20) not null  
  `CASH | CARD | SBP | BANK_TRANSFER`
- `status` varchar(20) not null  
  `INITIATED | SUCCESS | FAILED | CANCELLED`
- `paid_at` timestamp null
- `received_at` timestamp null
- `external_id` varchar(100) null
- `operator_id` uuid null
- `comment` text null
- `created_at` timestamp not null default now()
- `updated_at` timestamp not null default now()

### 3.4 `pmt_allocations`

- `id` bigint PK (`pmt_seq`) — лучше добавить surrogate key
- `tid` bigint not null
- `installment_id` bigint not null FK -> `pmt_installments.id`
- `payment_id` bigint not null FK -> `pmt_payments.id`
- `amount` decimal(18,2) not null
- `balance` decimal(18,2) null
- `created_at` timestamp not null default now()

Индексы:
- `(installment_id)`
- `(payment_id)`
- unique `(installment_id, payment_id)` (если одна аллокация на пару)

## 4) Бизнес-правила

## 4.1 Создание графика (`createInstallments`)

Вход:
- `policyId`, `startDate`, `totalAmount`, `currency`, `installmentType`.

Шаги:
- Получить template по `installmentType`.
- Для каждой строки template создать взнос:
  - `amount_i = round(totalAmount * percent_i / 100, 2)`.
  - `period_from/period_to` по `period_months`.
  - `installment_nr` из template.
  - `status = UNPAID`.
- Проверка: сумма `amount_i` = `totalAmount` (с учетом округления; остаток в последний взнос).

## 4.2 Создание платежа (`createPayment`)

Вход:
- `installmentId`, `paymentMethod`, опционально `amount`, `currency`.

Шаги:
- Вставить запись в `pmt_payments` со статусом `INITIATED`.
- Если `amount/currency` не переданы — брать из installment.
- Для `payment_method = CASH`:
  - если `operator_id` в запросе не задан, проставлять текущий `account_id` из user context.
- Возвращать `paymentId`.

## 4.3 Обновление статуса платежа (`paymentStatus`)

Вход:
- `paymentId`, `newStatus`, опционально `externalId`, `paidAt`, `comment`.

Ограничения:
- Менять можно только из `INITIATED`.
- Иначе ошибка `409`/`422`.

Логика:
- `SUCCESS`:
  - Допускается разнесение **одного payment на несколько installments**.
  - Разнесение строго в порядке `installment_nr` (ascending).
  - Остаток платежа переносится на следующий взнос.
  - Создать запись(и) в `pmt_allocations`.
  - Пересчитать `installment.status`: `UNPAID -> PARTIAL -> PAID`.
- `FAILED`/`CANCELLED`:
  - Только смена статуса payment, без аллокаций.

## 5) API (черновик контракта)

### 5.1 `POST /api/v1/{tenant}/payments/installments/{id}/payments`

Request:
```json
{
  "paymentMethod": "CARD",
  "amount": 1000.00,
  "currency": "RUB",
  "comment": "manual retry"
}
```

Response:
```json
{
  "paymentId": 12345,
  "status": "INITIATED"
}
```

### 5.2 `POST /api/v1/{tenant}/payments/{id}`

Request:
```json
{
  "status": "SUCCESS",
  "externalId": "provider_txn_777",
  "paidAt": "2026-04-30T12:00:00Z"
}
```

Response:
```json
{
  "paymentId": 12345,
  "status": "SUCCESS"
}
```

### 5.3 (рекомендуется добавить)

- `GET /payments/{id}`
- `GET /installments/{id}`
- `GET /policies/{policyId}/installments`

### 5.4 Webhook (без REST-контроллера на текущем этапе)

- В `PaymentService` добавить метод обработки callback от банка, например:
  - `void processProviderCallback(String providerCode, String payload)`
- Формат payload пока не фиксируется (ожидается уточнение от банка).

## 6) Ошибки/валидация

- `404` — installment/payment/policy not found.
- `400` — invalid body/status/method.
- `409` — invalid status transition.
- `422` — business rule violation (over-allocation, invalid distribution state).

## 7) Транзакционность

- `paymentStatus(SUCCESS)` выполнять в одной транзакции:
  - lock payment row (`FOR UPDATE`),
  - проверка текущего статуса,
  - запись allocation(ов) в порядке `installment_nr`,
  - обновление installment/payment.

## 8) Наблюдаемость

- Логировать:
  - `policyId/installmentId/paymentId/status`,
  - `externalId`,
  - итоговые суммы.
- Добавить метрики:
  - count successful/failed/cancelled payments,
  - average payment processing time.

## 9) Минимальный план реализации

1. Flyway миграция `V*_payments.sql`.
2. DTO + API interface (`pt-api`).
3. Entity/repository/service impl (`pt-db`).
4. Controller + validation (`pt-launcher`/`pt-process`).
5. Unit tests:
   - installment schedule rounding,
   - status transition guard,
   - allocation + installment status recalculation.

## 10) Зафиксированные решения

- Один `payment` может покрывать несколько `installments`; разнесение по `installment_nr`, остаток переносится дальше.
- `tid` обязателен во **всех** платежных таблицах и методах сервиса.
- Все расчеты выполняются в одной валюте: **RUB**.
- Нужен метод webhook-обработки в сервисе, но REST-контроллер пока не делаем.
- Для `CASH` `operator_id` не обязателен; если не передан — берем текущий `account_id` из user context.
- Историю смены статусов отдельной audit-таблицей не храним.

