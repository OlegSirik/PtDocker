# OpenAPI Guidelines

## Версия спецификации
Используется OpenAPI 3.0+

## Структура
- paths — только HTTP-контракты
- components/schemas — DTO
- components/securitySchemes — OAuth2
- components/responses — общие ошибки

## Именование схем
- PascalCase
- Суффиксы: Request / Response / DTO

Пример:

PolicyCreateRequest
PolicyResponse
Nullable
nullable используется явно

отсутствие поля ≠ null

Форматы
date: ISO-8601

datetime: RFC-3339

money: string (decimal)

Повторное использование
Все общие структуры выносятся в components

Копирование запрещено