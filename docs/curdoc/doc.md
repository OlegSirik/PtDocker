# Страховая платформа

## Архитектура, безопасность и доменная модель

### Техническая книга системы

Рабочая редакция v1.0 — 2026


---


# 0. Как читать эту книгу

Эта книга предназначена как единое техническое описание платформы: от общей архитектуры и границ модулей до аутентификации, авторизации, мультитенантности, продуктовой модели, расчёта, полиса и структуры страхового контракта.
Документ разделяет три уровня достоверности: зафиксировано — подтверждено текущим проектным контекстом или исходным кодом; архитектурное решение — принято в ходе проектирования; TO BE DECIDED — вопрос пока не подтверждён реализацией.
В книге также учтены ранее зафиксированные решения по модели страхового контракта: Party snapshot, masterPartyId, insuredObject, транспорт, груз, авиаперелёты, consumer electronics и специализированные *Attributes.

## Источники

- Проектный контекст CONTEXT.md, предоставленный в текущей рабочей сессии.

- Ранее выполненный анализ репозитория OlegSirik/PtDocker и его модулей.

- Зафиксированные в рабочем процессе архитектурные решения по страховым данным.

Прямое чтение текущего каталога docs репозитория в этой сессии не удалось надёжно выполнить через публичный доступ, поэтому содержимое конкретных файлов docs не считается проверенным источником для этой редакции.

---


# Содержание


---


# 1. Назначение и границы платформы

Платформа предназначена для автоматизации страхового бизнеса: управления страховыми продуктами, расчёта стоимости, подготовки и выпуска страховых договоров, хранения документов, работы с платежами и поддержки дополнительных страховых процессов.
В текущем техническом контексте платформа строится как единое backend-приложение с модульным разделением ответственности. Это позволяет сохранять единый транзакционный контур и одновременно разделять домены на уровне Java-модулей.

## Что является центральным объектом

Центральным бизнес-объектом выступает страховой контракт/полис. Он связывает продукт, застрахованный объект, участников договора, покрытия, тарифные параметры, документы и финансовые данные.

## Граница ответственности

- Продукт определяет, что и на каких условиях страхуется.

- Calculator вычисляет стоимость на основе тарифных данных и формул.

- Rules применяет бизнес-правила и проверки.

- Policy фиксирует результат оформления как исторический snapshot.

- Files отвечает за документы и объектное хранилище.

- Payments содержит финансовый контур; точный provider lifecycle требует дальнейшей фиксации.


---


# 2. Архитектурный стиль

Текущая архитектура определяется как modular monolith: один backend runtime, внутри которого существуют отдельные Gradle-модули с собственными зонами ответственности.

```text
Client / Browser / Partner
          |
          v
      HTTP API
          |
          v
+-----------------------------------------------+
|                 Backend                       |
|                                               |
| api | auth | product | calculator | rules    |
| process | numbers | files | payments | db    |
+-----------------------+-----------------------+
                        |
                        v
                   PostgreSQL

```


## Почему модульный монолит

- Единая транзакционная модель для критических страховых операций.

- Меньше инфраструктурной сложности, чем у набора микросервисов.

- Явные границы доменов можно сохранить внутри кода.

- При необходимости отдельный модуль впоследствии может стать самостоятельным сервисом.

Микросервисная декомпозиция не должна считаться текущим фактом. Она может быть будущей эволюцией, но не является описанием сегодняшнего runtime.

---


# 3. Общая архитектура системы


```text
                    +----------------------+
                    |       Frontend        |
                    | Angular / TypeScript  |
                    +----------+-----------+
                               |
                               v
                    +----------------------+
                    |      pt-api           |
                    | REST / OpenAPI        |
                    +----------+-----------+
                               |
        +----------------------+----------------------+
        |          |            |          |           |
        v          v            v          v           v
     pt-auth   pt-product   pt-calculator pt-rules  pt-process
        |          |            |          |           |
        +----------+------------+----------+-----------+
                               |
             +-----------------+------------------+
             |                 |                  |
             v                 v                  v
         pt-numbers         pt-files          pt-payments
             |                 |                  |
             +-----------------+------------------+
                               |
                               v
                         pt-db / PostgreSQL

```

Frontend — Angular/TypeScript. Backend — Java 21 / Spring Boot 3.3.x. Хранилище данных — PostgreSQL. Для файлов используется MinIO, для PDF-операций — PDFBox. Для миграций в текущем backend-контексте используется Flyway.

## Технологический контур


Слой | Технология / компонент

|---|---|

|Frontend|Angular / TypeScript|

|Backend|Java 21, Spring Boot 3.3.2|

|API|Spring Web, Springdoc OpenAPI|

|Persistence|Spring Data JPA / PostgreSQL|

|Security|Spring Security, JWT-related components, Keycloak configuration|

|Migrations|Flyway|

|Files|MinIO, PDFBox|

|Rules|CEL|

|Build|Gradle 8.7|

|Runtime|Docker, Corretto 21 Alpine|


---


# 4. Модульная структура backend


Модуль | Ответственность

|---|---|

|pt-api|API и внешние HTTP-контракты|

|pt-auth|Аутентификация, авторизация, Account/Privilege/Scope|

|pt-db|Доступ к данным и persistence|

|pt-numbers|Генерация номеров|

|pt-process|Процессные операции и orchestration|

|pt-product|Страховые продукты и версии продуктов|

|pt-calculator|Тарифный расчёт|

|pt-files|Файлы, документы, object storage|

|pt-payments|Платёжный контур|

|pt-rules|Бизнес-правила и CEL|

|pt-launcher|Сборка/запуск приложения|

Это разделение является архитектурной границей модульного монолита. Оно не означает наличие отдельного deployable-сервиса на каждый модуль.

## Направление зависимостей

API вызывает прикладные модули; прикладные модули используют persistence и инфраструктурные компоненты через определённые границы. Главная цель — не допустить превращения модульного монолита в неструктурированный общий слой.

---


# 5. Frontend и пользовательский интерфейс

Frontend построен на Angular/TypeScript. В текущем контексте используются Angular Router, Angular Material, Formly, RxJS, Chart.js и интеграция с Keycloak.
Маршрутизация содержит tenantId как часть URL-контекста. Это важно: tenant является не только внутренним параметром backend, но и частью пользовательского контекста frontend.

```text
/:tenantId/...
       |
       +-- authGuard
       |
       +-- TenantGuard
       |
       +-- tenant-scoped application UI

```

Таким образом, frontend должен передавать и поддерживать tenant context, а backend обязан независимо проверять принадлежность текущего principal к tenant scope. Нельзя считать URL единственным механизмом изоляции.

---


# 6. Аутентификация

Аутентификация отвечает на вопрос: кто выполняет запрос? В проекте присутствует Keycloak-конфигурация и JWT-инфраструктура. Точная граница ответственности между Keycloak и приложением должна быть подтверждена текущими security-конфигурациями и deployment setup.

## Концептуальный поток


```text
User / Client
     |
     | credentials / SSO
     v
 Identity Provider (Keycloak)
     |
     | access token
     v
 Frontend
     |
     | Authorization: Bearer <token>
     v
 Backend / Spring Security
     |
     +--> validate token
     +--> resolve Principal
     +--> resolve Account / tenant context

```


## Что не следует смешивать

- Authentication — проверка личности/credentials.

- Authorization — проверка разрешений.

- Tenant isolation — проверка области данных.

- Acting account — контекст, от имени которого выполняется операция.

Даже валидный JWT не должен автоматически давать доступ ко всем данным платформы.

---


# 7. Авторизация

В текущей модели авторизация строится не только вокруг ролей. Зафиксирована комбинация Account hierarchy, explicit privileges и data scope.

## Privilege

Privilege имеет формат &lt;RESOURCE&gt;:&lt;ACTION&gt;. Примеры концептуально: POLICY:READ, POLICY:CREATE, PRODUCT:UPDATE.

## Account и Acting Account

Account представляет субъект/организационный контекст доступа. Acting Account позволяет явно фиксировать, от имени какого аккаунта выполняется действие, что особенно важно при иерархии организаций, брокеров, агентов и дочерних аккаунтов.

## Модель проверки


```text
Request
  |
  v
Authenticated Principal
  |
  +--> Is authenticated?
  |
  +--> Which Account?
  |
  +--> Which Acting Account?
  |
  +--> Has required Privilege?
  |
  +--> Is resource inside Data Scope?
  |
  v
Allow / Deny

```

Privilege отвечает на вопрос <i>что можно сделать</i>, а data scope — <i>с какими данными можно это сделать</i>.

---


# 8. Мультитенантность и изоляция данных

Мультитенантность — одна из ключевых архитектурных характеристик платформы. В текущей модели tenant связан с Account hierarchy, а изоляция данных строится через дерево аккаунтов и область видимости.

## Концепция


```text
                 Root Account
                 /                          /                      Tenant A          Tenant B
          /     \             |
       Agent A1 Broker A1   Agent B1

```

Дочерний account может видеть только разрешённый ему scope. Для представления иерархии используется materialized path/дерево аккаунтов; конкретная схема таблиц должна считаться реализационной деталью и подтверждаться текущей DB-моделью.

## Критическое правило


## DEV / PROD

В модели присутствуют DEV/PROD scopes. Это позволяет отделять тестирование/администрирование продуктовых конфигураций от production-использования. Product admin может тестировать конфигурацию в DEV, тогда как sales-контур работает с PROD.

---


# 9. Product Domain

Продукт является конфигурацией страхового предложения. Внутри него находятся версии, покрытия, риски, тарифные таблицы, формулы, правила и документы.

```text
Product Family / Group
          |
          v
       Product
          |
          +---- Product Version
                    |
          +---------+---------+---------+
          |         |         |         |
       Coverages  Risks    Rates      Rules
                                                                     +-- Formulas

```


## Версионирование

Опубликованная Product Version не должна изменяться задним числом. Lifecycle: DRAFT → PUBLISHED → DEPRECATED.
Это необходимо для воспроизводимости расчёта и исторической корректности полиса: старый договор должен ссылаться на ту версию продукта, на которой он был оформлен.

---


# 10. Pricing, Calculator и Rules


## Calculator

Модуль Calculator использует rate tables, formulas и variables. Общая концепция расчёта:

```text
Input contract data
       |
       v
Risk / Rating factors
       |
       v
Rate Tables + Formulas
       |
       v
Adjustments / Discounts
       |
       v
Premium

```


## Rules

Модуль Rules использует CEL для вычисления бизнес-условий. Это позволяет отделять конфигурируемые eligibility/validation правила от Java-кода.

## Среды

- DEV — настройка и тестирование продукта/правил.

- PROD — использование опубликованной версии.

- Published configuration должна быть воспроизводимой.

Quote и Policy могут использовать один и тот же входной InsuranceContractRequest, если фактические поля совпадают. Разделение Quote и Policy является прежде всего lifecycle/resource distinction, а не обязательным различием входного DTO.

---


# 11. Quote и Policy lifecycle


```text
InsuranceContractRequest
          |
          +--> POST /quotes
          |       |
          |       +--> calculation
          |       +--> quote
          |
          +--> POST /policies
                  |
                  +--> validation
                  +--> calculation
                  +--> issuance
                  +--> policy snapshot

```


## Quote

Quote — результат предварительного расчёта и предложения. Его задача — показать условия и стоимость до окончательного выпуска.

## Policy

Policy — зафиксированный страховой договор. Для него критична историческая неизменяемость исходных данных: Party и Insured Object сохраняются как snapshot.

## Snapshot semantics

Если Party была создана на основе Master Data через masterPartyId, изменение Master Data после выпуска полиса не должно менять уже существующий полис.

---


# 12. Страховой контракт

Страховой контракт — общий бизнес-контракт для расчёта и оформления. Он включает productCode, даты, валюту, тип рассрочки, policyHolder и insuredObject.

```text
Contract
├── productCode
├── startDate
├── endDate
├── issueDate
├── currencyCode
├── installmentType
├── policyHolder
└── insuredObject

```


## Принцип

Контракт описывает страхуемую ситуацию, а не внутреннюю структуру базы данных. Поэтому JSON-контракт должен документироваться как business model, а JPA entity — как implementation model.

## Party metadata


```text
party: {
  partyId: "...",
  masterPartyId: "..."
}

```

partyId идентифицирует Party snapshot внутри договора. masterPartyId указывает на источник Master Data, из которого данные были скопированы.

---


# 13. Party и Master Data

Party — универсальный участник страхового договора. Он может быть физическим или юридическим лицом и может выступать в разных ролях.

```text
Party
├── party
│   ├── partyId
│   └── masterPartyId
├── person?
├── organization?
├── documents[]
├── addresses[]
├── phones[]
└── emails[]

```


## Person

Person содержит персональные атрибуты, включая INN и SNILS. Эти идентификаторы не следует превращать в документы.

## Organization

Organization содержит реквизиты юридического лица: INN, KPP, OGRN, полное наименование, legalForm, country, residency.

## Documents

Documents предназначены для удостоверяющих, регистрационных и квалификационных документов: паспорт, водительское удостоверение, регистрационные документы юридического лица и т.п.

## Snapshot

Party, вложенная в policy, является снимком на момент операции. Master Data — источник, но не runtime-ссылка с динамическим разрешением.

---


# 14. Insured Object

InsuredObject — расширяемый бизнес-объект. Он не должен быть привязан к одному виду страхования.

```text
insuredObject
├── party?
├── person?
├── organization?
├── documents[]
├── addresses[]
├── phones[]
├── emails[]
├── vehicle?
├── vehicleDocuments[]
├── vehicleOwner?
├── drivers[]
├── cargo?
├── carrier?
├── transport[]
├── delivery?
├── consumerElectronics?
├── flightSegments[]
├── packageCode?
├── sumInsured?
├── cascoAttributes?
├── accidentAttributes?
├── medicalAttributes?
├── cargoAttributes?
└── ...future *Attributes

```


## Главное правило расширения

Базовые/common objects располагаются непосредственно внутри insuredObject. Специализированные свойства конкретного страхового типа оформляются отдельными именованными блоками *Attributes. Универсального programAttributes-контейнера нет.

---


# 15. Транспорт, груз и доставка


## Transport

Transport является массивом, потому что груз может перемещаться мультимодально и проходить через перегрузки.

```text
transport: [
  { typeCode: "ROAD", vehicle: {...} },
  { typeCode: "SEA",  vessel:  {...} },
  { typeCode: "ROAD", vehicle: {...} }
]

```


## Carrier

Carrier — Party, выступающая перевозчиком.

## Vessel

Для морского/речного сегмента vessel может содержать название судна, IMO, флаг, тип, год постройки, call sign, порт регистрации, тоннаж, deadweight и сведения о владельце/операторе.

## Delivery

Delivery — самостоятельный объект внутри insuredObject, а не подчинённый только cargo.

## Addresses

insuredObject.addresses[] может содержать адреса, относящиеся к самому застрахованному объекту или маршруту перевозки. Для cargo transport возможны ORIGIN, TRANSSHIPMENT и DESTINATION; промежуточных точек может быть несколько.

---


# 16. Специализированные виды страхования


## Автострахование


```text
vehicle
├── vehicleModel
├── VIN
├── bodyNumber
├── chassisNumber
├── usedSince
├── mileage
├── cost
├── yearIssue
├── vehicleCategoryCode
└── vehicleTypeCode

```

Дополнительные параметры CASCO располагаются в cascoAttributes.

## Accident / Medical

Для страхования человека insuredObject может непосредственно содержать party/person/documents/addresses/phones/emails. Отдельный объект insuredPerson не требуется, если insuredObject уже является самим застрахованным лицом.

## Consumer electronics

consumerElectronics — базовый объект для страхования бытовой/потребительской электроники.

## Air passenger

flightSegments[] — массив сегментов поездки. Каждый сегмент содержит билетную стоимость, валюту, аэропорты отправления/прибытия, дату-время и авиакомпанию.

---


# 17. Документы и файловое хранилище

pt-files отвечает за файловый контур. В текущей технологии используются MinIO и PDFBox.

```text
Business document
       |
       v
Document metadata
       |
       +------> Object Storage (MinIO)
       |
       +------> PDF generation / processing (PDFBox)

```

Важно разделять документ как бизнес-сущность и файл как бинарный объект. Policy может иметь несколько документов, а один document record должен иметь понятный lifecycle и связь с бизнес-объектом.
Email-зависимости присутствуют в backend-контексте, однако точная схема notification service в текущем источнике не зафиксирована.

---


# 18. Номера и идентификаторы

pt-numbers отвечает за генерацию бизнес-номеров. В контексте зафиксирована поддержка placeholder-формата и reset policies, а для последовательностей упоминается pessimistic locking.

## Разные виды идентификаторов


Идентификатор | Назначение

|---|---|

|Technical ID|Внутренний уникальный идентификатор сущности|

|Business number|Номер полиса/документа, видимый бизнесу|

|partyId|Идентификатор Party snapshot|

|masterPartyId|Источник Party в Master Data|

|productCode|Код продукта|

Business number и technical ID не должны смешиваться. Номер полиса может иметь бизнес-правила генерации и форматирование, тогда как technical ID должен оставаться стабильным техническим ключом.

---


# 19. Платежи и комиссии

Модуль pt-payments существует в текущем backend. Однако конкретный payment provider и полный lifecycle в предоставленном контексте не зафиксированы.

```text
Policy / Invoice
      |
      v
Payment Created
      |
      v
Provider / payment channel
      |
      v
Result
      |
      +--> success
      +--> failure
      +--> refund

```

Для финансовых операций рекомендуется обеспечивать идемпотентность, correlation ID и явное хранение provider reference. Эти пункты относятся к архитектурным требованиям, если соответствующий финансовый workflow будет реализован.
Комиссии также присутствуют как доменная область, но точная схема settlement не подтверждена текущим контекстом.

---


# 20. Claims и незавершённые области

В контексте присутствует privilege для claims, однако отдельного pt-claims модуля нет. Следовательно, claims следует считать частично определённой/незавершённой областью, а не готовым самостоятельным bounded context.
Шаблон CONTEXT.md содержит типовой FNOL/Claims lifecycle, но он не является доказательством реализации в текущем репозитории.

## Event-driven architecture

В pt-process обнаружена зависимость jakarta.jms-api, а ActiveMQ starter присутствует в закомментированном виде. Это указывает на возможный процессный/event-driven контур, но не подтверждает работающий message broker в production.

---


# 21. API и интеграционные границы

REST API является основным внешним контрактом backend; Springdoc OpenAPI используется для описания API.

## Ключевой принцип

API DTO не должны быть зеркалом database entities. Они описывают внешний бизнес-контракт.

## Quote / Policy

Если входные поля одинаковы, допустимо использовать общий InsuranceContractRequest. Разные response/resource модели при этом остаются нормальными.

## Идемпотентность

Для критических операций, особенно выпуска полиса и финансовых действий, необходимо иметь явную стратегию идемпотентности. Конкретный реализованный механизм следует подтверждать endpoint-кодом.

---


# 22. Данные, миграции и Source of Truth

PostgreSQL является центральным persistent storage. JPA используется для persistence, а миграции в текущем контексте выполняются Flyway.

## Source of truth hierarchy


Уровень | Вес

|---|---|

|Текущий исходный код|Максимальный|

|DB migrations|Очень высокий|

|Runtime/config|Высокий|

|Tests|Высокий|

|API contracts|Высокий|

|Текущая документация|Средний|

|Исторический README / vision|Низкий|

Если документация расходится с кодом, документ должен быть исправлен или явно помечен как proposed. Нельзя незаметно подменять реализованную архитектуру желаемой.

## Data model vs business model

Business JSON contract документируется отдельно от JPA/DB схемы. Это позволяет менять storage без изменения внешнего API.

---


# 23. Docker и deployment

Текущий deployment-контур включает PostgreSQL, backend и frontend через Docker Compose.

```text
docker compose
├── postgres:15
├── backend: Java 21 / Spring Boot
│      └── 8080 (HTTP)
│      └── 5005 (debug, development)
└── frontend: Angular
       └── 80

```

Backend собирается multi-stage Gradle build с JDK 21 и запускается на Corretto 21 Alpine. Health endpoint доступен через Spring Boot Actuator.

## Environment

- local

- development

- staging

- production

Конкретный production orchestration beyond Docker Compose должен быть документирован отдельно, если он существует.

---


# 24. Observability и эксплуатация

Spring Boot Actuator присутствует в backend и предоставляет health endpoint. Полный production stack логирования, tracing, metrics backend и alerting в текущем контексте не определён.

## Минимальный эксплуатационный контур

- Health/readiness проверки.

- Structured application logs.

- Correlation ID для межмодульных операций.

- Метрики API и расчёта.

- Ошибки выпуска полиса и платежей.

- Контроль времени ответа Calculator/Rules.

Если будет добавлен event-driven контур, к observability следует добавить message lag, retry и dead-letter monitoring.

---


# 25. Архитектурные принципы

- <b>Business model first.</b> Сначала фиксируется смысл сущности, затем DTO и persistence.

- <b>Immutable product versions.</b> Published Product Version не меняется задним числом.

- <b>Policy snapshot.</b> Полис сохраняет данные, использованные при оформлении.

- <b>Master Data is provenance.</b> masterPartyId — источник, а не runtime inheritance.

- <b>Tenant isolation server-side.</b> Нельзя полагаться только на tenantId из URL.

- <b>Privilege + scope.</b> Permission и область данных — разные измерения доступа.

- <b>Extensible InsuredObject.</b> Новые страховые типы добавляют специализированный *Attributes блок.

- <b>No generic programAttributes.</b> Специализация должна быть явно именована.

- <b>Transport is a list.</b> Мультимодальная перевозка требует массива transport[].

- <b>Documents are not identifiers.</b> INN/KPP/OGRN/SNILS — реквизиты, а не документы.

- <b>Modular monolith first.</b> Модули разделяют ответственность без преждевременного перехода к микросервисам.


---


# 26. ADR и принятые решения


ID | Решение

|---|---|

|ADR-001|Backend — modular monolith|

|ADR-002|Product Version immutable after publication|

|ADR-003|Party data in policy is a snapshot|

|ADR-004|masterPartyId is provenance, not live inheritance|

|ADR-005|partyId/masterPartyId grouped into party object|

|ADR-006|Quote and Policy may share InsuranceContractRequest|

|ADR-007|InsuredObject may itself represent insured person|

|ADR-008|delivery is top-level member of insuredObject|

|ADR-009|transport is an array|

|ADR-010|carrier is a Party|

|ADR-011|insuredObject.addresses can describe object/route addresses|

|ADR-012|specialized data uses named *Attributes blocks|

|ADR-013|packageCode and sumInsured are direct insuredObject fields|

|ADR-014|flightSegments is a top-level array under insuredObject|

Эти ADR следует перенести в отдельные файлы docs/architecture/adr/ при формировании постоянной документации репозитория.

---


# 27. Открытые вопросы


Область | Вопрос

|---|---|

|Authentication|Точная граница Keycloak vs application JWT validation|

|Claims|Где реализуется claim lifecycle при отсутствии pt-claims|

|Payments|Какой provider и точный lifecycle|

|Events|Есть ли production broker и какой именно|

|Notifications|Как реализована доставка email/SMS/etc.|

|Audit|Есть ли полноценный immutable audit trail|

|Production|Какой deployment/orchestration используется вне Docker Compose|

|DB|Точная физическая схема tenant/account hierarchy|

|API|Точный набор текущих endpoints и idempotency semantics|

Открытые вопросы должны оставаться видимыми. Их нельзя закрывать предположениями только ради красивой документации.

---


# Приложение A. Пример страхового контракта

Ниже приведён сокращённый пример, отражающий согласованную модель. Он предназначен для объяснения структуры, а не как полный JSON Schema.

```text
{
  "productCode": "FINAUTO_HELP",
  "startDate": "2026-08-19T00:00:00+03:00",
  "endDate": "2027-08-19T00:00:00+03:00",
  "issueDate": "2026-08-19T00:00:00+03:00",
  "currencyCode": "RUB",
  "installmentType": "13",

  "policyHolder": {
    "party": {
      "partyId": "CP-001",
      "masterPartyId": "PARTY-001"
    },
    "organization": {
      "country": "RU",
      "inn": "7705757890",
      "fullName": "ООО "ФЛИТ АВТОЛИЗИНГ"",
      "kpp": "770901001",
      "ogrn": "1067757802793",
      "isResident": true
    },
    "documents": [],
    "addresses": [],
    "phones": [],
    "emails": []
  },

  "insuredObject": {
    "vehicle": {
      "vin": "5J8TB18548A064633",
      "mileage": 10,
      "cost": 2627469
    },
    "vehicleOwner": {
      "party": {
        "partyId": "CP-002",
        "masterPartyId": "PARTY-001"
      },
      "organization": {},
      "documents": [],
      "addresses": [],
      "phones": [],
      "emails": []
    },
    "drivers": [],
    "packageCode": "21",
    "sumInsured": 2627469,
    "cascoAttributes": {
      "coverageTerritory": "RU"
    }
  }
}
```

Для production-документации рядом с этим примером рекомендуется хранить JSON Schema и несколько полноценных scenario examples: CASCO, accident, medical, cargo и flight.

---


# Приложение B. Глоссарий


Термин | Значение

|---|---|

|Party|Участник страхового договора|

|Master Data|Источник актуальных данных Party|

|Snapshot|Зафиксированное состояние данных на момент операции|

|Insured Object|Объект страхования|

|Product|Конфигурация страхового предложения|

|Product Version|Версия продукта с фиксированным набором условий|

|Quote|Результат предварительного расчёта/предложения|

|Policy|Оформленный страховой договор|

|Coverage|Страховое покрытие|

|Risk|Риск, учитываемый продуктом/тарифом|

|Privilege|Разрешение формата RESOURCE:ACTION|

|Data Scope|Область данных, доступная субъекту|

|Tenant|Изолированный организационный контекст|

|Acting Account|Account, от имени которого выполняется действие|

|FNOL|First Notice of Loss|

|CEL|Common Expression Language|

Конец рабочей редакции. Следующая версия должна быть синхронизирована с исходным кодом, migrations, security configuration и фактическим API.

---
Конец рабочей редакции.
