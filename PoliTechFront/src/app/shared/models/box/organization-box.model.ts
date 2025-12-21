export class BoxOrganization {
  country: string = '';
  inn: string = '';
  fullName: string = '';
  fullNameEn: string = '';
  shortName: string = '';
  legalForm: string = '';
  kpp: string = '';
  ogrn: string = '';
  okpo: string = '';
  bic: string = '';
  isResident: string = '';
  group: string = '';
  ext_id: string = '';

  constructor(data?: Partial<BoxOrganization>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}
