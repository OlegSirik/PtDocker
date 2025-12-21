import { Person } from './person.model';

export class Identifier {
  isPrimary: boolean = false;
  typeCode: string = '';
  serial?: string = '';
  number: string = '';
  dateIssue?: string = '';
  validUntil?: string = '';
  whom?: string = '';
  divisionCode?: string = '';
  ext_id?: string = '';
  countryCode?: string = '';
  documentOwner?: Person;

  constructor(data?: Partial<Identifier>) {
    if (data) {
      Object.assign(this, data);
      if (data.documentOwner) {
        this.documentOwner = new Person(data.documentOwner);
      }
    }
  }
}
