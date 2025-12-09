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
import { BusinessLineEditService, BusinessLineEdit, BusinessLineVar, BusinessLineCover } from '../../shared';
import {JsonPipe} from '@angular/common';

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

  businessLine: BusinessLineEdit = { id:-1,mpCode: '', mpName: '', mpVars: [], mpCovers: [], mpPhType: '', mpInsObjectType: '' };
  originalBusinessLine: BusinessLineEdit | null = null;
  isNewRecord = true;
  hasChanges = false;

  varDisplayedColumns: string[] = ['varCode', 'varType', 'varPath', 'varName', 'varDataType', 'varActions'];
  coverDisplayedColumns: string[] = ['coverCode', 'coverName', 'risks', 'coverActions'];
  policyHolderDisplayedColumns: string[] = ['category', 'field', 'varName', 'varCode', 'actions'];
  policyInsObjectDisplayedColumns: string[] = ['category', 'field', 'varName', 'varCode', 'actions'];

  varSearchText = '';
  coverSearchText = '';
  exampleJsonText = '';


  get filteredCovers(): BusinessLineCover[] {
    const s = this.coverSearchText.trim().toLowerCase();
    if (!s) return this.businessLine.mpCovers;
    return this.businessLine.mpCovers.filter(c => c.coverCode.toLowerCase().includes(s) || c.coverName.toLowerCase().includes(s) || c.risks.toLowerCase().includes(s));
  }

  get paginatedVars(): BusinessLineVar[] {
    return this.businessLine.mpVars.filter(v => v.varType == 'MAGIC' || v.varType == 'VAR' || v.varType == 'CONST');
  }

  get paginatedCovers(): BusinessLineCover[] {
    //const startIndex = this.coverPageIndex * this.coverPageSize;
    //return this.filteredCovers.slice(startIndex, startIndex + this.coverPageSize);
    return this.filteredCovers;
  }

  get policyHolderVars(): any[] {
    // Filter vars that start with 'policyHolder.'
    const phVars = this.businessLine.mpVars.filter(v => v.varPath.startsWith('policyHolder.'));

    // Sort by varNr; varNr exists on BusinessLineVar interface, which is the type of phVars elements
    // If varNr is null or does not exist, call findVarNrByVarPath and set varNr with value
    phVars.forEach(v => {
      if (v.varNr == null) {
        const foundNr = this.svc.findVarNrByVarPath(v.varPath);
        if (foundNr != null) {
          v.varNr = foundNr;
        }
      }
    });
    const sorted = phVars.sort((a, b) => (a.varNr ?? 0) - (b.varNr ?? 0));
    let prevCategory = '';

    // Transform data to extract category and field
    return sorted.map(v => {
      const pathAfterPh = v.varPath.substring('policyHolder.'.length);
      let category = 'Policy Holder';
      let field = pathAfterPh;

      if (v.varPath.startsWith('policyHolder.person.')) {
        category = 'person';
        field = v.varPath.substring('policyHolder.person.'.length);
      } else if (v.varPath.startsWith('policyHolder.phone.')) {
        category = 'phone';
        field = v.varPath.substring('policyHolder.phone.'.length);
      } else if (v.varPath.startsWith('policyHolder.passport.')) {
        category = 'passport';
        field = v.varPath.substring('policyHolder.passport.'.length);
      } else if (v.varPath.startsWith('policyHolder.address.')) {
        category = 'address';
        field = v.varPath.substring('policyHolder.address.'.length);
      } else if (v.varPath.startsWith('policyHolder.organization.')) {
        category = 'organization';
        field = v.varPath.substring('policyHolder.organization.'.length);
      } else if (v.varPath.startsWith('policyHolder.document.')) {
        category = 'document';
        field = v.varPath.substring('policyHolder.document.'.length);
      } else if (v.varPath.startsWith('policyHolder.additionalProperties.')) {
        category = 'additionalProperties';
        field = v.varPath.substring('policyHolder.additionalProperties.'.length);
      }

      if (category === prevCategory) {
        category = '';
      } else {
        prevCategory = category;
      }

      return {
        category,
        field,
        varName: v.varName,
        varCode: v.varCode,
        original: v
      };
    });
  }

  get policyInsObjectVars(): any[] {
    // Filter vars that start with 'insuredObject.'
    const phVars = this.businessLine.mpVars.filter(v => v.varPath.startsWith('insuredObject.'));

    // Sort by varNr; varNr exists on BusinessLineVar interface, which is the type of phVars elements
    // If varNr is null or does not exist, call findVarNrByVarPath and set varNr with value
    phVars.forEach(v => {
      if (v.varNr == null) {
        const foundNr = this.svc.findVarNrByVarPath(v.varPath);
        if (foundNr != null) {
          v.varNr = foundNr;
        }
      }
    });
    const sorted = phVars.sort((a, b) => (a.varNr ?? 0) - (b.varNr ?? 0));
    let prevCategory = '';

    // Transform data to extract category and field
    return sorted.map(v => {
      const pathAfterPh = v.varPath.substring('insuredObject.'.length);
      let category = 'insuredObject';
      let field = pathAfterPh;

      if (v.varPath.startsWith('insuredObject.person.')) {
        category = 'person';
        field = v.varPath.substring('insuredObject.person.'.length);
      } else if (v.varPath.startsWith('insuredObject.phone.')) {
        category = 'phone';
        field = v.varPath.substring('insuredObject.device.'.length);
      } else if (v.varPath.startsWith('insuredObject.device.')) {
        category = 'device';
        field = v.varPath.substring('insuredObject.device.'.length);
      } else if (v.varPath.startsWith('insuredObject.address.')) {
        category = 'address';
        field = v.varPath.substring('insuredObject.address.'.length);
      } else if (v.varPath.startsWith('policyHolder.organization.')) {
        category = 'organization';
        field = v.varPath.substring('policyHolder.organization.'.length);
      } else if (v.varPath.startsWith('insuredObject.document.')) {
        category = 'document';
        field = v.varPath.substring('insuredObject.document.'.length);
      } else if (v.varPath.startsWith('insuredObject.additionalProperties.')) {
        category = 'additionalProperties';
        field = v.varPath.substring('insuredObject.additionalProperties.'.length);
      }

      if (category === prevCategory) {
        category = '';
      } else {
        prevCategory = category;
      }

      return {
        category,
        field,
        varName: v.varName,
        varCode: v.varCode,
        original: v
      };
    });
  }



  ngOnInit(): void {
    const code = this.route.snapshot.paramMap.get('mpCode');
    console.log('code', code);
    if (code) {
      this.isNewRecord = false;
      this.svc.getBusinessLineByCode(code).subscribe(doc => {
        if (!doc) { this.router.navigate(['/business-line']); return; }
        this.businessLine = {
          ...doc,
          mpPhType: doc.mpPhType || '',
          mpInsObjectType: doc.mpInsObjectType || ''
        };
        this.originalBusinessLine = {
          ...doc,
          mpPhType: doc.mpPhType || '',
          mpInsObjectType: doc.mpInsObjectType || ''
        };
        this.updateChanges();
      });
    } else {
      this.isNewRecord = true;
      this.businessLine.mpVars = this.svc.policyVars.concat(this.svc.policyMagicVars);
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
        v => !(v.varCode.startsWith('ph_'))
      );
    }
    
    let newVars: any[] =[];
    // Берём массив phPersonVars из сервиса, копируем уникальные varPath в this.businessLine.mpVars
    if (this.businessLine.mpPhType === 'person') {
      newVars = newVars.concat(this.svc.phPersonVars).
        concat(this.svc.phPhoneVars).
        concat(this.svc.phEmailVars).
        concat(this.svc.phPersonOrganizationVars).
        concat(this.svc.phPersonDocumentVars).
        concat(this.svc.phAddressVars).
        concat(this.svc.phPersonMagicVars);
      
    }
    if (this.businessLine.mpPhType === 'organization') {
      newVars = newVars.concat(this.svc.phOrganizationVars).
        concat(this.svc.phOrganizationDocumentVars).
        concat(this.svc.phPhoneVars).
        concat(this.svc.phEmailVars).
        concat(this.svc.phAddressVars);
    }

    this.businessLine.mpVars = [...this.businessLine.mpVars, ...newVars];
    this.updateChanges();


  }

  ioChanged(): void {

    // Remove all vars whose varPath starts with 'insuredObject.' from mpVars
    if (Array.isArray(this.businessLine.mpVars)) {
      this.businessLine.mpVars = this.businessLine.mpVars.filter(
        v => !(v.varPath && v.varCode.startsWith('io_'))
      );
    }
    let newVars: any[] = [];

    // For 'person' type, transform policyHolder vars to insuredObject vars
    if (this.businessLine.mpInsObjectType === 'person') {
      const phVars = [
        ...this.svc.phPersonVars,
        ...this.svc.phPhoneVars,
        ...this.svc.phEmailVars,
        ...this.svc.phPersonOrganizationVars,
        ...this.svc.phPersonDocumentVars,
        ...this.svc.phAddressVars,
        ...this.svc.ioPersonMagicVars
      ];

      // Transform varPath from policyHolder.* to insuredObject.* and update varCode prefix
      newVars = phVars.map(v => ({
        ...v,
        varPath: v.varPath.replace('policyHolder.', 'insuredObject.'),
        varCode: v.varCode.replace('ph_', 'io_'),
        varNr: v.varNr ? v.varNr + 500 : 0 // Offset varNr to avoid conflicts
      }));
    }

    if (this.businessLine.mpInsObjectType === 'device') {
      newVars = newVars.concat(this.svc.ioDeviceVars);
    }

    if (this.businessLine.mpInsObjectType === 'property') {
      newVars = newVars.concat(this.svc.ioPropertyVars);
    }

    const existingVarPaths = new Set(this.businessLine.mpVars.map(v => v.varPath));
    const uniqueVarsToAdd = newVars.filter(v => !existingVarPaths.has(v.varPath));
    this.businessLine.mpVars = [...this.businessLine.mpVars, ...uniqueVarsToAdd];
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
      this.businessLine = {
        ...saved,
        mpPhType: saved.mpPhType || '',
        mpInsObjectType: saved.mpInsObjectType || ''
      };
      this.originalBusinessLine = {
        ...saved,
        mpPhType: saved.mpPhType || '',
        mpInsObjectType: saved.mpInsObjectType || ''
      };
      this.isNewRecord = false;
      this.updateChanges();
      this.snack.open('Сохранено', 'OK', { duration: 2000 });
    });
  }

  addVar(): void {
    this.openVarDialog({ varCode: '', varType: 'IN', varPath: '', varName: '', varDataType: 'STRING' }, (res) => {
      if (!res) return;
//      if (this.isNewRecord) {
        const model: BusinessLineVar = { varCode: res.varCode  , varType: res.varType, varPath: res.varPath, varName: res.varName, varDataType: res.varDataType, varValue: '', varNr: 0 };
        this.businessLine.mpVars = [...this.businessLine.mpVars, model];
        this.updateChanges();
//      } else {
//        this.svc.addVar(this.businessLine.mpCode, { varType: res.varType, varPath: res.varPath, varName: res.varName, varDataType: res.varDataType }).subscribe(created => {
//          this.businessLine.mpVars = [...this.businessLine.mpVars, created];
//          this.updateChanges();
//        });
//      }
    });
  }

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

  addCoverVars(code: string): void {
// add var to mpVars if not exists - co_+code+_premium, co_+code+_sumInsured, co_+code+_deductibleNr
  const newVars: any[] = [];
  newVars.push({ varCode: 'co_' + code + '_premium', varType: 'VAR', 
    varPath: '$..covers[?(@.cover.code == "' + code + '")].premium', 
    varName: 'Премия по покрытию ' + code, varDataType: 'NUMBER' });
  newVars.push({ varCode: 'co_' + code + '_sumInsured', varType: 'VAR', 
    varPath: '$..covers[?(@.cover.code == "' + code + '")].sumInsured', 
    varName: 'Сумма страхования по покрытию ' + code, varDataType: 'NUMBER' });
  newVars.push({ varCode: 'co_' + code + '_deductibleNr', varType: 'VAR', 
    varPath: '', 
    varName: 'Id франшизы по покрытию ' + code, varDataType: 'NUMBER' });
    newVars.push({ varCode: 'co_' + code + '_deductible', varType: 'VAR', 
      varPath: '$..covers[?(@.cover.code == "' + code + '")].deductible', 
      varName: 'Франшиза по покрытию ' + code, varDataType: 'NUMBER' });
    this.businessLine.mpVars = [...this.businessLine.mpVars, ...newVars];
  }

  deleteCoverVars(code: string): void {
    this.businessLine.mpVars = this.businessLine.mpVars.filter(v => v.varCode !== 'co_' + code);
  }

  addCover(): void {
    this.openCoverDialog({ coverCode: '', coverName: '', risks: '' }, (res) => {
      if (!res) return;
        const model: BusinessLineCover = { coverCode: res.coverCode, coverName: res.coverName, risks: res.risks };
        this.businessLine.mpCovers = [...this.businessLine.mpCovers, model];
        this.addCoverVars(res.coverCode);
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
        this.deleteCoverVars(c.coverCode);
        this.updateChanges();
      } else {
        this.svc.deleteCover(this.businessLine.mpCode, c.coverCode).subscribe(() => {
          this.businessLine.mpCovers = this.businessLine.mpCovers.filter(x => x.coverCode !== c.coverCode);
          this.deleteCoverVars(c.coverCode);
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
          varNr: maxVarNr + 1
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
          varNr: maxVarNr + 1
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
    <mat-form-field appearance="outline" style="min-width: 200px;">
      <mat-label>coverCode</mat-label>
      <input matInput [(ngModel)]="model.coverCode">
    </mat-form-field>
    <mat-form-field appearance="outline" style="min-width: 200px;">
      <mat-label>coverName</mat-label>
      <input matInput [(ngModel)]="model.coverName">
    </mat-form-field>
    <mat-form-field appearance="outline" style="min-width: 200px;">
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
