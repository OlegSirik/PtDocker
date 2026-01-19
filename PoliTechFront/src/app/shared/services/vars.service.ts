import { Injectable } from '@angular/core';

export interface LobVar {
    varCode: string;
    varType: string;
    varPath: string;
    varName: string;
    varDataType: string;
    varValue: string;
    varCdm: string
    varNr: number;
  }

@Injectable({
    providedIn: 'root'
  })

export class VarsService {
  private allVars: LobVar[] = [];

  constructor() {
    this.allVars = [
      ...this.phPersonVars,
      ...this.phPersonMagicVars,
      ...this.phContactsVars,
      ...this.phPersonOrganizationVars,
      ...this.phPersonIdentifiersVars,
      ...this.phAddressVars,
      ...this.phOrganizationVars,
      ...this.phOrganizationIdentifiersVars,
      ...this.ioDeviceVars,
      ...this.ioPropertyVars,
      ...this.ioPersonMagicVars,
      ...this.policyVars,
      ...this.policyMagicVars,
      ...this.getIoPersonVars(),
      ...this.getIoOrganizationVars(),
      ...this.getIoContactsVars(),
      ...this.getIoPersonIdentifiersVars(),
      ...this.getIoAddressVars()
    ]; 
  }

//  getVarByCode(code: string): LobVar | undefined {
//    return this.allVars.find(v => v.varCode === code);
//  }

  getPhCategories(type: string): string[] | any[] {
    if (type === 'person') {
      return [
        "person",
        "contacts",
        "organization",
        "identifiers",
        "addresses",
        "additionalProperties"
      ];
    }
    if (type === 'organization') {
      return [
        "organization",
        "identifiers",
        "addresses",
        "additionalProperties"
      ];
    }
    return [];
  }

  getPhDefVars(type: string): LobVar[] | any[]{
    if (type === 'person') {
      return   this.phPersonVars.
        concat(this.phContactsVars).
        concat(this.phOrganizationVars).
        concat(this.phPersonIdentifiersVars).
        concat(this.phAddressVars).
        concat(this.phPersonMagicVars);
    }
    if (type === 'organization') {
      return this.phOrganizationVars.
      concat(this.phOrganizationIdentifiersVars).
      concat(this.phContactsVars).
      concat(this.phAddressVars);
      //concat(this.phOrganizationMagicVars).
    }
    return [];
  }

  getIoDefVars(type: string): LobVar[] | any[]{
    if (type === 'person') {

        return [
        ...this.getIoPersonVars(),
        ...this.getIoOrganizationVars(),
        ...this.getIoContactsVars(),
        ...this.getIoPersonIdentifiersVars(),
        ...this.getIoAddressVars()
      ];

    }
    if (type === 'device') {
        return this.ioDeviceVars;
    }
  
    if (type === 'property') {
        return this.ioPropertyVars;
    }
    if (type === 'avia-ns') {
      return [
        ...this.getIoPersonVars(),
        ...this.getIoContactsVars(),
        ...this.ioTravelSegmentsVars
      ];
  }
  return [];
  }

  getIoCategories(type: string): string[] | any[] {
    if (type === 'person') {
      return [
        "person",
        "contacts",
        "organization",
        "identifiers",
        "addresses",
        "additionalProperties"
      ];
    }
    if (type === 'device') {
      return [
        "device",
        "additionalProperties"
      ];
    }
    if (type === 'property') {
      return [
        "property",
        "additionalProperties"
      ];
    }
    if (type === 'avia-ns') {
      return [
        "person",
        "contacts",
        "travelSegments",
        "additionalProperties"
      ];
    }
    return [];
  }

  getIoPersonVars(): LobVar[] {
    return this.phPersonVars.map(v => ({
      ...v,
      varCode: v.varCode.replace('ph_', 'io_'),
      varPath: v.varPath.replace('policyHolder.', 'insuredObject.'),
      varCdm: v.varCdm.replace('policyHolder.', 'insuredObject.'),
      varNr: v.varNr ? v.varNr + 5000 : 0 // Offset varNr to avoid conflicts
    }));
  }
  getIoOrganizationVars(): LobVar[] {
    return this.phOrganizationVars.map(v => ({
      ...v,
      varCode: v.varCode.replace('ph_', 'io_'),
      varPath: v.varPath.replace('policyHolder.', 'insuredObject.'),
      varCdm: v.varCdm.replace('policyHolder.', 'insuredObject.'),
      varNr: v.varNr ? v.varNr + 5000 : 0 // Offset varNr to avoid conflicts
    }));
  }
  
  getIoContactsVars(): LobVar[] {
    return this.phContactsVars.map(v => ({
      ...v,
      varCode: v.varCode.replace('ph_', 'io_'),
      varPath: v.varPath.replace('policyHolder.', 'insuredObject.'),
      varCdm: v.varCdm.replace('policyHolder.', 'insuredObject.'),
      varNr: v.varNr ? v.varNr + 5000 : 0 // Offset varNr to avoid conflicts
    }));
  }
  getIoPersonIdentifiersVars(): LobVar[] {
    return this.phPersonIdentifiersVars.map(v => ({
      ...v,
      varCode: v.varCode.replace('ph_', 'io_'),
      varPath: v.varPath.replace('policyHolder.', 'insuredObject.'),
      varCdm: v.varCdm.replace('policyHolder.', 'insuredObject.'),
      varNr: v.varNr ? v.varNr + 5000 : 0 // Offset varNr to avoid conflicts
    }));
  }
  getIoAddressVars(): LobVar[] {
    
    return this.phAddressVars.map(v => ({
      ...v,
      varCode: v.varCode.replace('ph_', 'io_person_'),
      varPath: v.varPath.replace('policyHolder.', 'insuredObject.'),
      varCdm: v.varCdm.replace('policyHolder.', 'insuredObject.'),
      varNr: v.varNr ? v.varNr + 5000 : 0 // Offset varNr to avoid conflicts
    }));
  }

  phPersonVars: LobVar[] = [
    {
      "varNr": 1,
      "varDataType": "STRING",
      "varCode": "ph_firstName",
      "varName": "Страхователь.имя",
      "varPath": "policyHolder.person.firstName",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.person.firstName"
    },
    {
      "varNr": 2,
      "varDataType": "STRING",
      "varCode": "ph_lastName",
      "varName": "Страхователь.фамилия",
      "varPath": "policyHolder.person.lastName",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.person.lastName"
    },
    {
      "varNr": 3,
      "varDataType": "STRING",
      "varCode": "ph_middleName",
      "varName": "Страхователь.отчество",
      "varPath": "policyHolder.person.middleName",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.person.middleName"
    },
    {
      "varNr": 4,
      "varDataType": "STRING",
      "varCode": "ph_birthDate",
      "varName": "Страхователь.дата рождения",
      "varPath": "policyHolder.person.birthDate",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.person.birthDate"
    },
    {
      "varNr": 5,
      "varDataType": "STRING",
      "varCode": "ph_fullName",
      "varName": "Страхователь.полное ФИО",
      "varPath": "policyHolder.person.fullName",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.person.fullName"
    },
    {
      "varNr": 6,
      "varDataType": "STRING",
      "varCode": "ph_fullNameEn",
      "varName": "Страхователь.полное ФИО англ",
      "varPath": "policyHolder.person.fullNameEn",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.person.fullNameEn"
    },
    {
      "varNr": 7,
      "varDataType": "STRING",
      "varCode": "ph_birthPlace",
      "varName": "Страхователь.место рождения",
      "varPath": "policyHolder.person.birthPlace",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.person.birthPlace"
    },
    {
      "varNr": 8,
      "varDataType": "STRING",
      "varCode": "ph_citizenship",
      "varName": "Страхователь.гражданство",
      "varPath": "policyHolder.person.citizenship",
      "varType": "IN",
      "varValue": "RU",
      "varCdm": "policyHolder.person.citizenship"
    },
    {
      "varNr": 9,
      "varDataType": "STRING",
      "varCode": "ph_gender",
      "varName": "Страхователь.пол",
      "varPath": "policyHolder.person.gender",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.person.gender"
    },
    {
      "varNr": 10,
      "varDataType": "STRING",
      "varCode": "ph_familyState",
      "varName": "Страхователь.семейное положение",
      "varPath": "policyHolder.person.familyState",
      "varType": "IN",
      "varValue": "SINGLE",
      "varCdm": "policyHolder.person.familyState"
    },
    {
      "varNr": 11,
      "varDataType": "STRING",
      "varCode": "ph_isPublicOfficial",
      "varName": "Страхователь.признак ПДЛ",
      "varPath": "policyHolder.person.isPublicOfficial",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.person.isPublicOfficial"
    },
    {
      "varNr": 12,
      "varDataType": "STRING",
      "varCode": "ph_isResident",
      "varName": "Страхователь.резидент РФ",
      "varPath": "policyHolder.person.isResident",
      "varType": "IN",
      "varValue": "true",
      "varCdm": "policyHolder.person.isResident"
    },
    {
      "varNr": 14,
      "varDataType": "STRING",
      "varCode": "ph_ext_id",
      "varName": "Страхователь.внешний ID",
      "varPath": "policyHolder.person.ext_id",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.person.ext_id"
    }
  ];

  phPersonMagicVars: LobVar[] = [
    {
      "varNr": 1113,
      "varDataType": "STRING",
      "varCode": "ph_isMale",
      "varName": "Страхователь.пол.М",
      "varPath": "",
      "varType": "MAGIC",
      "varValue": "",
      "varCdm": "policyHolder.person.isMale"
    },
    {
      "varNr": 1114,
      "varDataType": "STRING",
      "varCode": "ph_isFemale",
      "varName": "Страхователь.пол.Ж",
      "varPath": "",
      "varType": "MAGIC",
      "varValue": "",
      "varCdm": "policyHolder.person.isFemale"
    },
    {
      "varNr": 1115,
      "varDataType": "NUMBER",
      "varCode": "ph_age_issue",
      "varName": "Страхователь.возраст на дату выпуска",
      "varPath": "",
      "varType": "MAGIC",
      "varValue": "",
      "varCdm": "policyHolder.person.age_issue"
    },

  ];

  phContactsVars: LobVar[] = [
  {
    "varNr": 81,
    "varDataType": "STRING",
    "varCode": "ph_phone",
    "varName": "Страхователь.телефон",
    "varPath": "policyHolder.phone.phoneNumber",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.contacts.phone"
  },
  {
    "varNr": 91,
    "varDataType": "STRING",
    "varCode": "ph_email",
    "varName": "Страхователь.email",
    "varPath": "policyHolder.email",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.contacts..email"
  }
  ];

  phPersonOrganizationVars: LobVar[] = [
    {
      "varNr": 51,
      "varDataType": "STRING",
      "varCode": "ph_country",
      "varName": "Страхователь.код страны регистрации",
      "varPath": "policyHolder.organization.country",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.organization.country"
    },
    {
      "varNr": 52,
      "varDataType": "STRING",
      "varCode": "ph_org_inn",
      "varName": "Страхователь.ИНН юр.лица",
      "varPath": "policyHolder.organization.inn",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.organization.inn"
    },
    {
      "varNr": 53,
      "varDataType": "STRING",
      "varCode": "ph_org_fullName",
      "varName": "Страхователь.полное наименование юр.лица",
      "varPath": "policyHolder.organization.fullName",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.organization.fullName"
    },
    {
      "varNr": 54,
      "varDataType": "STRING",
      "varCode": "ph_org_fullNameEn",
      "varName": "Страхователь.полное наименование  юр.лица англ",
      "varPath": "policyHolder.organization.fullNameEn",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.organization.fullNameEn"
    },
    {
      "varNr": 55,
      "varDataType": "STRING",
      "varCode": "ph_org_shortName",
      "varName": "Страхователь.краткое наименование юр.лица",
      "varPath": "policyHolder.organization.shortName",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.organization.shortName"
    },
    {
      "varNr": 56,
      "varDataType": "STRING",
      "varCode": "ph_org_legalForm",
      "varName": "Страхователь.организационно-правовая форма",
      "varPath": "policyHolder.organization.legalForm",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.organization.legalForm"
    },
    {
      "varNr": 57,
      "varDataType": "STRING",
      "varCode": "ph_org_kpp",
      "varName": "Страхователь.КПП",
      "varPath": "policyHolder.organization.kpp",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.organization.kpp"
    },
    {
      "varNr": 58,
      "varDataType": "STRING",
      "varCode": "ph_org_ogrn",
      "varName": "Страхователь.ОГРН",
      "varPath": "policyHolder.organization.ogrn",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.organization.ogrn"
    },
    {
      "varNr": 59,
      "varDataType": "STRING",
      "varCode": "ph_org_okpo",
      "varName": "Страхователь.ОКПО",
      "varPath": "policyHolder.organization.okpo",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.organization.okpo"
    },
    {
      "varNr": 60,
      "varDataType": "STRING",
      "varCode": "ph_org_bic",
      "varName": "Страхователь.БИК",
      "varPath": "policyHolder.organization.bic",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.organization.bic"
    },
    {
      "varNr": 61,
      "varDataType": "STRING",
      "varCode": "ph_org_isResident",
      "varName": "Страхователь.резидент РФ юр.лица",
      "varPath": "policyHolder.organization.isResident",
      "varType": "IN",
      "varValue": "true",
      "varCdm": "policyHolder.organization.isResident"
    },
    {
      "varNr": 62,
      "varDataType": "STRING",
      "varCode": "ph_org_group",
      "varName": "Страхователь.Gруппа страхователя ЮЛ",
      "varPath": "policyHolder.organization.group",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.organization.group"
    },
    {
      "varNr": 64,
      "varDataType": "STRING",
      "varCode": "ph_org_ext_id",
      "varName": "Страхователь.внешний ID",
      "varPath": "policyHolder.organization.ext_id",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.organization.ext_id"
    },
    {
      "varNr": 65,
      "varDataType": "STRING",
      "varCode": "ph_org_nciCode",
      "varName": "Страхователь.НСИ код",
      "varPath": "policyHolder.organization.nciCode",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.organization.nciCode"
    }
  ];

  phPersonIdentifiersVars: LobVar[] = [
    {
      "varNr": 101,
      "varDataType": "STRING",
      "varCode": "ph_doc_typeCode",
      "varName": "Документ.код типа документа",
      "varPath": "policyHolder.passport.typeCode",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.identifiers.typeCode"
    },
    {
      "varNr": 102,
      "varDataType": "STRING",
      "varCode": "ph_doc_serial",
      "varName": "Документ.серия документа",
      "varPath": "policyHolder.passport.serial",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.identifiers.serial"
    },
    {
      "varNr": 103,
      "varDataType": "STRING",
      "varCode": "ph_doc_number",
      "varName": "Документ.номер документа",
      "varPath": "policyHolder.passport.number",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.identifiers.number"
    },
    {
      "varNr": 104,
      "varDataType": "STRING",
      "varCode": "ph_doc_dateIssue",
      "varName": "Документ.дата выдачи",
      "varPath": "policyHolder.passport.dateIssue",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.identifiers.dateIssue"
    },
    {
      "varNr": 105,
      "varDataType": "STRING",
      "varCode": "ph_doc_validUntil",
      "varName": "Документ.действительно до",
      "varPath": "policyHolder.passport.validUntil",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.identifiers.validUntil"
    },
    {
      "varNr": 106,
      "varDataType": "STRING",
      "varCode": "ph_doc_whom",
      "varName": "Документ.кем выдан",
      "varPath": "policyHolder.passport.whom",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.identifiers.whom"
    },
    {
      "varNr": 107,
      "varDataType": "STRING",
      "varCode": "ph_doc_divisionCode",
      "varName": "Документ.код подразделения",
      "varPath": "policyHolder.passport.divisionCode",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.identifiers.divisionCode"
    },
    {
      "varNr": 108,
      "varDataType": "STRING",
      "varCode": "ph_doc_vsk_id",
      "varName": "Документ.ID ВСК",
      "varPath": "policyHolder.passport.vsk_id",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.identifiers.vsk_id"
    },
    {
      "varNr": 109,
      "varDataType": "STRING",
      "varCode": "ph_doc_ext_id",
      "varName": "Документ.внешний ID",
      "varPath": "policyHolder.passport.ext_id",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.identifiers.ext_id"
    },
    {
      "varNr": 110,
      "varDataType": "STRING",
      "varCode": "ph_doc_countryCode",
      "varName": "Документ.страна выдачи документа",
      "varPath": "policyHolder.passport.countryCode",
      "varType": "IN",
      "varValue": "RU",
      "varCdm": "policyHolder.identifiers.countryCode"
    }
  ];

  phAddressVars: LobVar[] = [
    {
      "varNr": 121,
      "varDataType": "STRING",
      "varCode": "ph_addr_typeCode",
      "varName": "Адрес.тип адреса",
      "varPath": "policyHolder.address.typeCode",
      "varType": "IN",
      "varValue": "REGISTRATION",
      "varCdm": "policyHolder.addresses.typeCode"
    },
    {
      "varNr": 122,
      "varDataType": "STRING",
      "varCode": "ph_addr_countryCode",
      "varName": "Адрес.код страны",
      "varPath": "policyHolder.address.countryCode",
      "varType": "IN",
      "varValue": "RU",
      "varCdm": "policyHolder.addresses.countryCode"
    },
    {
      "varNr": 123,
      "varDataType": "STRING",
      "varCode": "ph_addr_region",
      "varName": "Адрес.регион",
      "varPath": "policyHolder.address.region",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.addresses.region"
    },
    {
      "varNr": 124,
      "varDataType": "STRING",
      "varCode": "ph_addr_city",
      "varName": "Адрес.город",
      "varPath": "policyHolder.address.city",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.addresses.city"
    },
    {
      "varNr": 125,
      "varDataType": "STRING",
      "varCode": "ph_addr_street",
      "varName": "Адрес.улица",
      "varPath": "policyHolder.address.street",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.addresses.street"
    },
    {
      "varNr": 126,
      "varDataType": "STRING",
      "varCode": "ph_addr_house",
      "varName": "Адрес.дом",
      "varPath": "policyHolder.address.house",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.addresses.house"
    },
    {
      "varNr": 127,
      "varDataType": "STRING",
      "varCode": "ph_addr_building",
      "varName": "Адрес.строение",
      "varPath": "policyHolder.address.building",
      "varType": "IN",
        "varValue": "",
      "varCdm": "policyHolder.addresses.building"
    },
    {
      "varNr": 128,
      "varDataType": "STRING",
      "varCode": "ph_addr_flat",
      "varName": "Адрес.квартира",
      "varPath": "policyHolder.address.flat",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.addresses.flat"
    },
    {
      "varNr": 129,
      "varDataType": "STRING",
      "varCode": "ph_addr_room",
      "varName": "Адрес.комната",
      "varPath": "policyHolder.address.room",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.addresses.room"
    },
    {
      "varNr": 130,
      "varDataType": "STRING",
      "varCode": "ph_addr_zipCode",
      "varName": "Адрес.индекс",
      "varPath": "policyHolder.address.zipCode",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.addresses.zipCode"
    },
    {
      "varNr": 131,
      "varDataType": "STRING",
      "varCode": "ph_addr_kladrId",
      "varName": "Адрес.код КЛАДР",
      "varPath": "policyHolder.address.kladrId",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.addresses.kladrId"
    },
    {
      "varNr": 132,
      "varDataType": "STRING",
      "varCode": "ph_addr_fiasId",
      "varName": "Адрес.код ФИАС",
      "varPath": "policyHolder.address.fiasId",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.addresses.fiasId"
    },
    {
      "varNr": 133,
      "varDataType": "STRING",
      "varCode": "ph_addr_addressStr",
      "varName": "Адрес.адресная строка",
      "varPath": "policyHolder.address.addressStr",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.addresses.addressStr"
    },
    {
      "varNr": 134,
      "varDataType": "STRING",
      "varCode": "ph_addr_addressStrEn",
      "varName": "Адрес.адресная строка англ",
      "varPath": "policyHolder.address.addressStrEn",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.addresses.addressStrEn"
    },
    {
      "varNr": 136,
      "varDataType": "STRING",
      "varCode": "ph_addr_ext_id",
      "varName": "Адрес.внешний ID",
      "varPath": "policyHolder.address.ext_id",
      "varType": "IN",
      "varValue": "",
      "varCdm": "policyHolder.addresses.ext_id"
    }
  ];

  phOrganizationVars: LobVar[] = [
  {
    "varNr": 141,
    "varDataType": "STRING",
    "varCode": "ph_country",
    "varName": "Страхователь.код страны регистрации",
    "varPath": "policyHolder.organization.country",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.organization.country"
  },
  {
    "varNr": 142,
    "varDataType": "STRING",
    "varCode": "ph_inn",
    "varName": "Страхователь.ИНН юр.лица",
    "varPath": "policyHolder.organization.inn",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.organization.inn"
  },
  {
    "varNr": 143,
    "varDataType": "STRING",
    "varCode": "ph_fullName",
    "varName": "Страхователь.полное наименование юр.лица",
    "varPath": "policyHolder.organization.fullName",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.organization.fullName"
  },
  {
    "varNr": 144,
    "varDataType": "STRING",
    "varCode": "ph_fullNameEn",
    "varName": "Страхователь.полное наименование  юр.лица англ",
    "varPath": "policyHolder.organization.fullNameEn",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.organization.fullNameEn"
  },
  {
    "varNr": 145,
    "varDataType": "STRING",
    "varCode": "ph_shortName",
    "varName": "Страхователь.краткое наименование юр.лица",
    "varPath": "policyHolder.organization.shortName",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.organization.shortName"
  },
  {
    "varNr": 146,
    "varDataType": "STRING",
    "varCode": "ph_legalForm",
    "varName": "Страхователь.организационно-правовая форма",
    "varPath": "policyHolder.organization.legalForm",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.organization.legalForm"
  },
  {
    "varNr": 147,
    "varDataType": "STRING",
    "varCode": "ph_kpp",
    "varName": "Страхователь.КПП",
    "varPath": "policyHolder.organization.kpp",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.organization.kpp"
  },
  {
    "varNr": 148,
    "varDataType": "STRING",
    "varCode": "ph_ogrn",
    "varName": "Страхователь.ОГРН",
    "varPath": "policyHolder.organization.ogrn",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.organization.ogrn"
  },
  {
    "varNr": 149,
    "varDataType": "STRING",
    "varCode": "ph_org_okpo",
    "varName": "Страхователь.ОКПО",
    "varPath": "policyHolder.organization.okpo",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.organization.okpo"
  },
  {
    "varNr": 150,
    "varDataType": "STRING",
    "varCode": "ph_org_bic",
    "varName": "Страхователь.БИК",
    "varPath": "policyHolder.organization.bic",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.organization.bic"
  },
  {
    "varNr": 151,
    "varDataType": "STRING",
    "varCode": "ph_org_isResident",
    "varName": "Страхователь.резидент РФ юр.лица",
    "varPath": "policyHolder.organization.isResident",
    "varType": "IN",
    "varValue": "true",
    "varCdm": "policyHolder.organization.isResident"
  },
  {
    "varNr": 152,
    "varDataType": "STRING",
    "varCode": "ph_org_group",
    "varName": "Страхователь.Gруппа страхователя ЮЛ",
    "varPath": "policyHolder.organization.group",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.organization.group"
  },
  {
    "varNr": 154,
    "varDataType": "STRING",
    "varCode": "ph_org_ext_id",
    "varName": "Страхователь.внешний ID",
    "varPath": "policyHolder.organization.ext_id",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.organization.ext_id"
  },
  {
    "varNr": 155,
    "varDataType": "STRING",
    "varCode": "ph_org_nciCode",
    "varName": "Страхователь.НСИ код",
    "varPath": "policyHolder.organization.nciCode",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.organization.nciCode"
  }
  ];

  phOrganizationIdentifiersVars: LobVar[] = [
  {
    "varNr": 161,
    "varDataType": "STRING",
    "varCode": "ph_doc_typeCode",
    "varName": "Документ.код типа документа",
    "varPath": "policyHolder.document.typeCode",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.identifiers.typeCode"
  },
  {
    "varNr": 162,
    "varDataType": "STRING",
    "varCode": "ph_doc_serial",
    "varName": "Документ.серия документа",
    "varPath": "policyHolder.document.serial",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.identifiers.serial"
  },
  {
    "varNr": 163,
    "varDataType": "STRING",
    "varCode": "ph_doc_number",
    "varName": "Документ.номер документа",
    "varPath": "policyHolder.document.number",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.identifiers.number"
  },
  {
    "varNr": 164,
    "varDataType": "STRING",
    "varCode": "ph_doc_dateIssue",
    "varName": "Документ.дата выдачи",
    "varPath": "policyHolder.document.dateIssue",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.identifiers.dateIssue"
  },
  {
    "varNr": 165,
    "varDataType": "STRING",
    "varCode": "ph_doc_validUntil",
    "varName": "Документ.действительно до",
    "varPath": "policyHolder.document.validUntil",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.identifiers.validUntil"
  },
  {
    "varNr": 166,
    "varDataType": "STRING",
    "varCode": "ph_doc_whom",
    "varName": "Документ.кем выдан",
    "varPath": "policyHolder.document.whom",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.identifiers.whom"
  },
  {
    "varNr": 167,
    "varDataType": "STRING",
    "varCode": "ph_doc_divisionCode",
    "varName": "Документ.код подразделения",
    "varPath": "policyHolder.document.divisionCode",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.identifiers.divisionCode"
  },
  {
    "varNr": 168,
    "varDataType": "STRING",
    "varCode": "ph_doc_vsk_id",
    "varName": "Документ.ID ВСК",
    "varPath": "policyHolder.document.vsk_id",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.identifiers.vsk_id"
  },
  {
    "varNr": 169,
    "varDataType": "STRING",
    "varCode": "ph_doc_ext_id",
    "varName": "Документ.внешний ID",
    "varPath": "policyHolder.document.ext_id",
    "varType": "IN",
    "varValue": "",
    "varCdm": "policyHolder.identifiers.ext_id"
  },
  {
    "varNr": 170,
    "varDataType": "STRING",
    "varCode": "ph_doc_countryCode",
    "varName": "Документ.страна выдачи документа",
    "varPath": "policyHolder.document.countryCode",
    "varType": "IN",
    "varValue": "RU",
    "varCdm": "policyHolder.identifiers.countryCode"
  }
  ];

  /* io с varNr < 100 системные и не удаляются */ 
  ioDeviceVars: LobVar[] = [
        {
          "varNr": 10,
          "varDataType": "NUMBER",
          "varCode": "sumInsured",
          "varName": "Страховая сумма объекта страхования",
          "varPath": "insuredObjects[0].sumInsured",
          "varType": "IN",
          "varValue": "100000",
          "varCdm": "insuredObject.sumInsured"
        },
        {
        "varNr": 1001,
        "varDataType": "STRING",
        "varCode": "io_device_name",
        "varName": "Застрахованное ус-во. название",
        "varPath": "insuredObject.device.deviceName",
        "varType": "IN",
        "varValue": "Телефон",
        "varCdm": "insuredObject.device.deviceName"
        },
        {
        "varNr": 1002,
        "varDataType": "STRING",
        "varCode": "io_device_typeCode",
        "varName": "Застрахованное ус-во. код типа",
        "varPath": "insuredObject.device.deviceTypeCode",
        "varType": "IN",
        "varValue": "PHONE",
        "varCdm": "insuredObject.device.deviceTypeCode"
        },
        {
        "varNr": 1003,
        "varDataType": "STRING",
        "varCode": "io_device_tradeMark",
        "varName": "Застрахованное ус-во. торговая марка",
        "varPath": "insuredObject.device.tradeMark",
        "varType": "IN",
        "varValue": "Samsung",
        "varCdm": "insuredObject.device.tradeMark"
        },
        {
        "varNr": 1004,
        "varDataType": "STRING",
        "varCode": "io_device_model",
        "varName": "Застрахованное ус-во. модель",
        "varPath": "insuredObject.device.model",
        "varType": "IN",
        "varValue": "Galaxy S21",
        "varCdm": "insuredObject.device.model"
        },
        {
        "varNr": 1005,
        "varDataType": "STRING",
        "varCode": "io_device_serialNr",
        "varName": "Застрахованное ус-во. серийный номер",
        "varPath": "insuredObject.device.serialNr",
        "varType": "IN",
        "varValue": "1234567890",
        "varCdm": "insuredObject.device.serialNr"
        },
        {
        "varNr": 1006,
        "varDataType": "STRING",
        "varCode": "io_device_licenseKey",
        "varName": "Застрахованное ус-во. ключ лицензии",
        "varPath": "insuredObject.device.licenseKey",
        "varType": "IN",
        "varValue": "1234567890",
        "varCdm": "insuredObject.device.licenseKey"
        },
        {
        "varNr": 1007,
        "varDataType": "STRING",
        "varCode": "io_device_imei",
        "varName": "Застрахованное ус-во. IMEI",
        "varPath": "insuredObject.device.imei",
        "varType": "IN",
        "varValue": "1234567890",
        "varCdm": "insuredObject.device.imei"
        },
        {
        "varNr": 1008,
        "varDataType": "STRING",
        "varCode": "io_device_osName",
        "varName": "Застрахованное ус-во. название ОС",
        "varPath": "insuredObject.device.osName",
        "varType": "IN",
        "varValue": "Android",
        "varCdm": "insuredObject.device.osName"
        },
        {
        "varNr": 1008,
        "varDataType": "STRING",
        "varCode": "io_device_osVersion",
        "varName": "Застрахованное ус-во. версия ОС",
        "varPath": "insuredObject.device.osVersion",
        "varType": "IN",
        "varValue": "10",
        "varCdm": "insuredObject.device.osVersion"
        },
        {
        "varNr": 1009,
        "varDataType": "STRING",
        "varCode": "io_device_countryCode",
        "varName": "Застрахованное ус-во. код страны",
        "varPath": "insuredObject.device.countryCode",
        "varType": "IN",
        "varValue": "RU",
        "varCdm": "insuredObject.device.countryCode"
        },
        {
        "varNr": 1010,
        "varDataType": "STRING",
        "varCode": "io_device_devicePrice",
        "varName": "Застрахованное ус-во. цена",
        "varPath": "insuredObject.device.devicePrice",
        "varType": "IN",
        "varValue": "10000",
        "varCdm": "insuredObject.device.devicePrice"
        }
  ];

  ioPropertyVars: LobVar[] = [
    {
      "varNr": 1001,
      "varDataType": "STRING",
      "varCode": "io_propertyType_code",
      "varName": "Имущество.тип имущества код",
      "varPath": "insuredObject.property.propertyType.code",
      "varType": "IN",
      "varValue": "",
      "varCdm": "insuredObject.property.propertyType.code"
    },
    {
      "varNr": 1002,
      "varDataType": "STRING",
      "varCode": "io_addr_typeCode",
      "varName": "Имущество.адрес.тип адреса",
      "varPath": "insuredObject.property.address.typeCode",
      "varType": "IN",
      "varValue": "REGISTRATION",
      "varCdm": "insuredObject.property.address.typeCode"
    },
    {
      "varNr": 1003,
      "varDataType": "STRING",
      "varCode": "io_addr_countryCode",
      "varName": "Имущество.адрес.код страны",
      "varPath": "insuredObject.property.address.countryCode",
      "varType": "IN",
      "varValue": "RU",
      "varCdm": "insuredObject.property.address.countryCode"
    },
    {
      "varNr": 1004,
      "varDataType": "STRING",
      "varCode": "io_addr_region",
      "varName": "Имущество.адрес.регион",
      "varPath": "insuredObject.property.address.region",
      "varType": "IN",
        "varValue": "г Москва",
      "varCdm": "insuredObject.property.address.region"
    },
    {
      "varNr": 1005,
      "varDataType": "STRING",
      "varCode": "io_addr_city",
      "varName": "Имущество.адрес.город",
      "varPath": "insuredObject.property.address.city",
      "varType": "IN",
      "varValue": "Москва",
      "varCdm": "insuredObject.property.address.city"
    },
    {
      "varNr": 1006,
      "varDataType": "STRING",
      "varCode": "io_addr_street",
      "varName": "Имущество.адрес.улица",
      "varPath": "insuredObject.property.address.street",
      "varType": "IN",
      "varValue": "Академика Королева",
      "varCdm": "insuredObject.property.address.street"
    },
    {
      "varNr": 1007,
      "varDataType": "STRING",
      "varCode": "io_addr_house",
      "varName": "Имущество.адрес.дом",
      "varPath": "insuredObject.property.address.house",
      "varType": "IN",
      "varValue": "3",
      "varCdm": "insuredObject.property.address.house"
    },
    {
      "varNr": 1008,
      "varDataType": "STRING",
      "varCode": "io_addr_building",
      "varName": "Имущество.адрес.строение",
      "varPath": "insuredObject.property.address.building",
      "varType": "IN",
      "varValue": "2",
      "varCdm": "insuredObject.property.address.building"
    },
    {
      "varNr": 1009,
      "varDataType": "STRING",
      "varCode": "io_addr_flat",
      "varName": "Имущество.адрес.квартира",
      "varPath": "insuredObject.property.address.flat",
      "varType": "IN",
      "varValue": "25",
      "varCdm": "insuredObject.property.address.flat"
    },
    {
      "varNr": 1010,
      "varDataType": "STRING",
      "varCode": "io_addr_room",
      "varName": "Имущество.адрес.комната",
      "varPath": "insuredObject.property.address.room",
      "varType": "IN",
      "varValue": "2",
      "varCdm": "insuredObject.property.address.room"
    },
    {
      "varNr": 1011,
      "varDataType": "STRING",
      "varCode": "io_addr_zipCode",
      "varName": "Имущество.адрес.индекс",
      "varPath": "insuredObject.property.address.zipCode",
      "varType": "IN",
      "varValue": "129515",
      "varCdm": "insuredObject.property.address.zipCode"
    },
    {
      "varNr": 1012,
      "varDataType": "STRING",
      "varCode": "io_addr_kladrId",
      "varName": "Имущество.адрес.код КЛАДР",
      "varPath": "insuredObject.property.address.kladrId",
      "varType": "IN",
      "varValue": "7700000000015450062",
      "varCdm": "insuredObject.property.address.kladrId"
    },
    {
      "varNr": 1013,
      "varDataType": "STRING",
      "varCode": "io_addr_fiasId",
      "varName": "Имущество.адрес.код ФИАС",
      "varPath": "insuredObject.property.address.fiasId",
      "varType": "IN",
      "varValue": "f64c75cd-a640-41ed-9893-c1aaef58e638",
      "varCdm": "insuredObject.property.address.fiasId"
    },
    {
      "varNr": 1014,
      "varDataType": "STRING",
      "varCode": "io_addr_addressStr",
      "varName": "Имущество.адрес.адресная строка",
      "varPath": "insuredObject.property.address.addressStr",
      "varType": "IN",
      "varValue": "129515, г.Москва, ул.Академика Королева, д.2, к.3, кв.25",
      "varCdm": "insuredObject.property.address.addressStr"
    },
    {
      "varNr": 1015,
      "varDataType": "STRING",
      "varCode": "io_addr_addressStrEn",
      "varName": "Имущество.адрес.адресная строка англ",
      "varPath": "insuredObject.property.address.addressStrEn",
      "varType": "IN",
      "varValue": "129515, г.Moscow",
      "varCdm": "insuredObject.property.address.addressStrEn"
    },
    {
      "varNr": 1016,
      "varDataType": "STRING",
      "varCode": "io_addr_vsk_id",
      "varName": "Имущество.адрес.ID ВСК",
      "varPath": "insuredObject.property.address.vsk_id",
      "varType": "IN",
      "varValue": "",
      "varCdm": "insuredObject.property.address.vsk_id"
    },
    {
      "varNr": 1017,
      "varDataType": "STRING",
      "varCode": "io_addr_ext_id",
      "varName": "Имущество.адрес.внешний ID",
      "varPath": "insuredObject.property.address.ext_id",
      "varType": "IN",
      "varValue": "",
      "varCdm": "insuredObject.property.address.ext_id"
    },
    {
      "varNr": 1018,
      "varDataType": "STRING",
      "varCode": "io_cadastrNr",
      "varName": "Имущество.кадастровый номер",
      "varPath": "insuredObject.property.cadastrNr",
      "varType": "IN",
      "varValue": "77:07:0018002:2590",
      "varCdm": "insuredObject.property.cadastrNr"
    },
    {
      "varNr": 1019,
      "varDataType": "STRING",
      "varCode": "io_wallsMaterial",
      "varName": "Имущество.материал стен",
      "varPath": "insuredObject.property.wallsMaterial",
      "varType": "IN",
      "varValue": "Каменные, кирпичные",
      "varCdm": "insuredObject.property.wallsMaterial"
    },
    {
      "varNr": 1020,
      "varDataType": "STRING",
      "varCode": "io_wallsMaterialOther",
      "varName": "Имущество.материал стен другой",
      "varPath": "insuredObject.property.wallsMaterialOther",
      "varType": "IN",
      "varValue": "Каменные, кирпичные",
      "varCdm": "insuredObject.property.wallsMaterialOther"
    },
    {
      "varNr": 1021,
      "varDataType": "STRING",
      "varCode": "io_ceilingMaterial",
      "varName": "Имущество.материал перекрытий",
      "varPath": "insuredObject.property.ceilingMaterial",
      "varType": "IN",
      "varValue": "Смешанные",
      "varCdm": "insuredObject.property.ceilingMaterial"
    },
    {
      "varNr": 1022,
      "varDataType": "STRING",
      "varCode": "io_ceilingMaterialOther",
      "varName": "Имущество.материал перекрытий другой",
      "varPath": "insuredObject.property.ceilingMaterialOther",
      "varType": "IN",
      "varValue": "Смешанные",
      "varCdm": "insuredObject.property.ceilingMaterialOther"
    },
    {
      "varNr": 1023,
      "varDataType": "STRING",
      "varCode": "io_constructionYear",
      "varName": "Имущество.год постройки",
      "varPath": "insuredObject.property.constructionYear",
      "varType": "IN",
      "varValue": "",
      "varCdm": "insuredObject.property.constructionYear"
    },
    {
      "varNr": 1024,
      "varDataType": "STRING",
      "varCode": "io_repairYear",
      "varName": "Имущество.год ремонта",
      "varPath": "insuredObject.property.repairYear",
      "varType": "IN",
      "varValue": "",
      "varCdm": "insuredObject.property.repairYear"
    },
    {
      "varNr": 1025,
      "varDataType": "NUMBER",
      "varCode": "io_buildingArea",
      "varName": "Имущество.площадь здания",
      "varPath": "insuredObject.property.buildingArea",
      "varType": "IN",
      "varValue": "0",
      "varCdm": "insuredObject.property.buildingArea"
    },
    {
      "varNr": 1026,
      "varDataType": "NUMBER",
      "varCode": "io_landArea",
      "varName": "Имущество.площадь участка",
      "varPath": "insuredObject.property.landArea",
      "varType": "IN",
      "varValue": "0",
      "varCdm": "insuredObject.property.landArea"
    },
    {
      "varNr": 1027,
      "varDataType": "NUMBER",
      "varCode": "io_buildingValue",
      "varName": "Имущество.стоимость здания",
      "varPath": "insuredObject.property.buildingValue",
      "varType": "IN",
      "varValue": "0",
      "varCdm": "insuredObject.property.buildingValue"
    },
    {
      "varNr": 1028,
      "varDataType": "NUMBER",
      "varCode": "io_wearCoefficient",
      "varName": "Имущество.коэффициент износа",
      "varPath": "insuredObject.property.wearCoefficient",
      "varType": "IN",
      "varValue": "0",
      "varCdm": "insuredObject.property.wearCoefficient"
    },
    {
      "varNr": 1029,
      "varDataType": "NUMBER",
      "varCode": "io_numberOfFloors",
      "varName": "Имущество.количество этажей",
      "varPath": "insuredObject.property.numberOfFloors",
      "varType": "IN",
      "varValue": "0",
      "varCdm": "insuredObject.property.numberOfFloors"
    },
    {
      "varNr": 1030,
      "varDataType": "STRING",
      "varCode": "io_propertyLocation",
      "varName": "Имущество.расположение имущества",
      "varPath": "insuredObject.property.propertyLocation",
      "varType": "IN",
      "varValue": "В многоквартирном доме",
      "varCdm": "insuredObject.property.propertyLocation"
    },
    {
      "varNr": 1031,
      "varDataType": "STRING",
      "varCode": "io_isNewBuilding",
      "varName": "Имущество.новостройка",
      "varPath": "insuredObject.property.isNewBuilding",
      "varType": "IN",
      "varValue": "",
      "varCdm": "insuredObject.property.isNewBuilding"
    },
    {
      "varNr": 1032,
      "varDataType": "NUMBER",
      "varCode": "io_propertyValue",
      "varName": "Имущество.стоимость имущества",
      "varPath": "insuredObject.property.propertyValue",
      "varType": "IN",
      "varValue": "0",
      "varCdm": "insuredObject.property.propertyValue"
    },
    {
      "varNr": 1033,
      "varDataType": "STRING",
      "varCode": "io_commissioningDate",
      "varName": "Имущество.дата ввода в эксплуатацию",
      "varPath": "insuredObject.property.commissioningDate",
      "varType": "IN",
      "varValue": "",
      "varCdm": "insuredObject.property.commissioningDate"
    },
    {
      "varNr": 1034,
      "varDataType": "NUMBER",
      "varCode": "io_floor",
      "varName": "Имущество.этаж",
      "varPath": "insuredObject.property.floor",
      "varType": "IN",
      "varValue": "0",
      "varCdm": "insuredObject.property.floor"
    }
  ];

  ioPersonMagicVars: LobVar[] = [
  {
    "varNr": 1101,
    "varDataType": "NUMBER",
    "varCode": "io_age_issue",
    "varName": "Возраст застрахованного на дату выпуска полиса",
    "varPath": "",
    "varType": "MAGIC",
    "varValue": "",
    "varCdm": "insuredObject.person.age_issue"
  },
  {
    "varNr": 1102,
    "varDataType": "NUMBER",
    "varCode": "io_age_end",
    "varName": "Возраст застрахованного на дату окончания полиса",
    "varPath": "",
    "varType": "MAGIC",
    "varValue": "",
    "varCdm": "insuredObject.person.age_end"
  }
  ];

  policyVars: LobVar[] = [];
  policyMagicVars: LobVar[] = [
  {
    "varNr": 1201,
    "varDataType": "NUMBER",
    "varCode": "pl_TermMonths",
    "varName": "Срок полиса в месяцах",
    "varPath": "",
    "varType": "MAGIC",
    "varValue": "",
    "varCdm": "policy.magic.termMonths"
  },
  {
    "varNr": 1202,
    "varDataType": "NUMBER",
    "varCode": "pl_TermDays",
    "varName": "Срок полиса в днях",
    "varPath": "",
    "varType": "MAGIC",
    "varValue": "",
    "varCdm": "policy.magic.termDays"
  }
  ];     

  ioTravelSegmentsVars: LobVar[] = [
    {
      "varNr": 101,
      "varDataType": "NUMBER",
      "varCode": "io_legs",
      "varName": "количество перелетов",
      "varPath": "insuredObjects[0].travelSegments[*].count()",
      "varType": "IN",
      "varValue": "",
      "varCdm": "insuredObject.travelSegments.legs"
      },
      {
        "varNr": 101,
        "varDataType": "NUMBER",
        "varCode": "io_ticketPrice",
        "varName": "стоимость билета",
        "varPath": "insuredObjects[0].travelSegments[*].ticketPrice.sum()",
        "varType": "IN",
        "varValue": "",
        "varCdm": "insuredObject.travelSegments.ticketPrice"
        },
        {
    "varNr": 1001,
    "varDataType": "STRING",
    "varCode": "io_ticket_nr",
    "varName": "номер билета",
    "varPath": "insuredObjects[0].travelSegments[*].ticketNr",
    "varType": "IN",
    "varValue": "",
    "varCdm": "insuredObject.travelSegments.ticketNr"
    },
    {
    "varNr": 1002,
    "varDataType": "STRING",
    "varCode": "io_departure_date",
    "varName": "дата вылета",
    "varPath": "insuredObjects[0].travelSegments[*].departureDate",
    "varType": "IN",
    "varValue": "",
    "varCdm": "insuredObject.travelSegments.departureDate"
    },
    {
    "varNr": 1003,
    "varDataType": "STRING",
    "varCode": "io_departure_time",
    "varName": "время вылета",
    "varPath": "insuredObjects[0].travelSegments[*].departureTime",
    "varType": "IN",
    "varValue": "",
    "varCdm": "insuredObject.travelSegments.departureTime"
    },
    {
    "varNr": 1004,
    "varDataType": "STRING",
    "varCode": "io_departure_city",
    "varName": "город вылета",
    "varPath": "insuredObjects[0].travelSegments[*].departureCity",
    "varType": "IN",
    "varValue": "",
    "varCdm": "insuredObject.travelSegments.departureCity"
    },
    {
    "varNr": 1005,
    "varDataType": "STRING",
    "varCode": "io_arrival_city",
    "varName": "город прилета",
    "varPath": "insuredObjects[0].travelSegments[*].arrivalCity",
    "varType": "IN",
    "varValue": "",
    "varCdm": "insuredObject.travelSegments.arrivalCity"
    }
  ]


}