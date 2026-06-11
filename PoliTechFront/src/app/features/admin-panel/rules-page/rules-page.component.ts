import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import {
  Rule,
  RuleListFilter,
  RulesService,
  RuleScopeType,
  RuleType,
} from '../../../shared/services/api/rules.service';
import { AuthService } from '../../../shared/services/auth.service';

@Component({
  selector: 'app-rules-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatSnackBarModule,
  ],
  templateUrl: './rules-page.component.html',
  styleUrls: ['./rules-page.component.scss'],
})
export class RulesPageComponent implements OnInit {
  private rulesService = inject(RulesService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private auth = inject(AuthService);
  private snack = inject(MatSnackBar);

  displayedColumns = ['code', 'name', 'llmText', 'scopeType', 'scopeCode', 'ruleType', 'priority', 'status', 'actions'];
  rules: Rule[] = [];
  loading = false;

  filter: RuleListFilter = { recordStatus: 'ACTIVE' };

  readonly ruleTypes: RuleType[] = [
    'PRE_QUOTE_VALIDATION',
    'POST_QUOTE_VALIDATION',
    'PRE_SAVE_VALIDATION',
    'POST_SAVE_VALIDATION',
  ];
  readonly scopeTypes: RuleScopeType[] = ['PRODUCT', 'LOB', 'TENANT', 'CLIENT'];

  ngOnInit(): void {
    this.loadRules();
  }

  loadRules(): void {
    this.loading = true;
    this.rulesService.list(this.filter).subscribe({
      next: (rules) => {
        this.rules = rules;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.snack.open('Ошибка загрузки правил', 'OK', { duration: 2500 });
      },
    });
  }

  reloadCache(): void {
    this.rulesService.reloadCache().subscribe({
      next: () => this.snack.open('Кэш правил обновлён', 'OK', { duration: 2000 }),
      error: () => this.snack.open('Не удалось обновить кэш', 'OK', { duration: 2500 }),
    });
  }

  addRule(): void {
    const tenantCode = this.route.snapshot.params['tenantId'] || this.auth.tenant || '';
    this.router.navigate(['/', tenantCode, 'admin', 'rules', 'edit']);
  }

  editRule(rule: Rule, event?: Event): void {
    event?.stopPropagation();
    if (!rule.id) return;
    const tenantCode = this.route.snapshot.params['tenantId'] || this.auth.tenant || '';
    this.router.navigate(['/', tenantCode, 'admin', 'rules', rule.id.toString()]);
  }
}
