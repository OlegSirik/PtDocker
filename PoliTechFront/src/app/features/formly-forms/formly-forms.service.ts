import { HttpClient } from '@angular/common/http';
import { Inject, Injectable, OnInit } from '@angular/core';
import { Observable, of } from 'rxjs';
import { tap, catchError, delay } from 'rxjs/operators';
import { BASE_URL } from '../../shared/tokens';
import { Product, ProductService } from '../../shared/services/product.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';

export interface Field {
  key: string;
  style: string;
  show: boolean;
  label: string;
  mandatory: boolean;
  value?: string;
}

export interface FormData {

  item1: string;
  item2: string;
  item3: string;
  item4: string;
  item5: string;
}

@Injectable({
  providedIn: 'root'
})
export class FormlyFormsService{

  constructor(
    private productService: ProductService,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute,
    ) {}


  submitForm(productId: number, versionNo: number, formData: any): Observable<any> {
    return of(null);
  }

}
