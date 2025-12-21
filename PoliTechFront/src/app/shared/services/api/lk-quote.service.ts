import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { EnvService } from '../env.service';
import { AuthService } from '../auth.service';
import { BaseApiService } from './base-api.service';

export interface Quote {
  id?: string; // uuid
  draftId?: string;
  policyNr?: string;
  productCode?: string;
  insCompany?: string;
  createDate?: Date | string;
  issueDate?: Date | string;
  issueTimezone?: string;
  paymentDate?: Date | string;
  startDate?: Date | string;
  endDate?: Date | string;
  policyStatus?: string;
  phDigest?: string;
  ioDigest?: string;
  premium?: number;
  agentDigest?: string;
  agentKvPrecent?: number;
  agentKvAmount?: number;
  comand1?: boolean;
  comand2?: boolean;
  comand3?: boolean;
  comand4?: boolean;
  comand5?: boolean;
  comand6?: boolean;
  comand7?: boolean;
  comand8?: boolean;
  comand9?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class LkQuoteService extends BaseApiService<Quote> {
  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
    super(http, env, 'sales/quotes', authService);
  }


   getAllMock(): Observable<Quote[]> {
    // Generate and return mock data for 13 records
    const mockQuotes: Quote[] = [
    {
      id: '1',
      draftId: 'DRAFT-001',
      policyNr: 'POL-1001',
      productCode: 'AUTO',
      insCompany: 'InsureCo A',
      createDate: '2024-06-01',
      issueDate: '2024-06-02',
      issueTimezone: 'Europe/Moscow',
      paymentDate: '2024-06-03',
      startDate: '2024-06-04',
      endDate: '2025-06-03',
      policyStatus: 'DRAFT',
      phDigest: 'Иван Иванов',
      ioDigest: 'Тестовый объект 1',
      premium: 12000,
      agentDigest: 'Агент Агентов',
      agentKvPrecent: 10,
      agentKvAmount: 1200,
      comand1: true,
      comand2: true,
      comand3: false
    },
    {
      id: '2',
      draftId: 'DRAFT-002',
      policyNr: 'POL-1002',
      productCode: 'HOME',
      insCompany: 'InsureCo B',
      createDate: '2024-05-15',
      issueDate: '2024-05-16',
      issueTimezone: 'Europe/Kaliningrad',
      paymentDate: '2024-05-18',
      startDate: '2024-05-20',
      endDate: '2025-05-19',
      policyStatus: 'ISSUED',
      phDigest: 'Петр Петров',
      ioDigest: 'Квартира',
      premium: 8000,
      agentDigest: 'Анна Смирнова',
      agentKvPrecent: 8,
      agentKvAmount: 640,
      comand1: true,
      comand2: false,
      comand3: true,
      comand4: true
    },
    {
      id: '3',
      draftId: 'DRAFT-003',
      policyNr: 'POL-1003',
      productCode: 'LIFE',
      insCompany: 'LifeInsure Pro',
      createDate: '2024-04-10',
      issueDate: '2024-04-11',
      issueTimezone: 'Europe/Samara',
      paymentDate: '2024-04-14',
      startDate: '2024-04-15',
      endDate: '2025-04-14',
      policyStatus: 'PAID',
      phDigest: 'Елена Алексеева',
      ioDigest: 'Жизнь',
      premium: 18000,
      agentDigest: 'Олег Добр',
      agentKvPrecent: 12,
      agentKvAmount: 2160,
      comand2: true,
      comand3: true,
      comand5: true
    },
    {
      id: '4',
      draftId: 'DRAFT-004',
      policyNr: 'POL-1004',
      productCode: 'HEALTH',
      insCompany: 'Здоровье+',
      createDate: '2024-03-22',
      issueDate: '2024-03-22',
      issueTimezone: 'Europe/Volgograd',
      paymentDate: '2024-03-25',
      startDate: '2024-04-01',
      endDate: '2025-03-31',
      policyStatus: 'CANCELLED',
      phDigest: 'Сергей Честнов',
      ioDigest: 'Офис',
      premium: 9500,
      agentDigest: 'Дмитрий Терехов',
      agentKvPrecent: 11,
      agentKvAmount: 1045,
      comand1: false,
      comand4: true,
      comand6: true
    },
    {
      id: '5',
      draftId: 'DRAFT-005',
      policyNr: 'POL-1005',
      productCode: 'TRAVEL',
      insCompany: 'ТурИнс',
      createDate: '2024-02-18',
      issueDate: '2024-02-18',
      issueTimezone: 'Asia/Yekaterinburg',
      paymentDate: '2024-02-20',
      startDate: '2024-03-01',
      endDate: '2024-04-01',
      policyStatus: 'EXPIRED',
      phDigest: 'Галина Романова',
      ioDigest: 'Путешествие',
      premium: 2400,
      agentDigest: 'Владимир Лебедев',
      agentKvPrecent: 7,
      agentKvAmount: 168,
      comand7: true
    },
    {
      id: '6',
      draftId: 'DRAFT-006',
      policyNr: 'POL-1006',
      productCode: 'AUTO',
      insCompany: 'InsureCo A',
      createDate: '2024-01-25',
      issueDate: '2024-01-26',
      issueTimezone: 'Europe/Moscow',
      paymentDate: '2024-01-28',
      startDate: '2024-02-01',
      endDate: '2025-01-31',
      policyStatus: 'ISSUED',
      phDigest: 'Мария Кириллова',
      ioDigest: 'Audi Q7',
      premium: 15100,
      agentDigest: 'Егор Павлов',
      agentKvPrecent: 9,
      agentKvAmount: 1359,
      comand8: true
    },
    {
      id: '7',
      draftId: 'DRAFT-007',
      policyNr: 'POL-1007',
      productCode: 'HOME',
      insCompany: 'InsureCo B',
      createDate: '2024-06-05',
      issueDate: '2024-06-06',
      issueTimezone: 'Europe/Moscow',
      paymentDate: '2024-06-07',
      startDate: '2024-06-10',
      endDate: '2025-06-09',
      policyStatus: 'DRAFT',
      phDigest: 'Оксана Блинова',
      ioDigest: 'Дом 32',
      premium: 10300,
      agentDigest: 'Артем Жуков',
      agentKvPrecent: 8,
      agentKvAmount: 824,
      comand9: true
    },
    {
      id: '8',
      draftId: 'DRAFT-008',
      policyNr: 'POL-1008',
      productCode: 'LIFE',
      insCompany: 'LifeInsure Pro',
      createDate: '2024-05-22',
      issueDate: '2024-05-23',
      issueTimezone: 'Europe/Samara',
      paymentDate: '2024-05-25',
      startDate: '2024-06-01',
      endDate: '2025-05-31',
      policyStatus: 'PAID',
      phDigest: 'Андрей Перевалов',
      ioDigest: 'Жизнь',
      premium: 20000,
      agentDigest: 'Дарья Долгополова',
      agentKvPrecent: 13,
      agentKvAmount: 2600,
      comand1: true,
      comand6: true
    },
    {
      id: '9',
      draftId: 'DRAFT-009',
      policyNr: 'POL-1009',
      productCode: 'HEALTH',
      insCompany: 'Здоровье+',
      createDate: '2024-05-12',
      issueDate: '2024-05-13',
      issueTimezone: 'Europe/Volgograd',
      paymentDate: '2024-05-15',
      startDate: '2024-05-20',
      endDate: '2025-05-19',
      policyStatus: 'PAID',
      phDigest: 'Кирилл Виноградов',
      ioDigest: 'Офис',
      premium: 9200,
      agentDigest: 'Михаил Назаров',
      agentKvPrecent: 9,
      agentKvAmount: 828,
      comand2: true
    },
    {
      id: '10',
      draftId: 'DRAFT-010',
      policyNr: 'POL-1010',
      productCode: 'TRAVEL',
      insCompany: 'ТурИнс',
      createDate: '2024-03-13',
      issueDate: '2024-03-15',
      issueTimezone: 'Asia/Yekaterinburg',
      paymentDate: '2024-03-16',
      startDate: '2024-03-20',
      endDate: '2024-04-19',
      policyStatus: 'CANCELLED',
      phDigest: 'Василиса Жигалова',
      ioDigest: 'Путешествие 2',
      premium: 3050,
      agentDigest: 'Владислав Орлов',
      agentKvPrecent: 7,
      agentKvAmount: 213,
      comand5: true
    },
    {
      id: '11',
      draftId: 'DRAFT-011',
      policyNr: 'POL-1011',
      productCode: 'AUTO',
      insCompany: 'InsureCo A',
      createDate: '2024-01-09',
      issueDate: '2024-01-10',
      issueTimezone: 'Europe/Moscow',
      paymentDate: '2024-01-11',
      startDate: '2024-01-12',
      endDate: '2025-01-11',
      policyStatus: 'ISSUED',
      phDigest: 'Лидия Антонова',
      ioDigest: 'Toyota Camry',
      premium: 12200,
      agentDigest: 'Денис Сомов',
      agentKvPrecent: 10,
      agentKvAmount: 1220,
      comand4: true
    },
    {
      id: '12',
      draftId: 'DRAFT-012',
      policyNr: 'POL-1012',
      productCode: 'LIFE',
      insCompany: 'LifeInsure Pro',
      createDate: '2024-02-01',
      issueDate: '2024-02-02',
      issueTimezone: 'Europe/Samara',
      paymentDate: '2024-02-03',
      startDate: '2024-02-05',
      endDate: '2025-02-04',
      policyStatus: 'EXPIRED',
      phDigest: 'Антон Юдин',
      ioDigest: 'Жизнь',
      premium: 17000,
      agentDigest: 'Кристина Малова',
      agentKvPrecent: 11,
      agentKvAmount: 1870,
      comand3: true
    },
    {
      id: '13',
      draftId: 'DRAFT-013',
      policyNr: 'POL-1013',
      productCode: 'HEALTH',
      insCompany: 'Здоровье+',
      createDate: '2024-04-14',
      issueDate: '2024-04-15',
      issueTimezone: 'Europe/Volgograd',
      paymentDate: '2024-04-17',
      startDate: '2024-04-21',
      endDate: '2025-04-20',
      policyStatus: 'DRAFT',
      phDigest: 'Дмитрий Суханов',
      ioDigest: 'Офис 12',
      premium: 8400,
      agentDigest: 'Григорий Миляев',
      agentKvPrecent: 8,
      agentKvAmount: 672,
      comand7: true,
      comand8: true
    }

    ];

    return of(mockQuotes);
  }
}
