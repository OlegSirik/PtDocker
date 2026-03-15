import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthService } from '../auth.service';
import { BaseApiService } from './base-api.service';
import { EnvService } from '../env.service';

export interface FileTemplate {
  id?: number;
  filename?: string;
  contentType?: string;
  size?: number;
}

export interface FileUploadResponse {
  id: string;
}

@Injectable({
  providedIn: 'root'
})
export class FilesService extends BaseApiService<FileTemplate> {

  constructor(http: HttpClient, env: EnvService, authService: AuthService) {
    super(http, env, 'admin/files', authService);
  }

  /** Upload file - backend uses filename, contentType, size from the File */
  uploadFile(file: File): Observable<FileUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    const url = this.getUrl();
    return this.http.post<FileUploadResponse>(url, formData);
  }

  /** Alias for uploadFile for backward compatibility */
  uploadFileWithResponse(file: File, _fileName?: string): Observable<FileUploadResponse> {
    return this.uploadFile(file);
  }

  /** Download file as blob, returns blob and suggested filename from Content-Disposition */
  downloadFile(fileId: number | string): Observable<{ blob: Blob; filename: string }> {
    const url = this.getUrl(fileId);
    return this.http.get(url, {
      responseType: 'blob',
      observe: 'response'
    }).pipe(
      map(res => {
        let filename = 'download';
        const cd = res.headers.get('Content-Disposition');
        if (cd) {
          const match = cd.match(/filename="?([^"]+)"?/);
          if (match) filename = match[1];
        }
        return { blob: res.body!, filename };
      })
    );
  }

  deleteFile(id: number | string): Observable<void> {
    return this.delete(id);
  }
}
