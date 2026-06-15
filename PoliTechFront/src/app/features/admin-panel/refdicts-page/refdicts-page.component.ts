import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import {
  RefDataItem,
  RefDict,
  RefDictsService,
} from '../../../shared/services/api/refdicts.service';
import {
  RefDictsNavigatorService,
} from '../../../shared/services/api/refdicts-navigator.service';
import {
  RefDictEntryDialogComponent,
} from './refdict-entry-dialog.component';

type PageMode = 'edit' | 'select';

@Component({
  selector: 'app-refdicts-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSnackBarModule,
    MatTableModule,
    MatDialogModule,
  ],
  templateUrl: './refdicts-page.component.html',
  styleUrls: ['./refdicts-page.component.scss'],
})
export class RefDictsPageComponent implements OnInit {
  private refDictsService = inject(RefDictsService);
  private navigator = inject(RefDictsNavigatorService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snack = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  mode: PageMode = 'edit';
  dicts: RefDict[] = [];
  items: RefDataItem[] = [];
  selectedDictCode: string | null = null;
  loadingDicts = false;
  loadingItems = false;

  dictColumns = ['code', 'name', 'actions'];
  itemColumns = ['code', 'name', 'actions'];
  selectItemColumns = ['select', 'code', 'name'];

  returnUrl = '';
  selectedValues = '';
  private selectedSet = new Set<string>();

  ngOnInit(): void {
    const pathMode = this.route.snapshot.routeConfig?.path;
    this.mode = pathMode === 'select' ? 'select' : 'edit';

    if (this.mode === 'select') {
      this.initSelectMode();
      return;
    }

    this.loadDicts();
  }

  private initSelectMode(): void {
    const dictCode = this.route.snapshot.queryParamMap.get('dictCode')?.trim() ?? '';
    this.returnUrl = this.route.snapshot.queryParamMap.get('returnUrl')?.trim() ?? '';
    const fromQuery = this.route.snapshot.queryParamMap.get('selected')?.trim() ?? '';
    const fromSession = this.navigator.getSessionSelected(dictCode);
    this.selectedValues = fromQuery || fromSession;
    this.selectedSet = new Set(
      this.selectedValues
        .split(',')
        .map((v) => v.trim())
        .filter(Boolean)
    );

    if (!dictCode) {
      this.snack.open('Не указан dictCode', 'OK', { duration: 3000 });
      return;
    }

    this.selectedDictCode = dictCode;
    this.loadItems();
  }

  loadDicts(): void {
    this.loadingDicts = true;
    this.refDictsService.listDicts().subscribe({
      next: (dicts) => {
        this.dicts = dicts ?? [];
        this.loadingDicts = false;
        if (this.mode === 'edit' && this.dicts.length > 0 && !this.selectedDictCode) {
          this.selectDict(this.dicts[0]);
        }
      },
      error: () => {
        this.loadingDicts = false;
        this.snack.open('Ошибка загрузки справочников', 'OK', { duration: 3000 });
      },
    });
  }

  selectDict(dict: RefDict): void {
    this.selectedDictCode = dict.code;
    this.loadItems();
  }

  loadItems(): void {
    if (!this.selectedDictCode) {
      this.items = [];
      return;
    }
    this.loadingItems = true;
    this.refDictsService.listItems(this.selectedDictCode).subscribe({
      next: (items) => {
        this.items = items ?? [];
        this.loadingItems = false;
      },
      error: () => {
        this.loadingItems = false;
        this.snack.open('Ошибка загрузки значений', 'OK', { duration: 3000 });
      },
    });
  }

  isSelected(code: string): boolean {
    return this.selectedSet.has(code);
  }

  toggleSelection(code: string, checked: boolean): void {
    if (checked) {
      this.selectedSet.add(code);
    } else {
      this.selectedSet.delete(code);
    }
    this.selectedValues = [...this.selectedSet].join(',');
    if (this.selectedDictCode) {
      this.navigator.setSessionSelected(this.selectedDictCode, this.selectedValues);
    }
  }

  addDict(): void {
    this.openEntryDialog({
      title: 'Добавить справочник',
      code: '',
      name: '',
      isNew: true,
    }).subscribe((result) => {
      if (!result) return;
      this.refDictsService.createDict(result).subscribe({
        next: () => {
          this.snack.open('Справочник создан', 'OK', { duration: 2000 });
          this.loadDicts();
        },
        error: () => this.snack.open('Ошибка создания справочника', 'OK', { duration: 3000 }),
      });
    });
  }

  editDict(dict: RefDict, event?: Event): void {
    event?.stopPropagation();
    this.openEntryDialog({
      title: 'Редактировать справочник',
      code: dict.code,
      name: dict.name,
      isNew: false,
    }).subscribe((result) => {
      if (!result || (result.code === dict.code && result.name === dict.name)) return;
      this.refDictsService.updateDict(dict.code, result).subscribe({
        next: () => {
          this.snack.open('Справочник обновлён', 'OK', { duration: 2000 });
          if (this.selectedDictCode === dict.code && result.code !== dict.code) {
            this.selectedDictCode = result.code;
          }
          this.loadDicts();
          if (this.selectedDictCode === result.code) {
            this.loadItems();
          }
        },
        error: () => this.snack.open('Ошибка обновления справочника', 'OK', { duration: 3000 }),
      });
    });
  }

  deleteDict(dict: RefDict, event?: Event): void {
    event?.stopPropagation();
    if (!confirm(`Удалить справочник ${dict.code}?`)) return;
    this.refDictsService.deleteDict(dict.code).subscribe({
      next: () => {
        if (this.selectedDictCode === dict.code) {
          this.selectedDictCode = null;
          this.items = [];
        }
        this.snack.open('Справочник удалён', 'OK', { duration: 2000 });
        this.loadDicts();
      },
      error: () => this.snack.open('Ошибка удаления справочника', 'OK', { duration: 3000 }),
    });
  }

  addItem(): void {
    if (!this.selectedDictCode) return;
    this.openEntryDialog({
      title: 'Добавить значение',
      code: '',
      name: '',
      isNew: true,
    }).subscribe((result) => {
      if (!result) return;
      this.refDictsService.createItem(this.selectedDictCode!, result).subscribe({
        next: () => {
          this.snack.open('Значение добавлено', 'OK', { duration: 2000 });
          this.loadItems();
        },
        error: () => this.snack.open('Ошибка добавления значения', 'OK', { duration: 3000 }),
      });
    });
  }

  editItem(item: RefDataItem, event?: Event): void {
    event?.stopPropagation();
    if (!this.selectedDictCode) return;
    this.openEntryDialog({
      title: 'Редактировать значение',
      code: item.code,
      name: item.name,
      isNew: false,
    }).subscribe((result) => {
      if (!result || (result.code === item.code && result.name === item.name)) return;
      this.refDictsService
        .updateItem(this.selectedDictCode!, item.code, result)
        .subscribe({
          next: () => {
            this.snack.open('Значение обновлено', 'OK', { duration: 2000 });
            this.loadItems();
          },
          error: () => this.snack.open('Ошибка обновления значения', 'OK', { duration: 3000 }),
        });
    });
  }

  deleteItem(item: RefDataItem, event?: Event): void {
    event?.stopPropagation();
    if (!this.selectedDictCode) return;
    if (!confirm(`Удалить значение ${item.code}?`)) return;
    this.refDictsService.deleteItem(this.selectedDictCode, item.code).subscribe({
      next: () => {
        this.snack.open('Значение удалено', 'OK', { duration: 2000 });
        this.loadItems();
      },
      error: () => this.snack.open('Ошибка удаления значения', 'OK', { duration: 3000 }),
    });
  }

  confirmSelect(): void {
    this.navigateBack('ok');
  }

  cancelSelect(): void {
    this.navigateBack('cancel');
  }

  private navigateBack(action: 'ok' | 'cancel'): void {
    const dictCode = this.selectedDictCode ?? '';
    const target = this.returnUrl || '/';
    this.router.navigateByUrl(target, {
      state: {
        [RefDictsNavigatorService.RESULT_STATE_KEY]: {
          action,
          dictCode,
          selected: action === 'ok' ? this.selectedValues : '',
        },
      },
    });
  }

  private openEntryDialog(data: {
    title: string;
    code: string;
    name: string;
    isNew: boolean;
  }) {
    return this.dialog
      .open(RefDictEntryDialogComponent, {
        width: '480px',
        data,
      })
      .afterClosed();
  }
}
