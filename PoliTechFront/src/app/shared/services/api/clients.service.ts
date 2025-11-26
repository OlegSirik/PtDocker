import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { tap, catchError, delay, map } from 'rxjs/operators';
import { EnvService } from '../env.service';
import { BaseApiService } from './base-api.service';

export interface Client {
    id?: number;
    tid: number;
    code: string;
    name: string;
    description: string;
    trusted_email: string;
    createdAt?: Date | string;
    updateAt?: Date | string;
    status: 'ACTIVE' | 'DELETED' | 'SUSPENDED';
  }

@Injectable({
  providedIn: 'root'
})

export class ClientsService extends BaseApiService<Client> {
  constructor(http: HttpClient, env: EnvService) {
    super(http, env, 'acc/tnts/38/clients');
  }

  override getUrl(id?: (number | string)): string {
    let url = this.resourcePath;
    /*
    TODO Длбавить получение ИД тенанта из окружения 
    */
    let tenatId = this.env.TENANT_HEADER

    if (id) {
      url += '/' + id;
    }
    return this.env.BASE_URL + '/' + url;
  }


}
