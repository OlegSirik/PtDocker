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

  private getMockFormFields(productId: number, versionNo: number): Field[] {
    // Mock data - returns only some fields to demonstrate visibility logic
    return [
      {
        key: 'field1',
        style: 'field-primary field-large',
        show: true,
        label: 'First Name',
        mandatory: true,
        value: ''
      },
      {
        key: 'field2',
        style: 'field-primary field-bold',
        show: true,
        label: 'Last Name',
        mandatory: true,
        value: ''
      },
      {
        key: 'field3',
        style: 'field-secondary',
        show: true,
        label: 'Email Address',
        mandatory: false,
        value: ''
      },
      {
        key: 'field4',
        style: 'field-accent',
        show: true,
        label: 'Phone Number',
        mandatory: false,
        value: ''
      },
      {
        key: 'field5',
        style: 'field-warn field-italic',
        show: true,
        label: 'Company Name',
        mandatory: true,
        value: ''
      },
      {
        key: 'field6',
        style: 'field-primary field-small',
        show: false, // This field will be hidden
        label: 'Hidden Field',
        mandatory: false,
        value: ''
      },
      {
        key: 'field7',
        style: 'field-secondary field-large',
        show: true,
        label: 'Address',
        mandatory: false,
        value: ''
      },
      {
        key: 'field8',
        style: 'field-accent field-bold',
        show: true,
        label: 'City',
        mandatory: true,
        value: ''
      },
      {
        key: 'field9',
        style: 'field-primary',
        show: true,
        label: 'Country',
        mandatory: false,
        value: ''
      },
      {
        key: 'field10',
        style: 'field-warn field-large field-bold',
        show: true,
        label: 'Additional Notes',
        mandatory: false,
        value: ''
      }
    ];
  }
}
