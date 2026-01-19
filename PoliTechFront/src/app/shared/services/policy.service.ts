import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { 
  BoxPolicy, BoxPolicyHolder, BoxInsuredObject, BoxPerson, BoxContacts, BoxIdentifier, 
  BoxAddress, BoxOrganization, BoxCover, BoxCoverDetails, BoxDevice,
  Policy, PolicyHolder, InsuredObject, Person, Address, Organization, Cover, CoverDetails, Device,
  TravelSegment
} from '../models/policy.models';
import { Contacts } from '../models/contacts.model';
import { Identifier } from '../models/identifier.model';
import { BaseApiService } from './api/base-api.service';
import { EnvService } from './env.service';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class PolicyService extends BaseApiService<Policy> {
  constructor(
    http: HttpClient,
    env: EnvService,
    authService: AuthService,
  ) {
    super(http, env, 'sales', authService);
  }

override  getUrl(id?: (number | string)): string {

  let url2 = this.authService.baseApiUrl.toString(); // http://localhost:8080/api/v1/VSK
  return url2 + '/sales';
  }

  // Get policy by ID
  getPolicy(id: string): Observable<BoxPolicy> {
    // In real implementation, this would make an HTTP call
    return of(new BoxPolicy());
  }

  // Create new policy
  createPolicy(policy: BoxPolicy): Observable<BoxPolicy> {
    // In real implementation, this would make an HTTP POST call
    return of(policy);
  }

  // Update existing policy
  updatePolicy(id: string, policy: BoxPolicy): Observable<BoxPolicy> {
    // In real implementation, this would make an HTTP PUT call
    return of(policy);
  }

  // Delete policy
  deletePolicy(id: string): Observable<void> {
    // In real implementation, this would make an HTTP DELETE call
    return of(void 0);
  }

  // Fast Calc - calculate policy
  fastCalc(policy: Policy): Observable<Policy> {
    return this.http.post<Policy>(this.getUrl() + '/quotes', policy);
  }

  // Save policy
  savePolicy(policy: Policy): Observable<Policy> {
    return this.http.post<Policy>(this.getUrl() + '/policies', policy);
  }

  conversPolicy2Box(policy: Policy): BoxPolicy {
    const box = new BoxPolicy();
    
    // Copy all basic properties from Policy to BoxPolicy, convert to strings
    box.id = policy.id || '';
    box.policyNumber = policy.policyNumber || '';
    box.previousPolicyNumber = policy.previousPolicyNumber || '';
    box.productCode = policy.productCode || '';
    box.statusCode = policy.statusCode || '';
    box.startDate = policy.startDate || '';
    box.endDate = policy.endDate || '';
    box.issueDate = policy.issueDate || '';
    box.issueTimeZone = policy.issueTimeZone || '';
    box.policyTerm = policy.policyTerm || '';
    box.waitingPeriod = policy.waitingPeriod || '';
    box.createDate = policy.createDate || '';
    box.paymentDate = policy.paymentDate || '';
    box.cancellationDate = policy.cancellationDate || '';
    box.premium = policy.premium?.toString() || '';
    box.placeOfIssue = policy.placeOfIssue || '';
    box.draftId = policy.draftId || '';
    box.installmentType = policy.installmentType || '';
    
    // Convert PolicyHolder to BoxPolicyHolder
    box.policyHolder = new BoxPolicyHolder();
    
    // Copy person - convert all properties to strings
    if (policy.policyHolder.person) {
      box.policyHolder.person = new BoxPerson({
        firstName: policy.policyHolder.person.firstName || '',
        lastName: policy.policyHolder.person.lastName || '',
        middleName: policy.policyHolder.person.middleName || '',
        birthDate: policy.policyHolder.person.birthDate || '',
        fullName: policy.policyHolder.person.fullName || '',
        fullNameEn: policy.policyHolder.person.fullNameEn || '',
        birthPlace: policy.policyHolder.person.birthPlace || '',
        citizenship: policy.policyHolder.person.citizenship || '',
        gender: policy.policyHolder.person.gender || '',
        familyState: policy.policyHolder.person.familyState || '',
        isPublicOfficial: policy.policyHolder.person.isPublicOfficial?.toString() || '',
        isResident: policy.policyHolder.person.isResident?.toString() || '',
        ext_id: policy.policyHolder.person.ext_id || ''
      });
    }
    
    // Copy contacts
    if (policy.policyHolder.contacts) {
      box.policyHolder.contacts = new BoxContacts({
        phone: policy.policyHolder.contacts.phone || '',
        email: policy.policyHolder.contacts.email || '',
        telegram: policy.policyHolder.contacts.telegram || ''
      });
    }
    
    // Copy identifiers - find primary or take first, convert to BoxIdentifier
    if (policy.policyHolder.identifiers && policy.policyHolder.identifiers.length > 0) {
      const primaryIdentifier = policy.policyHolder.identifiers.find(id => id.isPrimary === true) 
        || policy.policyHolder.identifiers[0];
      box.policyHolder.identifiers = new BoxIdentifier({
        isPrimary: primaryIdentifier.isPrimary?.toString() || '',
        typeCode: primaryIdentifier.typeCode || '',
        serial: primaryIdentifier.serial || '',
        number: primaryIdentifier.number || '',
        dateIssue: primaryIdentifier.dateIssue || '',
        validUntil: primaryIdentifier.validUntil || '',
        whom: primaryIdentifier.whom || '',
        divisionCode: primaryIdentifier.divisionCode || '',
        ext_id: primaryIdentifier.ext_id || '',
        countryCode: primaryIdentifier.countryCode || ''
      });
    }
    
    // Copy addresses - find primary or take first, convert to BoxAddress
    if (policy.policyHolder.addresses && policy.policyHolder.addresses.length > 0) {
      const primaryAddress = policy.policyHolder.addresses.find(addr => addr.isPrimary === true)
        || policy.policyHolder.addresses[0];
      box.policyHolder.addresses = new BoxAddress({
        isPrimary: primaryAddress.isPrimary?.toString() || '',
        typeCode: primaryAddress.typeCode || '',
        countryCode: primaryAddress.countryCode || '',
        region: primaryAddress.region || '',
        city: primaryAddress.city || '',
        street: primaryAddress.street || '',
        house: primaryAddress.house || '',
        building: primaryAddress.building || '',
        flat: primaryAddress.flat || '',
        room: primaryAddress.room || '',
        zipCode: primaryAddress.zipCode || '',
        kladrId: primaryAddress.kladrId || '',
        fiasId: primaryAddress.fiasId || '',
        addressStr: primaryAddress.addressStr || '',
        addressStrEn: primaryAddress.addressStrEn || '',
        ext_id: primaryAddress.ext_id || ''
      });
    }
    
    // Copy organization - convert all properties to strings
    if (policy.policyHolder.organization) {
      box.policyHolder.organization = new BoxOrganization({
        country: policy.policyHolder.organization.country || '',
        inn: policy.policyHolder.organization.inn || '',
        fullName: policy.policyHolder.organization.fullName || '',
        fullNameEn: policy.policyHolder.organization.fullNameEn || '',
        shortName: policy.policyHolder.organization.shortName || '',
        legalForm: policy.policyHolder.organization.legalForm || '',
        kpp: policy.policyHolder.organization.kpp || '',
        ogrn: policy.policyHolder.organization.ogrn || '',
        okpo: policy.policyHolder.organization.okpo || '',
        bic: policy.policyHolder.organization.bic || '',
        isResident: policy.policyHolder.organization.isResident?.toString() || '',
        group: policy.policyHolder.organization.group || '',
        ext_id: policy.policyHolder.organization.ext_id || ''
      });
    }
    
    // Copy customFields - convert values to strings
    if (policy.policyHolder.customFields) {
      const boxCustomFields: { [key: string]: string } = {};
      Object.keys(policy.policyHolder.customFields).forEach(key => {
        boxCustomFields[key] = String(policy.policyHolder.customFields?.[key] || '');
      });
      box.policyHolder.customFields = boxCustomFields;
    }
    
    // Copy insuredObject - convert to BoxInsuredObject
    box.insuredObject = new BoxInsuredObject();
    box.insuredObject.ioType = policy.insuredObjects[0].ioType || '';
    box.insuredObject.packageCode = policy.insuredObjects[0].packageCode || '';
    box.insuredObject.objectId = policy.insuredObjects[0].objectId || '';
    box.insuredObject.sumInsured = policy.insuredObjects[0].sumInsured || '';

    // Copy coverage if exists - convert to BoxCover array
    // Handle both single Cover object and Cover[] array

    if (Array.isArray(policy.insuredObjects[0].covers) && policy.insuredObjects[0].covers.length > 0) {

      policy.insuredObjects[0].coverage = policy.insuredObjects[0].covers;
    } else if (policy.insuredObjects[0].covers && !Array.isArray(policy.insuredObjects[0].covers)) {
      policy.insuredObjects[0].coverage = [policy.insuredObjects[0].covers];
    }


    if (policy.insuredObjects[0].coverage) {
      const coverages = Array.isArray(policy.insuredObjects[0].coverage) 
        ? policy.insuredObjects[0].coverage 
        : [policy.insuredObjects[0].coverage];
      
      box.coverage = coverages.map(cover => new BoxCover({
        cover: new BoxCoverDetails({
          code: cover.cover?.code || '',
          option: cover.cover?.option || '',
          description: cover.cover?.description || ''
        }),
        risk: cover.risk || [],
        startDate: cover.startDate || '',
        endDate: cover.endDate || '',
        sumInsured: cover.sumInsured?.toString() || '',
        premium: cover.premium?.toString() || '',
        deductibleType: cover.deductibleType || '',
        deductible: cover.deductible?.toString() || '',
        sumInsuredCur: cover.sumInsuredCur?.toString() || '',
        premiumCur: cover.premiumCur?.toString() || '',
        deductibleCur: cover.deductibleCur?.toString() || '',
        deductiblePercent: cover.deductiblePercent?.toString() || '',
        deductibleMin: cover.deductibleMin?.toString() || '',
        deductibleUnit: cover.deductibleUnit || '',
        deductibleSpecific: cover.deductibleSpecific || '',
        deductibleNr: cover.deductibleNr?.toString() || '',
        deductibleText: cover.deductibleText || ''
      }));
      
      // Set the first coverage to insuredObject.coverage for backward compatibility
      if (box.coverage.length > 0) {
        box.insuredObject.coverage = box.coverage;
      }
    }
    
    // Copy person from insuredObject if exists
    if (policy.insuredObjects[0].person) {
      box.insuredObject.person = new BoxPerson({
        firstName: policy.insuredObjects[0].person.firstName || '',
        lastName: policy.insuredObjects[0].person.lastName || '',
        middleName: policy.insuredObjects[0].person.middleName || '',
        birthDate: policy.insuredObjects[0].person.birthDate || '',
        fullName: policy.insuredObjects[0].person.fullName || '',
        fullNameEn: policy.insuredObjects[0].person.fullNameEn || '',
        birthPlace: policy.insuredObjects[0].person.birthPlace || '',
        citizenship: policy.insuredObjects[0].person.citizenship || '',
        gender: policy.insuredObjects[0].person.gender || '',
        familyState: policy.insuredObjects[0].person.familyState || '',
        isPublicOfficial: policy.insuredObjects[0].person.isPublicOfficial?.toString() || '',
        isResident: policy.insuredObjects[0].person.isResident?.toString() || '',
        ext_id: policy.insuredObjects[0].person.ext_id || ''
      });
    }
    
    // Copy contacts from insuredObject if exists
    if (policy.insuredObjects[0].contacts) {
      box.insuredObject.contacts = new BoxContacts({
        phone: policy.insuredObjects[0].contacts.phone || '',
        email: policy.insuredObjects[0].contacts.email || '',
        telegram: policy.insuredObjects[0].contacts.telegram || ''
      });
    }
    
    // Copy identifiers from insuredObject if exists
    if (policy.insuredObjects[0].identifiers) {
      box.insuredObject.identifiers = new BoxIdentifier({
        isPrimary: policy.insuredObjects[0].identifiers.isPrimary?.toString() || '',
        typeCode: policy.insuredObjects[0].identifiers.typeCode || '',
        serial: policy.insuredObjects[0].identifiers.serial || '',
        number: policy.insuredObjects[0].identifiers.number || '',
        dateIssue: policy.insuredObjects[0].identifiers.dateIssue || '',
        validUntil: policy.insuredObjects[0].identifiers.validUntil || '',
        whom: policy.insuredObjects[0].identifiers.whom || '',
        divisionCode: policy.insuredObjects[0].identifiers.divisionCode || '',
        ext_id: policy.insuredObjects[0].identifiers.ext_id || '',
        countryCode: policy.insuredObjects[0].identifiers.countryCode || ''
      });
    }
    
    // Copy address from insuredObject if exists
    if (policy.insuredObjects[0].address) {
      box.insuredObject.address = new BoxAddress({
        isPrimary: policy.insuredObjects[0].address.isPrimary?.toString() || '',
        typeCode: policy.insuredObjects[0].address.typeCode || '',
        countryCode: policy.insuredObjects[0].address.countryCode || '',
        region: policy.insuredObjects[0].address.region || '',
        city: policy.insuredObjects[0].address.city || '',
        street: policy.insuredObjects[0].address.street || '',
        house: policy.insuredObjects[0].address.house || '',
        building: policy.insuredObjects[0].address.building || '',
        flat: policy.insuredObjects[0].address.flat || '',
        room: policy.insuredObjects[0].address.room || '',
        zipCode: policy.insuredObjects[0].address.zipCode || '',
        kladrId: policy.insuredObjects[0].address.kladrId || '',
        fiasId: policy.insuredObjects[0].address.fiasId || '',
        addressStr: policy.insuredObjects[0].address.addressStr || '',
        addressStrEn: policy.insuredObjects[0].address.addressStrEn || '',
        ext_id: policy.insuredObjects[0].address.ext_id || ''
      });
    }
    
    // Copy device from insuredObject if exists
    if (policy.insuredObjects[0].device) {
      box.insuredObject.device = new BoxDevice({
        deviceName: policy.insuredObjects[0].device.deviceName || '',
        deviceTypeCode: policy.insuredObjects[0].device.deviceTypeCode || '',
        tradeMark: policy.insuredObjects[0].device.tradeMark || '',
        model: policy.insuredObjects[0].device.model || '',
        serialNr: policy.insuredObjects[0].device.serialNr || '',
        licenseKey: policy.insuredObjects[0].device.licenseKey || '',
        imei: policy.insuredObjects[0].device.imei || '',
        osName: policy.insuredObjects[0].device.osName || '',
        osVersion: policy.insuredObjects[0].device.osVersion || '',
        countryCode: policy.insuredObjects[0].device.countryCode || '',
        devicePrice: policy.insuredObjects[0].device.devicePrice?.toString() || ''
      });
    }
    
    return box;
  }

  conversBox2Policy(box: BoxPolicy): Policy {
    
    const policy = new Policy();
    //console.log('Policy 2 return - '+ policy);
    //return policy;

    // Copy all basic properties from BoxPolicy to Policy, skip empty strings
    if (box.id && box.id !== '') {
      policy.id = box.id;
    }
    
    if (box.policyNumber && box.policyNumber !== '') {
      policy.policyNumber = box.policyNumber;
    }
    
    if (box.previousPolicyNumber && box.previousPolicyNumber !== '') {
      policy.previousPolicyNumber = box.previousPolicyNumber;
    }
    
    policy.productCode = box.productCode || '';
    policy.statusCode = box.statusCode || 'NEW';
    if (box.startDate && box.startDate !== '') {
      policy.startDate = box.startDate;
    }
    if (box.endDate && box.endDate !== '') {
      policy.endDate = box.endDate;
    }
    if (box.issueDate && box.issueDate !== '') {
      policy.issueDate = box.issueDate;
    }
    if (box.issueTimeZone && box.issueTimeZone !== '') {
      policy.issueTimeZone = box.issueTimeZone;
    }
    if (box.policyTerm && box.policyTerm !== '') {
      policy.policyTerm = box.policyTerm;
    }
    if (box.waitingPeriod && box.waitingPeriod !== '') {
      policy.waitingPeriod = box.waitingPeriod;
    }
    if (box.createDate && box.createDate !== '') {
      policy.createDate = box.createDate;
    }
    
    if (box.paymentDate && box.paymentDate !== '') {
      policy.paymentDate = box.paymentDate;
    }
    
    if (box.cancellationDate && box.cancellationDate !== '') {
      policy.cancellationDate = box.cancellationDate;
    }
    
    // Convert premium from string to number, skip if empty
    if (box.premium && box.premium !== '') {
      const premiumNum = parseFloat(box.premium);
      if (!isNaN(premiumNum)) {
        policy.premium = premiumNum;
      }
    }
    
    if (box.placeOfIssue && box.placeOfIssue !== '') {
      policy.placeOfIssue = box.placeOfIssue;
    }
    
    if (box.draftId && box.draftId !== '') {
      policy.draftId = box.draftId;
    }
    
    if (box.installmentType && box.installmentType !== '') {
      policy.installmentType = box.installmentType;
    }
    
    // Convert BoxPolicyHolder to PolicyHolder
    policy.policyHolder = new PolicyHolder();

    // Copy person - convert strings back to appropriate types, only if at least one field is not empty
    if (box.policyHolder.person) {
      const firstName = box.policyHolder.person.firstName || '';
      const lastName = box.policyHolder.person.lastName || '';
      const middleName = box.policyHolder.person.middleName || '';
      const birthDate = box.policyHolder.person.birthDate || '';
      const fullName = box.policyHolder.person.fullName || '';
      const fullNameEn = box.policyHolder.person.fullNameEn || '';
      const birthPlace = box.policyHolder.person.birthPlace || '';
      const citizenship = box.policyHolder.person.citizenship || '';
      const gender = box.policyHolder.person.gender || '';
      const familyState = box.policyHolder.person.familyState || '';
      const ext_id = box.policyHolder.person.ext_id || '';



      if (firstName !== '' || lastName !== '' || middleName !== '' || birthDate !== '' || 
          fullName !== '' || fullNameEn !== '' || birthPlace !== '' || citizenship !== '' || 
          gender !== '' || familyState !== '' || ext_id !== '') {
        const personData: any = {};
        if (firstName !== '') personData.firstName = firstName;
        if (lastName !== '') personData.lastName = lastName;
        if (middleName !== '') personData.middleName = middleName;
        if (birthDate !== '') personData.birthDate = birthDate;
        if (fullName !== '') personData.fullName = fullName;
        if (fullNameEn !== '') personData.fullNameEn = fullNameEn;
        if (birthPlace !== '') personData.birthPlace = birthPlace;
        if (citizenship !== '') personData.citizenship = citizenship;
        if (gender !== '') personData.gender = gender;
        if (familyState !== '') personData.familyState = familyState as any;
        if (
          box.policyHolder.person.isPublicOfficial === 'true'
        ) {
          personData.isPublicOfficial = true;
        }
        if (
          box.policyHolder.person.isResident === 'true'
        ) {
          personData.isResident = true;
        }
        if (ext_id !== '') personData.ext_id = ext_id;

        policy.policyHolder.person = new Person(personData);
      }
    }
    
    // Copy contacts - only if at least one field is not empty
    if (box.policyHolder.contacts) {
      const phone = box.policyHolder.contacts.phone || '';
      const email = box.policyHolder.contacts.email || '';
      const telegram = box.policyHolder.contacts.telegram || '';
      
      if (phone !== '' || email !== '' || telegram !== '') {
        policy.policyHolder.contacts = new Contacts({
          phone: phone,
          email: email,
          telegram: telegram
        });
      }
    }
    
    // Convert single identifier to array with isPrimary = true, only if at least one field is not empty
    if (box.policyHolder.identifiers) {
      const typeCode = box.policyHolder.identifiers.typeCode || '';
      const serial = box.policyHolder.identifiers.serial || '';
      const number = box.policyHolder.identifiers.number || '';
      const dateIssue = box.policyHolder.identifiers.dateIssue || '';
      const validUntil = box.policyHolder.identifiers.validUntil || '';
      const whom = box.policyHolder.identifiers.whom || '';
      const divisionCode = box.policyHolder.identifiers.divisionCode || '';
      const ext_id = box.policyHolder.identifiers.ext_id || '';
      const countryCode = box.policyHolder.identifiers.countryCode || '';
      
      if (typeCode !== '' || serial !== '' || number !== '' || dateIssue !== '' || 
          validUntil !== '' || whom !== '' || divisionCode !== '' || ext_id !== '' || countryCode !== '') {
        const identifierData: any = {};
        if (typeCode !== '') identifierData.typeCode = typeCode;
        if (serial !== '') identifierData.serial = serial;
        if (number !== '') identifierData.number = number;
        if (dateIssue !== '') identifierData.dateIssue = dateIssue;
        if (validUntil !== '') identifierData.validUntil = validUntil;
        if (whom !== '') identifierData.whom = whom;
        if (divisionCode !== '') identifierData.divisionCode = divisionCode;
        if (ext_id !== '') identifierData.ext_id = ext_id;
        if (countryCode !== '') identifierData.countryCode = countryCode;
        if (box.policyHolder.identifiers.isPrimary === 'true') {
          identifierData.isPrimary = true;
        }
        policy.policyHolder.identifiers = [new Identifier(identifierData)];
      }
    }
    
    // Convert single address to array with isPrimary = true, only if at least one field is not empty
    if (box.policyHolder.addresses) {
      const typeCode = box.policyHolder.addresses.typeCode || '';
      const countryCode = box.policyHolder.addresses.countryCode || '';
      const region = box.policyHolder.addresses.region || '';
      const city = box.policyHolder.addresses.city || '';
      const street = box.policyHolder.addresses.street || '';
      const house = box.policyHolder.addresses.house || '';
      const building = box.policyHolder.addresses.building || '';
      const flat = box.policyHolder.addresses.flat || '';
      const room = box.policyHolder.addresses.room || '';
      const zipCode = box.policyHolder.addresses.zipCode || '';
      const kladrId = box.policyHolder.addresses.kladrId || '';
      const fiasId = box.policyHolder.addresses.fiasId || '';
      const addressStr = box.policyHolder.addresses.addressStr || '';
      const addressStrEn = box.policyHolder.addresses.addressStrEn || '';
      const ext_id = box.policyHolder.addresses.ext_id || '';
      
      if (typeCode !== '' || countryCode !== '' || region !== '' || city !== '' || 
          street !== '' || house !== '' || building !== '' || flat !== '' || room !== '' || 
          zipCode !== '' || kladrId !== '' || fiasId !== '' || addressStr !== '' || 
          addressStrEn !== '' || ext_id !== '') {
        const addressData: any = {};
        if (typeCode !== '') addressData.typeCode = typeCode;
        if (countryCode !== '') addressData.countryCode = countryCode;
        if (region !== '') addressData.region = region;
        if (city !== '') addressData.city = city;
        if (street !== '') addressData.street = street;
        if (house !== '') addressData.house = house;
        if (building !== '') addressData.building = building;
        if (flat !== '') addressData.flat = flat;
        if (room !== '') addressData.room = room;
        if (zipCode !== '') addressData.zipCode = zipCode;
        if (kladrId !== '') addressData.kladrId = kladrId;
        if (fiasId !== '') addressData.fiasId = fiasId;
        if (addressStr !== '') addressData.addressStr = addressStr;
        if (addressStrEn !== '') addressData.addressStrEn = addressStrEn;
        if (ext_id !== '') addressData.ext_id = ext_id;
        if (box.policyHolder.addresses.isPrimary === 'true') {
          addressData.isPrimary = true;
        }
        policy.policyHolder.addresses = [new Address(addressData)];
      }
    }
    
    // Copy organization - convert strings back to appropriate types, only if at least one field is not empty
    if (box.policyHolder.organization) {
      const country = box.policyHolder.organization.country || '';
      const inn = box.policyHolder.organization.inn || '';
      const fullName = box.policyHolder.organization.fullName || '';
      const fullNameEn = box.policyHolder.organization.fullNameEn || '';
      const shortName = box.policyHolder.organization.shortName || '';
      const legalForm = box.policyHolder.organization.legalForm || '';
      const kpp = box.policyHolder.organization.kpp || '';
      const ogrn = box.policyHolder.organization.ogrn || '';
      const okpo = box.policyHolder.organization.okpo || '';
      const bic = box.policyHolder.organization.bic || '';
      const group = box.policyHolder.organization.group || '';
      const ext_id = box.policyHolder.organization.ext_id || '';
      
      if (country !== '' || inn !== '' || fullName !== '' || fullNameEn !== '' || 
          shortName !== '' || legalForm !== '' || kpp !== '' || ogrn !== '' || 
          okpo !== '' || bic !== '' || group !== '' || ext_id !== '') {

          const organizationData: any = {};
          if (country !== '') organizationData.country = country;
          if (inn !== '') organizationData.inn = inn;
          if (fullName !== '') organizationData.fullName = fullName;
          if (fullNameEn !== '') organizationData.fullNameEn = fullNameEn;
          if (shortName !== '') organizationData.shortName = shortName;
          if (legalForm !== '') organizationData.legalForm = legalForm;
          if (kpp !== '') organizationData.kpp = kpp;
          if (ogrn !== '') organizationData.ogrn = ogrn;
          if (okpo !== '') organizationData.okpo = okpo;
          if (bic !== '') organizationData.bic = bic;
          if (group !== '') organizationData.group = group;
          if (ext_id !== '') organizationData.ext_id = ext_id;
          if (box.policyHolder.organization.isResident === 'true') {
            organizationData.isResident = true;
          }
          //policy.policyHolder.organization = new Organization(organizationData);
      }
    }
    
    // Copy customFields - convert string values back to any
    if (box.policyHolder.customFields && Object.keys(box.policyHolder.customFields).length > 0) {
      policy.policyHolder.customFields = { ...box.policyHolder.customFields };
    }
    
    // Copy insuredObject - convert to InsuredObject
    policy.insuredObjects[0] = new InsuredObject();
    policy.insuredObjects[0].ioType = box.insuredObject.ioType || '';
    if (box.insuredObject.packageCode && box.insuredObject.packageCode !== '') {
      policy.insuredObjects[0].packageCode = box.insuredObject.packageCode;
    }
    if (box.insuredObject.objectId && box.insuredObject.objectId !== '') {
      policy.insuredObjects[0].objectId = box.insuredObject.objectId;
    }
    if (box.insuredObject.sumInsured && box.insuredObject.sumInsured != '') {
      policy.insuredObjects[0].sumInsured = box.insuredObject.sumInsured;
    }
    
    // Copy coverage from box.coverage array - convert each BoxCover to Cover
    // Use box.coverage array if available, otherwise fall back to box.insuredObject.coverage
    const coveragesToConvert = (box.coverage && box.coverage.length > 0) 
      ? box.coverage 
      : (box.insuredObject.coverage ? [box.insuredObject.coverage] : []);
/*    
    if (coveragesToConvert.length > 0) {
      const convertedCoverages = coveragesToConvert.map(boxCover => {
        const code = boxCover.cover.code;
        const option = boxCover.cover.option;
        const description = boxCover.cover.description;
        const risk = boxCover.risk;
        const startDate = boxCover.startDate;
        const endDate = boxCover.endDate;
        const sumInsured = boxCover.sumInsured;
        const premium = boxCover.premium;
        const deductibleType = boxCover.deductibleType;
        const deductible = boxCover.deductible;
     //   const deductibleUnit = boxCover.deductibleUnit || '';
     //   const deductibleSpecific = boxCover.deductibleSpecific ;
    //    const deductibleText = boxCover.deductibleText || '';
        
        // Only create Cover if at least one field is not empty
        if (code !== '' || option !== '' || description !== '' || risk.length > 0 || 
            startDate !== '' || endDate !== '' || sumInsured !== '' || premium !== '' || 
            deductibleType !== '' || deductible !== '' ) {
          return new Cover({
            cover: new CoverDetails({
              code: code,
              option: option,
              description: description
            }),
            risk: risk,
            startDate: startDate,
            endDate: endDate,
            sumInsured: sumInsured ? parseFloat(sumInsured) : 0,
            premium: premium ? parseFloat(premium) : 0,
            deductibleType: deductibleType,
            deductible: deductible ? parseFloat(deductible) : 0,
         //   sumInsuredCur: boxCover.sumInsuredCur ? parseFloat(boxCover.sumInsuredCur) : 0,
         //   premiumCur: boxCover.premiumCur ? parseFloat(boxCover.premiumCur) : 0,
          //  deductibleCur: boxCover.deductibleCur ? parseFloat(boxCover.deductibleCur) : 0,
          //  deductiblePercent: boxCover.deductiblePercent ? parseFloat(boxCover.deductiblePercent) : 0,
          //  deductibleMin: boxCover.deductibleMin ? parseFloat(boxCover.deductibleMin) : 0,
          //  deductibleUnit: deductibleUnit,
          //  deductibleSpecific: deductibleSpecific,
          //  deductibleNr: boxCover.deductibleNr ? parseInt(boxCover.deductibleNr) : -1,
          //  deductibleText: deductibleText
          });
        }
        return null;
      }).filter((cover): cover is Cover => cover !== null);
      
      // Set coverage - use array if multiple, single object if one
      if (convertedCoverages.length > 0) {
        policy.insuredObjects[0].coverage = convertedCoverages.length === 1 
          ? convertedCoverages[0] 
          : convertedCoverages as any; // Type assertion needed because InsuredObject.coverage can be Cover or Cover[]
      }
    }
*/    
    // Copy person from insuredObject if exists, only if at least one field is not empty
    if (box.insuredObject.person) {
      const firstName = box.insuredObject.person.firstName || '';
      const lastName = box.insuredObject.person.lastName || '';
      const middleName = box.insuredObject.person.middleName || '';
      const birthDate = box.insuredObject.person.birthDate || '';
      const fullName = box.insuredObject.person.fullName || '';
      const fullNameEn = box.insuredObject.person.fullNameEn || '';
      const birthPlace = box.insuredObject.person.birthPlace || '';
      const citizenship = box.insuredObject.person.citizenship || '';
      const gender = box.insuredObject.person.gender || '';
      const familyState = box.insuredObject.person.familyState || '';
      const ext_id = box.insuredObject.person.ext_id || '';
      
      if (firstName !== '' || lastName !== '' || middleName !== '' || birthDate !== '' || 
          fullName !== '' || fullNameEn !== '' || birthPlace !== '' || citizenship !== '' || 
          gender !== '' || familyState !== '' || ext_id !== '') {
        policy.insuredObjects[0].person = new Person({
          firstName: firstName,
          lastName: lastName || undefined,
          middleName: middleName || undefined,
          birthDate: birthDate || undefined,
          fullName: fullName || undefined,
          fullNameEn: fullNameEn || undefined,
          birthPlace: birthPlace || undefined,
          citizenship: citizenship || undefined,
          gender: gender || undefined,
          familyState: familyState as any || undefined,
          isPublicOfficial: box.insuredObject.person.isPublicOfficial === 'true' || box.insuredObject.person.isPublicOfficial === '1' ? true : undefined,
          isResident: box.insuredObject.person.isResident === 'true' || box.insuredObject.person.isResident === '1' ? true : undefined,
          ext_id: ext_id || undefined
        });
      }
    }
    
    // Copy contacts from insuredObject if exists, only if at least one field is not empty
    if (box.insuredObject.contacts) {
      const phone = box.insuredObject.contacts.phone || '';
      const email = box.insuredObject.contacts.email || '';
      const telegram = box.insuredObject.contacts.telegram || '';
      
      if (phone !== '' || email !== '' || telegram !== '') {
        policy.insuredObjects[0].contacts = new Contacts({
          phone: phone,
          email: email,
          telegram: telegram
        });
      }
    }
    
    // Copy identifiers from insuredObject if exists, only if at least one field is not empty
    if (box.insuredObject.identifiers) {
      const typeCode = box.insuredObject.identifiers.typeCode || '';
      const serial = box.insuredObject.identifiers.serial || '';
      const number = box.insuredObject.identifiers.number || '';
      const dateIssue = box.insuredObject.identifiers.dateIssue || '';
      const validUntil = box.insuredObject.identifiers.validUntil || '';
      const whom = box.insuredObject.identifiers.whom || '';
      const divisionCode = box.insuredObject.identifiers.divisionCode || '';
      const ext_id = box.insuredObject.identifiers.ext_id || '';
      const countryCode = box.insuredObject.identifiers.countryCode || '';
      
      if (typeCode !== '' || serial !== '' || number !== '' || dateIssue !== '' || 
          validUntil !== '' || whom !== '' || divisionCode !== '' || ext_id !== '' || countryCode !== '') {
        policy.insuredObjects[0].identifiers = new Identifier({
          isPrimary: box.insuredObject.identifiers.isPrimary === 'true' || box.insuredObject.identifiers.isPrimary === '1',
          typeCode: typeCode,
          serial: serial || undefined,
          number: number,
          dateIssue: dateIssue || undefined,
          validUntil: validUntil || undefined,
          whom: whom || undefined,
          divisionCode: divisionCode || undefined,
          ext_id: ext_id || undefined,
          countryCode: countryCode || undefined
        });
      }
    }
    
    // Copy address from insuredObject if exists, only if at least one field is not empty
    if (box.insuredObject.address) {
      const typeCode = box.insuredObject.address.typeCode || '';
      const countryCode = box.insuredObject.address.countryCode || '';
      const region = box.insuredObject.address.region || '';
      const city = box.insuredObject.address.city || '';
      const street = box.insuredObject.address.street || '';
      const house = box.insuredObject.address.house || '';
      const building = box.insuredObject.address.building || '';
      const flat = box.insuredObject.address.flat || '';
      const room = box.insuredObject.address.room || '';
      const zipCode = box.insuredObject.address.zipCode || '';
      const kladrId = box.insuredObject.address.kladrId || '';
      const fiasId = box.insuredObject.address.fiasId || '';
      const addressStr = box.insuredObject.address.addressStr || '';
      const addressStrEn = box.insuredObject.address.addressStrEn || '';
      const ext_id = box.insuredObject.address.ext_id || '';
      
      if (typeCode !== '' || countryCode !== '' || region !== '' || city !== '' || 
          street !== '' || house !== '' || building !== '' || flat !== '' || room !== '' || 
          zipCode !== '' || kladrId !== '' || fiasId !== '' || addressStr !== '' || 
          addressStrEn !== '' || ext_id !== '') {
        policy.insuredObjects[0].address = new Address({
          isPrimary: box.insuredObject.address.isPrimary === 'true' || box.insuredObject.address.isPrimary === '1',
          typeCode: typeCode || 'REGISTRATION',
          countryCode: countryCode || undefined,
          region: region || undefined,
          city: city || undefined,
          street: street || undefined,
          house: house || undefined,
          building: building || undefined,
          flat: flat || undefined,
          room: room || undefined,
          zipCode: zipCode || undefined,
          kladrId: kladrId || undefined,
          fiasId: fiasId || undefined,
          addressStr: addressStr,
          addressStrEn: addressStrEn || undefined,
          ext_id: ext_id || undefined
        });
      }
    }
    
    // Copy device from insuredObject if exists, only if at least one field is not empty
    if (box.insuredObject.device) {
      const deviceName = box.insuredObject.device.deviceName || '';
      const deviceTypeCode = box.insuredObject.device.deviceTypeCode || '';
      const tradeMark = box.insuredObject.device.tradeMark || '';
      const model = box.insuredObject.device.model || '';
      const serialNr = box.insuredObject.device.serialNr || '';
      const licenseKey = box.insuredObject.device.licenseKey || '';
      const imei = box.insuredObject.device.imei || '';
      const osName = box.insuredObject.device.osName || '';
      const osVersion = box.insuredObject.device.osVersion || '';
      const countryCode = box.insuredObject.device.countryCode || '';
      const devicePrice = box.insuredObject.device.devicePrice || '';
      
      if (deviceName !== '' || deviceTypeCode !== '' || tradeMark !== '' || model !== '' || 
          serialNr !== '' || licenseKey !== '' || imei !== '' || osName !== '' || 
          osVersion !== '' || countryCode !== '' || devicePrice !== '') {
        policy.insuredObjects[0].device = new Device({
          deviceName: deviceName,
          deviceTypeCode: deviceTypeCode || undefined,
          tradeMark: tradeMark || undefined,
          model: model || undefined,
          serialNr: serialNr || undefined,
          licenseKey: licenseKey || undefined,
          imei: imei || undefined,
          osName: osName || undefined,
          osVersion: osVersion || undefined,
          countryCode: countryCode || undefined,
          devicePrice: devicePrice ? parseFloat(devicePrice) : undefined
        });
      }
    }
    
    if (box.insuredObject.travelSegments && Array.isArray(box.insuredObject.travelSegments)) {
      const segments = box.insuredObject.travelSegments
        .map(segment => new TravelSegment(segment))
        .filter(segment => Object.values(segment).some(value => value !== ''));

      if (segments.length > 0) {
        // Keep backward compatibility with singular travelSegment
        policy.insuredObjects[0].travelSegment = segments[0];
        // Preserve array for APIs expecting travelSegments
        (policy.insuredObjects[0] as any).travelSegments = segments;
      }
    }


    // Filter out empty string values and create a new Policy instance
    const filteredData = Object.fromEntries(
      Object.entries(policy).filter(([_, v]) => {
        // Keep non-empty strings, objects, arrays, and numbers (including 0)
        if (v === '') return false;
        return true;
      })
    ) as Partial<Policy>;
    
    // Ensure required properties are present
    if (!filteredData.productCode) filteredData.productCode = policy.productCode;
    if (!filteredData.statusCode) filteredData.statusCode = policy.statusCode;
    if (!filteredData.policyHolder) filteredData.policyHolder = policy.policyHolder;
    if (!filteredData.insuredObjects) filteredData.insuredObjects = policy.insuredObjects;
    
    return new Policy(filteredData);
  }

  getPf(policyNr: string, pfType: string): Observable<Blob> {
    return this.http.get(`${this.authService.baseApiUrl}/sales/policies/${policyNr}/printpf/${pfType}`, {
      headers: { 'Content-Type': 'application/json' },
      responseType: 'blob'
    })
  }
}

