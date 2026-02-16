import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EnvService } from '../env.service';
import { AuthService } from '../auth.service';
import { BaseApiService } from './base-api.service';

export interface AddonProductRef {
  productId: number;
  preconditions?: string;
}

export interface PricelistDto {
  id?: number;
  providerId: number;
  code: string;
  name: string;
  categoryCode?: string;
  price?: number;
  amountFree?: number;
  amountBooked?: number;
  status?: string;
  addonProducts?: AddonProductRef[];
}

export interface PricelistListDto {
  id: number;
  providerId: number;
  code: string;
  name: string;
  categoryCode?: string;
  price?: number;
  amountFree?: number;
  status?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AddonPricelistService {
  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getUrl(id?: number | string): string {
    const base = `${this.authService.baseApiUrl}/admin/addon/pricelists`;
    return id != null ? `${base}/${id}` : base;
  }

  getPricelists(providerId?: number): Observable<PricelistListDto[]> {
    let url = this.getUrl();
    if (providerId != null) {
      url += `?spId=${providerId}`;
    }
    return this.http.get<PricelistListDto[]>(url);
  }

  getPricelist(id: number): Observable<PricelistDto> {
    return this.http.get<PricelistDto>(this.getUrl(id));
  }

  createPricelist(dto: PricelistDto): Observable<PricelistDto> {
    return this.http.post<PricelistDto>(this.getUrl(), dto);
  }

  updatePricelist(id: number, dto: PricelistDto): Observable<PricelistDto> {
    return this.http.put<PricelistDto>(this.getUrl(id), { ...dto, id });
  }

  deletePricelist(id: number): Observable<void> {
    return this.http.delete<void>(this.getUrl(id));
  }

  suspendPricelist(id: number): Observable<void> {
    return this.http.post<void>(`${this.getUrl(id)}/suspend`, {});
  }
}
