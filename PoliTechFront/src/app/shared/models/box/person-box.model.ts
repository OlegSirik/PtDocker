export type BoxFamilyState = 'SINGLE' | 'MARRIED' | 'DIVORCED' | 'WIDOWED';

export class BoxPerson {
  firstName: string = '';
  lastName: string = '';
  middleName: string = '';
  birthDate: string = '';
  fullName: string = '';
  fullNameEn: string = '';
  birthPlace: string = '';
  citizenship: string = '';
  gender: string = '';
  familyState: string = '';
  isPublicOfficial: string = '';
  isResident: string = '';
  ext_id: string = '';

  constructor(data?: Partial<BoxPerson>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}
