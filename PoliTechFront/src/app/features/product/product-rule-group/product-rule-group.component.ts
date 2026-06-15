import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import type { Rule, RuleScopeType, RuleType } from '../../../shared/services/api/rules.service';

const SCOPE_ORDER: Record<RuleScopeType, number> = {
  TENANT: 0,
  LOB: 1,
  PRODUCT: 2,
  CLIENT: 3,
};

@Component({
  selector: 'app-product-rule-group',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule, MatIconModule, MatTooltipModule],
  templateUrl: './product-rule-group.component.html',
  styleUrls: ['./product-rule-group.component.scss'],
})
export class ProductRuleGroupComponent implements OnChanges {
  @Input() title = '';
  @Input() ruleType!: RuleType;
  @Input() rules: Rule[] = [];

  @Output() edit = new EventEmitter<Rule>();
  @Output() delete = new EventEmitter<Rule>();
  @Output() add = new EventEmitter<RuleType>();

  displayedColumns = ['code', 'priority', 'name', 'llmText', 'errorText', 'actions'];
  sortedRules: Rule[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['rules']) {
      this.sortedRules = this.sortRules(this.rules ?? []);
    }
  }

  onEdit(rule: Rule, event: Event): void {
    event.stopPropagation();
    this.edit.emit(rule);
  }

  onDelete(rule: Rule, event: Event): void {
    event.stopPropagation();
    this.delete.emit(rule);
  }

  onAdd(): void {
    this.add.emit(this.ruleType);
  }

  private sortRules(rules: Rule[]): Rule[] {
    return [...rules].sort((a, b) => {
      const scopeA = SCOPE_ORDER[a.scopeType] ?? 99;
      const scopeB = SCOPE_ORDER[b.scopeType] ?? 99;
      if (scopeA !== scopeB) {
        return scopeA - scopeB;
      }
      return (a.priority ?? 100) - (b.priority ?? 100);
    });
  }
}
