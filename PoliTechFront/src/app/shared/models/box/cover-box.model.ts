import { BoxCoverDetails } from './cover-details-box.model';

export class BoxCover {
  cover: BoxCoverDetails = new BoxCoverDetails();
  risk: string[] = [];
  startDate: string = '';
  endDate: string = '';
  sumInsured: string = '';
  premium: string = '';
  deductibleType: string = '';
  deductible: string = '';
  sumInsuredCur: string = '';
  premiumCur: string = '';
  deductibleCur: string = '';
  deductiblePercent: string = '';
  deductibleMin: string = '';
  deductibleUnit: string = '';
  deductibleSpecific: string = '';
  deductibleNr: string = '';
  deductibleText: string = '';

  constructor(data?: Partial<BoxCover>) {
    if (data) {
      Object.assign(this, data);
      if (data.cover) {
        this.cover = new BoxCoverDetails(data.cover);
      }
    }
  }
}
