export class Contacts {
  phone?: string = '';
  email?: string = '';
  telegram?: string = '';
  

  constructor(data?: Partial<Contacts>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}
