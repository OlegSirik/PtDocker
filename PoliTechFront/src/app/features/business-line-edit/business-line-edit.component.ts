import { Component, OnInit, Inject, inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatTabsModule } from '@angular/material/tabs';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { BusinessLineEditService, BusinessLineEdit, BusinessLineVar, BusinessLineCover, BusinessLineFile } from '../../shared';
import {JsonPipe} from '@angular/common';
import { VarsService } from '../../shared/services/vars.service';
import { AuthService } from '../../shared/services/auth.service';

@Component({
    selector: 'app-business-line-edit',
    imports: [
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatTableModule,
    MatSelectModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTabsModule,
    MatPaginatorModule
],
    templateUrl: './business-line-edit.component.html',
    styleUrls: ['./business-line-edit.component.scss']
})
export class BusinessLineEditComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private svc = inject(BusinessLineEditService);
  private snack = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private authService = inject(AuthService);
  varService = inject(VarsService);

  businessLine: BusinessLineEdit = { id:-1,mpCode: '', mpName: '', mpVars: [], mpCovers: [], mpFiles: [], mpPhType: '', mpInsObjectType: '' };
  originalBusinessLine: BusinessLineEdit | null = null;
  isNewRecord = true;
  hasChanges = false;

  varDisplayedColumns: string[] = ['varCode', 'varType', 'varPath', 'varName', 'varDataType', 'varActions'];
  coverDisplayedColumns: string[] = ['coverCode', 'coverName', 'risks', 'coverActions'];
  fileDisplayedColumns: string[] = ['fileCode', 'fileName', 'fileActions'];
  policyHolderDisplayedColumns: string[] = ['category', 'field', 'varName', 'varCode', 'actions'];
  policyInsObjectDisplayedColumns: string[] = ['category', 'field', 'varName', 'varCode', 'actions'];
  textVarDisplayedColumns: string[] = ['varCode', 'varName', 'varValue', 'textVarActions'];

//  varSearchText = '';
//  coverSearchText = '';
  exampleJsonText = '';

//  get fullVarsCollection(): BusinessLineVar[] {
//    if (!this.businessLine) { return [] ; }
//    return this.varService.getPhDefVars (this.businessLine.mpPhType).concat(this.varService.getIoDefVars(this.businessLine.mpInsObjectType));
//  }

//  get filteredCovers(): BusinessLineCover[] {
//    const s = this.coverSearchText.trim().toLowerCase();
//    if (!s) return this.businessLine.mpCovers;
//    return this.businessLine.mpCovers.filter(c => c.coverCode.toLowerCase().includes(s) || c.coverName.toLowerCase().includes(s) || c.risks.toLowerCase().includes(s));
//  }

  get paginatedVars(): BusinessLineVar[] {
    return this.businessLine.mpVars.filter(v => v.varType == 'MAGIC' || v.varType == 'VAR' || v.varType == 'CONST');
  }

  get paginatedCovers(): BusinessLineCover[] {
    return this.businessLine.mpCovers;
  }

  get policyHolderVars(): any[] {
    // Получаем категории страхователя в зависимости от типа
    
    //this.businessLine.mpVars = this.businessLine.mpVars.map(v => this.varService.enrichVar(v));

    const categories = this.varService.getPhCategories(this.businessLine.mpPhType) || [];
    let result: any[] = [];
    let prevCategory = '';
    
    for (const category of categories) {
      // Only keep variables whose varCdm fits category
      const matchedVars = this.businessLine.mpVars
        .filter(v => v.varCdm && v.varCdm.startsWith(`policyHolder.${category}.`))
        .sort((a, b) => (a.varNr ?? 0) - (b.varNr ?? 0))
        .map(v => {
          let cat = '';
          if (category !== prevCategory) {
            cat = category;
            prevCategory = category;
          }
          return {
            category: cat,
            field: v.varPath.split('.').slice(2).join('.'),
            varName: v.varName,
            varCode: v.varCode,
            original: v
          };
        });
      result = result.concat(matchedVars);
    }
    return result;
  }

  get policyInsObjectVars(): any[] {
    //this.businessLine.mpVars = this.businessLine.mpVars.map(v => this.varService.enrichVar(v));

    const categories = this.varService.getIoCategories(this.businessLine.mpInsObjectType) || [];
    
    let result: any[] = [];
    let prevCategory = '';
    
    //console.log('this.businessLine.mpVars', this.businessLine.mpVars);

    for (const category of categories) {
      //console.log('categories', category);
      // Only keep variables whose varCdm fits category
      const matchedVars = this.businessLine.mpVars
        .filter(v => v.varCdm && v.varCdm.startsWith(`insuredObject.${category}.`))
        .sort((a, b) => (a.varNr ?? 0) - (b.varNr ?? 0))
        .map(v => {
          let cat = '';
          if (category !== prevCategory) {
            cat = category;
            prevCategory = category;
          }
          console.log('v', v);
          return {
            category: cat,
            field: v.varPath.split('.').slice(2).join('.'),
            varName: v.varName,
            varCode: v.varCode,
            original: v
          };
        });
      result = result.concat(matchedVars);
    }
    return result;
  }

  ngOnInit(): void {
    const code = this.route.snapshot.paramMap.get('mpCode');
    this.varService.getAllVars();
    
    if (code) {
      this.isNewRecord = false;
      this.svc.getBusinessLineByCode(code).subscribe(doc => {
        if (!doc) { this.router.navigate(['/', this.authService.tenant, 'business-line']); return; }
        
        // Normalize mpFiles property names (filename -> fileName)
        const normalizedFiles = (doc.mpFiles || []).map((file: any) => ({
          fileCode: file.fileCode,
          fileName: file.fileName || file.filename || ''
        }));

        this.businessLine = {
          ...doc,
          mpPhType: doc.mpPhType || '',
          mpInsObjectType: doc.mpInsObjectType || '',
          mpFiles: normalizedFiles
        };
        this.originalBusinessLine = {
          ...doc,
          mpPhType: doc.mpPhType || '',
          mpInsObjectType: doc.mpInsObjectType || '',
          mpFiles: normalizedFiles
        };
        this.updateChanges();
      });
    } else {
      this.isNewRecord = true;
      this.businessLine.mpVars = this.varService.getAllVars();
      this.updateChanges();
    }
  }

  updateChanges(): void {
    this.hasChanges = !this.originalBusinessLine || JSON.stringify(this.businessLine) !== JSON.stringify(this.originalBusinessLine);
  }

  phTypeChanged(): void {
    
    // Remove all vars whose varPath starts with 'policyHolder.' from mpVars
    if (Array.isArray(this.businessLine.mpVars)) {
      this.businessLine.mpVars = this.businessLine.mpVars.filter(
        v => !(v.varCdm.startsWith('policyHolder'))
      );
    }    
    let newVars: any[] =[];
    newVars = this.varService.getPhDefVars(this.businessLine.mpPhType);

    this.businessLine.mpVars = [...this.businessLine.mpVars, ...newVars];
    this.updateChanges();


  }

  ioChanged(): void {

    // Remove all vars whose varPath starts with 'insuredObject.' from mpVars
    if (Array.isArray(this.businessLine.mpVars)) {
      this.businessLine.mpVars = this.businessLine.mpVars.filter(
        v => !(v.varCdm.startsWith('insuredObject'))
      );
    }
    let newVars: any[] = [];
    newVars = this.varService.getIoDefVars(this.businessLine.mpInsObjectType);

    this.businessLine.mpVars = [...this.businessLine.mpVars, ...newVars];
    this.updateChanges();
  }

  onFieldChange(): void {
    this.updateChanges();
  }

  save(): void {
    if (!this.businessLine.mpCode || !this.businessLine.mpName) {
      this.snack.open('Заполните mpCode и mpName', 'OK', { duration: 2500 });
      return;
    }
    const dataToSave = {
      ...this.businessLine,
      mpPhType: this.businessLine.mpPhType || '',
      mpInsObjectType: this.businessLine.mpInsObjectType || ''
    };
    this.svc.saveBusinessLine(dataToSave).subscribe(saved => {
      // Normalize mpFiles property names (filename -> fileName)
      const normalizedFiles = (saved.mpFiles || []).map((file: any) => ({
        fileCode: file.fileCode,
        fileName: file.fileName || ''
      }));

      this.businessLine = {
        ...saved,
        mpPhType: saved.mpPhType || '',
        mpInsObjectType: saved.mpInsObjectType || '',
        mpFiles: normalizedFiles
      };
      this.originalBusinessLine = {
        ...saved,
        mpPhType: saved.mpPhType || '',
        mpInsObjectType: saved.mpInsObjectType || '',
        mpFiles: normalizedFiles
      };
      this.isNewRecord = false;
      this.updateChanges();
      this.snack.open('Сохранено', 'OK', { duration: 2000 });
    });
  }

  addVar(): void {
    this.openVarDialog({ varCode: '', varType: 'IN', varPath: '', varName: '', varDataType: 'STRING' }, (res) => {
      if (!res) return;
        const model: BusinessLineVar = { 
          varCode: res.varCode  , 
          varType: res.varType, 
          varPath: res.varPath, 
          varName: res.varName, 
          varDataType: res.varDataType, 
          varValue: '', 
          varNr: 0, 
          varCdm: res.varPath };
        this.businessLine.mpVars = [...this.businessLine.mpVars, model];
        this.updateChanges();

    });
  }
/*
  addAllVars(): void {
    let newVars: any[] =[];
  // Берём массив phPersonVars из сервиса, копируем уникальные varPath в this.businessLine.mpVars
  if (this.businessLine.mpPhType === 'person') {
    newVars = newVars.concat(this.svc.phPersonVars).
      concat(this.svc.phPhoneVars).
      concat(this.svc.phEmailVars).
      concat(this.svc.phPersonOrganizationVars).
      concat(this.svc.phPersonDocumentVars).
      concat(this.svc.phAddressVars);
  }
  if (this.businessLine.mpPhType === 'organization') {
    newVars = newVars.concat(this.svc.phOrganizationVars).
      concat(this.svc.phOrganizationDocumentVars).
      concat(this.svc.phPhoneVars).
      concat(this.svc.phEmailVars).
      concat(this.svc.phAddressVars);
  }
  const existingVarPaths = new Set(this.businessLine.mpVars.map(v => v.varPath));
  const uniqueVarsToAdd = newVars.filter(v => !existingVarPaths.has(v.varPath));
  this.businessLine.mpVars = [...this.businessLine.mpVars, ...uniqueVarsToAdd];
  this.updateChanges();

  }
*/
  editVar(v: BusinessLineVar): void {
    this.openVarDialog({ ...v }, (res) => {
      if (!res) return;
      if (this.isNewRecord) {
        const index = this.businessLine.mpVars.findIndex(x => x.varCode === v.varCode);
        if (index !== -1) {
          this.businessLine.mpVars = [
            ...this.businessLine.mpVars.slice(0, index),
            { ...v, ...res },
            ...this.businessLine.mpVars.slice(index + 1)
          ];
        }
        this.updateChanges();
      } else {
        this.svc.updateVar(this.businessLine.mpCode, v.varCode, res).subscribe(updated => {
          if (updated) {
            const index = this.businessLine.mpVars.findIndex(x => x.varCode === v.varCode);
            if (index !== -1) {
              this.businessLine.mpVars = [
                ...this.businessLine.mpVars.slice(0, index),
                updated,
                ...this.businessLine.mpVars.slice(index + 1)
              ];
            }
          }
          this.updateChanges();
        });
      }
    });
  }

  deleteVar(v: BusinessLineVar): void {
    //this.openConfirm('Удалить переменную?', () => {
      if (this.isNewRecord) {
        this.businessLine.mpVars = this.businessLine.mpVars.filter(x => x.varCode !== v.varCode);
        this.updateChanges();
      } else {
        this.svc.deleteVar(this.businessLine.mpCode, v.varCode).subscribe(() => {
          this.businessLine.mpVars = this.businessLine.mpVars.filter(x => x.varCode !== v.varCode);
          this.updateChanges();
        });
      }
    //});
  }

  addCover(): void {
    this.openCoverDialog({ coverCode: '', coverName: '', risks: '' }, (res) => {
      if (!res) return;
        const model: BusinessLineCover = { coverCode: res.coverCode, coverName: res.coverName, risks: res.risks };
        this.businessLine.mpCovers = [...this.businessLine.mpCovers, model];
        this.updateChanges();

    });
  }

  editCover(c: BusinessLineCover): void {
    this.openCoverDialog({ ...c }, (res) => {
      if (!res) return;
      if (this.isNewRecord) {
        const index = this.businessLine.mpCovers.findIndex(x => x.coverCode === c.coverCode);
        if (index !== -1) {
          this.businessLine.mpCovers = [
            ...this.businessLine.mpCovers.slice(0, index),
            { ...c, ...res },
            ...this.businessLine.mpCovers.slice(index + 1)
          ];
        }
        this.updateChanges();
      } else {
        this.svc.updateCover(this.businessLine.mpCode, c.coverCode, res).subscribe(updated => {
          if (updated) {
            const index = this.businessLine.mpCovers.findIndex(x => x.coverCode === c.coverCode);
            if (index !== -1) {
              this.businessLine.mpCovers = [
                ...this.businessLine.mpCovers.slice(0, index),
                updated,
                ...this.businessLine.mpCovers.slice(index + 1)
              ];
            }
          }
          this.updateChanges();
        });
      }
    });
  }

  deleteCover(c: BusinessLineCover): void {
    this.openConfirm('Удалить покрытие?', () => {
      if (this.isNewRecord) {
        this.businessLine.mpCovers = this.businessLine.mpCovers.filter(x => x.coverCode !== c.coverCode);
        this.updateChanges();
      } else {
        this.svc.deleteCover(this.businessLine.mpCode, c.coverCode).subscribe(() => {
          this.businessLine.mpCovers = this.businessLine.mpCovers.filter(x => x.coverCode !== c.coverCode);
          this.updateChanges();
        });
      }
    });

  }

  // JSON File operations
  saveToFile(): void {
    const dataStr = JSON.stringify(this.businessLine, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `business-line_${this.businessLine.mpCode || 'new'}_${Date.now()}.json`;
    link.click();
    URL.revokeObjectURL(url);
    this.snack.open('JSON файл сохранен', 'Закрыть', { duration: 2000 });
  }

  loadFromFile(): void {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json';
    input.onchange = (event: any) => {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e: any) => {
          try {
            const jsonData = JSON.parse(e.target.result);
            jsonData.id = -1;
            this.businessLine = { ...this.businessLine, ...jsonData };
            this.updateChanges();
            this.snack.open('JSON файл загружен успешно', 'Закрыть', { duration: 2000 });
          } catch (error) {
            console.error('Error parsing JSON:', error);
            this.snack.open('Ошибка при загрузке JSON файла', 'Закрыть', { duration: 3000 });
          }
        };
        reader.readAsText(file);
      }
    };
    input.click();
  }

  openVarDialog(data: any, cb: (res?: any)=>void) {
    const ref = this.dialog.open(VarEditDialog, { data });
    ref.afterClosed().subscribe(cb);
  }

  openCoverDialog(data: any, cb: (res?: any)=>void) {
    const ref = this.dialog.open(CoverEditDialog, { data });
    ref.afterClosed().subscribe(cb);
  }

  openConfirm(message: string, onYes: ()=>void) {
    const ref = this.dialog.open(ConfirmDialog, { data: { message } });
    ref.afterClosed().subscribe((yes) => { if (yes) onYes(); });
  }

  onGetExample(): void {
    this.exampleJsonText = JSON.stringify("{ val1: val1, val2: val2, val3: val3 }", null, 2);
  }

  showPolicyHolderJson(): void {
    const lobCode = this.businessLine.mpCode;

    if (!lobCode) {
      this.snack.open('Please set mpCode first', 'Close', { duration: 2000 });
      return;
    }

    this.svc.getExampleJson(lobCode).subscribe(exampleData => {
      this.dialog.open(JsonViewDialog, {
        data: { title: 'PolicyHolder JSON', object: exampleData },
        width: '800px',
        maxHeight: '80vh'
      });
    });
  }

  addPolicyHolderVar(): void {
    const ref = this.dialog.open(AddPolicyHolderVarDialog, {
      width: '500px'
    });

    ref.afterClosed().subscribe(result => {
      if (result && result.varCode && result.varName) {
        // Calculate next varNr
        const maxVarNr = this.businessLine.mpVars.reduce((max, v) => {
          return Math.max(max, v.varNr ?? 0);
        }, 0);

        // Create new variable with policyHolder prefix
        const newVar: BusinessLineVar = {
          varCode: 'ph_' + result.varCode,
          varName: result.varName,
          varPath: `policyHolder.additionalProperties.${result.varCode}`,
          varType: 'IN',
          varDataType: 'STRING',
          varValue: '',
          varNr: maxVarNr + 1,
          varCdm: 'policyHolder.additionalProperties.' + result.varCode
        };

        this.businessLine.mpVars = [...this.businessLine.mpVars, newVar];
        this.updateChanges();
        this.snack.open('Variable added successfully', 'Close', { duration: 2000 });
      }
    });
  }

  addInsObjectVar(): void {
    const ref = this.dialog.open(AddInsObjectVarDialog, {
      width: '500px'
    });

    ref.afterClosed().subscribe(result => {
      if (result && result.varCode && result.varName) {
        // Calculate next varNr
        const maxVarNr = this.businessLine.mpVars.reduce((max, v) => {
          return Math.max(max, v.varNr ?? 0);
        }, 0);

        // Create new variable with insuredObject prefix
        const newVar: BusinessLineVar = {
          varCode: 'io_' + result.varCode,
          varName: result.varName,
          varPath: `insuredObject.additionalProperties.${result.varCode}`,
          varType: 'IN',
          varDataType: 'STRING',
          varValue: '',
          varNr: maxVarNr + 1,
          varCdm: ''
        };

        this.businessLine.mpVars = [...this.businessLine.mpVars, newVar];
        this.updateChanges();
        this.snack.open('Variable added successfully', 'Close', { duration: 2000 });
      }
    });
  }

  showInsObjectJson(): void {
    const lobCode = this.businessLine.mpCode;

    if (!lobCode) {
      this.snack.open('Please set mpCode first', 'Close', { duration: 2000 });
      return;
    }

    this.svc.getExampleJson(lobCode).subscribe(exampleData => {
      this.dialog.open(JsonViewDialog, {
        data: { title: 'Insured Object JSON', object: exampleData },
        width: '800px',
        maxHeight: '80vh'
      });
    });
  }

  get files(): BusinessLineFile[] {
    return this.businessLine.mpFiles || [];
  }

  get textVars(): BusinessLineVar[] {
    return (this.businessLine.mpVars || [])
      .filter(v => v.varType === 'TEXT')
      .sort((a, b) => (a.varCdm || '').localeCompare(b.varCdm || ''));
  }

  addFile(): void {
    this.openFileDialog({ fileCode: '', fileName: '' }, (res) => {
      if (!res) return;
      
      // Ensure fileCode is lowercase
      res.fileCode = res.fileCode.toLowerCase();
      
      // Validate fileCode uniqueness
      const existingFileCodes = this.files.map(f => f.fileCode);
      if (existingFileCodes.includes(res.fileCode)) {
        this.snack.open('fileCode должен быть уникальным', 'OK', { duration: 2500 });
        return;
      }
      
      const model: BusinessLineFile = { fileCode: res.fileCode, fileName: res.fileName };
      this.businessLine.mpFiles = [...(this.businessLine.mpFiles || []), model];
      this.updateChanges();
    });
  }

  editFile(f: BusinessLineFile): void {
    this.openFileDialog({ ...f, isEdit: true }, (res) => {
      if (!res) return;
      
      // Ensure fileCode is lowercase (though it shouldn't change in edit mode)
      res.fileCode = res.fileCode.toLowerCase();
      
      // Validate fileCode uniqueness (excluding current file)
      const existingFileCodes = this.files
        .filter(file => file.fileCode !== f.fileCode)
        .map(file => file.fileCode);
      if (existingFileCodes.includes(res.fileCode)) {
        this.snack.open('fileCode должен быть уникальным', 'OK', { duration: 2500 });
        return;
      }
      
      const index = this.files.findIndex(x => x.fileCode === f.fileCode);
      if (index !== -1) {
        this.businessLine.mpFiles = [
          ...this.files.slice(0, index),
          { ...f, fileName: res.fileName },
          ...this.files.slice(index + 1)
        ];
        this.updateChanges();
      }
    });
  }

  deleteFile(f: BusinessLineFile): void {
    this.openConfirm('Удалить файл?', () => {
      this.businessLine.mpFiles = this.files.filter(x => x.fileCode !== f.fileCode);
      this.updateChanges();
    });
  }

  openFileDialog(data: any, cb: (res?: any)=>void) {
    const ref = this.dialog.open(FileEditDialog, { data });
    ref.afterClosed().subscribe(cb);
  }

  addTextVar(): void {
    this.openTextVarDialog({ code: '', name: '', value: '' }, (res) => {
      if (!res) return;
      
      // Check unique varCode
      const existingCodes = this.businessLine.mpVars.map(v => v.varCode);
      if (existingCodes.includes(res.code)) {
        this.snack.open('varCode должен быть уникальным', 'OK', { duration: 2500 });
        return;
      }
      
      const newVar: BusinessLineVar = {
        varDataType: 'STRING',
        varCode: res.code,
        varName: res.name,
        varPath: '',
        varType: 'TEXT',
        varValue: res.value,
        varCdm: 'strings.' + res.code,
        varNr: (this.businessLine.mpVars.length || 0) + 1000
      };
      
      this.businessLine.mpVars = [...this.businessLine.mpVars, newVar];
      this.updateChanges();
    });
  }

  editTextVar(v: BusinessLineVar): void {
    this.openTextVarDialog({ 
      code: v.varCode, 
      name: v.varName, 
      value: v.varValue || '',
      isEdit: true,
      originalVarCode: v.varCode
    }, (res) => {
      if (!res) return;
      
      // Check unique varCode (excluding current)
      if (res.code !== res.originalVarCode) {
        const existingCodes = this.businessLine.mpVars
          .filter(v => v.varCode !== res.originalVarCode)
          .map(v => v.varCode);
        if (existingCodes.includes(res.code)) {
          this.snack.open('varCode должен быть уникальным', 'OK', { duration: 2500 });
          return;
        }
      }
      
      const index = this.businessLine.mpVars.findIndex(x => x.varCode === res.originalVarCode);
      if (index !== -1) {
        this.businessLine.mpVars = [
          ...this.businessLine.mpVars.slice(0, index),
          {
            ...v,
            varCode: res.code,
            varName: res.name,
            varValue: res.value,
            varCdm: 'strings.' + res.code
          },
          ...this.businessLine.mpVars.slice(index + 1)
        ];
        this.updateChanges();
      }
    });
  }

  deleteTextVar(v: BusinessLineVar): void {
    this.openConfirm('Удалить строку?', () => {
      this.businessLine.mpVars = this.businessLine.mpVars.filter(x => x.varCode !== v.varCode);
      this.updateChanges();
    });
  }

  openTextVarDialog(data: any, cb: (res?: any)=>void) {
    const ref = this.dialog.open(TextVarEditDialog, { data });
    ref.afterClosed().subscribe(cb);
  }

}

@Component({
    selector: 'app-var-edit-dialog',
    imports: [FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
    template: `
  <h2 mat-dialog-title>Переменная</h2>
  <div mat-dialog-content>
    <mat-form-field appearance="outline" style="min-width: 400px;">
      <mat-label>varCode</mat-label>
      <input matInput [(ngModel)]="model.varCode">
    </mat-form-field>
    <mat-form-field appearance="outline" style="min-width: 400px;">
      <mat-label>varType</mat-label>
      <mat-select [(ngModel)]="model.varType">
        <mat-option value="IN">IN</mat-option>
        <mat-option value="VAR">VAR</mat-option>
        <mat-option value="CONST">OUT</mat-option>
        <mat-option value="MAGIC">MAGIC</mat-option>
      </mat-select>
    </mat-form-field>
    <mat-form-field appearance="outline" style="min-width: 400px;">
      <mat-label>varPath</mat-label>
      <input matInput [(ngModel)]="model.varPath">
    </mat-form-field>
    <mat-form-field appearance="outline" style="min-width: 400px;">
      <mat-label>varName</mat-label>
      <input matInput [(ngModel)]="model.varName">
    </mat-form-field>
    <mat-form-field appearance="outline" style="min-width: 400px;">
      <mat-label>varDataType</mat-label>
      <mat-select [(ngModel)]="model.varDataType">
        <mat-option value="STRING">STRING</mat-option>
        <mat-option value="NUMBER">NUMBER</mat-option>
        <mat-option value="DATE">DATE</mat-option>
        <mat-option value="PERIOD">PERIOD</mat-option>
      </mat-select>
    </mat-form-field>
  </div>
  <div mat-dialog-actions align="end">
    <button mat-button mat-dialog-close>Отмена</button>
    <button mat-raised-button color="primary" [mat-dialog-close]="model">OK</button>
  </div>
  `
})
export class VarEditDialog {
  model: any;
  constructor(@Inject(MAT_DIALOG_DATA) data: any) { this.model = { ...data }; }
}

@Component({
    selector: 'app-cover-edit-dialog',
    imports: [FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
    template: `
  <h2 mat-dialog-title>Покрытие</h2>
  <div mat-dialog-content>
    <mat-form-field appearance="outline" style="min-width: 400px;">
      <mat-label>coverCode</mat-label>
      <input matInput [(ngModel)]="model.coverCode">
    </mat-form-field>
    <mat-form-field appearance="outline" style="min-width: 400px;">
      <mat-label>coverName</mat-label>
      <input matInput [(ngModel)]="model.coverName">
    </mat-form-field>
    <mat-form-field appearance="outline" style="min-width: 400px;">
      <mat-label>risks</mat-label>
      <input matInput [(ngModel)]="model.risks">
    </mat-form-field>
  </div>
  <div mat-dialog-actions align="end">
    <button mat-button mat-dialog-close>Отмена</button>
    <button mat-raised-button color="primary" [mat-dialog-close]="model">OK</button>
  </div>
  `
})
export class CoverEditDialog {
  model: any;
  constructor(@Inject(MAT_DIALOG_DATA) data: any) { this.model = { ...data }; }
}

@Component({
    selector: 'app-confirm-dialog',
    imports: [MatDialogModule, MatButtonModule],
    template: `
  <h2 mat-dialog-title>Подтверждение</h2>
  <div mat-dialog-content>{{data.message}}</div>
  <div mat-dialog-actions align="end">
    <button mat-button [mat-dialog-close]="false">Нет</button>
    <button mat-raised-button color="warn" [mat-dialog-close]="true">Да</button>
  </div>
  `
})
export class ConfirmDialog {
  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {}
}

@Component({
    selector: 'app-json-view-dialog',
    imports: [MatDialogModule, MatButtonModule, MatIconModule, JsonPipe],
    template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <div mat-dialog-content style="max-height: 60vh; overflow: auto;">
      <pre style="background-color: #f5f5f5; padding: 16px; border-radius: 4px; overflow-x: auto;">{{ data.object | json }}</pre>
    </div>
    <div mat-dialog-actions align="end">
      <button mat-button (click)="copyToClipboard()">
        <mat-icon>content_copy</mat-icon>
        Copy
      </button>
      <button mat-raised-button color="primary" mat-dialog-close>Close</button>
    </div>
  `,
    styles: [`
    pre {
      margin: 0;
      font-family: 'Courier New', Courier, monospace;
      font-size: 13px;
      line-height: 1.5;
      white-space: pre-wrap;
      word-wrap: break-word;
    }
  `]
})
export class JsonViewDialog {
  constructor(@Inject(MAT_DIALOG_DATA) public data: { title: string; object: any }) {}

  copyToClipboard(): void {
    const jsonString = JSON.stringify(this.data.object, null, 2);
    navigator.clipboard.writeText(jsonString).then(() => {
      // Could show a snackbar notification here if needed
    });
  }
}

@Component({
    selector: 'app-add-policy-holder-var-dialog',
    imports: [FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
    template: `
    <h2 mat-dialog-title>Add PolicyHolder Variable</h2>
    <div mat-dialog-content>
      <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
        <mat-label>varCode</mat-label>
        <input matInput [(ngModel)]="varCode" placeholder="e.g., person.firstName">
      </mat-form-field>

      <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
        <mat-label>varName</mat-label>
        <input matInput [(ngModel)]="varName" placeholder="e.g., First Name">
      </mat-form-field>

      <p style="font-size: 12px; color: #666; margin: 0;">
        Variable path will be: <strong>policyHolder.additionalProperties.{{ varCode || '...' }}</strong>
      </p>
    </div>
    <div mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-raised-button
              color="primary"
              [mat-dialog-close]="{varCode: varCode, varName: varName}"
              [disabled]="!varCode || !varName">
        OK
      </button>
    </div>
  `
})
export class AddPolicyHolderVarDialog {
  varCode = '';
  varName = '';
}

@Component({
    selector: 'app-add-ins-object-var-dialog',
    imports: [FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
    template: `
    <h2 mat-dialog-title>Add Insured Object Variable</h2>
    <div mat-dialog-content>
      <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
        <mat-label>varCode</mat-label>
        <input matInput [(ngModel)]="varCode" placeholder="e.g., device.serialNumber">
      </mat-form-field>

      <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
        <mat-label>varName</mat-label>
        <input matInput [(ngModel)]="varName" placeholder="e.g., Serial Number">
      </mat-form-field>

      <p style="font-size: 12px; color: #666; margin: 0;">
        Variable path will be: <strong>insuredObject.additionalProperties.{{ varCode || '...' }}</strong>
      </p>
    </div>
    <div mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-raised-button
              color="primary"
              [mat-dialog-close]="{varCode: varCode, varName: varName}"
              [disabled]="!varCode || !varName">
        OK
      </button>
    </div>
  `
})
export class AddInsObjectVarDialog {
  varCode = '';
  varName = '';
}

@Component({
    selector: 'app-file-edit-dialog',
    imports: [FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
    template: `
  <h2 mat-dialog-title>{{ isEdit ? 'Редактировать файл' : 'Добавить файл' }}</h2>
  <div mat-dialog-content>
    <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
      <mat-label>fileCode</mat-label>
      <input matInput [(ngModel)]="model.fileCode" [readonly]="isEdit" (ngModelChange)="onFileCodeChange($event)">
      @if (hasUpperCase) {
        <mat-hint style="color: #f44336;">fileCode должен содержать только строчные буквы</mat-hint>
      }
    </mat-form-field>
    <mat-form-field appearance="outline" style="width: 100%;">
      <mat-label>fileName</mat-label>
      <input matInput [(ngModel)]="model.fileName">
    </mat-form-field>
  </div>
  <div mat-dialog-actions align="end">
    <button mat-button mat-dialog-close>Отмена</button>
    <button mat-raised-button color="primary" [mat-dialog-close]="model" [disabled]="!isValid()">Сохранить</button>
  </div>
  `
})
export class FileEditDialog {
  model: any;
  isEdit: boolean = false;
  hasUpperCase: boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) data: any) { 
    this.isEdit = data.isEdit || false;
    this.model = { ...data };
    delete this.model.isEdit;
    // Convert existing fileCode to lowercase if present
    if (this.model.fileCode) {
      this.model.fileCode = this.model.fileCode.toLowerCase();
    }
  }

  onFileCodeChange(value: string): void {
    if (value) {
      // Convert to lowercase
      const lowerValue = value.toLowerCase();
      this.model.fileCode = lowerValue;
      // Check if original had uppercase
      this.hasUpperCase = value !== lowerValue;
    } else {
      this.hasUpperCase = false;
    }
  }

  isValid(): boolean {
    if (!this.model.fileCode || !this.model.fileName) {
      return false;
    }
    // Check if fileCode contains only lowercase letters, numbers, and underscores
    const lowerCasePattern = /^[a-z0-9_]+$/;
    return lowerCasePattern.test(this.model.fileCode);
  }
}

@Component({
    selector: 'app-text-var-edit-dialog',
    imports: [FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
    template: `
  <h2 mat-dialog-title>{{ isEdit ? 'Редактировать строку' : 'Добавить строку' }}</h2>
  <div mat-dialog-content>
    <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
      <mat-label>code</mat-label>
      <input matInput [(ngModel)]="model.code" [readonly]="isEdit">
    </mat-form-field>
    <mat-form-field appearance="outline" style="width: 100%; margin-bottom: 16px;">
      <mat-label>name</mat-label>
      <input matInput [(ngModel)]="model.name">
    </mat-form-field>
    <mat-form-field appearance="outline" style="width: 100%;">
      <mat-label>value</mat-label>
      <textarea matInput [(ngModel)]="model.value" rows="3"></textarea>
    </mat-form-field>
  </div>
  <div mat-dialog-actions align="end">
    <button mat-button mat-dialog-close>Отмена</button>
    <button mat-raised-button color="primary" [mat-dialog-close]="model" [disabled]="!isValid()">Сохранить</button>
  </div>
  `
})
export class TextVarEditDialog {
  model: any;
  isEdit: boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) data: any) { 
    this.isEdit = data.isEdit || false;
    this.model = { ...data };
  }

  isValid(): boolean {
    return !!(this.model.code && this.model.name);
  }
}
