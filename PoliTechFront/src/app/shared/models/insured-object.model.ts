import { Cover } from './cover.model';
import { Person } from './person.model';
import { Contacts } from './contacts.model';
import { Identifier } from './identifier.model';
import { Address } from './address.model';
import { Device } from './device.model';

export class InsuredObject {
  ioType: string = '';
  insAmount?: number;
  packageCode?: string;
  coverage?: Cover[];
  covers?: Cover[]
  objectId?: string;
  person?: Person;
  contacts?: Contacts
  identifiers?: Identifier
  address?: Address 
  device?: Device;
  //property?: Property 

  constructor(data?: Partial<InsuredObject>) {
    if (data) {
      Object.assign(this, data);

      if (data.device) {
        this.device = new Device(data.device);
      }

      if (Array.isArray((data as any).coverage)) {
        this.coverage = (data as any).coverage.map((cover: any) => new Cover(cover));
      }

      if (data.person) {
        this.person = new Person(data.person);
      }

      if (data.contacts) {
        this.contacts = new Contacts(data.contacts);
      }

      if (data.identifiers) {
        this.identifiers = new Identifier(data.identifiers);
      }

      if (data.address) {
        this.address = new Address(data.address);
      }
    }
  }
}
