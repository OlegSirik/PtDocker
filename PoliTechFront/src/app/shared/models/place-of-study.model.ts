export class PlaceOfStudy {
  university: string = '';
  faculty: string = '';
  course: string = '';

  constructor(data?: Partial<PlaceOfStudy>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}
