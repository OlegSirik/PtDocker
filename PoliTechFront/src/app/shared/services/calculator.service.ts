import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, delay, tap } from 'rxjs/operators';
import { BASE_URL } from '../tokens';

export interface Calculator {
  id?: string;
  productId: string;
  productCode: string;
  versionNo: string;
  packageNo: string;
  vars: CalculatorVar[];
  formulas: CalculatorFormula[];
  coefficients: CalculatorCoefficient[];
}

export interface CalculatorVar {
  varCode: string;
  varName: string;
  varPath: string;
  varType: 'VAR' | 'CONST'|'COEFFICIENT'|'IN';
  varValue: string;
  varDataType: 'NUMBER'|'STRING'|'DATE'|'PERIOD';
}

export interface CalculatorFormula {
  varCode: string;
  varName: string;
  lines: FormulaLine[];
}

export interface FormulaLine {
  nr: string;
  conditionLeft: string;
  conditionOperator: string;
  conditionRight: string;
  expressionResult: string;
  expressionLeft: string;
  expressionOperator: string;
  expressionRight: string;
  postProcessor?: string;
}

export interface VarReference1 {
  varCode: string;
  varName: string;
}

export interface CalculatorCoefficient {
  varCode: string;
  varName: string;
  columns: CoefficientColumn[];
}

export interface CoefficientColumn {
  varCode: string;
  varDataType: string;
  nr: string;
  conditionOperator: string;
  sortOrder: string;
}

export interface CoefficientDataRow {
  id: number;
  conditionValue: (string | null)[];
  resultValue: number;
}

@Injectable({
  providedIn: 'root'
})
export class CalculatorService {
  constructor(private http: HttpClient, @Inject(BASE_URL) private baseUrl: string) {}

  private mockData: Calculator = {
    id: 'calc-1',
    productId: '1202',
    productCode: 'NS-SRAVNIRU',
    versionNo: '0',
    packageNo: 'Basic',
    vars: [
      {
        varCode: 'ph_firstname',
        varName: 'Имя страхователя',
        varPath: 'policyHolder.person.firstName',
        varType: 'VAR',
        varValue: '',
        varDataType: 'STRING'
      },
      {
        varCode: 'ins_amount',
        varName: 'Страховая сумма',
        varPath: 'insurance.amount',
        varType: 'VAR',
        varValue: '120000',
        varDataType: 'NUMBER'
      }
    ],
    formulas: [
      {
        varCode: 'premium',
        varName: 'Премия',
        lines: [
          {
            nr: '1',
            conditionLeft:  'ins_amount',
            conditionOperator: '>',
            conditionRight: 'ph_firstname',
            expressionResult: 'premium',
            expressionLeft: 'ins_amount',
            expressionOperator: '*',
            expressionRight: 'ph_firstname',
            postProcessor: 'ROUND'
          }
        ]
      }
    ],
    coefficients: [
      {
        varCode: 'age_factor',
        varName: 'Возрастной коэффициент',
        columns: [
          {
            varCode: 'age',
            varDataType: 'NUMBER',
            nr: '1',
            conditionOperator: '>=',
            sortOrder: 'ASC'
          }
        ]
      }
    ]
  };

  // GET calculator
  getCalculator(productId: string, versionNo: string, packageNo: string): Observable<Calculator> {
    if (!this.http) {
      throw new Error('HttpClient is not initialized');
    }

    return this.http.get<Calculator>(`${this.baseUrl}admin/products/${productId}/versions/${versionNo}/packages/${packageNo}/calculator`).pipe(
      tap(data => {
        this.mockData = data;
      }),
      catchError(error => {
        console.error('Error fetching calculator:', error);
        throw error;
        return of(this.mockData);
      })
    );
  }

  // POST calculator (create new)
  createCalculator(productId: string, versionNo: string, packageNo: string): Observable<Calculator> {
    const body = {
      productId: productId,
      versionNo: versionNo,
      packageNo: packageNo
    };

    if (this.http) {
      return this.http.post<Calculator>(`${this.baseUrl}admin/products/${productId}/versions/${versionNo}/packages/${packageNo}/calculator`, body).pipe(
        tap(createdCalculator => {
          this.mockData = { ...createdCalculator };
        }),
        catchError(error => {
          console.error('Error creating calculator:', error);
          this.mockData = { ...this.mockData, ...body };
          return of({ ...this.mockData });
        })
      );
    }

    this.mockData = { ...this.mockData, ...body };
    return of({ ...this.mockData }).pipe(delay(500));
  }

  // PUT calculator (update)
  updateCalculator(calculator: Calculator): Observable<Calculator> {
    if (this.http) {
      return this.http.put<Calculator>(`${this.baseUrl}admin/products/${calculator.productId}/versions/${calculator.versionNo}/packages/${calculator.packageNo}/calculator`, calculator).pipe(
        tap(updatedCalculator => {
          this.mockData = { ...updatedCalculator };
        }),
        catchError(error => {
          console.error('Error updating calculator:', error);
          return of({ ...this.mockData });
        })
      );
    }

    this.mockData = { ...calculator };
    return of({ ...this.mockData }).pipe(delay(500));
  }

  // Mock data for dropdowns
  getVarTypeOptions(): Observable<string[]> {
    return of(['IN', 'OUT', 'CALC']).pipe(delay(200));
  }

  getConditionOperatorOptions(): Observable<string[]> {
    return of(['=', '!=', '>', '<', '>=', '<=', 'IN', 'NOT_IN']).pipe(delay(200));
  }

  getExpressionOperatorOptions(): Observable<string[]> {
    return of(['+', '-', '*', '/', '%', 'POW', 'MIN', 'MAX']).pipe(delay(200));
  }

  getPostProcessorOptions(): Observable<string[]> {
    return of(['ROUND', 'FLOOR', 'CEIL', 'ABS', 'NONE']).pipe(delay(200));
  }

  getSortOrderOptions(): Observable<string[]> {
    return of(['ASC', 'DESC','NONE']).pipe(delay(200));
  }

  // Coefficient data API
  getCoefficientData(calculatorId: string, coefficientCode: string): Observable<CoefficientDataRow[]> {
    if (!this.http) {
      throw new Error('HttpClient is not initialized');
    }
    return this.http.get<CoefficientDataRow[]>(`${this.baseUrl}admin/calculator/${calculatorId}/coefficients/${coefficientCode}`).pipe(
      catchError(error => {
        console.error('Error fetching coefficient data:', error);
        // Return mock empty data
        return of([]);
      })
    );
  }

  createCoefficientData(calculatorId: string, coefficientCode: string, rows: CoefficientDataRow[]): Observable<CoefficientDataRow[]> {
    if (!this.http) {
      throw new Error('HttpClient is not initialized');
    }
    return this.http.post<CoefficientDataRow[]>(`${this.baseUrl}admin/calculator/${calculatorId}/coefficients/${coefficientCode}`, rows).pipe(
      catchError(error => {
        console.error('Error creating coefficient data:', error);
        return of(rows);
      })
    );
  }

  updateCoefficientData(calculatorId: string, coefficientCode: string, rows: CoefficientDataRow[]): Observable<CoefficientDataRow[]> {
    if (!this.http) {
      throw new Error('HttpClient is not initialized');
    }
    return this.http.put<CoefficientDataRow[]>(`${this.baseUrl}admin/calculator/${calculatorId}/coefficients/${coefficientCode}`, rows).pipe(
      catchError(error => {
        console.error('Error updating coefficient data:', error);
        return of(rows);
      })
    );
  }
}
