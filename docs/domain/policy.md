# Схема договора страхования

## Шапка договора


  ### id
  Локальный уникальный идентификатор договора. Генерится сиквенсом.

  ### publicId
  "publicId": "ff175935-32ea-4e69-923c-d1b849f2e0d6"
  GUID, может передаваться при расчете и создании договора, если нужно иметь внешнюю ссылку на договор.
  Если ничего не передается, то генерится локально при создании записи.
  Используется для получения ссылки на договор, как более безопасный идентификатор, исключающий передор.

  ### productCode + productVersion
  "productCode": "NSCLASSIC"
  "productVersion": 4
  Код продукта и номер версии, по которой создан договор страхования.
  В процессе жизненного цикла договора код и версия не меняются, это позволяет ипользовать теже условия, что были при продаже, для расчета допником, растожений и т.д.
  Шаблон договора так же привязан к версии, что позволяет не хранить все сгенеренные документы, а повторно собирать по тому же шаблону.

  ### issueDate
  "issueDate": "2026-03-11T09:36:40.019778538+03:00"
  Дата, время и часовой пояс, когда был выпущен договор. Часовой пояс только этого параметра используется далее для приведения всех дат к единому формату.

  ### Даты договора
  "startDate": "2026-03-26T00:00:00+03:00"
  "endDate": "2026-12-25T23:59:59+03:00"
  "policyTerm": "P9M"
  "waitingPeriod": "P15D"
  Дата начала и окончания периода действия договора страхования, плюс интервал. Интервал может быть использован как тарифный фактор или как ограничение на период.
  Зависит от настроек продукта.
  Часовой пояс приводится к часовому поясу issueDate, не зависит от того, что передали при создании договора.

  ### statusCode 
  "statusCode": "NEW"
  Статус версии договора страхования. 
  Может быть - новый, ожидает оплаты, оплачен, расторгнут.

  ### premium
  "premium": 874.38
  Рассчитанная премия по договору страхования.
  
  ### policyNumber 
  "policyNumber": "26ФФ0YZ000029",
  Номер договора страхования.

## Коммиссионное вознагражление
  "commission"

  Номер агентского договора
  "agdNumber": "A1",
  Вычисляется в момент расчета, по логину, партнеру и т.д.

  Сумма кВ  
  "commissionAmount": 22,
  Сумма полученная в процессе расчета кВ. В общем случае это %кВ * премию, но есть варианты.

  Примененный % кВ  
  "appliedCommissionRate": null,
  Примененный %кВ. Может быть пустым, если кВ задан в абсолютной сумме , а не в %

  Запрошенный кВ  
  "requestedCommissionRate": 10
  Продавец может изменять свой %кВ если это разрешено договором. В договоре указывается мин и макс %. Если желаемый % попадает в диапазон, то применяется он, иначе стандартный % из договора.

## Страхователь"policyHolder"

    "person": {
      "fullName": "Kepybwj Cntgfy Bdydfbc",
      "lastName": "Кузнецов",
      "birthDate": "1999-01-01",
      "firstName": "Сергей",
      "middleName": "sadfsda",
      "citizenship": "RU"
    },
    "contacts": {
      "email": "2345234523",
      "phone": "53425234",
      "telegram": ""
    },
    "addresses": [
      {
        "isPrimary": true,
        "addressStr": "улица Ленина дом 5"
      }
    ],
    "identifiers": [
      {
        "whom": "выдан 12 отделением горорда",
        "number": "123456",
        "serial": "0123",
        "typeCode": "1",
        "dateIssue": "2000-01-02",
        "isPrimary": true
      }
    ]
  },

  "insuredObjects": [
    {
      "covers": [
        {
          "cover": {
            "code": "ACCIDENT_DEATH",
            "option": "",
            "description": ""
          },
          "endDate": "2026-12-25T23:59:59+03:00",
          "premium": 179.78,
          "LimitMax": 0,
          "LimitMin": 0,
          "limitMax": 0,
          "limitMin": 0,
          "startDate": "2026-03-26T00:00:00+03:00",
          "deductible": {
            "id": 0
          },
          "sumInsured": 150000
        },
        {
          "cover": {
            "code": "ACCIDENT_TEMP_DISABILITY",
            "option": "",
            "description": ""
          },
          "endDate": "2026-12-25T23:59:59+03:00",
          "premium": 95,
          "LimitMax": 0,
          "LimitMin": 0,
          "limitMax": 0,
          "limitMin": 0,
          "startDate": "2026-03-26T00:00:00+03:00",
          "deductible": {
            "id": 0
          },
          "sumInsured": 150000
        },
        {
          "cover": {
            "code": "ACCIDENT_DISABILITY",
            "option": "",
            "description": ""
          },
          "endDate": "2026-12-25T23:59:59+03:00",
          "premium": 599.6,
          "LimitMax": 0,
          "LimitMin": 0,
          "limitMax": 0,
          "limitMin": 0,
          "startDate": "2026-03-26T00:00:00+03:00",
          "deductible": {
            "id": 0
          },
          "sumInsured": 150000
        }
      ],
      "person": {
        "fullName": "Kepybwj Cntgfy Bdydfbc",
        "lastName": "Кузнецов",
        "birthDate": "1999-01-01",
        "firstName": "Сергей",
        "middleName": "sadfsda",
        "citizenship": "RU"
      },
      "contacts": {
        "email": "2345234523",
        "phone": "53425234",
        "telegram": ""
      },
      "addresses": [
        {
          "isPrimary": true,
          "addressStr": "улица Ленина дом 5"
        }
      ],
      "sumInsured": 100000,
      "identifiers": [
        {
          "whom": "выдан 12 отделением горорда",
          "number": "123456",
          "serial": "0123",
          "typeCode": "1",
          "dateIssue": "2000-01-02",
          "isPrimary": true
        }
      ],
      "packageCode": 0,
      "riskFactors": {
        "sport1": "шашки",
        "profSport": "false"
      },
      "travelSegments": [
        {
          "ticketNr": "WZ-123",
          "ticketPrice": 12000
        },
        {
          "ticketNr": "WZ-222",
          "ticketPrice": 2000
        },
        {
          "ticketNr": "WZ-333",
          "ticketPrice": 2000
        }
      ]
    }
  ],
  
###

