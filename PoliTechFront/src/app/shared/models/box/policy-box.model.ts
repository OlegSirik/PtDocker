import { BoxPolicyHolder } from './policy-holder-box.model';
import { BoxInsuredObject } from './insured-object-box.model';
import { BoxCover } from './cover-box.model';

export class BoxPolicy {
  id: string = '';
  policyNumber: string = '';
  previousPolicyNumber: string = '';
  productCode: string = '';
  statusCode: string = '';
  startDate: string = '';
  endDate: string = '';
  issueDate: string = '';
  policyTerm: string = '';
  waitingPeriod: string = '';
  issueTimeZone: string = '';
  createDate: string = '';
  paymentDate: string = '';
  cancellationDate: string = '';
  premium: string = '';
  placeOfIssue: string = '';
  draftId: string = '';
  installmentType: string = '';
  
  policyHolder: BoxPolicyHolder = new BoxPolicyHolder();
  insuredObject: BoxInsuredObject = new BoxInsuredObject();
  coverage: BoxCover[] = []
  
  constructor(data?: Partial<BoxPolicy>) {
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
        this.policyHolder = new BoxPolicyHolder(data.policyHolder as Partial<BoxPolicyHolder>);
      }
      if (data.insuredObject) {
        this.insuredObject = new BoxInsuredObject(data.insuredObject as Partial<BoxInsuredObject>);
      }
    }
  }
}
