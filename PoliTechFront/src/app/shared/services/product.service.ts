import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { catchError, delay, tap } from 'rxjs/operators';
import { AuthService } from './auth.service';

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
  varDataType: string;
  varValue: string;
  varType: string;
  varCdm: string;
  varNr: number;
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

export interface PackageFile {
  fileCode: string;
  fileName: string;
  fileId?: number;
}

export interface Package {
  code: string;
  name: string;
  covers: Cover[];
  files: PackageFile[];
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
  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {};

  private mockData: Product = {
    id: -1,
    lob: 'NS',
    code: 'NS_SPORT',
    name: 'НС спорт',
    versionNo: 1,
    waitingPeriod: {
      validatorType: undefined,
      validatorValue: undefined
    },
    policyTerm: {
      validatorType: undefined,
      validatorValue: undefined
    },
    numberGenerator: {
      mask: '',
      maxValue: 0,
      resetPolicy: 'MONTHLY',
      xorMask: ''
    },
    quoteValidator: [],
    saveValidator: [],
    packages: [],
    vars: []
  };

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
    let baseUrl = this.authService.baseApiUrl + '/admin/products/' + id + '/versions/' + versionNo;

    return this.http.get<Product>(baseUrl).pipe(
      tap(data => {
          this.fixProduct();
      })
    );
  }

  // POST - Create new product
  createProduct(product: Product): Observable<Product> {


    if (this.http) {
      return this.http.post<Product>(`${this.authService.baseApiUrl}/admin/products`, product).pipe(
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
      return this.http.put<Product>(`${this.authService.baseApiUrl}/admin/products/${id}/versions/${versionNo}`, product).pipe(
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

    const url = `${this.authService.baseApiUrl}/admin/products/${productId}/versions/${versionNo}/example_quote`;
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

    const url = `${this.authService.baseApiUrl}/admin/products/${productId}/versions/${versionNo}/example_save`;
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
