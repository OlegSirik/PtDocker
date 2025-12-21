import { BoxCover } from './cover-box.model';
import { BoxPerson } from './person-box.model';
import { BoxContacts } from './contacts-box.model';
import { BoxIdentifier } from './identifier-box.model';
import { BoxAddress } from './address-box.model';
import { BoxDevice } from './device-box.model';

export class BoxInsuredObject {
  ioType: string = '';
  packageCode: string = '';
  coverage: BoxCover[] = [];
  objectId: string = '';
  person: BoxPerson = new BoxPerson();
  contacts: BoxContacts = new BoxContacts();
  identifiers: BoxIdentifier = new BoxIdentifier();
  address: BoxAddress = new BoxAddress();
  device: BoxDevice = new BoxDevice();

  constructor(data?: Partial<BoxInsuredObject>) {
    if (data) {
      Object.assign(this, data);

      if (data.device) {
        this.device = new BoxDevice(data.device);
      }

      if (data.coverage) {
        this.coverage = Array.isArray(data.coverage)
          ? data.coverage.map(cov => new BoxCover(cov))
          : [];
      }

      if (data.person) {
        this.person = new BoxPerson(data.person);
      }

      if (data.contacts) {
        this.contacts = new BoxContacts(data.contacts);
      }

      if (data.identifiers) {
        this.identifiers = new BoxIdentifier(data.identifiers);
      }

      if (data.address) {
        this.address = new BoxAddress(data.address);
      }
    }
  }
}
