import { PolicyHolder } from './policy-holder.model';
import { InsuredObject } from './insured-object.model';
import { Cover } from './cover.model';

export class Policy {
  id?: string = '';
  policyNumber?: string;
  previousPolicyNumber?: string;
  productCode: string = '';
  statusCode: string = '';
  startDate?: string = '';
  endDate?: string = '';
  issueDate?: string = '';
  issueTimeZone?: string = '';
  policyTerm?: string = '';
  waitingPeriod?: string = '';
  createDate?: string;
  paymentDate?: string;
  cancellationDate?: string;
  premium?: number;
  placeOfIssue?: string;
  draftId?: string;
  installmentType?: string;
  
  policyHolder: PolicyHolder = new PolicyHolder();
  insuredObjects: InsuredObject[] = [];

  constructor(data?: Partial<Policy>) {
    if (data) {
      Object.assign(this, data);

      // Ensure product and status remain strings
      if (typeof data.productCode === 'string') {
        this.productCode = data.productCode;
      }

      if (typeof data.statusCode === 'string') {
        this.statusCode = data.statusCode;
      }

      if (data.policyHolder) {
        this.policyHolder = new PolicyHolder(data.policyHolder);
      }
      if (data.insuredObjects) {
        this.insuredObjects[0] = new InsuredObject(data.insuredObjects[0]);
      }
      }
    }
  }

