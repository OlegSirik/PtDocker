import { BoxPerson } from './person-box.model';

export class BoxIdentifier {
  isPrimary: string = '';
  typeCode: string = '';
  serial: string = '';
  number: string = '';
  dateIssue: string = '';
  validUntil: string = '';
  whom: string = '';
  divisionCode: string = '';
  ext_id: string = '';
  countryCode: string = '';
  documentOwner: BoxPerson = new BoxPerson();

  constructor(data?: Partial<BoxIdentifier>) {
    if (data) {
      Object.assign(this, data);
      if (data.documentOwner) {
        this.documentOwner = new BoxPerson(data.documentOwner);
      }
    }
  }
}
