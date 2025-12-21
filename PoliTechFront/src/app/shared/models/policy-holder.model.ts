import { Person } from './person.model';
import { Contacts } from './contacts.model';
import { Identifier } from './identifier.model';
import { Address } from './address.model';
import { PlaceOfWork } from './place-of-work.model';
import { Organization } from './organization.model';

export class PolicyHolder {
  person?: Person;
  contacts?: Contacts;
  identifiers?: Identifier[];
  addresses?: Address[];
  organization?: Organization;
  customFields?: { [key: string]: any };

  constructor(data?: Partial<PolicyHolder>) {
    if (data) {
      Object.assign(this, data);
      if (data.person) {
        this.person = new Person(data.person);
      }
      if (data.contacts) {
        this.contacts = new Contacts(data.contacts);
      }
      if (data.identifiers) {
        this.identifiers = data.identifiers.map(id => new Identifier(id));
      }
      if (data.addresses) {
        this.addresses = data.addresses.map(addr => new Address(addr));
      }
      if (data.organization) {
        this.organization = new Organization(data.organization);
      }
    }
  }
}
