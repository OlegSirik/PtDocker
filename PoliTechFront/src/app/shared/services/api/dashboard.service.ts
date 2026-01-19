import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../auth.service';

export interface DashboardChartPoint {
  period: string;
  amount: number;
  sum: number;
}

export interface DashboardChartResponse {
  periodType: string;
  points: DashboardChartPoint[];
}

export interface DashboardCard {
  title: string;
  value: string;
  unit?: string | null;
}

export interface DashboardCardsResponse {
  cards: DashboardCard[];
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  constructor(private http: HttpClient, private authService: AuthService) {}

  getChart(period: string, from?: string, to?: string): Observable<DashboardChartResponse> {
    let params = new HttpParams().set('period', period);
    if (from) {
      params = params.set('from', from);
    }
    if (to) {
      params = params.set('to', to);
    }
    return this.http.get<DashboardChartResponse>(`${this.authService.baseApiUrl}/dashboard/chart`, { params });
  }

  getCards(): Observable<DashboardCardsResponse> {
    return this.http.get<DashboardCardsResponse>(`${this.authService.baseApiUrl}/dashboard/cards`);
  }
}
