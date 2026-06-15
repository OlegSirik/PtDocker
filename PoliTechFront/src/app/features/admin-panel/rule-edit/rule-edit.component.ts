import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import {
  Rule,
  RulesService,
  RuleScopeType,
  RuleType,
} from '../../../shared/services/api/rules.service';
import { LlmAssistResponse, LlmRuleDraft, LlmService } from '../../../shared/services/api/llm.service';
import { ProductsService } from '../../../shared/services/products.service';
import { AuthService } from '../../../shared/services/auth.service';

@Component({
  selector: 'app-rule-edit',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule,
  ],
  templateUrl: './rule-edit.component.html',
  styleUrls: ['./rule-edit.component.scss'],
})
export class RuleEditComponent implements OnInit {
  private rulesService = inject(RulesService);
  private llmService = inject(LlmService);
  private productsService = inject(ProductsService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private auth = inject(AuthService);
  private snack = inject(MatSnackBar);

  rule: Rule = {
    code: '',
    name: '',
    scopeType: 'PRODUCT',
    scopeCode: '',
    ruleType: 'PRE_QUOTE_VALIDATION',
    priority: 100,
    recordStatus: 'ACTIVE',
    expressionLanguage: 'CEL',
    expression: '',
    message: '',
    llmText: '',
  };

  isNew = true;
  saving = false;
  askingLlm = false;
  scopeLocked = false;
  ruleTypeLocked = false;

  /** Set when opened from product page (scopeType=PRODUCT). */
  private contextProductId?: string;
  private contextVersionNo?: string;
  private contextReturnUrl?: string;
  private contextTab?: string;
  private contextRuleType?: RuleType;

  readonly ruleTypes: RuleType[] = [
    'PRE_QUOTE_VALIDATION',
    'POST_QUOTE_VALIDATION',
    'PRE_SAVE_VALIDATION',
    'POST_SAVE_VALIDATION',
    'QUOTE_CALCULATION',
    'UNDERWRITING',
    'WORKFLOW',
    'CROSS_SELL',
    'FRAUD_CHECK',
    'ISSUANCE',
    'RENEWAL',
  ];
  readonly scopeTypes: RuleScopeType[] = ['PRODUCT', 'LOB', 'TENANT', 'CLIENT'];

  ngOnInit(): void {
    this.applyNavigationContext();

    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'edit') {
      this.isNew = false;
      this.rulesService.getById(id).subscribe({
        next: (rule) => {
          this.rule = { ...rule };
          if (this.scopeLocked) {
            this.rule.scopeType = this.route.snapshot.queryParamMap.get('scopeType') as RuleScopeType;
            this.rule.scopeCode = this.route.snapshot.queryParamMap.get('scopeCode') ?? '';
          }
          if (this.ruleTypeLocked && this.contextRuleType) {
            this.rule.ruleType = this.contextRuleType;
          }
        },
        error: () => this.snack.open('Правило не найдено', 'OK', { duration: 2500 }),
      });
    }
  }

  private applyNavigationContext(): void {
    const qp = this.route.snapshot.queryParamMap;
    const scopeType = qp.get('scopeType') as RuleScopeType | null;
    const scopeCode = qp.get('scopeCode')?.trim() ?? '';

    this.contextProductId = qp.get('productId') ?? undefined;
    this.contextVersionNo = qp.get('versionNo') ?? undefined;
    this.contextReturnUrl = qp.get('returnUrl') ?? undefined;
    this.contextTab = qp.get('tab') ?? undefined;

    if (scopeType && scopeCode) {
      this.scopeLocked = true;
      this.rule.scopeType = scopeType;
      this.rule.scopeCode = scopeCode;
    }

    const ruleType = qp.get('ruleType') as RuleType | null;
    if (ruleType && this.ruleTypes.includes(ruleType)) {
      this.ruleTypeLocked = true;
      this.contextRuleType = ruleType;
      this.rule.ruleType = ruleType;
    }
  }

  save(): void {
    this.saving = true;
    const done = () => {
      this.saving = false;
      this.back();
    };
    if (this.isNew) {
      this.rulesService.create(this.rule).subscribe({
        next: () => {
          this.snack.open('Правило создано', 'OK', { duration: 2000 });
          done();
        },
        error: () => {
          this.saving = false;
          this.snack.open('Ошибка сохранения', 'OK', { duration: 2500 });
        },
      });
    } else if (this.rule.id) {
      this.rulesService.update(this.rule.id, this.rule).subscribe({
        next: () => {
          this.snack.open('Правило обновлено', 'OK', { duration: 2000 });
          done();
        },
        error: () => {
          this.saving = false;
          this.snack.open('Ошибка сохранения', 'OK', { duration: 2500 });
        },
      });
    }
  }

  delete(): void {
    if (!this.rule.id || !confirm('Удалить правило?')) return;
    this.rulesService.delete(this.rule.id).subscribe({
      next: () => {
        this.snack.open('Правило удалено', 'OK', { duration: 2000 });
        this.back();
      },
      error: () => this.snack.open('Ошибка удаления', 'OK', { duration: 2500 }),
    });
  }

  askLlm(): void {
    const text = (this.rule.llmText || '').trim();
    if (!text) {
      return;
    }
    if (this.rule.scopeType !== 'PRODUCT' || !this.rule.scopeCode?.trim()) {
      this.snack.open('Укажите scope PRODUCT и код продукта в Scope code', 'OK', { duration: 3500 });
      return;
    }

    this.askingLlm = true;
    const productCode = this.rule.scopeCode.trim();

    this.productsService.getProducts().subscribe({
      next: (products) => {
        const product = products.find((p) => p.code === productCode);
        if (!product?.id) {
          this.askingLlm = false;
          this.snack.open(`Продукт не найден: ${productCode}`, 'OK', { duration: 3500 });
          return;
        }
        const versionNo = product.devVersionNo ?? product.prodVersionNo;
        if (!versionNo) {
          this.askingLlm = false;
          this.snack.open('У продукта нет версии для LLM', 'OK', { duration: 3500 });
          return;
        }
        this.llmService.assist(product.id, versionNo, text, 'RULE').subscribe({
          next: (response) => this.applyLlmResponse(response),
          error: (err) => {
            this.askingLlm = false;
            const msg = err?.error?.message || err?.message || 'Ошибка вызова LLM';
            this.snack.open(msg, 'OK', { duration: 4000 });
          },
        });
      },
      error: () => {
        this.askingLlm = false;
        this.snack.open('Не удалось загрузить список продуктов', 'OK', { duration: 3000 });
      },
    });
  }

  private applyLlmResponse(response: LlmAssistResponse): void {
    this.askingLlm = false;
    if (!response.success) {
      const msg = response.errors?.join('; ') || 'LLM вернул ошибку';
      this.snack.open(msg, 'OK', { duration: 4000 });
      return;
    }
    const draft = response.result as LlmRuleDraft | undefined;
    if (!draft?.condition) {
      this.snack.open('В ответе нет CEL-выражения', 'OK', { duration: 3000 });
      return;
    }
    this.rule.expression = draft.condition;
    if (this.isNew && draft.code && !this.rule.code) {
      this.rule.code = draft.code;
    }
    if (draft.name && !this.rule.name) {
      this.rule.name = draft.name;
    }
    if (draft.message) {
      this.rule.message = draft.message;
    }
    if (response.warnings?.length) {
      this.snack.open(response.warnings.join('; '), 'OK', { duration: 5000 });
    } else {
      this.snack.open('CEL-выражение подставлено', 'OK', { duration: 2000 });
    }
  }

  back(): void {
    const tenantCode = this.route.snapshot.params['tenantId'] || this.auth.tenant || '';
    const qp = this.route.snapshot.queryParamMap;
    const contextScopeType = (qp.get('scopeType') as RuleScopeType | null) ?? this.rule.scopeType;

    if (this.contextReturnUrl) {
      this.router.navigateByUrl(this.contextReturnUrl);
      return;
    }

    if (contextScopeType === 'PRODUCT' && this.contextProductId) {
      const queryParams: Record<string, string> = {};
      if (this.contextTab) {
        queryParams['tab'] = this.contextTab;
      }
      if (this.contextVersionNo) {
        this.router.navigate(
          ['/', tenantCode, 'product', this.contextProductId, 'version', this.contextVersionNo],
          { queryParams },
        );
      } else {
        this.router.navigate(['/', tenantCode, 'product', this.contextProductId], { queryParams });
      }
      return;
    }

    this.router.navigate(['/', tenantCode, 'admin', 'rules']);
  }
}
