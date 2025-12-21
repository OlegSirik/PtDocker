import { BoxPerson } from './person-box.model';
import { BoxContacts } from './contacts-box.model';
import { BoxIdentifier } from './identifier-box.model';
import { BoxAddress } from './address-box.model';
import { BoxOrganization } from './organization-box.model';

export class BoxPolicyHolder {
  person: BoxPerson = new BoxPerson();
  contacts: BoxContacts = new BoxContacts();
  identifiers: BoxIdentifier = new BoxIdentifier();
  addresses: BoxAddress = new BoxAddress();
  organization: BoxOrganization = new BoxOrganization();
  customFields: { [key: string]: string } = {};

  constructor(data?: Partial<BoxPolicyHolder>) {
    if (data) {
      Object.assign(this, data);
      if (data.person) {
        this.person = new BoxPerson(data.person);
      }
      if (data.contacts) {
        this.contacts = new BoxContacts(data.contacts);
      }
      if (data.identifiers) {
        this.identifiers = new BoxIdentifier(data.identifiers);
      }
      if (data.addresses) {
        this.addresses = new BoxAddress(data.addresses);
      }
      if (data.organization) {
        this.organization = new BoxOrganization(data.organization);
      }
    }
  }
}
