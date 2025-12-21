export class TravelSegment {
  ticketNr: string = '';
  departureDate: string = '';
  departureTime: string = '';
  departureCity: string = '';
  arrivalCity: string = '';
  ticketPrice: string = '';
  insurionUUID: string = '';
  insurionErrorCode: string = '';

  constructor(data?: Partial<TravelSegment>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}
