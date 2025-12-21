export type FamilyState = 'SINGLE' | 'MARRIED' | 'DIVORCED' | 'WIDOWED';

export class Person {
  firstName: string = '';
  lastName?: string;
  middleName?: string;
  birthDate?: string;
  fullName?: string;
  fullNameEn?: string;
  birthPlace?: string;
  citizenship?: string;
  gender?: string;
  familyState?: FamilyState;
  isPublicOfficial?: boolean;
  isResident?: boolean;
  ext_id?: string;

  constructor(data?: Partial<Person>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}
