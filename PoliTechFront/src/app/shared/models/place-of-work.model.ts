export class PlaceOfWork {
  organization: string = '';
  occupationType: string = '';
  occupation: string = '';
  address: string = '';
  phone: string = '';

  constructor(data?: Partial<PlaceOfWork>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}
