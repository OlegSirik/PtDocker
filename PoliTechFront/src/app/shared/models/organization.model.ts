

export class Organization {
  country?: string = '';
  inn?: string = '';
  fullName?: string = '';
  fullNameEn?: string = '';
  shortName?: string = '';
  legalForm?: string = '';
  kpp?: string = '';
  ogrn?: string = '';
  okpo?: string = '';
  bic?: string = '';
  isResident?: boolean;
  group?: string = '';
  ext_id?: string = '';

  constructor(data?: Partial<Organization>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}
