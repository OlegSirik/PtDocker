export class BoxContacts {
  phone: string = '';
  email: string = '';
  telegram: string = '';

  constructor(data?: Partial<BoxContacts>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}
