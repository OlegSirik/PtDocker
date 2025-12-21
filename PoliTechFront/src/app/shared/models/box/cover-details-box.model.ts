export class BoxCoverDetails {
  code: string = '';
  option: string = '';
  description: string = '';

  constructor(data?: Partial<BoxCoverDetails>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}
