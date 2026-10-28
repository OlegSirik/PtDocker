import { CommonModule } from '@angular/common';
import { Component, ElementRef, Input, OnChanges, SimpleChanges, ViewChild, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { marked } from 'marked';
import { ProductDocumentation, ProductService } from '../../../shared/services/product.service';
import { AuthService } from '../../../shared/services/auth.service';
import { VarsService } from '../../../shared/services/vars.service';
import { BASE_URL } from '../../../shared/tokens';
import { buildProductSalesOpenApiYaml } from '../../../shared/utils/openapi-from-schema.util';

@Component({
  selector: 'app-product-documentation',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule, MatSnackBarModule],
  templateUrl: './product-documentation.component.html',
  styleUrl: './product-documentation.component.scss',
})
export class ProductDocumentationComponent implements OnChanges {
  @Input({ required: true }) productId!: number;
  @Input({ required: true }) versionNo!: number;
  @Input() productCode = '';
  @Input() productName = '';
  @Input() reloadToken = 0;

  @ViewChild('pdfSource') pdfSource?: ElementRef<HTMLElement>;

  private productService = inject(ProductService);
  private sanitizer = inject(DomSanitizer);
  private varsService = inject(VarsService);
  private authService = inject(AuthService);
  private readonly apiBaseUrl = inject(BASE_URL);
  private snackBar = inject(MatSnackBar);

  loading = false;
  downloadingApiYaml = false;
  error: string | null = null;
  markdown = '';
  renderedHtml: SafeHtml = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['productId'] || changes['versionNo'] || changes['reloadToken']) {
      this.loadDocumentation();
    }
  }

  loadDocumentation(): void {
    if (!this.productId || !this.versionNo) {
      return;
    }
    this.loading = true;
    this.error = null;
    this.productService.getDocumentation(this.productId, this.versionNo).subscribe({
      next: (doc: ProductDocumentation) => {
        this.markdown = doc.markdown ?? '';
        this.renderMarkdown();
        this.loading = false;
      },
      error: (err: { error?: { message?: string }; message?: string }) => {
        this.loading = false;
        this.error = err?.error?.message || err?.message || 'Не удалось загрузить документацию';
        this.markdown = '';
        this.renderedHtml = '';
      },
    });
  }

  async downloadPdf(): Promise<void> {
    const element = this.pdfSource?.nativeElement;
    if (!element) {
      return;
    }
    const fileName = `product_${this.productCode || this.productId}_v${this.versionNo}.pdf`;
    const html2pdf = (await import('html2pdf.js')).default;
    await html2pdf()
      .set({
        margin: [10, 10, 10, 10],
        filename: fileName,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2, useCORS: true },
        jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' },
      })
      .from(element)
      .save();
  }

  /** OpenAPI YAML: server = BASE_URL, POST /sales/quotes|policies, schema = схема договора. */
  downloadApiYaml(): void {
    if (this.downloadingApiYaml) {
      return;
    }
    this.downloadingApiYaml = true;
    this.varsService.getAttributesMetadataJson('INSURANCE_CONTRACT').subscribe({
      next: (contractDocument) => {
        this.downloadingApiYaml = false;
        const yaml = buildProductSalesOpenApiYaml({
          serverUrl: this.apiBaseUrl || window.location.origin,
          tenantCode: this.authService.tenant || this.authService.getTenantCode(),
          productCode: this.productCode || String(this.productId),
          productName: this.productName || this.productCode || 'Product',
          versionNo: this.versionNo ?? 1,
          contractDocument,
        });
        const blob = new Blob([yaml], { type: 'application/yaml;charset=utf-8' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `api_${this.productCode || this.productId}_v${this.versionNo}.yaml`;
        link.click();
        URL.revokeObjectURL(url);
        this.snackBar.open('api.yaml сохранён', 'Закрыть', { duration: 2000 });
      },
      error: (err: { error?: { message?: string } }) => {
        this.downloadingApiYaml = false;
        this.snackBar.open(err?.error?.message ?? 'Не удалось получить схему договора', 'Закрыть', {
          duration: 4000,
        });
      },
    });
  }

  private renderMarkdown(): void {
    const html = marked.parse(this.markdown || '', { async: false }) as string;
    this.renderedHtml = this.sanitizer.bypassSecurityTrustHtml(html);
  }
}
