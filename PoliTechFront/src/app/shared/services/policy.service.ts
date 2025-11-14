import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Policy } from '../models/policy.models';

@Injectable({
  providedIn: 'root'
})
export class PolicyService {
  constructor(private http: HttpClient) {}

  // Mock policy data for demonstration
  getMockPolicy(): Policy {
    return {
      policyNumber: "POL-2024-001",
      previousPolicyNumber: "",
      product: {
        code: "AUTO_INSURANCE",
        description: "Автомобильное страхование"
      },
      status: {
        code: "NEW",
        description: "Новый полис"
      },
      startDate: "2024-01-01",
      endDate: "2024-12-31",
      issueDate: "2024-01-01",
      createDate: "2024-01-01",
      paymentDate: "2024-01-01",
      cancellationDate: "",
      premium: 50000,
      premiumCur: 50000,
      currencyCode: "RUB",
      currencyRate: 1,
      placeOfIssue: "Москва",
      draftId: "DRAFT-001",
      additionalProperties: {
        pos: "POS-001",
        discountKVPercent: 0,
        promocode: "",
        insurerRepresentative: "Иванов И.И.",
        kfav: "KFAV-001",
        channelTypeId: "ONLINE",
        channelId: "WEB-SITE",
        creatorDivisionName: "Отдел продаж",
        contractDivisionName: "Отдел договоров",
        agentRepresentativeName: "Петров П.П.",
        agentRepresentativeDivisionName: "Агентский отдел",
        agentContractNumber: "AGENT-001",
        agentName: "Агентство ООО Рога и Копыта",
        contractResponsibleName: "Сидоров С.С.",
        contractResponsibleId: "USER-001",
        contractDivisionId: "DIV-001",
        coefDesiredPremium: 0,
        reductionCoef: 0
      },
      policyHolder: {
        person: {
          firstName: "Иван",
          lastName: "Иванов",
          middleName: "Иванович",
          birthDate: "1990-01-01",
          fullName: "Иванов Иван Иванович",
          fullNameEn: "Ivanov Ivan Ivanovich",
          birthPlace: "Москва",
          citizenship: "RU",
          gender: "M",
          familyState: "SINGLE",
          isPublicOfficial: false,
          isResident: true,
          vsk_id: "VSK-001",
          ext_id: "EXT-001"
        },
        phone: {
          phoneNumber: "+7-999-123-45-67",
          contactPerson: "Иванов И.И."
        },
        email: "ivan.ivanov@example.com",
        passport: {
          typeCode: "PASSPORT_RF",
          serial: "1234",
          number: "567890",
          dateIssue: "2010-01-01",
          validUntil: "2020-01-01",
          whom: "ОУФМС России",
          divisionCode: "123-456",
          vsk_id: "PASSPORT-001",
          ext_id: "PASSPORT-EXT-001",
          countryCode: "RU"
        },
        address: {
          typeCode: "REGISTRATION",
          countryCode: "RU",
          region: "г Москва",
          city: "Москва",
          street: "Академика Королева",
          house: "3",
          building: "2",
          flat: "25",
          room: "2",
          zipCode: "129515",
          kladrId: "7700000000015450062",
          fiasId: "f64c75cd-a640-41ed-9893-c1aaef58e638",
          addressStr: "129515, г.Москва, ул.Академика Королева, д.2, к.3, кв.25",
          addressStrEn: "129515, г.Moscow",
          vsk_id: "ADDRESS-001",
          ext_id: "ADDRESS-EXT-001"
        },
        placeOfWork: {
          organization: "ООО Пример",
          occupationType: "Офисный работник",
          occupation: "Менеджер",
          address: "г. Москва, ул. Примерная, д. 1",
          phone: "+7-999-765-43-21"
        },
        inn: "1234567890",
        snils: "123-456-789 00",
        otherAddresses: [],
        otherDocuments: [],
        organization: {
          country: "RU",
          inn: "1234567890",
          fullName: "Общество с ограниченной ответственностью Пример",
          fullNameEn: "Example Limited Liability Company",
          shortName: "ООО Пример",
          legalForm: "OOO",
          kpp: "123456789",
          ogrn: "1234567890123",
          okpo: "12345678",
          bic: "044525225",
          isResident: true,
          group: "Группа компаний Пример",
          vsk_id: "ORG-001",
          ext_id: "ORG-EXT-001",
          nciCode: "NCI-001"
        },
        isGovernmentService: false,
        customFields: {
        }
      },
      insuredObject: {
        packageCode: "1",
        device: {countryCode: '', devicePrice: 0, imei: '', licenseKey: '', model: '', deviceName: '', osName: '', osVersion: '', serialNr: '', tradeMark: '', deviceTypeCode: ''},
        covers: [
          {
            cover: {
              code: "AUTO_COVER",
              option: "FULL",
              description: "Полное покрытие автомобиля"
            },
            risk: ["Угон", "Ущерб", "Гражданская ответственность"],
            startDate: "2024-01-01",
            endDate: "2024-12-31",
            sumInsured: 1000000,
            premium: 50000,
            deductibleType: "FIXED",
            deductible: 5000,
            sumInsuredCur: 1000000,
            premiumCur: 50000,
            deductibleCur: 5000,
            deductiblePercent: 0,
            deductibleMin: 0,
            deductibleUnit: "RUB",
            deductibleSpecific: "5000"
          }
        ],
        objectId: "OBJECT-001",
        insureds: [
          {
            person: {
              firstName: "Иван",
              lastName: "Иванов",
              middleName: "Иванович",
              birthDate: "1990-01-01",
              fullName: "Иванов Иван Иванович",
              fullNameEn: "Ivanov Ivan Ivanovich",
              birthPlace: "Москва",
              citizenship: "RU",
              gender: "M",
              familyState: "SINGLE",
              isPublicOfficial: false,
              isResident: true,
              vsk_id: "VSK-001",
              ext_id: "EXT-001"
            },
            phone: {
              phoneNumber: "+7-999-123-45-67",
              contactPerson: "Иванов И.И."
            },
            email: "ivan.ivanov@example.com",
            passport: {
              typeCode: "PASSPORT_RF",
              serial: "1234",
              number: "567890",
              dateIssue: "2010-01-01",
              validUntil: "2020-01-01",
              whom: "ОУФМС России",
              divisionCode: "123-456",
              vsk_id: "PASSPORT-001",
              ext_id: "PASSPORT-EXT-001",
              countryCode: "RU"
            },
            address: {
              typeCode: "REGISTRATION",
              countryCode: "RU",
              region: "г Москва",
              city: "Москва",
              street: "Академика Королева",
              house: "3",
              building: "2",
              flat: "25",
              room: "2",
              zipCode: "129515",
              kladrId: "7700000000015450062",
              fiasId: "f64c75cd-a640-41ed-9893-c1aaef58e638",
              addressStr: "129515, г.Москва, ул.Академика Королева, д.2, к.3, кв.25",
              addressStrEn: "129515, г.Moscow",
              vsk_id: "ADDRESS-001",
              ext_id: "ADDRESS-EXT-001"
            },
            placeOfWork: {
              organization: "ООО Пример",
              occupationType: "Офисный работник",
              occupation: "Менеджер",
              address: "г. Москва, ул. Примерная, д. 1",
              phone: "+7-999-765-43-21"
            },
            plaсeOfStudy: {
              university: "МГУ",
              faculty: "Экономический",
              course: "3"
            },
            additionalFactors: {
              isProfessional: false,
              isSporttime: false,
              isSumInsuredInCredit: false,
              typeOfSport: "",
              travelSegments: []
            },
            certificate: "CERT-001"
          }
        ]
      }
    };
  }

  // Get policy by ID
  getPolicy(id: string): Observable<Policy> {
    // In real implementation, this would make an HTTP call
    return of(this.getMockPolicy());
  }

  // Create new policy
  createPolicy(policy: Policy): Observable<Policy> {
    // In real implementation, this would make an HTTP POST call
    return of(policy);
  }

  // Update existing policy
  updatePolicy(id: string, policy: Policy): Observable<Policy> {
    // In real implementation, this would make an HTTP PUT call
    return of(policy);
  }

  // Delete policy
  deletePolicy(id: string): Observable<void> {
    // In real implementation, this would make an HTTP DELETE call
    return of(void 0);
  }
}