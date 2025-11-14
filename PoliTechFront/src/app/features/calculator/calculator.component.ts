import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatBadgeModule } from '@angular/material/badge';

import { CalculatorService, Calculator, CalculatorVar, CalculatorFormula, FormulaLine, CalculatorCoefficient, CoefficientColumn, CoefficientDataRow } from '../../shared/services/calculator.service';
import { VarDialogComponent } from './var-dialog/var-dialog.component';
import { FormulaDialogComponent } from './formula-dialog/formula-dialog.component';
import { LineDialogComponent } from './line-dialog/line-dialog.component';
import { CoefficientDialogComponent } from './coefficient-dialog/coefficient-dialog.component';
import { ColumnDialogComponent } from './column-dialog/column-dialog.component';

import * as XLSX from 'xlsx';

@Component({
  selector: 'app-calculator',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTabsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTableModule,
    MatPaginatorModule,
    MatDialogModule,
    MatBadgeModule
  ],
  templateUrl: './calculator.component.html',
  styleUrls: ['./calculator.component.scss']
})
export class CalculatorComponent implements OnInit {
  calculator: Calculator = {
    productId: '',
    productCode: '',
    versionNo: '',
    packageNo: '',
    vars: [],
    formulas: [],
    coefficients: []
  };

  hasChanges = false;

  // Dropdown options
  varTypeOptions: string[] = [];
  conditionOperatorOptions: string[] = [];
  expressionOperatorOptions: string[] = [];
  postProcessorOptions: string[] = [];
  sortOrderOptions: string[] = [];

  // Vars table
  varsDisplayedColumns = ['varCode', 'varName', 'varPath', 'varType', 'varDataType', 'varValue', 'actions'];
  varsSearchText = '';
  varsPageSize = 10;
  varsPageIndex = 0;
  filteredVars: CalculatorVar[] = [];
  paginatedVars: CalculatorVar[] = [];

  // Formulas table
  formulasDisplayedColumns = ['varCode', 'varName', 'actions'];
  formulasSearchText = '';
  formulasPageSize = 10;
  formulasPageIndex = 0;
  filteredFormulas: CalculatorFormula[] = [];
  paginatedFormulas: CalculatorFormula[] = [];
  selectedFormulaIndex = -1;

  // Formula lines table
  linesDisplayedColumns = ['nr', 'conditionLeft', 'conditionOperator', 'conditionRight', 'expressionResult', 'expressionLeft', 'expressionOperator', 'expressionRight', 'postProcessor', 'actions'];
  linesSearchText = '';
  linesPageSize = 10;
  linesPageIndex = 0;
  filteredLines: FormulaLine[] = [];
  paginatedLines: FormulaLine[] = [];

  // Coefficients table
  coefficientsDisplayedColumns = ['varCode', 'varName', 'actions'];
  coefficientsSearchText = '';
  coefficientsPageSize = 10;
  coefficientsPageIndex = 0;
  filteredCoefficients: CalculatorCoefficient[] = [];
  paginatedCoefficients: CalculatorCoefficient[] = [];
  selectedCoefficientIndex = -1;

  // Coefficient columns table
  columnsDisplayedColumns = ['nr', 'conditionOperator', 'varCode',  'sortOrder', 'varName', 'actions'];
  columnsSearchText = '';
  columnsPageSize = 10;
  columnsPageIndex = 0;
  filteredColumns: CoefficientColumn[] = [];
  paginatedColumns: CoefficientColumn[] = [];
  selectedColumnCoefficientCode: string | null = null;

  // Coefficient data dynamic table
  coefficientDataVisible = false;
  coefficientDataColumns: string[] = ['col1','col2'];
  coefficientDataIndexByNr: Record<number, number> = {};
  coefficientDataColumnsSorted: CoefficientColumn[] = [];
  coefficientDataRows: CoefficientDataRow[] = [];
  filteredCoefficientData: CoefficientDataRow[] = [];
  paginatedCoefficientData: CoefficientDataRow[] = [];
  coefficientDataSearchText = '';
  coefficientDataPageSize = 10;
  coefficientDataPageIndex = 0;
  selectedCoefficientCode: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private calculatorService: CalculatorService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadDropdownOptions();
    
    const productId = this.route.snapshot.paramMap.get('productId');
    const versionNo = this.route.snapshot.paramMap.get('versionNo');
    const packageNo = this.route.snapshot.paramMap.get('packageNo');
    
    if (productId && versionNo && packageNo) {
      this.loadCalculator(productId, versionNo, packageNo);
    }
  }

  loadDropdownOptions(): void {
    this.calculatorService.getVarTypeOptions().subscribe(options => this.varTypeOptions = options);
    this.calculatorService.getConditionOperatorOptions().subscribe(options => this.conditionOperatorOptions = options);
    this.calculatorService.getExpressionOperatorOptions().subscribe(options => this.expressionOperatorOptions = options);
    this.calculatorService.getPostProcessorOptions().subscribe(options => this.postProcessorOptions = options);
    this.calculatorService.getSortOrderOptions().subscribe(options => this.sortOrderOptions = options);
  }

  loadCalculator(productId: string, versionNo: string, packageNo: string): void {
    this.calculatorService.getCalculator(productId, versionNo, packageNo).subscribe({
      next: (calculator) => {
        this.calculator = calculator;
        this.updateTables();
      },
      error: (error) => {
        console.error('Error loading calculator:', error);
        // If GET fails, try POST to create new calculator
        this.calculatorService.createCalculator(productId, versionNo, packageNo).subscribe({
          next: (newCalculator) => {
            this.calculator = newCalculator;
            this.updateTables();
          },
          error: (createError) => {
            console.error('Error creating calculator:', createError);
            this.snackBar.open('Ошибка загрузки калькулятора', 'Закрыть', { duration: 3000 });
          }
        });
      }
    });
  }

  updateChanges(): void {
    this.hasChanges = true;
  }

  save(): void {
    this.calculatorService.updateCalculator(this.calculator).subscribe({
      next: (updatedCalculator) => {
        this.calculator = updatedCalculator;
        this.hasChanges = false;
        this.snackBar.open('Калькулятор сохранен успешно', 'Закрыть', { duration: 3000 });
      },
      error: (error) => {
        console.error('Error saving calculator:', error);
        this.snackBar.open('Ошибка сохранения калькулятора', 'Закрыть', { duration: 3000 });
      }
    });
  }

  saveToFile(): void {
    const dataStr = JSON.stringify(this.calculator, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `calculator_${this.calculator.productCode}_${this.calculator.versionNo}_${this.calculator.packageNo}_${Date.now()}.json`;
    link.click();
    URL.revokeObjectURL(url);
    this.snackBar.open('JSON файл сохранен', 'Закрыть', { duration: 2000 });
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

            jsonData.id = this.calculator.id;
            jsonData.productId = this.calculator.productId;
            jsonData.productCode = this.calculator.productCode;
            jsonData.versionNo = this.calculator.versionNo;
            jsonData.packageNo = this.calculator.packageNo;

            this.calculator = { ...this.calculator, ...jsonData };
            this.updateTables();
            this.updateChanges();
            this.snackBar.open('JSON файл загружен успешно', 'Закрыть', { duration: 2000 });
          } catch (error) {
            console.error('Error parsing JSON:', error);
            this.snackBar.open('Ошибка при загрузке JSON файла', 'Закрыть', { duration: 3000 });
          }
        };
        reader.readAsText(file);
      }
    };
    input.click();
  }

  // Vars methods
  addVar(): void {
    const dialogRef = this.dialog.open(VarDialogComponent, {
      width: '700px',
      minWidth: '700px',
      data: {
        isNew: true,
        varTypeOptions: this.varTypeOptions
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Check if varCode is unique
        const existingVar = this.calculator.vars.find(v => v.varCode === result.varCode);
        if (existingVar) {
          this.snackBar.open('Код переменной должен быть уникальным', 'Закрыть', { duration: 3000 });
          return;
        }
        
        this.calculator.vars.push(result);
        this.updateVarsTable();
        this.updateChanges();
      }
    });
  }

  editVar(varItem: CalculatorVar, index: number): void {
    const dialogRef = this.dialog.open(VarDialogComponent, {
      width: '700px',
      minWidth: '700px',
      data: {
        varItem: varItem,
        isNew: false,
        varTypeOptions: this.varTypeOptions
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Check if varCode is unique (excluding current item)
        const existingVar = this.calculator.vars.find((v, i) => i !== index && v.varCode === result.varCode);
        if (existingVar) {
          this.snackBar.open('Код переменной должен быть уникальным', 'Закрыть', { duration: 3000 });
          return;
        }
        
        this.calculator.vars[index] = result;
        this.updateVarsTable();
        this.updateChanges();
      }
    });
  }

  deleteVar(varItem: CalculatorVar, index: number): void {
    if (confirm('Удалить переменную?')) {
      this.calculator.vars.splice(index, 1);
      this.updateVarsTable();
      this.updateChanges();
    }
  }

  newCalculatorVar(varItem: CalculatorVar): void {
    const existingVar = this.calculator.vars.find(v => v.varCode === varItem.varCode);
        if (existingVar) {
          this.snackBar.open('Код переменной должен быть уникальным', 'Закрыть', { duration: 3000 });
          return;
        }
    this.calculator.vars.push(varItem);
    this.updateVarsTable();
    this.updateChanges();
  }

  updateVarsTable(): void {
    this.filteredVars = this.calculator.vars.filter(item =>
      (item.varCode && item.varCode.toLowerCase().includes(this.varsSearchText.toLowerCase())) ||
      (item.varName && item.varName.toLowerCase().includes(this.varsSearchText.toLowerCase())) ||
      (item.varPath && item.varPath.toLowerCase().includes(this.varsSearchText.toLowerCase()))
    );
    this.updateVarsPagination();
  }

  onVarsPageChange(event: PageEvent): void {
    this.varsPageSize = event.pageSize;
    this.varsPageIndex = event.pageIndex;
    this.updateVarsPagination();
  }

  updateVarsPagination(): void {
    const startIndex = this.varsPageIndex * this.varsPageSize;
    this.paginatedVars = this.filteredVars.slice(startIndex, startIndex + this.varsPageSize);
  }

  // Formulas methods
  addFormula(): void {
    const dialogRef = this.dialog.open(FormulaDialogComponent, {
      width: '600px',
      data: {
        isNew: true,
        vars: this.calculator.vars
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.calculator.formulas.push(result);
        this.updateFormulasTable();
        this.updateChanges();
      }
    });
  }

  editFormula(formula: CalculatorFormula, index: number): void {
    const dialogRef = this.dialog.open(FormulaDialogComponent, {
      width: '600px',
      data: {
        formula: formula,
        isNew: false,
        vars: this.calculator.vars
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.calculator.formulas[index] = result;
        this.updateFormulasTable();
        this.updateChanges();
      }
    });
  }

  deleteFormula(formula: CalculatorFormula, index: number): void {
    if (confirm('Удалить формулу?')) {
      this.calculator.formulas.splice(index, 1);
      this.updateFormulasTable();
      this.updateChanges();
    }
  }

  showLines(formula: CalculatorFormula, index: number): void {
    this.selectedFormulaIndex = index;
    this.updateLinesTable();
  }

  updateFormulasTable(): void {
    this.filteredFormulas = this.calculator.formulas.filter(item =>
      (item.varCode && item.varCode.toLowerCase().includes(this.formulasSearchText.toLowerCase())) ||
      (item.varName && item.varName.toLowerCase().includes(this.formulasSearchText.toLowerCase()))
    );
    this.updateFormulasPagination();
  }

  onFormulasPageChange(event: PageEvent): void {
    this.formulasPageSize = event.pageSize;
    this.formulasPageIndex = event.pageIndex;
    this.updateFormulasPagination();
  }

  updateFormulasPagination(): void {
    const startIndex = this.formulasPageIndex * this.formulasPageSize;
    this.paginatedFormulas = this.filteredFormulas.slice(startIndex, startIndex + this.formulasPageSize);
  }

  // Lines methods
  addLine(): void {
    const dialogRef = this.dialog.open(LineDialogComponent, {
      width: '1000px',
      maxWidth: '1000px',
      data: {
        isNew: true,
        vars: this.calculator.vars,
        conditionOperatorOptions: this.conditionOperatorOptions,
        expressionOperatorOptions: this.expressionOperatorOptions,
        postProcessorOptions: this.postProcessorOptions
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Update var names based on selected var codes
        const updateVarNames = (varRef: any) => {
          const varItem = this.calculator.vars.find(v => v.varCode === varRef.varCode);
          if (varItem) {
            varRef.varName = varItem.varName;
          }
        };
        
        updateVarNames(result.conditionLeft);
        updateVarNames(result.conditionRight);
        updateVarNames(result.expressionResult);
        updateVarNames(result.expressionLeft);
        updateVarNames(result.expressionRight);
        
        this.calculator.formulas[this.selectedFormulaIndex].lines.push(result);
        this.updateLinesTable();
        this.updateChanges();
      }
    });
  }

  editLine(line: FormulaLine, index: number): void {
    const dialogRef = this.dialog.open(LineDialogComponent, {
      width: '1000px',
      maxWidth: '1000px',
      data: {
        line: line,
        isNew: false,
        vars: this.calculator.vars,
        conditionOperatorOptions: this.conditionOperatorOptions,
        expressionOperatorOptions: this.expressionOperatorOptions,
        postProcessorOptions: this.postProcessorOptions
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Update var names based on selected var codes
        const updateVarNames = (varRef: any) => {
          const varItem = this.calculator.vars.find(v => v.varCode === varRef.varCode);
          if (varItem) {
            varRef.varName = varItem.varName;
          }
        };
        
        updateVarNames(result.conditionLeft);
        updateVarNames(result.conditionRight);
        updateVarNames(result.expressionResult);
        updateVarNames(result.expressionLeft);
        updateVarNames(result.expressionRight);
        
        this.calculator.formulas[this.selectedFormulaIndex].lines[index] = result;
        this.updateLinesTable();
        this.updateChanges();
      }
    });
  }

  deleteLine(line: FormulaLine, index: number): void {
    if (confirm('Удалить строку?')) {
      this.calculator.formulas[this.selectedFormulaIndex].lines.splice(index, 1);
      this.updateLinesTable();
      this.updateChanges();
    }
  }

  updateLinesTable(): void {
    if (this.selectedFormulaIndex === -1) {
      this.filteredLines = [];
      this.paginatedLines = [];
      return;
    }

    const lines = this.calculator.formulas[this.selectedFormulaIndex].lines.slice();

    // Sort by nr (as number, fallback to 0 if not a number)
    lines.sort((a, b) => {
      const aNr = parseInt(a.nr, 10);
      const bNr = parseInt(b.nr, 10);
      return (isNaN(aNr) ? 0 : aNr) - (isNaN(bNr) ? 0 : bNr);
    });

    const search = this.linesSearchText.toLowerCase();

    this.filteredLines = lines.filter(item => {
      // nr as string
      const nrStr = (item.nr ?? '').toString().toLowerCase();
      // conditionLeft and expressionResult as varName or varCode if object, or string if string
      let conditionLeftStr = '';
      if (item.conditionLeft) {
        if (
          typeof item.conditionLeft === 'object' &&
          item.conditionLeft !== null &&
          'varName' in item.conditionLeft &&
          typeof (item.conditionLeft as any).varName === 'string'
        ) {
          conditionLeftStr = (item.conditionLeft as any).varName.toLowerCase();
        } else if (typeof item.conditionLeft === 'string') {
          conditionLeftStr = item.conditionLeft.toLowerCase();
        }
      }
      let expressionResultStr = '';
      if (item.expressionResult) {
        if (
          typeof item.expressionResult === 'object' &&
          item.expressionResult !== null &&
          'varName' in item.expressionResult &&
          typeof (item.expressionResult as any).varName === 'string'
        ) {
          expressionResultStr = (item.expressionResult as any).varName.toLowerCase();
        } else if (typeof item.expressionResult === 'string') {
          expressionResultStr = item.expressionResult.toLowerCase();
        }
      }
      return (
        (nrStr && nrStr.includes(search)) ||
        (conditionLeftStr && conditionLeftStr.includes(search)) ||
        (expressionResultStr && expressionResultStr.includes(search))
      );
    });
    this.updateLinesPagination();
  }

  onLinesPageChange(event: PageEvent): void {
    this.linesPageSize = event.pageSize;
    this.linesPageIndex = event.pageIndex;
    this.updateLinesPagination();
  }

  updateLinesPagination(): void {
    const startIndex = this.linesPageIndex * this.linesPageSize;
    this.paginatedLines = this.filteredLines.slice(startIndex, startIndex + this.linesPageSize);
  }

  // Coefficients methods
  addCoefficient(): void {
    const dialogRef = this.dialog.open(CoefficientDialogComponent, {
      width: '600px',
      data: {
        isNew: true,
        vars: this.calculator.vars
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // result.varCode, varName
        const varItem: CalculatorVar = {
          varCode: result.varCode,
          varName: result.varName,
          varPath: '',
          varType: 'COEFFICIENT',
          varValue: '',
          varDataType: 'NUMBER'
        }
        this.newCalculatorVar(varItem);

        this.calculator.coefficients.push(result);
        this.updateCoefficientsTable();
        this.updateChanges();
      }
    });
  }

  editCoefficient(coefficient: CalculatorCoefficient, index: number): void {
    const dialogRef = this.dialog.open(CoefficientDialogComponent, {
      width: '600px',
      data: {
        coefficient: coefficient,
        isNew: false,
        vars: this.calculator.vars
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.calculator.coefficients[index] = result;
        this.updateCoefficientsTable();
        this.updateChanges();
      }
    });
  }

  deleteCoefficient(coefficient: CalculatorCoefficient, index: number): void {
    if (confirm('Удалить коэффициент?')) {
      this.calculator.coefficients.splice(index, 1);
      this.updateCoefficientsTable();
      this.updateChanges();
    }
  }

  showColumns(coefficient: CalculatorCoefficient, index: number): void {
    this.selectedColumnCoefficientCode = coefficient.varCode;
    this.selectedCoefficientIndex = index;
    this.updateColumnsTable();
  }

  updateCoefficientsTable(): void {
    this.filteredCoefficients = this.calculator.coefficients.filter(item =>
      (item.varCode && item.varCode.toLowerCase().includes(this.coefficientsSearchText.toLowerCase())) ||
      (item.varName && item.varName.toLowerCase().includes(this.coefficientsSearchText.toLowerCase()))
    );
    this.updateCoefficientsPagination();
  }

  onCoefficientsPageChange(event: PageEvent): void {
    this.coefficientsPageSize = event.pageSize;
    this.coefficientsPageIndex = event.pageIndex;
    this.updateCoefficientsPagination();
  }

  updateCoefficientsPagination(): void {
    const startIndex = this.coefficientsPageIndex * this.coefficientsPageSize;
    this.paginatedCoefficients = this.filteredCoefficients.slice(startIndex, startIndex + this.coefficientsPageSize);
  }

  // Columns methods
  addColumn(): void {
    const dialogRef = this.dialog.open(ColumnDialogComponent, {
      width: '900px',
      minWidth: '900px',
      data: {
        isNew: true,
        vars: this.calculator.vars,
        conditionOperatorOptions: this.conditionOperatorOptions,
        sortOrderOptions: this.sortOrderOptions
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.calculator.coefficients[this.selectedCoefficientIndex].columns.push(result);
        this.updateColumnsTable();
        this.updateChanges();
      }
    });
  }

  editColumn(column: CoefficientColumn, index: number): void {
    const dialogRef = this.dialog.open(ColumnDialogComponent, {
      width: '900px',
      minWidth: '900px',
      data: {
        column: column,
        isNew: false,
        vars: this.calculator.vars,
        conditionOperatorOptions: this.conditionOperatorOptions,
        sortOrderOptions: this.sortOrderOptions
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.calculator.coefficients[this.selectedCoefficientIndex].columns[index] = result;
        this.updateColumnsTable();
        this.updateChanges();
      }
    });
  }

  deleteColumn(column: CoefficientColumn, index: number): void {
    if (confirm('Удалить колонку?')) {
      this.calculator.coefficients[this.selectedCoefficientIndex].columns.splice(index, 1);
      this.updateColumnsTable();
      this.updateChanges();
    }
  }

  updateColumnsTable(): void {
    if (this.selectedCoefficientIndex === -1) {
      this.filteredColumns = [];
      this.paginatedColumns = [];
      return;
    }

    this.filteredColumns = this.calculator.coefficients[this.selectedCoefficientIndex].columns.filter(item =>
     
      (item.varCode && item.varCode.toLowerCase().includes(this.columnsSearchText.toLowerCase()))
    );
    this.updateColumnsPagination();
  }

  onColumnsPageChange(event: PageEvent): void {
    this.columnsPageSize = event.pageSize;
    this.columnsPageIndex = event.pageIndex;
    this.updateColumnsPagination();
  }

  updateColumnsPagination(): void {
    const startIndex = this.columnsPageIndex * this.columnsPageSize;
    this.paginatedColumns = this.filteredColumns.slice(startIndex, startIndex + this.columnsPageSize);
  }

  updateTables(): void {
    this.updateVarsTable();
    this.updateFormulasTable();
    this.updateLinesTable();
    this.updateCoefficientsTable();
    this.updateColumnsTable();
  }

  // Coefficient data methods
  showCoefficientData(coefficient: CalculatorCoefficient): void {
    const columnsOrdered = [...coefficient.columns].sort((a, b) => parseInt(a.nr) - parseInt(b.nr));
    this.coefficientDataColumnsSorted = columnsOrdered;
    this.coefficientDataColumns = [
      ...columnsOrdered.map((_, idx) => `c:${idx}`),
      'r'
    ];
    this.selectedCoefficientCode = coefficient.varCode;
    this.coefficientDataVisible = true;
    // Reset filters and pagination for fresh view
    this.coefficientDataSearchText = '';
    this.coefficientDataPageIndex = 0;

    if (!this.calculator.id) {
      this.snackBar.open('Calculator ID is missing', 'Закрыть', { duration: 3000 });
      return;
    }

    this.calculatorService.getCoefficientData(this.calculator.id, coefficient.varCode).subscribe(rows => {
      this.coefficientDataRows = rows || [];
      this.updateCoefficientDataTable();
    });
  }

  updateCoefficientDataTable(): void {
    const query = this.coefficientDataSearchText.toLowerCase();
    this.filteredCoefficientData = (this.coefficientDataRows || []).filter(row => {
      if (!query) return true;
      const resultMatch = (row.resultValue?.toString() || '').toLowerCase().includes(query);
      const condMatch = (row.conditionValue || []).some(v => (v ?? '').toString().toLowerCase().includes(query));
      return resultMatch || condMatch;
    });
    this.updateCoefficientDataPagination();
  }

  onCoefficientDataPageChange(event: PageEvent): void {
    this.coefficientDataPageSize = event.pageSize;
    this.coefficientDataPageIndex = event.pageIndex;
    this.updateCoefficientDataPagination();
  }

  updateCoefficientDataPagination(): void {
    const startIndex = this.coefficientDataPageIndex * this.coefficientDataPageSize;
    this.paginatedCoefficientData = this.filteredCoefficientData.slice(startIndex, startIndex + this.coefficientDataPageSize);
  }

  getCoefficientDataHeader(colKey: string): string {
    if (colKey === 'r') return 'Coefficient';
    if (colKey.startsWith('c:')) {
      const idx = Number(colKey.substring(2));
      const col = this.coefficientDataColumnsSorted[idx];
      if (!col) return 'Error';
      const varPart = col.varCode ?? '';
      const opPart = col.conditionOperator ?? '';
      return `${opPart} ${varPart}`.trim();
    }
    return 'Error';
  }

  getCoefficientDataCell(row: CoefficientDataRow, colKey: string): string | number | null {
    if (colKey === 'r') return row.resultValue;
    if (colKey.startsWith('c:')) {
      const idx = Number(colKey.substring(2));
      return row.conditionValue?.[idx] ?? null;
    }
    return null;
  }
  

  exportCoefficientDataCsv(): void {
    const headers: string[] = this.coefficientDataColumns.map(col => this.getCoefficientDataHeader(col));
    const rows: any[][] = [];
    const dataCols = this.coefficientDataColumns;
    for (const row of this.filteredCoefficientData) {
      const rowData = dataCols.map(col => this.getCoefficientDataCell(row, col));
      rows.push(rowData);
    }

    const allData = [headers, ...rows];

    const workbook = XLSX.utils.book_new();
    // Создаем worksheet из данных
    const worksheet: XLSX.WorkSheet = XLSX.utils.aoa_to_sheet(allData);

    XLSX.utils.book_append_sheet(workbook, worksheet, 'Coefficient Data');
    XLSX.writeFile(workbook, `coefficients_${this.selectedCoefficientCode || 'data'}.xlsx`);
    
  }

  importCoefficientDataCsv(): void {
    this.importFromExcel();
    return;
  }


  importFromExcel(): void {
    const headers: string[] = this.coefficientDataColumns.map(col => this.getCoefficientDataHeader(col));

    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.xlsx';
    input.onchange = (event: any) => {
      const file = event.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = (e: any) => {
        const workbook = XLSX.read(e.target.result, { type: 'buffer' });
        const worksheet = workbook.Sheets[workbook.SheetNames[0]];
        const data = XLSX.utils.sheet_to_json(worksheet);

// Конвертируем в двумерный массив
const data1: any[][] = XLSX.utils.sheet_to_json(worksheet, {
  header: 1, // Важно: header: 1 возвращает массив массивов
  defval: true,
  raw: false // Получать форматированные значения
  //dateNF: 'dd.mm.yyyy' // Формат дат
});


// Удаляем первую строку (заголовки)
if (data1.length > 0) {
  data1.shift();
}

// Преобразуем строки в объекты CoefficientDataRow
const rows: CoefficientDataRow[] = data1
  .filter(row => Array.isArray(row) && row.length > 0 && row.some(cell => cell !== undefined && cell !== null && cell !== ''))
  .map((row, idx) => {
    const conditionValue = row.slice(0, row.length - 1);
    const resultValue = row[row.length - 1];
    return {
      id: idx + 1,
      conditionValue: conditionValue,
      resultValue: resultValue
    };
  });

this.coefficientDataRows = rows;


//        this.coefficientDataRows = data as CoefficientDataRow[];
        this.updateCoefficientDataTable();
        this.updateChanges();
      };
      reader.readAsArrayBuffer(file);
    };
    input.click();
  }

  private parseCsvLine(line: string): string[] {
    const result: string[] = [];
    let current = '';
    let inQuotes = false;
    for (let i = 0; i < line.length; i++) {
      const ch = line[i];
      if (inQuotes) {
        if (ch === '"') {
          if (i + 1 < line.length && line[i + 1] === '"') { current += '"'; i++; }
          else { inQuotes = false; }
        } else {
          current += ch;
        }
      } else {
        if (ch === '"') { inQuotes = true; }
        else if (ch === ',') { result.push(current); current = ''; }
        else { current += ch; }
      }
    }
    result.push(current);
    return result.map(s => s.trim());
  }

  saveCoefficientData(): void {
    if (!this.calculator.id || !this.selectedCoefficientCode) return;
    // Use PUT to update full table
    this.calculatorService.updateCoefficientData(this.calculator.id, this.selectedCoefficientCode, this.coefficientDataRows)
      .subscribe(() => this.snackBar.open('Данные коэффициентов сохранены', 'Закрыть', { duration: 2000 }));
  }

  syncVars() {
    throw new Error('Method not implemented.');
    }
    
}
