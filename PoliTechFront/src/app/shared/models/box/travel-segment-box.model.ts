export class BoxTravelSegment {
  ticketNr: string = '';
  departureDate: string = '';
  departureTime: string = '';
  departureCity: string = '';
  arrivalCity: string = '';
  ticketPrice: string = '';
  insurionUUID: string = '';
  insurionErrorCode: string = '';

  constructor(data?: Partial<BoxTravelSegment>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}
