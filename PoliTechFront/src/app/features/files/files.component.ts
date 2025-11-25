import { Component, OnInit, ViewChild } from '@angular/core';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { FilesService, FileTemplate } from '../../shared/services/files.service';
import { ProductsService, ProductList } from '../../shared/services/products.service';
import { AddFileDialogComponent } from './add-file-dialog/add-file-dialog.component';
import { DeleteConfirmDialogComponent } from './delete-confirm-dialog/delete-confirm-dialog.component';
import { ProcessFileDialogComponent } from './process-file-dialog/process-file-dialog.component';

@Component({
    selector: 'app-files',
    imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatInputModule,
    MatFormFieldModule,
    MatDialogModule,
    MatTooltipModule,
    MatSnackBarModule,
    FormsModule,
    ReactiveFormsModule
],
    templateUrl: './files.component.html',
    styleUrls: ['./files.component.scss']
})
export class FilesComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = ['id', 'productCode', 'packageCode', 'fileType', 'fileDescription', 'actions'];
  dataSource: FileTemplate[] = [];
  filteredData: FileTemplate[] = [];
  searchTerm: string = '';
  products: ProductList[] = [];

  constructor(
    private filesService: FilesService,
    private productsService: ProductsService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadFiles();
    this.loadProducts();
  }

  loadFiles(): void {
    this.filesService.getFiles().subscribe(files => {
      this.dataSource = files;
      this.filteredData = files;
      this.updatePaginator();
    });
  }

  loadProducts(): void {
    this.productsService.getProducts().subscribe(products => {
      this.products = products;
    });
  }

  onSearch(): void {
    if (!this.searchTerm.trim()) {
      this.filteredData = this.dataSource;
    } else {
      this.filteredData = this.dataSource.filter(file =>
        file.productCode.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        file.fileType.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        file.fileDescription.toLowerCase().includes(this.searchTerm.toLowerCase())
      );
    }
    this.updatePaginator();
  }

  updatePaginator(): void {
    if (this.paginator) {
      this.paginator.firstPage();
    }
  }

  openAddFileDialog(): void {
    const dialogRef = this.dialog.open(AddFileDialogComponent, {
      width: '500px',
      data: { products: this.products }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadFiles();
        this.snackBar.open('Файл успешно добавлен', 'Закрыть', { duration: 3000 });
      }
    });
  }

  openDeleteDialog(file: FileTemplate): void {
    const dialogRef = this.dialog.open(DeleteConfirmDialogComponent, {
      width: '400px',
      data: { fileName: file.fileDescription }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && file.id) {
        this.filesService.deleteFile(file.id).subscribe(() => {
          this.loadFiles();
          this.snackBar.open('Файл успешно удален', 'Закрыть', { duration: 3000 });
        });
      }
    });
  }

  openProcessDialog(file: FileTemplate): void {
    const dialogRef = this.dialog.open(ProcessFileDialogComponent, {
      width: '500px',
      data: { fileId: file.id }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && file.id) {
        this.filesService.processFile(file.id, result).subscribe(blob => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `processed_${file.fileName || 'file'}`;
          a.click();
          window.URL.revokeObjectURL(url);
          this.snackBar.open('Файл обработан и скачан', 'Закрыть', { duration: 3000 });
        });
      }
    });
  }
}
