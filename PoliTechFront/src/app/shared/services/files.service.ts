import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from './auth.service';

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
export class FilesService {
  private mockFiles: FileTemplate[] = [
    {
      id: 1,
      productCode: 'NS_SPORT',
      fileType: 'Policy',
      fileDescription: 'Полис страхования спорта',
      fileName: 'policy_sport.pdf',
      packageCode: '0'
    },
    {
      id: 2,
      productCode: 'CASCO_PREMIUM',
      fileType: 'KID',
      fileDescription: 'Ключевой информационный документ',
      fileName: 'kid_casco.pdf',
      packageCode: '0'
    }
  ];

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  getFiles(): Observable<FileTemplate[]> {
    // на маке надо так this.http.get<FileTemplate[]>(`${this.baseUrl}/admin/files`) -- нет слеша
    return this.http.get<FileTemplate[]>(`${this.authService.baseApiUrl}/admin/files`).pipe(
      catchError(() => of(this.mockFiles))
    );
  }

  createFile(fileData: Omit<FileTemplate, 'id'>): Observable<FileUploadResponse> {
    const newFile: FileTemplate = {
      ...fileData,
      id: Date.now()
    };

    return this.http.post<FileUploadResponse>(`${this.authService.baseApiUrl}/admin/files`, {
      fileType: fileData.fileType.toLowerCase(),
      fileDescription: fileData.fileDescription,
      productCode: fileData.productCode,
      packageCode: fileData.packageCode
    }).pipe(
      catchError(() => {
        this.mockFiles.push(newFile);
        return of({ id: newFile.id! });
      })
    );
  }

  uploadFile(id: number, file: File): Observable<void> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<void>(`${this.authService.baseApiUrl}/admin/files/${id}`, formData).pipe(
      catchError(() => of(void 0))
    );
  }

  deleteFile(id: number): Observable<void> {
    return this.http.delete<void>(`${this.authService.baseApiUrl}/admin/files/${id}`).pipe(
      catchError(() => {
        const index = this.mockFiles.findIndex(f => f.id === id);
        if (index !== -1) {
          this.mockFiles.splice(index, 1);
        }
        return of(void 0);
      })
    );
  }

  processFile(id: number, keyValueData: string): Observable<Blob> {
    // Try different request formats based on the API requirements
    let requestBody: any;
    let contentType = 'application/json';

    try {
      // Try to parse as JSON first
      const parsedData = JSON.parse(keyValueData);
      requestBody = parsedData;
    } catch (e) {
      // If it's not valid JSON, send as raw text with multiple possible formats
      requestBody = {
        data: keyValueData,
        keyValueData: keyValueData,
        payload: keyValueData,
        text: keyValueData
      };
    }

    return this.http.post(`${this.authService.baseApiUrl}/admin/files/${id}/cmd/process`,
      requestBody,
      {
        responseType: 'blob',
        headers: {
          'Content-Type': contentType
        }
      }
    ).pipe(
      catchError((error) => {
        // Mock response - create a simple text file
        console.error('Ошибка обработки файла (mock):', keyValueData, error);
        const mockContent = `Processed file with data:\n${keyValueData}\n\nGenerated at: ${new Date().toISOString()}\n\nNote: This is a mock response as the API endpoint is not available.`;
        const blob = new Blob([mockContent], { type: 'text/plain' });
        return of(blob);
      })
    );
  }
}
