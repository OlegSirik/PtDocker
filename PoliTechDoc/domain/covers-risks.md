
# Страхование от несчастных случаев (НС)

## 1. Базовые риски (Risk Code)

| Risk Code | Risk name |
|-----------|-----------|
| `ACCIDENTAL_DEATH` | Смерть в результате несчастного случая |
| `PERMANENT_TOTAL_DISABILITY` | Полная постоянная инвалидность |
| `PERMANENT_PARTIAL_DISABILITY` | Частичная постоянная инвалидность |
| `TEMPORARY_TOTAL_DISABILITY` | Временная полная нетрудоспособность |
| `TEMPORARY_PARTIAL_DISABILITY` | Временная частичная нетрудоспособность |
| `MEDICAL_EXPENSES_ACCIDENT` | Медицинские расходы после несчастного случая |
| `HOSPITALIZATION_ACCIDENT` | Госпитализация |
| `SURGERY_BENEFIT` | Хирургическая операция |
| `FRACTURE` | Переломы |
| `BURNS` | Ожоги |
| `DENTAL_INJURY` | Травмы зубов |
| `DISMEMBERMENT` | Потеря конечности |
| `LOSS_OF_SIGHT` | Потеря зрения |
| `LOSS_OF_HEARING` | Потеря слуха |
| `LOSS_OF_SPEECH` | Потеря речи |

---

## 2. Основные группы покрытий

### 2.1. Несчастный случай (Accident)

| Код | Описание |
|-----|----------|
| `ACCIDENT_DEATH` | Смерть в результате несчастного случая |
| `ACCIDENT_DISABILITY` | Инвалидность в результате несчастного случая |
| `ACCIDENT_TEMP_DISABILITY` | Временная нетрудоспособность в результате несчастного случая |
| `ACCIDENT_DISMEMBERMENT` | Утрата конечностей в результате несчастного случая |
| `ACCIDENT_BURNS_FRACTURES` | Переломы и ожоги в результате несчастного случая |
| `ACCIDENT_INTERNAL_INJURY` | Внутренние повреждения в результате несчастного случая |
| `ACCIDENT_FACIAL_SCARS` | Шрамы и обезображивание лица в результате несчастного случая |

### 2.2. Заболевания (Disease)

| Код | Описание |
|-----|----------|
| `DISEASE_DEATH` | Смерть от заболевания, впервые диагностированного в период страхования |
| `DISEASE_DISABILITY` | Инвалидность I или II группы в связи с заболеванием |
| `DISEASE_CRITICAL` | Диагностирование критического заболевания (онкология, инфаркт, инсульт и др.) |
| `DISEASE_SURGERY` | Проведение хирургической операции в связи с заболеванием |

### 2.3. Медицинские расходы

| Код | Описание |
|-----|----------|
| `MEDEXPENSE_ACCIDENT` | Медицинские расходы при несчастном случае |
| `HOSPITALIZATION_ACCIDENT` | Госпитализация в результате несчастного случая |
| `HOSPITALIZATION_DISEASE` | Госпитализация в связи с заболеванием |
| `ICU_DAILY_BENEFIT` | Пребывание в отделении реанимации (фиксированная выплата за сутки) |
| `REHABILITATION_ACCIDENT` | Реабилитация после несчастного случая |

### 2.4. Travel (поездки)

| Код | Описание |
|-----|----------|
| `EVACUATION_REPATRIATION` | Эвакуация и репатриация (транспортировка в клинику или на родину) |
| `TRAVEL_DELAY` | Задержка рейса (компенсация суточных и дополнительных расходов) |
| `BAGGAGE_LOSS` | Утрата или кража багажа во время поездки |

### 2.5. Социальные риски

| Код | Описание |
|-----|----------|
| `JOB_LOSS` | Потеря работы (дохода от заработной платы) |

### 2.6. Специальные расширения

| Код | Описание |
|-----|----------|
| `SPORT_EXTENSION` | Занятие спортом (включая любительские соревнования и активный отдых) |
| `TERROR_ACT` | Травма или смерть в результате террористического акта или диверсии |
| `PUBLIC_TRANSPORT_ACCIDENT` | Травма в результате несчастного случая в общественном транспорте |

---

## 3. Комплексные покрытия

| Cover Code | Cover Name | Входящие риски |
|------------|------------|----------------|
| `COMPLEX_ACCIDENT` | Покрытие по договору НС (комплексное) | `ACCIDENT_DEATH`, `ACCIDENT_DISABILITY`, `ACCIDENT_TEMP_DISABILITY`, `ACCIDENT_DISMEMBERMENT` |
| `COMPLEX_DISEASE` | Покрытие по критическим заболеваниям (комплексное) | `DISEASE_DEATH`, `DISEASE_DISABILITY`, `DISEASE_CRITICAL` |
| `TRAVEL_PROTECTION` | Защита путешественника | `EVACUATION_REPATRIATION`, `TRAVEL_DELAY`, `BAGGAGE_LOSS`, `MEDEXPENSE_ACCIDENT` |

---







# Страхование бытовой техники

| Risk Code            | Risk                        |
| -------------------- | --------------------------- |
| MECHANICAL_BREAKDOWN | механическая поломка        |
| ELECTRICAL_FAILURE   | электрическая неисправность |
| POWER_SURGE          | скачок напряжения           |
| ACCIDENTAL_DAMAGE    | случайное повреждение       |
| LIQUID_DAMAGE        | повреждение жидкостью       |
| DROPPING_IMPACT      | падение или удар            |
| THEFT                | кража                       |
| BURGLARY             | кража со взломом            |
| FIRE                 | пожар                       |
| LIGHTNING            | удар молнии                 |
| EXPLOSION            | взрыв                       |
| NATURAL_DISASTER     | стихийные бедствия          |

Основные покрытия (Coverages)

Repair coverage - REPAIR
Покрывает:
 MECHANICAL_BREAKDOWN
 ELECTRICAL_FAILURE
Выплата: cost of repair

Replacement coverage - REPLACEMENT
если ремонт невозможен.
Выплата:  replacement cost

Accidental damage - ACCIDENTAL_DAMAGE
Покрывает:
 dropping
 impact
 liquid damage

Power surge protection - POWER_SURGE_DAMAGE

Theft coverage - THEFT_PROTECTION
Покрывает: THEFT, BURGLARY

Extended warranty - EXTENDED_WARRANTY
покрывает: mechanical breakdown after manufacturer warranty

Transportation damage - TRANSPORT_DAMAGE
если устройство повреждено при перевозке.

Data recovery (для электроники) - DATA_RECOVERY


Страхование пассажиров (Passenger Insurance)

Основные риски
Risk Code	Risk
TRANSPORT_ACCIDENT	транспортное происшествие
ACCIDENTAL_DEATH	смерть
PERMANENT_TOTAL_DISABILITY	полная инвалидность
PERMANENT_PARTIAL_DISABILITY	частичная инвалидность
TEMPORARY_DISABILITY	временная нетрудоспособность
INJURY	травма
HOSPITALIZATION	госпитализация
MEDICAL_EXPENSES	медицинские расходы

Основные покрытия
Accidental death
PASSENGER_ACCIDENTAL_DEATH

выплата:

sum insured
Permanent disability
PASSENGER_DISABILITY

покрывает:

PERMANENT_TOTAL_DISABILITY
PERMANENT_PARTIAL_DISABILITY
Medical expenses
PASSENGER_MEDICAL

покрывает:

treatment
medicine
doctor services
Hospital cash
PASSENGER_HOSPITAL_CASH

выплата:

daily benefit
Temporary disability
PASSENGER_TEMPORARY_DISABILITY

выплата:

weekly compensation
Emergency transport
AMBULANCE
Funeral benefit
FUNERAL_EXPENSE

Дополнительные покрытия
Baggage accident
BAGGAGE_DAMAGE
Delay benefit
TRIP_DELAY
Rescue / evacuation
RESCUE
EVACUATION


1. Accident / Injury risks

Используются в:

accident insurance
passenger insurance
travel insurance
Code	Risk
ACCIDENT	несчастный случай
ACCIDENTAL_DEATH	смерть
INJURY	травма
PERMANENT_TOTAL_DISABILITY	полная инвалидность
PERMANENT_PARTIAL_DISABILITY	частичная инвалидность
TEMPORARY_DISABILITY	временная нетрудоспособность
FRACTURE	перелом
BURNS	ожоги
DISMEMBERMENT	потеря конечности
LOSS_OF_SIGHT	потеря зрения
LOSS_OF_HEARING	потеря слуха

2. Medical risks

Используются в:

accident
travel
health riders

Code	Risk

MEDICAL_EXPENSES	медицинские расходы
HOSPITALIZATION	госпитализация
SURGERY	операция
AMBULANCE	скорая помощь
EVACUATION	медицинская эвакуация
REHABILITATION	реабилитация
DENTAL_INJURY	травма зубов

3. Property risks

Используются в:

home insurance
gadget insurance
appliance insurance
Code	Risk

FIRE	пожар
LIGHTNING	молния
EXPLOSION	взрыв
SMOKE_DAMAGE	дым
WATER_DAMAGE	вода
FLOOD	наводнение
EARTHQUAKE	землетрясение
STORM	шторм
HAIL	град

4. Theft / crime risks
Code	Risk

THEFT	кража
BURGLARY	кража со взломом
ROBBERY	грабеж
VANDALISM	вандализм
5. Electronics / appliance risks

Используются в:

gadget insurance
extended warranty
appliance insurance
Code	Risk
MECHANICAL_BREAKDOWN	механическая поломка
ELECTRICAL_FAILURE	электрическая неисправность
POWER_SURGE	скачок напряжения
ACCIDENTAL_DAMAGE	случайное повреждение
LIQUID_DAMAGE	повреждение жидкостью
DROPPING_IMPACT	падение
OVERHEATING	перегрев
SHORT_CIRCUIT	короткое замыкание
6. Transport risks

Используются в:

passenger insurance
travel insurance
cargo insurance
Code	Risk
TRANSPORT_ACCIDENT	транспортная авария
AIR_ACCIDENT	авиакатастрофа
TRAIN_ACCIDENT	авария поезда
BUS_ACCIDENT	авария автобуса
SHIP_ACCIDENT	морская авария
CRASH	столкновение
7. Travel risks
Code	Risk
TRIP_CANCELLATION	отмена поездки
TRIP_INTERRUPTION	прерывание поездки
TRIP_DELAY	задержка поездки
MISSED_CONNECTION	пропущенная пересадка
8. Baggage risks
Code	Risk
BAGGAGE_LOSS	потеря багажа
BAGGAGE_DELAY	задержка багажа
BAGGAGE_DAMAGE	повреждение багажа
9. Liability risks

Используются в:

personal liability
travel liability
Code	Risk
THIRD_PARTY_BODILY_INJURY	вред здоровью третьих лиц
THIRD_PARTY_PROPERTY_DAMAGE	ущерб имуществу
LEGAL_EXPENSE	судебные расходы
10. Assistance risks
Code	Risk
MEDICAL_EVACUATION	эвакуация
BODY_REPATRIATION	репатриация тела
EMERGENCY_ASSISTANCE	экстренная помощь






# Объект cover

Тип расчета премии для покрытий задается на уровне пакета
Это может быть калькуляторб либо фиксы

Расчет - выполнить калькулятор, если есть
По всем покрытиям договора

## Премия

| Тип	| Когда использовать | Как считается |
|-------| -------------------|---------------|
| NOT_CALCULATED	| Покрытие входит в базовый тариф | Премия = null, в покрытии не заполняется |
| FIXED	| Простое покрытие с фикс ценой | Всегда берется фиксированная сумма из настройки |
| PERCENT_OF_LIMIT	| Классическое страхование (процент от суммы) | Страховая сумма из объекта страхования * процент / 100 |
| TABLE_LIMIT_PREMIUM |	Градация по страховой сумме | Берется лимит, по таблице ищется премия. Если такого лимита нет то ошибка, премия = -1 |
| CALCULATOR	| сложные расчеты (внешний сервис) | берется из vars с именем co_COVER_CODE_premium |

## Лимит ответственности, страховая сумма

| Тип	| Когда использовать | Как считается |
|-------| -------------------|---------------|
| NOT_CALCULATED	| Не считается. Могуть быть сложные усломия, которые в не оцифровываются | SumInsured = null, в покрытии не заполняется |
| FIXED	| Простое покрытие с фикс страховой суммой | Всегда берется фиксированная сумма из настройки |
| SUM_INSURED	| СТраховая сумма по покрытию = страховой сумме объекта страхования | Страховая сумма из объекта страхования |
| TABLE_LIMIT_PREMIUM |	Градация по kbvbne jndtncdtyyjcnb | Берется лимит, по таблице ищется. Если такого лимита нет то ошибка, премия = -1 |
| CALCULATOR	| сложные расчеты (внешний сервис) | берется из vars с именем co_COVER_CODE__sumInsured |

## Франшиза

Франшиза задается списком допустимых значений.
ID, Текстовое описание франшизы.
Франшиза может быть обязательная или нет.
Если обязательная, то проверятся после расчета поурытия, заполнена или нет.

Задать можно либо через 

| Тип	| Когда использовать | Как считается |
|-------| -------------------|---------------|
| INPUT	| Передается номер франшизы в запросе | Проверяется что таое значение сть в списке |
| CALCULATOR | Номер применяемой франшизы вычисляется через калькулятор | Сложные условия |
 

```
{
  "code": "COMPLEX_ACCIDENT",
  "description": "не обязательное описание поктырия",

  "isMandatory": true,

  "waitingPeriod": "P1Y",
  "coverageTerm": "P1Y",

  "calcTypeOfPremium": "INPUT, CALCULATOR, ...",
  "premiumValue": "сумма или процент или пусто",

  "calcTypeOfSumInsured": "INPUT, CALCULATIR, .."
  "limits": [
    {
      "premium": 234234,
      "sumInsured": 42234
    }
  ]

  "isDeductibleMandatory": true,
  "calcTypeOfDeductible": "INPUT, CALCULATOR",
  "deductibles": [
    {
      "id": 1,
      "text": "fgsdgh gdssdjfgdsfg"
    }
  ]
}
```

~~~

{
  "coverCode": "ACCIDENT_DEATH",
  "coverName": "Смерть в результате несчастного случая",
  
  "periods": {
    "waitingPeriod": 14,        // период ожидания (дней)
    "coveragePeriod": 365,      // период покрытия (дней)
    "benefitPeriod": 730,       // период выплаты (для потери дохода)
    "survivalPeriod": 7         // период дожития (дней для выплаты)
  },
  
  "premium": {
    "type": "CALCULATOR",       // NONE, FIXED, CALCULATOR, TABLE
    "fixedAmount": null,
    "calculatorBean": "accidentPremiumCalculator",
    "rateTable": "rate_accident_age",
    "currency": "RUB"
  },
  
  "limit": {
    "type": "TABLE",            // NONE, FIXED, CALCULATOR, TABLE
    "fixedAmount": null,
    "minAmount": 10000,
    "maxAmount": 1000000,
    "defaultAmount": 100000,
    "calculatorBean": "limitCalculator",
    "limitTable": "limit_accident_death",
    "isAggregated": false,      // агрегатный лимит на весь период
    "isPerOccurrence": true     // на каждый страховой случай
  },
  
  "deductible": {
    "type": "OPTIONAL",         // NONE, MANDATORY, OPTIONAL
    "deductibleType": "ABSOLUTE", // ABSOLUTE, RELATIVE, FRANCHISE
    "options": [                // список доступных вариантов
      {
        "code": "DED_0",
        "value": 0,
        "valueType": "PERCENT", // FIXED, PERCENT
        "name": "Без франшизы"
      },
      {
        "code": "DED_5000",
        "value": 5000,
        "valueType": "FIXED",
        "name": "5 000 руб."
      },
      {
        "code": "DED_5_PERCENT",
        "value": 5,
        "valueType": "PERCENT",
        "name": "5% от страховой суммы"
      }
    ],
    "defaultOption": "DED_0",
    "isCalculated": false,      // расчетная франшиза
    "calculationFormula": null
  },
  
  "coverage": {
    "territory": "WORLDWIDE",   // WORLDWIDE, RUSSIA, CIS, EUROPE
    "excess": {                 // эксцедент (превышение лимита)
      "enabled": false,
      "limit": 500000
    },
    "aggregateLimit": 2000000,  // общий лимит на все случаи
    "subLimits": {              // подлимиты
      "perEvent": 500000,
      "perYear": 1000000,
      "lifetime": 2000000
    }
  },
  
  "benefits": {
    "type": "TABLE",            // FIXED, PERCENT_OF_SUM, TABLE, CALCULATOR
    "fixedAmount": null,
    "percentOfSumInsured": 100,
    "benefitTable": "benefit_accident_injury",
    "benefitSchedule": {
      "death": 100,
      "disability_group_1": 100,
      "disability_group_2": 70,
      "disability_group_3": 50,
      "temporary_disability_daily": 0.5,
      "fracture": {
        "arm": 10,
        "leg": 15,
        "rib": 5
      }
    }
  },
  
  "riskFactors": {
    "age": {
      "min": 18,
      "max": 65,
      "rateMultiplier": "age_coefficient"
    },
    "profession": {
      "excluded": ["miner", "firefighter"],
      "riskGroups": {
        "1": { "name": "Офисные", "coefficient": 1.0 },
        "2": { "name": "Рабочие", "coefficient": 1.5 },
        "3": { "name": "Опасные", "coefficient": 2.5 }
      }
    },
    "sports": {
      "excluded": ["boxing", "parachuting"],
      "allowed": ["football", "basketball"]
    },
    "healthConditions": {
      "excluded": ["epilepsy", "diabetes"],
      "requiresDeclaration": true
    }
  },
  
  "exclusions": {
    "standard": [
      "SUICIDE",
      "ALCOHOL",
      "DRUGS",
      "WAR",
      "NUCLEAR"
    ],
    "custom": [
      "EXTREME_SPORTS",
      "TERRORISM"
    ],
    "waitingPeriodExclusions": {
      "enabled": true,
      "excludedPeriod": 14,
      "excludedConditions": ["ACCIDENT_DEATH"]
    }
  },
  
  "multiplicity": {
    "maxPoliciesPerInsured": 1,
    "stackable": false,         // можно ли суммировать с другими покрытиями
    "proportional": false       // пропорциональная ответственность
  },
  
  "conditions": {
    "isRenewable": true,
    "autoRenewal": false,
    "renewalLimit": 10,
    "coolingOffPeriod": 14,     // период охлаждения (дней)
    "claimReportingPeriod": 30,  // срок уведомления о страховом случае
    "documentSubmissionPeriod": 60 // срок подачи документов
  },
  
  "rules": {
    "coinsurance": {            // сострахование
      "enabled": false,
      "percentage": 20
    },
    "excessOfLoss": {           // эксцедент убытка
      "enabled": false,
      "threshold": 100000,
      "excessLimit": 500000
    },
    "indexation": {             // индексация
      "enabled": false,
      "indexationRate": 5,      // % в год
      "indexationPeriod": "YEARLY"
    },
    "bonusMalus": {             // система бонус-малус
      "enabled": false,
      "coefficients": [1.0, 0.9, 0.8, 0.7]
    }
  },
  
  "metadata": {
    "createdAt": "2025-01-01T00:00:00Z",
    "updatedAt": "2025-01-01T00:00:00Z",
    "version": 1,
    "status": "ACTIVE",         // ACTIVE, DEPRECATED, DRAFT
    "effectiveFrom": "2025-01-01",
    "effectiveTo": "2026-12-31"
  }
}

~~~



{
  "linesOfBusiness": [
    {
      "lobCode": "ACC",
      "lobName": "Страхование от несчастных случаев"
    },
    {
      "lobCode": "HLT",
      "lobName": "Медицинское страхование"
    },
    {
      "lobCode": "TRV",
      "lobName": "Страхование путешествий"
    },
    {
      "lobCode": "PAX",
      "lobName": "Страхование пассажиров"
    },
    {
      "lobCode": "ELC",
      "lobName": "Страхование бытовой техники и электроники"
    },
    {
      "lobCode": "PRP",
      "lobName": "Страхование имущества"
    },
    {
      "lobCode": "AUT",
      "lobName": "Автострахование"
    },
    {
      "lobCode": "LIF",
      "lobName": "Страхование жизни"
    },
    {
      "lobCode": "CLI",
      "lobName": "Страхование критических заболеваний"
    },
    {
      "lobCode": "LIA",
      "lobName": "Страхование гражданской ответственности"
    },
    {
      "lobCode": "FIN",
      "lobName": "Финансовые риски"
    },
    {
      "lobCode": "GAD",
      "lobName": "Страхование гаджетов"
    }
  ]
}


{
  "covers": [
    {
      "coverCode": "PAX_ACCIDENT",
      "coverName": "Несчастный случай с пассажиром",
      "risks": [
        "ACCIDENT_DEATH",
        "ACCIDENT_DISABILITY"
      ]
    },
    {
      "coverCode": "PAX_DEATH",
      "coverName": "Смерть пассажира в результате несчастного случая",
      "risks": [
        "ACCIDENT_DEATH"
      ]
    },
    {
      "coverCode": "PAX_DISABILITY",
      "coverName": "Инвалидность пассажира вследствие несчастного случая",
      "risks": [
        "ACCIDENT_DISABILITY"
      ]
    },
    {
      "coverCode": "PAX_INJURY",
      "coverName": "Травмы пассажира",
      "risks": [
        "ACCIDENT_INJURY"
      ]
    },
    {
      "coverCode": "PAX_MEDICAL",
      "coverName": "Медицинские расходы пассажира",
      "risks": [
        "MEDICAL_EXPENSE"
      ]
    }
  ]
}


{
  "covers": [
    {
      "coverCode": "DEVICE_ACCIDENTAL_DAMAGE",
      "coverName": "Случайное повреждение устройства",
      "risks": "ACCIDENTAL_DAMAGE, SCREEN_DAMAGE, LIQUID_DAMAGE"
    },
    {
      "coverCode": "DEVICE_THEFT",
      "coverName": "Кража устройства",
      "risks": "THEFT, ROBBERY, BURGLARY"
    },
    {
      "coverCode": "DEVICE_LOSS",
      "coverName": "Утрата устройства",
      "risks": "LOSS"
    },
    {
      "coverCode": "DEVICE_FIRE_DAMAGE",
      "coverName": "Повреждение устройства пожаром",
      "risks": "FIRE_DAMAGE"
    },
    {
      "coverCode": "DEVICE_POWER_SURGE",
      "coverName": "Повреждение из-за скачка напряжения",
      "risks": "LIGHTNING_SURGE, ELECTRICAL_FAILURE"
    },
    {
      "coverCode": "DEVICE_MECHANICAL_BREAKDOWN",
      "coverName": "Механическая поломка",
      "risks": "MECHANICAL_BREAKDOWN, BATTERY_FAILURE"
    },
    {
      "coverCode": "DEVICE_NATURAL_DISASTER",
      "coverName": "Повреждение вследствие стихийных бедствий",
      "risks": "NATURAL_DISASTER"
    }
  ]
}