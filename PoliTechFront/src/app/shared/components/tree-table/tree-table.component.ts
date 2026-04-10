import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ContentChild,
  EventEmitter,
  Input,
  Output,
  TemplateRef,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule, type MatSlideToggleChange } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { isObjectVarTypeRow, type TreeTableFlatRow, type TreeTableSourceRow } from '../../models/tree-table.models';
import { TreeTableRowEditDialogComponent } from './tree-table-row-edit-dialog.component';

/** Черновик дочернего узла после диалога «Создать»; id задаёт родитель. */
export interface TreeTableChildCreatePayload<T extends TreeTableSourceRow = TreeTableSourceRow> {
  parentId: number;
  draft: T;
}

/**
 * `T` — тип строки: задайте свой интерфейс как `extends TreeTableSourceRow` и типизируйте массив `[rows]` в родителе.
 */
@Component({
  selector: 'app-tree-table',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatSlideToggleModule,
    MatTooltipModule,
  ],
  templateUrl: './tree-table.component.html',
  styleUrl: './tree-table.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TreeTableComponent<T extends TreeTableSourceRow = TreeTableSourceRow> {
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly dialog = inject(MatDialog);

  /** Исходные строки (плоский список с parent_id) */
  @Input({ required: true }) set rows(value: T[] | null | undefined) {
    this._rows = value ?? [];
    this.rebuildFlat();
  }
  get rows(): T[] {
    return this._rows;
  }
  private _rows: T[] = [];

  /** Колонки таблицы (иконка дерева встроена в колонку `name`) */
  @Input() displayedColumns: string[] = ['name', 'code', 'actions'];

  /** Отступ на каждый уровень вложенности (в пикселях) */
  @Input() indentPerLevel = 20;
  
  /** Включить анимацию при раскрытии/сворачивании */
  @Input() enableAnimation = true;
  
  /** Разрешить множественное раскрытие (если false - раскрывается только один узел) */
  @Input() multipleExpanded = true;

  /**
   * Если true, строки с {@link TreeTableSourceRow.isDeleted} участвуют в дереве (иначе скрываются).
   */
  @Input() set showDeleted(value: boolean) {
    this._showDeleted = !!value;
    this.childrenCache.clear();
    this.rebuildFlat();
    this.cdr.markForCheck();
  }
  get showDeleted(): boolean {
    return this._showDeleted;
  }
  private _showDeleted = false;

  /**
   * Для строк с {@link TreeTableSourceRow.varType} ≠ OBJECT по клику открывается диалог редактирования метаданных.
   */
  @Input() enableVarEditDialog = true;

  /** Скрыть встроенную кнопку мягкого удаления (например, только просмотр или PROD). */
  @Input() disableBuiltInSoftDelete = false;

  /** Событие при раскрытии/сворачивании узла */
  @Output() expandedChange = new EventEmitter<{ node: T; expanded: boolean }>();
  
  /** Событие при клике на строку */
  @Output() rowClick = new EventEmitter<T>();

  /** После сохранения в диалоге редактирования переменной (только если {@link enableVarEditDialog} и не OBJECT) */
  @Output() rowVarUpdated = new EventEmitter<T>();

  /** После сохранения в диалоге создания дочернего узла (id в {@code draft} игнорируйте — задайте в родителе) */
  @Output() rowChildCreate = new EventEmitter<TreeTableChildCreatePayload<T>>();

  /** После {@link markRowDeleted}: строка помечена {@link TreeTableSourceRow.isDeleted} и скрыта из дерева */
  @Output() rowSoftDeleted = new EventEmitter<T>();

  @ContentChild('actionsCell') actionsCellTpl?: TemplateRef<{ $implicit: T; row: TreeTableFlatRow<T> }>;
  @ContentChild('nameCell') nameCellTpl?: TemplateRef<{ $implicit: T; row: TreeTableFlatRow<T> }>;

  dataSource = new MatTableDataSource<TreeTableFlatRow<T>>([]);

  /** id родителей, у которых раскрыты дочерние узлы */
  private readonly expandedIds = new Set<number>();
  
  /** Кэш детей для оптимизации */
  private childrenCache = new Map<string, T[]>();

  /**
   * Переключение состояния раскрытия узла
   */
  toggle(row: TreeTableFlatRow<T>): void {
    if (!row.expandable) {
      return;
    }
    
    const id = row.data.id;
    const wasExpanded = this.expandedIds.has(id);
    
    if (!this.multipleExpanded && !wasExpanded) {
      // Закрываем все остальные
      this.expandedIds.clear();
    }
    
    if (wasExpanded) {
      this.expandedIds.delete(id);
      // Опционально: закрываем всех потомков
      this.closeDescendants(id);
    } else {
      this.expandedIds.add(id);
    }
    
    this.rebuildFlat();
    this.expandedChange.emit({ node: row.data, expanded: !wasExpanded });
    this.cdr.markForCheck();
  }

  /**
   * Закрыть все дочерние узлы
   */
  private closeDescendants(parentId: number): void {
    const toClose: number[] = [];
    for (const id of this.expandedIds) {
      if (this.isDescendant(id, parentId)) {
        toClose.push(id);
      }
    }
    toClose.forEach(id => this.expandedIds.delete(id));
  }
  
  /**
   * Проверка, является ли nodeId потомком parentId
   */
  private isDescendant(nodeId: number, parentId: number): boolean {
    const node = this._rows.find(r => r.id === nodeId);
    if (!node) return false;
    
    let currentParentId = node.parent_id;
    while (currentParentId !== null && currentParentId !== undefined) {
      if (currentParentId === parentId) return true;
      const parent = this._rows.find(r => r.id === currentParentId);
      currentParentId = parent?.parent_id ?? null;
    }
    return false;
  }

  /**
   * Развернуть все узлы
   */
  expandAll(): void {
    const allExpandable = this._rows
      .filter(row => {
        const children = this.childrenCache.get(String(row.id));
        return children && children.length > 0;
      })
      .map(row => row.id);
    
    allExpandable.forEach(id => this.expandedIds.add(id));
    this.rebuildFlat();
    this.cdr.markForCheck();
  }

  /**
   * Свернуть все узлы
   */
  collapseAll(): void {
    this.expandedIds.clear();
    this.rebuildFlat();
    this.cdr.markForCheck();
  }

  /**
   * Развернуть до определенного уровня
   */
  expandToLevel(maxLevel: number): void {
    const flat = this.buildFlatList();
    for (const row of flat) {
      if (row.level < maxLevel && row.expandable) {
        this.expandedIds.add(row.data.id);
      } else if (row.level >= maxLevel) {
        this.expandedIds.delete(row.data.id);
      }
    }
    this.rebuildFlat();
    this.cdr.markForCheck();
  }

  /**
   * Получить список всех выбранных узлов (если есть поле selected)
   */
  getSelectedNodes(): T[] {
    return this._rows.filter(row => (row as any).selected === true);
  }

  isExpanded(row: TreeTableFlatRow<T>): boolean {
    return this.expandedIds.has(row.data.id);
  }


  trackById = (_: number, row: TreeTableFlatRow<T>) => row.data.id;

  /** Для шаблона: переключатели не показываем на узлах OBJECT */
  isObjectVarTypeRow = isObjectVarTypeRow;


  onBuiltInDeleteClick(node: T, event: MouseEvent): void {
    event.stopPropagation();
    if (node.isSystem === true) {
      return;
    }
    this.markRowDeleted(node);
  }

  /** Диалог редактирования строки (не для узлов OBJECT и не для isDeleted). */
  openEditRowDialog(row: T): void {
    if (!this.enableVarEditDialog || isObjectVarTypeRow(row) || row.isDeleted) {
      return;
    }
    const ref = this.dialog.open(TreeTableRowEditDialogComponent, {
      width: '520px',
      data: {
        mode: 'edit',
        row: { ...row },
        parentId: row.parent_id,
        codeReadonly: row.isSystem === true,
      },
    });
    ref.afterClosed().subscribe((res?: TreeTableSourceRow) => {
      if (!res) return;
      Object.assign(row, {
        varCode: res.varCode,
        varName: res.varName,
        varType: res.varType,
        varDataType: res.varDataType,
        varList: res.varList,
        varPath: res.varPath,
        varValue: res.varValue,
        name: (res.name?.trim() || res.varName) ?? '',
      });
      this.rowVarUpdated.emit(row);
      this.cdr.markForCheck();
    });
  }

  /** Диалог создания дочернего узла. */
  openCreateChildDialog(parent: T): void {
    if (!this.enableVarEditDialog || parent.isDeleted) {
      return;
    }
    const empty: TreeTableSourceRow = {
      id: 0,
      parent_id: parent.id,
      varNr: 0,
      varName: '',
      varCode: '',
      varPath: '',
      varValue: '',
      varCdm: '',
      varType: 'STRING',
      varDataType: 'STRING',
      name: '',
    };
    const ref = this.dialog.open(TreeTableRowEditDialogComponent, {
      width: '520px',
      data: { mode: 'create', row: empty, parentId: parent.id },
    });
    ref.afterClosed().subscribe((res?: TreeTableSourceRow) => {
      if (!res) return;
      this.rowChildCreate.emit({ parentId: parent.id, draft: res as T });
      this.cdr.markForCheck();
    });
  }

  /** Мягкое удаление: {@code isDeleted = true}, строка и её поддерево исчезают из таблицы */
  markRowDeleted(node: T): void {
    node.isDeleted = true;
    this.expandedIds.delete(node.id);
    this.markDescendantsDeleted(node.id);
    this.closeDescendants(node.id);
    this.rebuildFlat();
    this.rowSoftDeleted.emit(node);
    this.cdr.markForCheck();
  }

  /** Пометить {@code isDeleted} у всех прямых и косвенных потомков {@code parentId}. */
  private markDescendantsDeleted(parentId: number): void {
    for (const row of this._rows) {
      if (row.parent_id === parentId) {
        row.isDeleted = true;
        this.expandedIds.delete(row.id);
        this.markDescendantsDeleted(row.id);
      }
    }
  }

  /**
   * Обновление данных с сохранением состояния раскрытия
   */
  refreshData(newRows: T[]): void {
    this._rows = newRows;
    this.childrenCache.clear();
    this.rebuildFlat();
  }

  private rebuildFlat(): void {
    this.childrenCache.clear();
    const childrenByParent = this.buildChildrenMap(this._rows);
    const flat: TreeTableFlatRow<T>[] = [];
    this.walk(childrenByParent, null, 0, flat);
    this.dataSource.data = flat;
  }

  /**
   * Построение плоского списка (без учета раскрытия) для внутренних нужд
   */
  private buildFlatList(): TreeTableFlatRow<T>[] {
    const childrenByParent = this.buildChildrenMap(this._rows);
    const flat: TreeTableFlatRow<T>[] = [];
    const walkFlat = (parentKey: string | null, level: number): void => {
      const key = parentKey ?? '__root__';
      const children = childrenByParent.get(key) ?? [];
      for (const node of children) {
        const childList = childrenByParent.get(String(node.id)) ?? [];
        flat.push({
          data: node,
          level,
          expandable: childList.length > 0,
          expanded: this.expandedIds.has(node.id),
        });
        walkFlat(String(node.id), level + 1);
      }
    };
    walkFlat(null, 0);
    return flat;
  }

  /**
   * Дети одного родителя отсортированы по varNr.
   * parentKey: null — корневой уровень (parent_id null / undefined / 0 при отсутствии родителя в данных)
   */
  private buildChildrenMap(rows: T[]): Map<string, T[]> {
    const byParent = new Map<string, T[]>();
    const idSet = new Set(rows.map((r) => r.id));

    for (const row of rows) {
      if (!this._showDeleted && row.isDeleted) {
        continue;
      }
      const key = this.parentKey(row.parent_id, idSet);
      const list = byParent.get(key) ?? [];
      list.push(row);
      byParent.set(key, list);
      
      // Кэшируем детей
      if (row.parent_id !== null && row.parent_id !== undefined && idSet.has(row.parent_id)) {
        const cacheKey = String(row.parent_id);
        const cached = this.childrenCache.get(cacheKey) ?? [];
        cached.push(row);
        this.childrenCache.set(cacheKey, cached);
      }
    }
    
    for (const [, list] of byParent) {
      list.sort((a, b) => (a.varNr ?? 0) - (b.varNr ?? 0));
    }
    return byParent;
  }

  /** Ключ группы детей: строковый, чтобы отличить null от 0 */
  private parentKey(parentId: number | null | undefined, idSet: Set<number>): string {
    if (parentId === null || parentId === undefined) {
      return '__root__';
    }
    if (!idSet.has(parentId)) {
      return '__root__';
    }
    return String(parentId);
  }

  private walk(
    childrenByParent: Map<string, T[]>,
    parentKey: string | null,
    level: number,
    out: TreeTableFlatRow<T>[],
  ): void {
    const key = parentKey ?? '__root__';
    const children = childrenByParent.get(key) ?? [];
    for (const node of children) {
      const childList = childrenByParent.get(String(node.id)) ?? [];
      const expandable = childList.length > 0;
      const expanded = this.expandedIds.has(node.id);
      out.push({
        data: node,
        level,
        expandable,
        expanded,
      });
      if (expandable && expanded) {
        this.walk(childrenByParent, String(node.id), level + 1, out);
      }
    }
  }
}