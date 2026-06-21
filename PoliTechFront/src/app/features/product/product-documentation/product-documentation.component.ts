import { CommonModule } from '@angular/common';
import { Component, ElementRef, Input, OnChanges, SimpleChanges, ViewChild, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { marked } from 'marked';
import { ProductDocumentation, ProductService } from '../../../shared/services/product.service';

@Component({
  selector: 'app-product-documentation',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './product-documentation.component.html',
  styleUrl: './product-documentation.component.scss',
})
export class ProductDocumentationComponent implements OnChanges {
  @Input({ required: true }) productId!: number;
  @Input({ required: true }) versionNo!: number;
  @Input() productCode = '';
  @Input() reloadToken = 0;

  @ViewChild('pdfSource') pdfSource?: ElementRef<HTMLElement>;

  private productService = inject(ProductService);
  private sanitizer = inject(DomSanitizer);

  loading = false;
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

  private renderMarkdown(): void {
    const html = marked.parse(this.markdown || '', { async: false }) as string;
    this.renderedHtml = this.sanitizer.bypassSecurityTrustHtml(html);
  }
}
