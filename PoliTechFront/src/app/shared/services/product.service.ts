import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { catchError, delay, tap } from 'rxjs/operators';
import { BASE_URL } from '../tokens';

export interface Product {
  id?: number;
  lob: string;
  code: string;
  name: string;
  versionNo: number;
  versionStatus?: string;
  waitingPeriod: {
    validatorType?: 'RANGE' | 'LIST' | 'NEXT_MONTH';
    validatorValue?: string;
  };
  policyTerm: {
    validatorType?: 'RANGE' | 'LIST' ;
    validatorValue?: string;
  };
  numberGenerator: {
    mask: string;
    maxValue: number;
    resetPolicy: 'YEARLY' | 'MONTHLY';
    xorMask: string;
  };
  quoteValidator: QuoteValidator[];
  saveValidator: QuoteValidator[];
  packages: Package[];
  vars: PolicyVar[];
}

export interface PolicyVar {
  varPath: string;
  varName: string;
  varCode: string;
  varDataType: 'STRING' | 'NUMBER' | 'DATE' | 'DURATION';
  varValue: string;
}

export interface QuoteValidator {
  lineNr?: number;
  keyLeft: string;
  ruleType: string;
  keyRight?: string;
  valueRight?: string;
  dataType: 'NUMBER' | 'STRING' | 'DATE' | 'DURATION';
  errorText: string;
}

export interface Package {
  code: string;
  name: string;
  covers: Cover[];
}

export interface Limit {
  nr?: number;
  sumInsured: number;
  premium: number;
}

export interface Cover {
  code: string;
  isMandatory: boolean;
  waitingPeriod: string;
  coverageTerm: string;
  isDeductibleMandatory: boolean;
  deductibles: Deductible[];
  limits: Limit[];
}

export interface Deductible {
  nr?: number;
  deductibleType: string;
  deductible: number;
  deductibleUnit: 'PERCENT' | 'DAY' | 'RUB';
  deductibleSpecific: 'EVERY' | 'FROM_SECOND';
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  constructor(private http: HttpClient, @Inject(BASE_URL) private baseUrl: string) {};

  private mockData: Product =
    {
      id: 1202,
      lob: 'NS',
      code: 'NS-SRAVNIRU',
      name: 'НС Сравниру',
      versionNo: 1,
      versionStatus: 'DRAFT',
      waitingPeriod: {
        validatorType: 'RANGE',
        validatorValue: 'P1D-P30D'
      },
      policyTerm: {
        validatorType: 'LIST',
        validatorValue: 'P1M,P3M,P6M,P1Y'
      },
      numberGenerator: {
        mask: 'POL-{YYYY}-{MM}-{NNNN}',
        maxValue: 9999,
        resetPolicy: 'MONTHLY',
        xorMask: '0xABCD'
      },
      quoteValidator: [
        {
          lineNr: 1,
          keyLeft: 'insAmount',
          ruleType: 'RANGE',
          valueRight: '10000-200000',
          dataType: 'NUMBER',
          errorText: 'Ошибка в страховой сумме'
        }
      ],
      saveValidator: [
        {
          lineNr: 1,
          keyLeft: 'insAmount',
          ruleType: 'RANGE',
          valueRight: '10000-200000',
          dataType: 'NUMBER',
          errorText: 'Ошибка в страховой сумме'
        }
      ],
      packages: [
        {
          code: 'Basic',
          name: 'Простой',
          covers: [
            {
              code: 'PD',
              isMandatory: true,
              waitingPeriod: 'P0D',
              coverageTerm: 'P1Y',
              isDeductibleMandatory: false,
              deductibles: [
                {
                  nr: 1,
                  deductibleType: 'MANDATORY',
                  deductible: 100,
                  deductibleUnit: 'RUB',
                  deductibleSpecific: 'EVERY'
                }
              ],
              limits: [
                {
                  nr: 1,
                  sumInsured: 100000,
                  premium: 5000
                },
                {
                  nr: 2,
                  sumInsured: 200000,
                  premium: 9000
                }
              ]
            }
          ]
        }
      ],
      vars: [
        {
          varPath: 'policyHolder.person.lastName',
          varName: 'Страхователь.фамилия',
          varCode: 'ph_lastName',
          varDataType: 'STRING',
          varValue: ''
        }
      ]
    };

  private nextId = 1203;

  // GET - Get product by ID
  fixProduct() {
    if (this.mockData) {
      if (!this.mockData.waitingPeriod || typeof this.mockData.waitingPeriod !== 'object') {
        this.mockData.waitingPeriod = { validatorType: 'RANGE', validatorValue: '' };
      }
      if (!this.mockData.policyTerm || typeof this.mockData.policyTerm !== 'object') {
        this.mockData.policyTerm = { validatorType: 'RANGE', validatorValue: '' };
      }
      if (!this.mockData.numberGenerator || typeof this.mockData.numberGenerator !== 'object') {
        this.mockData.numberGenerator = { mask: '', maxValue: 0, resetPolicy: 'MONTHLY', xorMask: '' };
      }
      if (!Array.isArray(this.mockData.quoteValidator)) {
        this.mockData.quoteValidator = [];
      }
      if (!Array.isArray(this.mockData.saveValidator)) {
        this.mockData.saveValidator = [];
      }
      if (!Array.isArray(this.mockData.packages)) {
        this.mockData.packages = [];
      }
    }
  }


  getProduct(id: number, versionNo: number): Observable<Product> {
    if (!this.http) {
      throw new Error('HttpClient is not initialized');
    }

    return this.http.get<Product>(`${this.baseUrl}admin/products/${id}/versions/${versionNo}`).pipe(
      tap(data => {
          this.mockData = data;
          this.fixProduct();
      }),
      catchError(error => {
        console.error('Error fetching business lines:', error);
        return of(this.mockData);
      })
    );

  //    return of( this.products ) // Mock API delay

  }




  // POST - Create new product
  createProduct(product: Product): Observable<Product> {


    if (this.http) {
      return this.http.post<Product>(`${this.baseUrl}admin/products`, product).pipe(
        tap(createdProduct => {
          this.mockData = { ...createdProduct };
          this.fixProduct();
        }),
        catchError(error => {
          this.handleHttpError(error);
          return throwError(() => error);
        })
      );
    }
    this.mockData = product;
    return of({ ...product }).pipe(delay(500));
  }

  // PUT - Update existing product
  updateProduct(id: number, product: Product): Observable<Product> {

    const versionNo = product.versionNo;

    if (this.http) {
      return this.http.put<Product>(`${this.baseUrl}admin/products/${id}/versions/${versionNo}`, product).pipe(
        tap(updatedProduct => {
          this.mockData = { ...updatedProduct };
          this.fixProduct();
        }),
        catchError(error => {
          console.error('Error updating product:', error);
          // Fallback to mockData in case of error
          return of({ ...this.mockData });
        })
      );
    }
    // Fallback to mock if http is not available
    this.mockData = { ...product, id };
    return of({ ...this.mockData }).pipe(delay(500));
  }

  // DELETE - Delete product
  deleteProduct(id: number): Observable<void> {
    const index = this.mockData

      this.mockData;
      return of(void 0).pipe(delay(500));

    throw new Error('Product not found');
  }

  // Mock data for dropdowns
  getLobOptions(): Observable<string[]> {
    return of(['NS', 'OSAGO', 'KASKO', 'PROPERTY']).pipe(delay(200));
  }

  getKeyLeftOptions(): Observable<string[]> {
    return of(['insAmount', 'policyTerm', 'waitingPeriod', 'coverageCode', 'deductible']).pipe(delay(200));
  }

  getRuleTypeOptions(): Observable<string[]> {
    return of(['NOT_NULL','RANGE', '=', '!=', '>', '<', 'IN_LIST']).pipe(delay(200));
  }

  getCoverCodeOptions(): Observable<string[]> {
    return of(['PD', 'TPL', 'COMPREHENSIVE', 'THEFT', 'FIRE']).pipe(delay(200));
  }

  getDeductibleTypeOptions(): Observable<string[]> {
    return of(['OPTIONAL', 'MANDATORY']).pipe(delay(200));
  }

  getDeductibleUnitOptions(): Observable<string[]> {
    return of(['PERCENT', 'DAY', 'RUB']).pipe(delay(200));
  }

  getDeductibleSpecificOptions(): Observable<string[]> {
    return of(['EVERY', 'FROM_SECOND']).pipe(delay(200));
  }

  getValidatorTypeOptions(): Observable<string[]> {
    return of(['RANGE', 'LIST', 'NEXT_MONTH']).pipe(delay(200));
  }

  getResetPolicyOptions(): Observable<string[]> {
    return of(['YEARLY', 'MONTHLY']).pipe(delay(200));
  }

handleHttpError(error: any): string {
  if (!error || !error.status) {
    return 'Неизвестная ошибка';
  }
  switch (error.status) {
    case 0:
      return 'Нет соединения с сервером';
    case 400:
      return error.error?.message || 'Некорректный запрос (400)';
    case 401:
      return 'Неавторизован (401)';
    case 403:
      return 'Доступ запрещен (403)';
    case 404:
      return 'Ресурс не найден (404)';
    case 409:
      return 'Конфликт данных (409)';
    case 500:
      return 'Внутренняя ошибка сервера (500)';
    case 503:
      return 'Сервис недоступен (503)';
    default:
      return `Ошибка: ${error.status}`;
  }
}

  getTestRequestQuote(productId: number, versionNo: number): Observable<any> {
    if (!this.http) {
      // Return mock example if no http client
      return of({
        productId: productId,
        versionNo: versionNo,
        exampleData: "Mock test request data"
      });
    }

    const url = `${this.baseUrl}admin/products/${productId}/versions/${versionNo}/example_quote`;
    return this.http.get<any>(url).pipe(
      catchError((error) => {
        console.error('Error fetching test request:', error);
        return of({
          error: 'Failed to fetch test request',
          productId: productId,
          versionNo: versionNo
        });
      })
    );
  }

  getTestRequestSave(productId: number, versionNo: number): Observable<any> {
    if (!this.http) {
      // Return mock example if no http client
      return of({
        productId: productId,
        versionNo: versionNo,
        exampleData: "Mock test request data"
      });
    }

    const url = `${this.baseUrl}admin/products/${productId}/versions/${versionNo}/example_save`;
    return this.http.get<any>(url).pipe(
      catchError((error) => {
        console.error('Error fetching test request:', error);
        return of({
          error: 'Failed to fetch test request',
          productId: productId,
          versionNo: versionNo
        });
      })
    );
  }

}
