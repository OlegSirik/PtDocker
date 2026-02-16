import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EnvService } from '../env.service';
import { AuthService } from '../auth.service';
import { BaseApiService } from './base-api.service';

export interface ProviderDto {
  id?: number;
  name: string;
  status: string;
  executionMode?: string;
}

export interface ProviderListDto {
  id: number;
  name: string;
  status: string;
  executionMode?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AddonProvidersService extends BaseApiService<ProviderDto> {
  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
    super(http, env, 'admin/addon/providers', authService);
  }

  getProviders(): Observable<ProviderListDto[]> {
    return this.http.get<ProviderListDto[]>(this.getUrl());
  }
}
