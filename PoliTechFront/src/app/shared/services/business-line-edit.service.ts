import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, map, Observable, of, tap } from 'rxjs';
import { AuthService } from './auth.service';
import { VarsService } from './vars.service';
import { BaseApiService } from './api/base-api.service';
import { EnvService } from './env.service';

export interface BusinessLineVar {
  varCode: string;
  varType: string;
  varPath: string;
  varName: string;
  varDataType: string;
  varValue: string;
  varNr: number;
  varCdm: string;
}

export interface BusinessLineCover {
  coverCode: string;
  coverName: string;
  risks: string;
}

export interface BusinessLineFile {
  fileCode: string;
  fileName: string;
}

export interface BusinessLineEdit {
  id: number;
  mpCode: string;
  mpName: string;
  mpPhType: string;
  mpInsObjectType: string;
  mpVars: BusinessLineVar[];
  mpCovers: BusinessLineCover[];
  mpFiles?: BusinessLineFile[];
}

@Injectable({
  providedIn: 'root'
})
export class BusinessLineEditService extends BaseApiService<BusinessLineEdit> {

  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
      super(http, env, 'admin/lobs', authService);
  }  

  getBusinessLineByCode(code: String): Observable<BusinessLineEdit> {
    return this.http.get<BusinessLineEdit>(`${this.authService.baseApiUrl}/admin/lobs/${code}`);
  }

  saveBusinessLine(businessLine: BusinessLineEdit): Observable<BusinessLineEdit> {
    if ( businessLine.id > 0 ) {
      return this.update(businessLine.id, businessLine);
    } else {
      return this.create(businessLine);
    }
  }

  getLobVars(lob: string): Observable<string[]> {
    return this.getBusinessLineByCode(lob).pipe(
      map((businessLine: BusinessLineEdit | null) => {
        if (businessLine && Array.isArray(businessLine.mpVars)) {
          return businessLine.mpVars.map((v: any) => v.varCode);
        }
        return [];
      }),
      catchError(() => of([]))
    );
  }

  getLobCovers(lob: string): Observable<string[]> {
    return this.getBusinessLineByCode(lob).pipe(
      map((businessLine: BusinessLineEdit | null) => {
        if (businessLine && Array.isArray(businessLine.mpCovers)) {
          return businessLine.mpCovers.map((c: any) => c.coverCode);
        }
        return [];
      }),
      catchError(() => of([]))
    );
  }

  getExampleJson(lobCode: string): Observable<any> {
   
    return this.http.get<any>(`${this.authService.baseApiUrl}/admin/lobs/${lobCode}/example`).pipe(
      catchError((error) => {
        console.error('Error fetching example JSON:', error);
        return of({
          policyHolder: {
            person: {
              firstName: 'John',
              lastName: 'Doe'
            }
          }
        });
      })
    );
  }

}