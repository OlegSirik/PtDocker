import { Injectable } from '@angular/core';
import { catchError, map, Observable, of, tap } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';

export interface ProductList {
  id?: number;
  lob: string;
  code: string;
  name: string;
  prodVersionNo: number;
  devVersionNo: number;
}

@Injectable({
  providedIn: 'root'
})
export class ProductsService {
  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {};

  private mockData: ProductList[] = [
    {
      id: 12345,
      lob: 'Ипотека',
      name: 'НС спорт',
      code: 'NS_SPORT',
      prodVersionNo: 1,
      devVersionNo: 1
    },
    {
      id: 12346,
      lob: 'Автострахование',
      name: 'Каско Премиум',
      code: 'CASCO_PREMIUM',
      prodVersionNo: 1,
      devVersionNo: 1
    },
    {
      id: 12347,
      lob: 'Ипотека',
      name: 'Страхование жизни',
      code: 'LIFE_INS',
      prodVersionNo: 1,
      devVersionNo: 1
    },
    {
      id: 12348,
      lob: 'Автострахование',
      name: 'ОСАГО Стандарт',
      code: 'OSAGO_STD',
      prodVersionNo: 1,
      devVersionNo: 1
    }
  ];

  getProducts(): Observable<ProductList[]> {
    if (!this.http) {
      throw new Error('HttpClient is not initialized');
    }

    return this.http.get<ProductList[]>(`${this.authService.baseApiUrl}/admin/products`).pipe(
      tap(data => {
        if (Array.isArray(data) && data.length !== 0) {
          this.mockData = data;
        }
      }),
      catchError(error => {
        console.error('Error fetching business lines:', error);
        return of(this.mockData);
      })
    );
  }

  getProductByCode(code: string): Observable<ProductList | null> {
    const product = this.mockData.find(item => item.code === code);
    return of(product || null);
  }

  createProduct(product: Omit<ProductList, 'id'>): Observable<ProductList> {
    const newProduct: ProductList = {
      ...product,
      id: Date.now()
    };
    this.mockData.push(newProduct);
    return of(newProduct);
  }

  updateProduct(code: string, updatedProduct: Partial<ProductList>): Observable<ProductList | null> {
    const index = this.mockData.findIndex(item => item.code === code);
    if (index !== -1) {
      this.mockData[index] = { ...this.mockData[index], ...updatedProduct };
      return of(this.mockData[index]);
    }
    return of(null);
  }

    deleteProduct(id: number): Observable<boolean> {
      if (this.http) {
        const url = `${this.authService.baseApiUrl}/admin/products/${id}`;
        return this.http.delete(url).pipe(
          map(() => true),
          catchError(error => {
            console.error('Error deleting product:', error);
            return of(false);
          })
        );
      }
      return of(false);
    }
}


