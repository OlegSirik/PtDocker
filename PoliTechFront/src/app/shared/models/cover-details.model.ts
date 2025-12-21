export class CoverDetails {
  code: string = '';
  option: string = '';
  description: string = '';

  constructor(data?: Partial<CoverDetails>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}
