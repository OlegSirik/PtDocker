import { TravelSegment } from './travel-segment.model';

export class AdditionalFactors {
  isProfessional: boolean = false;
  isSporttime: boolean = false;
  isSumInsuredInCredit: boolean = false;
  typeOfSport: string = '';
  travelSegments?: TravelSegment[];

  constructor(data?: Partial<AdditionalFactors>) {
    if (data) {
      Object.assign(this, data);
      if (data.travelSegments) {
        this.travelSegments = data.travelSegments.map(ts => new TravelSegment(ts));
      }
    }
  }
}
