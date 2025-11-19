import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { catchError, delay, map, Observable, of, tap } from 'rxjs';
import { BASE_URL } from '../tokens';

export interface BusinessLineVar {
  varCode: string;
  varType: 'IN' | 'VAR' | 'FORMULA' | 'CONST';
  varPath: string;
  varName: string;
  varDataType: 'STRING' | 'NUMBER' | 'DATE' | 'PERIOD';
  varValue: string;
  varNr: number;
}

export interface BusinessLineCover {
  coverCode: string;
  coverName: string;
  risks: string;
}

export interface BusinessLineEdit {
  id: number;
  mpCode: string;
  mpName: string;
  mpPhType: string;
  mpInsObjectType: string;
  mpVars: BusinessLineVar[];
  mpCovers: BusinessLineCover[];
}

@Injectable({
  providedIn: 'root'
})
export class BusinessLineEditService {
  constructor(private http: HttpClient, @Inject(BASE_URL) private baseUrl: string) {};

  private mockData: BusinessLineEdit =
    {
      id: -1,
      mpCode: 'MORTGAGE-MOCK',
      mpName: 'Ипотека',
      mpPhType: 'person',
      mpInsObjectType: 'Person',
      mpVars: [
        {
          varNr: 1,
          varCode: 'InsAmount',
          varType: 'IN',
          varPath: 'string',
          varName: 'Сумма страхования',
          varDataType: 'NUMBER' ,
          varValue: ''
        },
        {
          varNr: 2,
          varCode: 'LoanAmount',
          varType: 'VAR',
          varPath: 'number',
          varName: 'Размер кредита',
          varDataType: 'NUMBER',
          varValue: ''
        }
      ],
      mpCovers: [
        {
          coverCode: 'PROPERTY',
          coverName: 'Имущество',
          risks: 'Пожар, кража, стихийные бедствия'
        },
        {
          coverCode: 'LIABILITY',
          coverName: 'Ответственность',
          risks: 'Гражданская ответственность'
        }
      ]
    };

  getBusinessLineByCode(code: String): Observable<BusinessLineEdit | null> {
    if (!code) {
      return of(null);
    }

    if (!this.http) {
      return of(this.mockData);
    }

    return this.http.get<BusinessLineEdit>(`${this.baseUrl}admin/lobs/${code}`).pipe(
      tap((data: BusinessLineEdit) => {
        // Replace the item in mockData with the fetched one, or add if not present
          this.mockData = data;
      }),
      catchError((error) => {
        console.error('Error fetching business line:', error);
        return of(null);
      })
    );
  }

  saveBusinessLine(businessLine: BusinessLineEdit): Observable<BusinessLineEdit> {

    if (this.mockData.id != -1) {
      // Update existing
      this.mockData = {
        ...businessLine,
        mpPhType: businessLine.mpPhType || '',
        mpInsObjectType: businessLine.mpInsObjectType || ''
      };
    if (this.http) {
      // Also update backend with the full mockData
      const code = businessLine.mpCode;
      const url = `${this.baseUrl}admin/lobs/${code}`;
      // INSERT_YOUR_CODE
      console.log('HTTP PUT Request:', {
        url,
        body: businessLine,
        headers: this.http
      });
      this.http.put<BusinessLineEdit>(url, businessLine).subscribe({
        next: (data) => {
          // Replace local copy with the response data
          this.mockData = {
            ...data,
            mpPhType: data.mpPhType || '',
            mpInsObjectType: data.mpInsObjectType || ''
          };
        },
          error: (err) => {
          console.error('Error updating business line to backend:', err);
        }
      });
    }
    } else {
      // Add new
      this.mockData = {
        ...businessLine,
        mpPhType: businessLine.mpPhType || '',
        mpInsObjectType: businessLine.mpInsObjectType || ''
      };
    if (this.http) {
      // Also save to backend if http is available

      this.http.post(`${this.baseUrl}admin/lobs`, businessLine).subscribe({
        next: () => {},
        error: (err) => {
          console.error('Error saving business line to backend:', err);
        }
      });
    }
    }
    return of(businessLine);
  }

  addVar(mpCode: string, newVar: BusinessLineVar): Observable<BusinessLineVar> {
    const businessLine = this.mockData;
    if (businessLine && businessLine.mpCode === mpCode) {
      businessLine.mpVars.push(newVar);
      return of(newVar);
    }
    throw new Error('Business line not found');
  }

  updateVar(mpCode: string, varCode: string, updatedVar: Partial<BusinessLineVar>): Observable<BusinessLineVar | null> {
    const businessLine = this.mockData;
    if (businessLine && businessLine.mpCode === mpCode) {
      const varIndex = businessLine.mpVars.findIndex(v => v.varCode === varCode);
      if (varIndex !== -1) {
        businessLine.mpVars[varIndex] = { ...businessLine.mpVars[varIndex], ...updatedVar };
        return of(businessLine.mpVars[varIndex]);
      }
    }
    return of(null);
  }

  deleteVar(mpCode: string, varCode: string): Observable<boolean> {
    const businessLine = this.mockData;
    if (businessLine && businessLine.mpCode === mpCode) {
      const varIndex = businessLine.mpVars.findIndex(v => v.varCode === varCode);
      if (varIndex !== -1) {
        businessLine.mpVars.splice(varIndex, 1);
        return of(true);
      }
    }
    return of(false);
  }

  addCover(mpCode: string, newCover: BusinessLineCover): Observable<BusinessLineCover> {
    const businessLine = this.mockData;
    if (businessLine && businessLine.mpCode === mpCode) {
      businessLine.mpCovers.push(newCover);
      return of(newCover);
    }
    throw new Error('Business line not found');
  }

  updateCover(mpCode: string, coverCode: string, updatedCover: Partial<BusinessLineCover>): Observable<BusinessLineCover | null> {
    const businessLine = this.mockData;
    if (businessLine && businessLine.mpCode === mpCode) {
      const coverIndex = businessLine.mpCovers.findIndex(c => c.coverCode === coverCode);
      if (coverIndex !== -1) {
        businessLine.mpCovers[coverIndex] = { ...businessLine.mpCovers[coverIndex], ...updatedCover };
        return of(businessLine.mpCovers[coverIndex]);
      }
    }
    return of(null);
  }

  deleteCover(mpCode: string, coverCode: string): Observable<boolean> {
    const businessLine = this.mockData;
    if (businessLine && businessLine.mpCode === mpCode) {
      const coverIndex = businessLine.mpCovers.findIndex(c => c.coverCode === coverCode);
      if (coverIndex !== -1) {
        businessLine.mpCovers.splice(coverIndex, 1);
        return of(true);
      }
    }
    return of(false);
  }

  getLobVars(lob: string): Observable<string[]> {
    return this.getBusinessLineByCode(lob).pipe(
      // getBusinessLineByCode should return Observable<BusinessLine | null>
      // If not found or error, return []
      // Otherwise, map to array of var.varCode
      // Assuming businessLine.mpVars is the array of vars
      map((businessLine: BusinessLineEdit | null) => {
        if (businessLine && Array.isArray(businessLine.mpVars)) {
          return businessLine.mpVars.map((v: any) => v.varCode);
        }
        return [];
      }),
      catchError(() => of([]))
    );
    //return of(['insAmount', 'policyTerm', 'activationDelay', 'coverageCode', 'deductible']).pipe(delay(200));
  }

  getLobCovers(lob: string): Observable<string[]> {
    return this.getBusinessLineByCode(lob).pipe(
      // getBusinessLineByCode should return Observable<BusinessLine | null>
      // If not found or error, return []
      // Otherwise, map to array of var.varCode
      // Assuming businessLine.mpVars is the array of vars
      map((businessLine: BusinessLineEdit | null) => {
        if (businessLine && Array.isArray(businessLine.mpCovers)) {
          return businessLine.mpCovers.map((c: any) => c.coverCode);
        }
        return [];
      }),
      catchError(() => of([]))
    );
    //return of(['insAmount', 'policyTerm', 'activationDelay', 'coverageCode', 'deductible']).pipe(delay(200));
  }

  getExampleJson(lobCode: string): Observable<any> {
    if (!this.http) {
      // Return mock example if no http client
      return of({
        policyHolder: {
          person: {
            firstName: 'John',
            lastName: 'Doe'
          }
        }
      });
    }

    return this.http.get<any>(`${this.baseUrl}admin/lobs/${lobCode}/example`).pipe(
      catchError((error) => {
        console.error('Error fetching example JSON:', error);
        return of({
          policyHolder: {
            person: {
              firstName: 'John',
              lastName: 'Doe'
            }
          }
        });
      })
    );
  }

  /**
   * Searches all arrays with BusinessLineVars defined on this service and returns the varNr if varPath is found.
   * Returns the first varNr found, or null if not found.
   */
  findVarNrByVarPath(varPath: string): number | null {
    // Collect all arrays of BusinessLineVar defined on this service instance
    const varArrays: Array<BusinessLineVar[]> = [];
    for (const key of Object.keys(this)) {
      if (
        Array.isArray((this as any)[key]) &&
        (this as any)[key].length > 0 &&
        typeof (this as any)[key][0] === 'object' &&
        'varCode' in (this as any)[key][0] &&
        'varPath' in (this as any)[key][0]
      ) {
        varArrays.push((this as any)[key] as BusinessLineVar[]);
      }
    }
    for (const arr of varArrays) {
      for (const v of arr) {
        if (v.varPath === varPath) {
          return v.varNr;
        }
      }
    }
    return 500;
  }

  phPersonVars: BusinessLineVar[] = [
      {
        "varNr": 1,
        "varDataType": "STRING",
        "varCode": "ph_firstName",
        "varName": "Страхователь.имя",
        "varPath": "policyHolder.person.firstName",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 2,
        "varDataType": "STRING",
        "varCode": "ph_lastName",
        "varName": "Страхователь.фамилия",
        "varPath": "policyHolder.person.lastName",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 3,
        "varDataType": "STRING",
        "varCode": "ph_middleName",
        "varName": "Страхователь.отчество",
        "varPath": "policyHolder.person.middleName",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 4,
        "varDataType": "STRING",
        "varCode": "ph_birthDate",
        "varName": "Страхователь.дата рождения",
        "varPath": "policyHolder.person.birthDate",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 5,
        "varDataType": "STRING",
        "varCode": "ph_fullName",
        "varName": "Страхователь.полное ФИО",
        "varPath": "policyHolder.person.fullName",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 6,
        "varDataType": "STRING",
        "varCode": "ph_fullNameEn",
        "varName": "Страхователь.полное ФИО англ",
        "varPath": "policyHolder.person.fullNameEn",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 7,
        "varDataType": "STRING",
        "varCode": "ph_birthPlace",
        "varName": "Страхователь.место рождения",
        "varPath": "policyHolder.person.birthPlace",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 8,
        "varDataType": "STRING",
        "varCode": "ph_citizenship",
        "varName": "Страхователь.гражданство",
        "varPath": "policyHolder.person.citizenship",
        "varType": "IN",
        "varValue": "RU"
      },
      {
        "varNr": 9,
        "varDataType": "STRING",
        "varCode": "ph_gender",
        "varName": "Страхователь.пол",
        "varPath": "policyHolder.person.gender",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 10,
        "varDataType": "STRING",
        "varCode": "ph_familyState",
        "varName": "Страхователь.семейное положение",
        "varPath": "policyHolder.person.familyState",
        "varType": "IN",
        "varValue": "SINGLE"
      },
      {
        "varNr": 11,
        "varDataType": "STRING",
        "varCode": "ph_isPublicOfficial",
        "varName": "Страхователь.признак ПДЛ",
        "varPath": "policyHolder.person.isPublicOfficial",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 12,
        "varDataType": "STRING",
        "varCode": "ph_isResident",
        "varName": "Страхователь.резидент РФ",
        "varPath": "policyHolder.person.isResident",
        "varType": "IN",
        "varValue": "true"
      },
      {
        "varNr": 13,
        "varDataType": "STRING",
        "varCode": "ph_vsk_id",
        "varName": "Страхователь.ID ВСК",
        "varPath": "policyHolder.person.vsk_id",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 14,
        "varDataType": "STRING",
        "varCode": "ph_ext_id",
        "varName": "Страхователь.внешний ID",
        "varPath": "policyHolder.person.ext_id",
        "varType": "IN",
        "varValue": ""
      }
  ];
  phPhoneVars: BusinessLineVar[] = [
    {
      "varNr": 81,
      "varDataType": "STRING",
      "varCode": "ph_phone",
      "varName": "Страхователь.телефон",
      "varPath": "policyHolder.phone.phoneNumber",
      "varType": "IN",
      "varValue": ""
    }
  ];
  phEmailVars: BusinessLineVar[] = [
    {
      "varNr": 91,
      "varDataType": "STRING",
      "varCode": "ph_email",
      "varName": "Страхователь.email",
      "varPath": "policyHolder.email",
      "varType": "IN",
      "varValue": ""
    }
  ];
  phPersonOrganizationVars: BusinessLineVar[] = [
      {
        "varNr": 51,
        "varDataType": "STRING",
        "varCode": "ph_country",
        "varName": "Страхователь.код страны регистрации",
        "varPath": "policyHolder.organization.country",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 52,
        "varDataType": "STRING",
        "varCode": "ph_inn",
        "varName": "Страхователь.ИНН юр.лица",
        "varPath": "policyHolder.organization.inn",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 53,
        "varDataType": "STRING",
        "varCode": "ph_fullName",
        "varName": "Страхователь.полное наименование юр.лица",
        "varPath": "policyHolder.organization.fullName",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 54,
        "varDataType": "STRING",
        "varCode": "ph_fullNameEn",
        "varName": "Страхователь.полное наименование  юр.лица англ",
        "varPath": "policyHolder.organization.fullNameEn",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 55,
        "varDataType": "STRING",
        "varCode": "ph_shortName",
        "varName": "Страхователь.краткое наименование юр.лица",
        "varPath": "policyHolder.organization.shortName",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 56,
        "varDataType": "STRING",
        "varCode": "ph_legalForm",
        "varName": "Страхователь.организационно-правовая форма",
        "varPath": "policyHolder.organization.legalForm",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 57,
        "varDataType": "STRING",
        "varCode": "ph_kpp",
        "varName": "Страхователь.КПП",
        "varPath": "policyHolder.organization.kpp",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 58,
        "varDataType": "STRING",
        "varCode": "ph_ogrn",
        "varName": "Страхователь.ОГРН",
        "varPath": "policyHolder.organization.ogrn",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 59,
        "varDataType": "STRING",
        "varCode": "org_okpo",
        "varName": "Страхователь.ОКПО",
        "varPath": "policyHolder.organization.okpo",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 60,
        "varDataType": "STRING",
        "varCode": "ph_bic",
        "varName": "Страхователь.БИК",
        "varPath": "policyHolder.organization.bic",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 61,
        "varDataType": "STRING",
        "varCode": "ph_isResident",
        "varName": "Страхователь.резидент РФ юр.лица",
        "varPath": "policyHolder.organization.isResident",
        "varType": "IN",
        "varValue": "true"
      },
      {
        "varNr": 62,
        "varDataType": "STRING",
        "varCode": "ph_group",
        "varName": "Страхователь.Gруппа страхователя ЮЛ",
        "varPath": "policyHolder.organization.group",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 63,
        "varDataType": "STRING",
        "varCode": "ph_vsk_id",
        "varName": "Страхователь.ID ВСК",
        "varPath": "policyHolder.organization.vsk_id",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 64,
        "varDataType": "STRING",
        "varCode": "ph_ext_id",
        "varName": "Страхователь.внешний ID",
        "varPath": "policyHolder.organization.ext_id",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 65,
        "varDataType": "STRING",
        "varCode": "ph_nciCode",
        "varName": "Страхователь.НСИ код",
        "varPath": "policyHolder.organization.nciCode",
        "varType": "IN",
        "varValue": ""
      }
    ];

  phPersonDocumentVars: BusinessLineVar[] = [
      {
        "varNr": 101,
        "varDataType": "STRING",
        "varCode": "ph_doc_typeCode",
        "varName": "Документ.код типа документа",
        "varPath": "policyHolder.passport.typeCode",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 102,
        "varDataType": "STRING",
        "varCode": "ph_doc_serial",
        "varName": "Документ.серия документа",
        "varPath": "policyHolder.passport.serial",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 103,
        "varDataType": "STRING",
        "varCode": "ph_doc_number",
        "varName": "Документ.номер документа",
        "varPath": "policyHolder.passport.number",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 104,
        "varDataType": "STRING",
        "varCode": "ph_doc_dateIssue",
        "varName": "Документ.дата выдачи",
        "varPath": "policyHolder.passport.dateIssue",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 105,
        "varDataType": "STRING",
        "varCode": "ph_doc_validUntil",
        "varName": "Документ.действительно до",
        "varPath": "policyHolder.passport.validUntil",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 106,
        "varDataType": "STRING",
        "varCode": "ph_doc_whom",
        "varName": "Документ.кем выдан",
        "varPath": "policyHolder.passport.whom",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 107,
        "varDataType": "STRING",
        "varCode": "ph_doc_divisionCode",
        "varName": "Документ.код подразделения",
        "varPath": "policyHolder.passport.divisionCode",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 108,
        "varDataType": "STRING",
        "varCode": "ph_doc_vsk_id",
        "varName": "Документ.ID ВСК",
        "varPath": "policyHolder.passport.vsk_id",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 109,
        "varDataType": "STRING",
        "varCode": "ph_doc_ext_id",
        "varName": "Документ.внешний ID",
        "varPath": "policyHolder.passport.ext_id",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 110,
        "varDataType": "STRING",
        "varCode": "ph_doc_countryCode",
        "varName": "Документ.страна выдачи документа",
        "varPath": "policyHolder.passport.countryCode",
        "varType": "IN",
        "varValue": "RU"
      }
  ];
  phAddressVars: BusinessLineVar[] = [
      {
        "varNr": 121,
        "varDataType": "STRING",
        "varCode": "ph_addr_typeCode",
        "varName": "Адрес.тип адреса",
        "varPath": "policyHolder.address.typeCode",
        "varType": "IN",
        "varValue": "REGISTRATION"
      },
      {
        "varNr": 122,
        "varDataType": "STRING",
        "varCode": "ph_addr_countryCode",
        "varName": "Адрес.код страны",
        "varPath": "policyHolder.address.countryCode",
        "varType": "IN",
        "varValue": "RU"
      },
      {
        "varNr": 123,
        "varDataType": "STRING",
        "varCode": "ph_addr_region",
        "varName": "Адрес.регион",
        "varPath": "policyHolder.address.region",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 124,
        "varDataType": "STRING",
        "varCode": "ph_addr_city",
        "varName": "Адрес.город",
        "varPath": "policyHolder.address.city",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 125,
        "varDataType": "STRING",
        "varCode": "ph_addr_street",
        "varName": "Адрес.улица",
        "varPath": "policyHolder.address.street",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 126,
        "varDataType": "STRING",
        "varCode": "ph_addr_house",
        "varName": "Адрес.дом",
        "varPath": "policyHolder.address.house",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 127,
        "varDataType": "STRING",
        "varCode": "ph_addr_building",
        "varName": "Адрес.строение",
        "varPath": "policyHolder.address.building",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 128,
        "varDataType": "STRING",
        "varCode": "ph_addr_flat",
        "varName": "Адрес.квартира",
        "varPath": "policyHolder.address.flat",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 129,
        "varDataType": "STRING",
        "varCode": "ph_addr_room",
        "varName": "Адрес.комната",
        "varPath": "policyHolder.address.room",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 130,
        "varDataType": "STRING",
        "varCode": "ph_addr_zipCode",
        "varName": "Адрес.индекс",
        "varPath": "policyHolder.address.zipCode",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 131,
        "varDataType": "STRING",
        "varCode": "ph_addr_kladrId",
        "varName": "Адрес.код КЛАДР",
        "varPath": "policyHolder.address.kladrId",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 132,
        "varDataType": "STRING",
        "varCode": "ph_addr_fiasId",
        "varName": "Адрес.код ФИАС",
        "varPath": "policyHolder.address.fiasId",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 133,
        "varDataType": "STRING",
        "varCode": "ph_addr_addressStr",
        "varName": "Адрес.адресная строка",
        "varPath": "policyHolder.address.addressStr",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 134,
        "varDataType": "STRING",
        "varCode": "ph_addr_addressStrEn",
        "varName": "Адрес.адресная строка англ",
        "varPath": "policyHolder.address.addressStrEn",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 135,
        "varDataType": "STRING",
        "varCode": "ph_addr_vsk_id",
        "varName": "Адрес.ID ВСК",
        "varPath": "policyHolder.address.vsk_id",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 136,
        "varDataType": "STRING",
        "varCode": "ph_addr_ext_id",
        "varName": "Адрес.внешний ID",
        "varPath": "policyHolder.address.ext_id",
        "varType": "IN",
        "varValue": ""
      }
  ];

  phOrganizationVars: BusinessLineVar[] = [
    {
      "varNr": 141,
      "varDataType": "STRING",
      "varCode": "ph_country",
      "varName": "Страхователь.код страны регистрации",
      "varPath": "policyHolder.organization.country",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 142,
      "varDataType": "STRING",
      "varCode": "ph_inn",
      "varName": "Страхователь.ИНН юр.лица",
      "varPath": "policyHolder.organization.inn",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 143,
      "varDataType": "STRING",
      "varCode": "ph_fullName",
      "varName": "Страхователь.полное наименование юр.лица",
      "varPath": "policyHolder.organization.fullName",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 144,
      "varDataType": "STRING",
      "varCode": "ph_fullNameEn",
      "varName": "Страхователь.полное наименование  юр.лица англ",
      "varPath": "policyHolder.organization.fullNameEn",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 145,
      "varDataType": "STRING",
      "varCode": "ph_shortName",
      "varName": "Страхователь.краткое наименование юр.лица",
      "varPath": "policyHolder.organization.shortName",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 146,
      "varDataType": "STRING",
      "varCode": "ph_legalForm",
      "varName": "Страхователь.организационно-правовая форма",
      "varPath": "policyHolder.organization.legalForm",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 147,
      "varDataType": "STRING",
      "varCode": "ph_kpp",
      "varName": "Страхователь.КПП",
      "varPath": "policyHolder.organization.kpp",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 148,
      "varDataType": "STRING",
      "varCode": "ph_ogrn",
      "varName": "Страхователь.ОГРН",
      "varPath": "policyHolder.organization.ogrn",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 149,
      "varDataType": "STRING",
      "varCode": "org_okpo",
      "varName": "Страхователь.ОКПО",
      "varPath": "policyHolder.organization.okpo",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 150,
      "varDataType": "STRING",
      "varCode": "ph_bic",
      "varName": "Страхователь.БИК",
      "varPath": "policyHolder.organization.bic",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 151,
      "varDataType": "STRING",
      "varCode": "ph_isResident",
      "varName": "Страхователь.резидент РФ юр.лица",
      "varPath": "policyHolder.organization.isResident",
      "varType": "IN",
      "varValue": "true"
    },
    {
      "varNr": 152,
      "varDataType": "STRING",
      "varCode": "ph_group",
      "varName": "Страхователь.Gруппа страхователя ЮЛ",
      "varPath": "policyHolder.organization.group",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 153,
      "varDataType": "STRING",
      "varCode": "ph_vsk_id",
      "varName": "Страхователь.ID ВСК",
      "varPath": "policyHolder.organization.vsk_id",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 154,
      "varDataType": "STRING",
      "varCode": "ph_ext_id",
      "varName": "Страхователь.внешний ID",
      "varPath": "policyHolder.organization.ext_id",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 155,
      "varDataType": "STRING",
      "varCode": "ph_nciCode",
      "varName": "Страхователь.НСИ код",
      "varPath": "policyHolder.organization.nciCode",
      "varType": "IN",
      "varValue": ""
    }
  ];

  phOrganizationDocumentVars: BusinessLineVar[] = [
    {
      "varNr": 161,
      "varDataType": "STRING",
      "varCode": "ph_doc_typeCode",
      "varName": "Документ.код типа документа",
      "varPath": "policyHolder.document.typeCode",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 162,
      "varDataType": "STRING",
      "varCode": "ph_doc_serial",
      "varName": "Документ.серия документа",
      "varPath": "policyHolder.document.serial",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 163,
      "varDataType": "STRING",
      "varCode": "ph_doc_number",
      "varName": "Документ.номер документа",
      "varPath": "policyHolder.document.number",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 164,
      "varDataType": "STRING",
      "varCode": "ph_doc_dateIssue",
      "varName": "Документ.дата выдачи",
      "varPath": "policyHolder.document.dateIssue",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 165,
      "varDataType": "STRING",
      "varCode": "ph_doc_validUntil",
      "varName": "Документ.действительно до",
      "varPath": "policyHolder.document.validUntil",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 166,
      "varDataType": "STRING",
      "varCode": "ph_doc_whom",
      "varName": "Документ.кем выдан",
      "varPath": "policyHolder.document.whom",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 167,
      "varDataType": "STRING",
      "varCode": "ph_doc_divisionCode",
      "varName": "Документ.код подразделения",
      "varPath": "policyHolder.document.divisionCode",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 168,
      "varDataType": "STRING",
      "varCode": "ph_doc_vsk_id",
      "varName": "Документ.ID ВСК",
      "varPath": "policyHolder.document.vsk_id",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 169,
      "varDataType": "STRING",
      "varCode": "ph_doc_ext_id",
      "varName": "Документ.внешний ID",
      "varPath": "policyHolder.document.ext_id",
      "varType": "IN",
      "varValue": ""
    },
    {
      "varNr": 170,
      "varDataType": "STRING",
      "varCode": "ph_doc_countryCode",
      "varName": "Документ.страна выдачи документа",
      "varPath": "policyHolder.document.countryCode",
      "varType": "IN",
      "varValue": "RU"
    }
  ];

  ioDeviceVars: BusinessLineVar[] = [
  {
    "varNr": 1001,
    "varDataType": "STRING",
    "varCode": "io_device_name",
    "varName": "Застрахованное ус-во. название",
    "varPath": "insuredObject.device.deviceName",
    "varType": "IN",
    "varValue": "Телефон"
  },
  {
    "varNr": 1002,
    "varDataType": "STRING",
    "varCode": "io_device_typeCode",
    "varName": "Застрахованное ус-во. код типа",
    "varPath": "insuredObject.device.deviceTypeCode",
    "varType": "IN",
    "varValue": "PHONE"
  },
  {
    "varNr": 1003,
    "varDataType": "STRING",
    "varCode": "io_device_tradeMark",
    "varName": "Застрахованное ус-во. торговая марка",
    "varPath": "insuredObject.device.tradeMark",
    "varType": "IN",
    "varValue": "Samsung"
  },
  {
    "varNr": 1004,
    "varDataType": "STRING",
    "varCode": "io_device_model",
    "varName": "Застрахованное ус-во. модель",
    "varPath": "insuredObject.device.model",
    "varType": "IN",
    "varValue": "Galaxy S21"
  },
  {
    "varNr": 1005,
    "varDataType": "STRING",
    "varCode": "io_device_serialNr",
    "varName": "Застрахованное ус-во. серийный номер",
    "varPath": "insuredObject.device.serialNr",
    "varType": "IN",
    "varValue": "1234567890"
  },
  {
    "varNr": 1006,
    "varDataType": "STRING",
    "varCode": "io_device_licenseKey",
    "varName": "Застрахованное ус-во. ключ лицензии",
    "varPath": "insuredObject.device.licenseKey",
    "varType": "IN",
    "varValue": "1234567890"
  },
  {
    "varNr": 1007,
    "varDataType": "STRING",
    "varCode": "io_device_imei",
    "varName": "Застрахованное ус-во. IMEI",
    "varPath": "insuredObject.device.imei",
    "varType": "IN",
    "varValue": "1234567890"
  },
  {
    "varNr": 1008,
    "varDataType": "STRING",
    "varCode": "io_device_osName",
    "varName": "Застрахованное ус-во. название ОС",
    "varPath": "insuredObject.device.osName",
    "varType": "IN",
    "varValue": "Android"
  },
  {
    "varNr": 1008,
    "varDataType": "STRING",
    "varCode": "io_device_osVersion",
    "varName": "Застрахованное ус-во. версия ОС",
    "varPath": "insuredObject.device.osVersion",
    "varType": "IN",
    "varValue": "10"
  },
  {
    "varNr": 1009,
    "varDataType": "STRING",
    "varCode": "io_device_countryCode",
    "varName": "Застрахованное ус-во. код страны",
    "varPath": "insuredObject.device.countryCode",
    "varType": "IN",
    "varValue": "RU"
  },
  {
    "varNr": 1010,
    "varDataType": "STRING",
    "varCode": "io_device_devicePrice",
    "varName": "Застрахованное ус-во. цена",
    "varPath": "insuredObject.device.devicePrice",
    "varType": "IN",
    "varValue": "10000"
  }
  ];

  ioPropertyVars: BusinessLineVar[] = [
      {
        "varNr": 1001,
        "varDataType": "STRING",
        "varCode": "io_propertyType_code",
        "varName": "Имущество.тип имущества код",
        "varPath": "insuredObject.property.propertyType.code",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 1002,
        "varDataType": "STRING",
        "varCode": "io_addr_typeCode",
        "varName": "Имущество.адрес.тип адреса",
        "varPath": "insuredObject.property.address.typeCode",
        "varType": "IN",
        "varValue": "REGISTRATION"
      },
      {
        "varNr": 1003,
        "varDataType": "STRING",
        "varCode": "io_addr_countryCode",
        "varName": "Имущество.адрес.код страны",
        "varPath": "insuredObject.property.address.countryCode",
        "varType": "IN",
        "varValue": "RU"
      },
      {
        "varNr": 1004,
        "varDataType": "STRING",
        "varCode": "io_addr_region",
        "varName": "Имущество.адрес.регион",
        "varPath": "insuredObject.property.address.region",
        "varType": "IN",
        "varValue": "г Москва"
      },
      {
        "varNr": 1005,
        "varDataType": "STRING",
        "varCode": "io_addr_city",
        "varName": "Имущество.адрес.город",
        "varPath": "insuredObject.property.address.city",
        "varType": "IN",
        "varValue": "Москва"
      },
      {
        "varNr": 1006,
        "varDataType": "STRING",
        "varCode": "io_addr_street",
        "varName": "Имущество.адрес.улица",
        "varPath": "insuredObject.property.address.street",
        "varType": "IN",
        "varValue": "Академика Королева"
      },
      {
        "varNr": 1007,
        "varDataType": "STRING",
        "varCode": "io_addr_house",
        "varName": "Имущество.адрес.дом",
        "varPath": "insuredObject.property.address.house",
        "varType": "IN",
        "varValue": "3"
      },
      {
        "varNr": 1008,
        "varDataType": "STRING",
        "varCode": "io_addr_building",
        "varName": "Имущество.адрес.строение",
        "varPath": "insuredObject.property.address.building",
        "varType": "IN",
        "varValue": "2"
      },
      {
        "varNr": 1009,
        "varDataType": "STRING",
        "varCode": "io_addr_flat",
        "varName": "Имущество.адрес.квартира",
        "varPath": "insuredObject.property.address.flat",
        "varType": "IN",
        "varValue": "25"
      },
      {
        "varNr": 1010,
        "varDataType": "STRING",
        "varCode": "io_addr_room",
        "varName": "Имущество.адрес.комната",
        "varPath": "insuredObject.property.address.room",
        "varType": "IN",
        "varValue": "2"
      },
      {
        "varNr": 1011,
        "varDataType": "STRING",
        "varCode": "io_addr_zipCode",
        "varName": "Имущество.адрес.индекс",
        "varPath": "insuredObject.property.address.zipCode",
        "varType": "IN",
        "varValue": "129515"
      },
      {
        "varNr": 1012,
        "varDataType": "STRING",
        "varCode": "io_addr_kladrId",
        "varName": "Имущество.адрес.код КЛАДР",
        "varPath": "insuredObject.property.address.kladrId",
        "varType": "IN",
        "varValue": "7700000000015450062"
      },
      {
        "varNr": 1013,
        "varDataType": "STRING",
        "varCode": "io_addr_fiasId",
        "varName": "Имущество.адрес.код ФИАС",
        "varPath": "insuredObject.property.address.fiasId",
        "varType": "IN",
        "varValue": "f64c75cd-a640-41ed-9893-c1aaef58e638"
      },
      {
        "varNr": 1014,
        "varDataType": "STRING",
        "varCode": "io_addr_addressStr",
        "varName": "Имущество.адрес.адресная строка",
        "varPath": "insuredObject.property.address.addressStr",
        "varType": "IN",
        "varValue": "129515, г.Москва, ул.Академика Королева, д.2, к.3, кв.25"
      },
      {
        "varNr": 1015,
        "varDataType": "STRING",
        "varCode": "io_addr_addressStrEn",
        "varName": "Имущество.адрес.адресная строка англ",
        "varPath": "insuredObject.property.address.addressStrEn",
        "varType": "IN",
        "varValue": "129515, г.Moscow"
      },
      {
        "varNr": 1016,
        "varDataType": "STRING",
        "varCode": "io_addr_vsk_id",
        "varName": "Имущество.адрес.ID ВСК",
        "varPath": "insuredObject.property.address.vsk_id",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 1017,
        "varDataType": "STRING",
        "varCode": "io_addr_ext_id",
        "varName": "Имущество.адрес.внешний ID",
        "varPath": "insuredObject.property.address.ext_id",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 1018,
        "varDataType": "STRING",
        "varCode": "io_cadastrNr",
        "varName": "Имущество.кадастровый номер",
        "varPath": "insuredObject.property.cadastrNr",
        "varType": "IN",
        "varValue": "77:07:0018002:2590"
      },
      {
        "varNr": 1019,
        "varDataType": "STRING",
        "varCode": "io_wallsMaterial",
        "varName": "Имущество.материал стен",
        "varPath": "insuredObject.property.wallsMaterial",
        "varType": "IN",
        "varValue": "Каменные, кирпичные"
      },
      {
        "varNr": 1020,
        "varDataType": "STRING",
        "varCode": "io_wallsMaterialOther",
        "varName": "Имущество.материал стен другой",
        "varPath": "insuredObject.property.wallsMaterialOther",
        "varType": "IN",
        "varValue": "Каменные, кирпичные"
      },
      {
        "varNr": 1021,
        "varDataType": "STRING",
        "varCode": "io_ceilingMaterial",
        "varName": "Имущество.материал перекрытий",
        "varPath": "insuredObject.property.ceilingMaterial",
        "varType": "IN",
        "varValue": "Смешанные"
      },
      {
        "varNr": 1022,
        "varDataType": "STRING",
        "varCode": "io_ceilingMaterialOther",
        "varName": "Имущество.материал перекрытий другой",
        "varPath": "insuredObject.property.ceilingMaterialOther",
        "varType": "IN",
        "varValue": "Смешанные"
      },
      {
        "varNr": 1023,
        "varDataType": "STRING",
        "varCode": "io_constructionYear",
        "varName": "Имущество.год постройки",
        "varPath": "insuredObject.property.constructionYear",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 1024,
        "varDataType": "STRING",
        "varCode": "io_repairYear",
        "varName": "Имущество.год ремонта",
        "varPath": "insuredObject.property.repairYear",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 1025,
        "varDataType": "NUMBER",
        "varCode": "io_buildingArea",
        "varName": "Имущество.площадь здания",
        "varPath": "insuredObject.property.buildingArea",
        "varType": "IN",
        "varValue": "0"
      },
      {
        "varNr": 1026,
        "varDataType": "NUMBER",
        "varCode": "io_landArea",
        "varName": "Имущество.площадь участка",
        "varPath": "insuredObject.property.landArea",
        "varType": "IN",
        "varValue": "0"
      },
      {
        "varNr": 1027,
        "varDataType": "NUMBER",
        "varCode": "io_buildingValue",
        "varName": "Имущество.стоимость здания",
        "varPath": "insuredObject.property.buildingValue",
        "varType": "IN",
        "varValue": "0"
      },
      {
        "varNr": 1028,
        "varDataType": "NUMBER",
        "varCode": "io_wearCoefficient",
        "varName": "Имущество.коэффициент износа",
        "varPath": "insuredObject.property.wearCoefficient",
        "varType": "IN",
        "varValue": "0"
      },
      {
        "varNr": 1029,
        "varDataType": "NUMBER",
        "varCode": "io_numberOfFloors",
        "varName": "Имущество.количество этажей",
        "varPath": "insuredObject.property.numberOfFloors",
        "varType": "IN",
        "varValue": "0"
      },
      {
        "varNr": 1030,
        "varDataType": "STRING",
        "varCode": "io_propertyLocation",
        "varName": "Имущество.расположение имущества",
        "varPath": "insuredObject.property.propertyLocation",
        "varType": "IN",
        "varValue": "В многоквартирном доме"
      },
      {
        "varNr": 1031,
        "varDataType": "STRING",
        "varCode": "io_isNewBuilding",
        "varName": "Имущество.новостройка",
        "varPath": "insuredObject.property.isNewBuilding",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 1032,
        "varDataType": "NUMBER",
        "varCode": "io_propertyValue",
        "varName": "Имущество.стоимость имущества",
        "varPath": "insuredObject.property.propertyValue",
        "varType": "IN",
        "varValue": "0"
      },
      {
        "varNr": 1033,
        "varDataType": "STRING",
        "varCode": "io_commissioningDate",
        "varName": "Имущество.дата ввода в эксплуатацию",
        "varPath": "insuredObject.property.commissioningDate",
        "varType": "IN",
        "varValue": ""
      },
      {
        "varNr": 1034,
        "varDataType": "NUMBER",
        "varCode": "io_floor",
        "varName": "Имущество.этаж",
        "varPath": "insuredObject.property.floor",
        "varType": "IN",
        "varValue": "0"
      }
    ]

  }
