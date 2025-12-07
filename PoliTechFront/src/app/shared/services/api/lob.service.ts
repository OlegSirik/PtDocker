import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { tap, catchError, delay, map } from 'rxjs/operators';
import { EnvService } from '../env.service';
import { AuthService } from '../auth.service';
import { BaseApiService } from './base-api.service';

export interface BusinessLine {
    id: number;
    mpCode: string;
    mpName: string;
  }

@Injectable({
  providedIn: 'root'
})

export class LobService extends BaseApiService<BusinessLine> {
  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
    super(http, env, 'admin/lobs', authService);
  }

  getLobCodes(): Observable<string[]> {
    return this.getAll().pipe(
      map((businessLines: BusinessLine[]) => businessLines.map(item => item.mpCode))
    );

  }
}
