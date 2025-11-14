export interface Policy {
  policyNumber: string;
  previousPolicyNumber: string;
  product: PolicyProduct;
  status: PolicyStatus;
  startDate: string;
  endDate: string;
  issueDate: string;
  createDate: string;
  paymentDate: string;
  cancellationDate: string;
  premium: number;
  premiumCur: number;
  currencyCode: string;
  currencyRate?: number;
  placeOfIssue?: string;
  draftId: string;
  additionalProperties?: AdditionalProperties;
  policyHolder: PolicyHolder;
  insuredObject: InsuredObject;
}

export interface PolicyProduct {
  code: string;
  description: string;
}

export interface PolicyStatus {
  code: 'NEW' | 'ACTIVE' | 'EXPIRED' | 'CANCELLED';
  description: string;
}

export interface AdditionalProperties {
  pos?: string;
  discountKVPercent?: number;
  promocode?: string;
  insurerRepresentative?: string;
  kfav: string;
  channelTypeId?: string;
  channelId?: string;
  creatorDivisionName: string;
  contractDivisionName?: string;
  agentRepresentativeName?: string;
  agentRepresentativeDivisionName: string;
  agentContractNumber?: string;
  agentName?: string;
  contractResponsibleName: string;
  contractResponsibleId?: string;
  contractDivisionId?: string;
  coefDesiredPremium: number;
  reductionCoef? : number;
}

export interface PolicyHolder {
  person: Person;
  phone: Phone;
  email: string;
  passport: Passport;
  address: Address;
  placeOfWork: PlaceOfWork;
  inn: string;
  snils: string;
  otherAddresses: Address[];
  otherDocuments: Passport[];
  organization: Organization;
  isGovernmentService: boolean;
  customFields: {
    [key: string]: any; // динамические поля
  };
}

export interface InsuredObject {
  packageCode: string;
  covers: Cover[];
  objectId: string;
  insureds: Insured[];
  device: Device;
}

export interface Person {
  firstName: string;
  lastName?: string;
  middleName?: string;
  birthDate?: string;
  fullName?: string;
  fullNameEn?: string;
  birthPlace?: string;
  citizenship?: string;
  gender?: string;
  familyState?: 'SINGLE' | 'MARRIED' | 'DIVORCED' | 'WIDOWED';
  isPublicOfficial?: boolean;
  isResident?: boolean;
  vsk_id?: string;
  ext_id?: string;
}

export interface Phone {
  phoneNumber: string;
  contactPerson: string;
}

export interface Passport {
  typeCode: string;
  serial: string;
  number: string;
  dateIssue: string;
  validUntil: string;
  whom: string;
  divisionCode: string;
  vsk_id: string;
  ext_id: string;
  countryCode: string;
  documentOwner?: Person;
}

export interface Address {
  typeCode: 'REGISTRATION' | 'RESIDENCE' | 'WORK';
  countryCode: string;
  region: string;
  city: string;
  street: string;
  house: string;
  building?: string;
  flat?: string;
  room?: string;
  zipCode: string;
  kladrId: string;
  fiasId: string;
  addressStr: string;
  addressStrEn: string;
  vsk_id: string;
  ext_id: string;
}

export interface PlaceOfWork {
  organization: string;
  occupationType: string;
  occupation: string;
  address: string;
  phone: string;
}

export interface Organization {
  country: string;
  inn: string;
  fullName: string;
  fullNameEn: string;
  shortName: string;
  legalForm: 'OOO' | 'OAO' | 'ZAO' | 'IP';
  kpp: string;
  ogrn: string;
  okpo: string;
  bic: string;
  isResident: boolean;
  group: string;
  vsk_id: string;
  ext_id: string;
  nciCode: string;
}

export interface Cover {
  cover: CoverDetails;
  risk: string[];
  startDate: string;
  endDate: string;
  sumInsured: number;
  premium: number;
  deductibleType: string;
  deductible: number;
  sumInsuredCur: number;
  premiumCur: number;
  deductibleCur: number;
  deductiblePercent: number;
  deductibleMin: number;
  deductibleUnit: string;
  deductibleSpecific: string;
}

export interface CoverDetails {
  code: string;
  option: string;
  description: string;
}

export interface Insured {
  person?: Person;
  phone?: Phone;
  email?: string;
  passport?: Passport;
  address?: Address;
  placeOfWork?: PlaceOfWork;
  plaсeOfStudy?: PlaceOfStudy;
  additionalFactors?: AdditionalFactors;
  certificate?: string;
}

export interface PlaceOfStudy {
  university: string;
  faculty: string;
  course: string;
}

export interface AdditionalFactors {
  isProfessional: boolean;
  isSporttime: boolean;
  isSumInsuredInCredit: boolean;
  typeOfSport: string;
  travelSegments?: TravelSegment[];
}

export interface TravelSegment {
  ticketNr: string;
  departureDate: string;
  departureTime: string;
  departureCity: string;
  arrivalCity: string;
  ticketPrice: string;
  insurionUUID: string;
  insurionErrorCode: string;
}

export interface Device {
  countryCode: string;
  devicePrice: number;
  imei: string;
  licenseKey: string;
  model: string;
  deviceName: string;
  osName: string;
  osVersion: string;
  serialNr: string;
  tradeMark: string;
  deviceTypeCode: string;
}