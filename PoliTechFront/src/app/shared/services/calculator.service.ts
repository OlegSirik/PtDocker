import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, delay, tap } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { BaseApiService } from './api/base-api.service';
import { EnvService } from './env.service';

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
  varType: string;
  varValue: string;
  varDataType: string;
  varCdm: string;
  varNr: number;
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
  altVarName?: string;
  altVarValue?: number;
  columns: CoefficientColumn[];
  errorTextIfNotFound?: string;
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

/** Ответ POST `/admin/calculators/templates` — строки шаблона формулы */
export interface CalculatorTemplateLine {
  calculatorId?: number;
  lineNr?: number;
  text?: string;
}

export interface CalculatorTemplate {
  calculatorId?: number;
  calculatorName?: string;
  lines: CalculatorTemplateLine[];
}

@Injectable({
  providedIn: 'root'
})

export class CalculatorService extends BaseApiService<Calculator> {
  constructor(
    http: HttpClient,
    env: EnvService,
    authService: AuthService,
  ) {
    super(http, env, 'admin/calculators', authService);
  }

  private mockData: Calculator = {
    productId: '',
    productCode: '',
    versionNo: '',
    packageNo: '',
    vars: [],
    formulas: [],
    coefficients: []
  };

  // GET calculator
  getCalculator(productId: string, versionNo: string, packageNo: string): Observable<Calculator> {

    let baseUrl = this.getUrl() + '/products/' + productId + '/versions/' + versionNo + '/packages/' + packageNo;
    const headers = new HttpHeaders({ 'X-Skip-Global-Error': 'true' });
    return this.http.get<Calculator>(baseUrl, { headers }).pipe(
      tap(data => {
        this.mockData = data;
      }),
      catchError(error => {
        console.error('Error fetching calculator:', error);
        throw error;
      })
    );
  }

  // POST calculator (create new), optional from template
  createCalculator(productId: string, versionNo: string, packageNo: string, templateId?: number): Observable<Calculator> {
    const body = {
      productId: productId,
      versionNo: versionNo,
      packageNo: packageNo,
      templateId: templateId ?? null
    };

    let baseUrl = this.getUrl() + '/products/' + productId + '/versions/' + versionNo + '/packages/' + packageNo;
      return this.http.post<Calculator>(baseUrl, body).pipe(
        tap(createdCalculator => {
          this.mockData = { ...createdCalculator };
        }),
        catchError(error => {
          console.error('Error creating calculator:', error);
          this.mockData = { ...this.mockData, ...body };
          return of({ ...this.mockData });
        })
      );
    

    this.mockData = { ...this.mockData, ...body };
    return of({ ...this.mockData }).pipe(delay(500));
  }

  // PUT calculator (update)
  updateCalculator(calculator: Calculator): Observable<Calculator> {
    
    let baseUrl = this.getUrl() + '/products/' + calculator.productId + '/versions/' + calculator.versionNo + '/packages/' + calculator.packageNo;
      return this.http.put<Calculator>(baseUrl, calculator).pipe(
        tap(updatedCalculator => {
          this.mockData = { ...updatedCalculator };
        }),
        catchError(error => {
          console.error('Error updating calculator:', error);
          return of({ ...this.mockData });
        })
      );
    

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
    return of(['+', '-', '*', '/', 'min', 'max']).pipe(delay(200));
  }

  getPostProcessorOptions(): Observable<string[]> {
    return of(['round2_up', 'round2_down', 'round2_ceiling', 'round2_floor'
      , 'round2_halfup', 'round2_halfdown', 'round2_halfeven'
      , 'NONE']).pipe(delay(200));

  }

  getSortOrderOptions(): Observable<string[]> {
    return of(['ASC', 'DESC','NONE']).pipe(delay(200));
  }

  // Coefficient data API
  getCoefficientData(calculatorId: string, coefficientCode: string): Observable<CoefficientDataRow[]> {
    let baseUrl = this.getUrl(calculatorId) + '/coefficients/' + coefficientCode;

    return this.http.get<CoefficientDataRow[]>(baseUrl).pipe(
      catchError(error => {
        console.error('Error fetching coefficient data:', error);
        // Return mock empty data
        return of([]);
      })
    );
  }

  createCoefficientData(calculatorId: string, coefficientCode: string, rows: CoefficientDataRow[]): Observable<CoefficientDataRow[]> {
    let baseUrl = this.getUrl(calculatorId) + '/coefficients/' + coefficientCode;

    return this.http.post<CoefficientDataRow[]>(baseUrl, rows).pipe(
      catchError(error => {
        console.error('Error creating coefficient data:', error);
        return of(rows);
      })
    );
  }

  updateCoefficientData(calculatorId: string, coefficientCode: string, rows: CoefficientDataRow[]): Observable<CoefficientDataRow[]> {
    let baseUrl = this.getUrl(calculatorId) + '/coefficients/' + coefficientCode;

    return this.http.put<CoefficientDataRow[]>(baseUrl, rows).pipe(
      catchError(error => {
        console.error('Error updating coefficient data:', error);
        return of(rows);
      })
    );
  }

  /** GET coefficient SQL template (variable names as placeholders) */
  getCoefficientSql(calculatorId: string, coefficientCode: string): Observable<string> {
    const url = this.getUrl(calculatorId) + '/coefficients/' + encodeURIComponent(coefficientCode) + '/SQL';
    return this.http.get(url, { responseType: 'text' }).pipe(
      catchError(error => {
        console.error('Error fetching coefficient SQL:', error);
        throw error;
      })
    );
  }

  /** POST: создать шаблон формулы по калькулятору для LOB (бэкенд: createTemplate) */
  createTemplate(lobCode: string, calculatorId: string | number): Observable<CalculatorTemplate> {
    const body = {
      lobCode,
      calculatorId: typeof calculatorId === 'string' ? Number(calculatorId) : calculatorId,
    };
    return this.http.post<CalculatorTemplate>(`${this.getUrl()}/templates`, body).pipe(
      catchError(error => {
        console.error('Error creating calculator template:', error);
        throw error;
      })
    );
  }

  /** GET: получить шаблоны калькуляторов для LOB */
  getTemplates(lobCode: string): Observable<CalculatorTemplate[]> {
    const params = new HttpParams().set('lob', lobCode);
    return this.http.get<CalculatorTemplate[]>(`${this.getUrl()}/templates`, { params }).pipe(
      catchError(error => {
        console.error('Error fetching calculator templates:', error);
        throw error;
      })
    );
  }

  /** PUT: обновить имя шаблона калькулятора */
  updateTemplateName(templateId: number, templateName: string): Observable<CalculatorTemplate> {
    return this.http.put<CalculatorTemplate>(`${this.getUrl()}/templates/${templateId}`, { templateName }).pipe(
      catchError(error => {
        console.error('Error updating calculator template name:', error);
        throw error;
      })
    );
  }

  /** DELETE: удалить шаблон калькулятора */
  deleteTemplate(templateId: number): Observable<void> {
    return this.http.delete<void>(`${this.getUrl()}/templates/${templateId}`).pipe(
      catchError(error => {
        console.error('Error deleting calculator template:', error);
        throw error;
      })
    );
  }
}
