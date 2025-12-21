

export class Address {
  isPrimary: boolean = false;
  typeCode: string = '';
  countryCode?: string = '';
  region?: string = '';
  city?: string = '';
  street?: string = '';
  house?: string = '';
  building?: string;
  flat?: string;
  room?: string;
  zipCode?: string = '';
  kladrId?: string = '';
  fiasId?: string = '';
  addressStr: string = '';
  addressStrEn?: string = '';
  ext_id?: string = '';

  constructor(data?: Partial<Address>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}
