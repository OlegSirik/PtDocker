import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../auth.service';
import { BaseApiService } from './base-api.service';
import { EnvService } from '../env.service';

export interface FileTemplate {
  id?: number;
  productCode: string;
  fileType: 'Policy' | 'KID' ;
  fileDescription: string;
  fileName?: string;
  packageCode?: string;
}

export interface FileUploadResponse {
  id: number;
}

@Injectable({
  providedIn: 'root'
})
export class FilesService extends BaseApiService<FileTemplate>{
  

  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
    super(http, env, 'admin/files', authService);
  }

  uploadFile(id: number, file: File): Observable<void> {
    const formData = new FormData();
    formData.append('file', file);

    let url = this.getUrl();
    return this.http.post<void>(`${url}`, formData);
    
  }

  uploadFileWithResponse(file: File, fileName: string): Observable<FileUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    
    // Add filename as JSON with proper content type
    const filenameJson = JSON.stringify({ filename: fileName });
    const jsonBlob = new Blob([filenameJson], { type: 'application/json' });
    formData.append('filename', jsonBlob, 'filename.json');

    let url = this.getUrl();
    return this.http.post<FileUploadResponse>(url, formData);
  }

  updateFile(fileId: number, file: File, fileName: string): Observable<void> {
    const formData = new FormData();
    formData.append('file', file);
    
    // Add filename as JSON with proper content type
    const filenameJson = JSON.stringify({ filename: fileName });
    const jsonBlob = new Blob([filenameJson], { type: 'application/json' });
    formData.append('filename', jsonBlob, 'filename.json');

    let url = this.getUrl(fileId);
    return this.http.put<void>(url, formData);
  }

  deleteFile(id: number): Observable<void> {
    return this.delete(id);
  }

}
