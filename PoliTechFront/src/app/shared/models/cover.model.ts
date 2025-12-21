import { CoverDetails } from './cover-details.model';

export class Cover {
  cover: CoverDetails = new CoverDetails();
  risk: string[] = [];
  startDate: string = '';
  endDate: string = '';
  sumInsured: number = 0;
  premium: number = 0;
  deductibleType: string = '';
  deductible: number = 0;
  sumInsuredCur: number = 0;
  premiumCur: number = 0;
  deductibleCur: number = 0;
  deductiblePercent: number = 0;
  deductibleMin: number = 0;
  deductibleUnit: string = '';
  deductibleSpecific: string = '';
  deductibleNr: number = -1;
  deductibleText: string = '';

  constructor(data?: Partial<Cover>) {
    if (data) {
      Object.assign(this, data);
      if (data.cover) {
        this.cover = new CoverDetails(data.cover);
      }
    }
  }
}
