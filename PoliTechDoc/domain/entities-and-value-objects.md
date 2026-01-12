# Entities и Value Objects

## Entities
Entities имеют идентичность и жизненный цикл.

Примеры:
- Policy
- Claim
- Payment

## Value Objects
Value Objects:
- не имеют идентичности
- неизменяемы
- сравниваются по значению

Примеры:
- Money
- DateRange
- CoverageLimit
- Address

## Правила
- Value Objects не имеют setters
- Любая валидация происходит в конструкторе
