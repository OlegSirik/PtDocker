# Aggregates

## Принципы
- Aggregate — граница консистентности
- Только Aggregate Root доступен извне
- Инварианты соблюдаются внутри aggregate

## Основные агрегаты

### Policy (Aggregate Root)
- PolicyId
- Status
- Insured Objects
- Coverages
- Premium
- Versions

### Product
- ProductCode
- Version
- Coverages
- Rules

### Claim
- ClaimId
- PolicyId
- Status

## Запрещено
- Ссылки между агрегатами по объектам
- Каскадные изменения
