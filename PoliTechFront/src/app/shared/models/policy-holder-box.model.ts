import { Person } from './person.model';
import { Contacts } from './contacts.model';
import { Identifier } from './identifier.model';
import { Address } from './address.model';
import { PlaceOfWork } from './place-of-work.model';
import { Organization } from './organization.model';

export class BoxPolicyHolder {
  person: Person = new Person();
  contacts: Contacts = new Contacts();
  identifiers?: Identifier = new Identifier();
  addresses?: Address = new Address();
  organization?: Organization = new Organization();
  customFields: { [key: string]: any } = {};

  constructor(data?: Partial<BoxPolicyHolder>) {
    if (data) {
      Object.assign(this, data);
      if (data.person) {
        this.person = new Person(data.person);
      }
      if (data.contacts) {
        this.contacts = new Contacts(data.contacts);
      }
      if (data.identifiers) {
        this.identifiers = new Identifier(data.identifiers);
      }
      if (data.addresses) {
        this.addresses = new Address(data.addresses);
      }
      if (data.organization) {
        this.organization = new Organization(data.organization);
      }
    }
  }
}
