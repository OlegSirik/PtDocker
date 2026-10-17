import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { LkQuoteService, Quote } from '../../shared/services/api/lk-quote.service';
import { AuthService } from '../../shared/services/auth.service';
import { PolicyService } from '../../shared/services/policy.service';
import {
  PolicyFormService,
  ProductForQuoteDto,
} from '../../shared/services/api/policy-form.service';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-quotes',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatOptionModule,
    MatExpansionModule,
    MatIconModule
  ],
  templateUrl: './quotes.component.html',
  styleUrls: ['./quotes.component.scss']
})
export class QuotesComponent implements OnInit, OnDestroy {
  private router = inject(Router);
  private quoteService = inject(LkQuoteService);
  private authService = inject(AuthService);
  private policyService = inject(PolicyService);
  private policyFormService = inject(PolicyFormService);
  private destroy$ = new Subject<void>();
  private searchSubject$ = new Subject<string>();

  quotes: Quote[] = [];
  filteredQuotes: Quote[] = [];

  createProducts: ProductForQuoteDto[] = [];
  selectedCreateProductId: number | null = null;

  searchText: string = '';
  selectedStatus: string = '';

  statusOptions: string[] = ['DRAFT', 'ISSUED', 'PAID', 'CANCELLED', 'EXPIRED'];

  displayedQuotes: Quote[] = [];
  maxRows = 20;

  ngOnInit(): void {
    this.loadQuotes();
    this.loadCreateProducts();

    this.searchSubject$.pipe(
      debounceTime(500),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(searchText => {
      this.performSearch(searchText);
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadCreateProducts(): void {
    this.policyFormService.listProductsForQuote().subscribe({
      next: (list) => {
        this.createProducts = list ?? [];
      },
      error: (error) => {
        console.error('Error loading products for quote:', error);
        this.createProducts = [];
      }
    });
  }

  loadQuotes(): void {
    this.quoteService.getAll().subscribe({
      next: (quotes) => {
        this.quotes = quotes;
        this.applyFilters();
      },
      error: (error) => {
        console.error('Error loading quotes:', error);
      }
    });
  }

  onCreateProductSelected(): void {
    if (this.selectedCreateProductId == null) {
      return;
    }
    const product = this.createProducts.find(p => p.productId === this.selectedCreateProductId);
    if (!product) {
      this.selectedCreateProductId = null;
      return;
    }
    this.router.navigate(
      ['/', this.authService.tenant, 'lk', product.pageId],
      {
        queryParams: {
          productCode: product.productCode,
          versionNo: product.versionNo,
          title: product.title,
          ioType: product.ioType || 'PERSON',
        },
      },
    );
    this.selectedCreateProductId = null;
  }

  onSearchChange(): void {
    console.log('onSearchChange', this.searchText);
    this.performSearch(this.searchText);
  }


  private performSearch(searchText: string): void {
    console.log('performSearch', searchText);
    if (searchText.trim()) {
      this.quoteService.getAccountQuotes(searchText.trim()).subscribe({
        next: (quotes) => {
          this.quotes = quotes;
          this.applyFilters();
        },
        error: (error) => {
          console.error('Error searching quotes:', error);
          this.applyFilters();
        }
      });
    } else {
      this.loadQuotes();
    }
  }

  onStatusChange(): void {
    this.applyFilters();
  }

  applyFilters(): void {
    let filtered = [...this.quotes];
    
    if (this.searchText.trim()) {
      const searchLower = this.searchText.trim().toLowerCase();
      filtered = filtered.filter(q => 
        (q.phDigest && q.phDigest.toLowerCase().includes(searchLower)) ||
        (q.policyNr && q.policyNr.toLowerCase().includes(searchLower)) ||
        (q.productCode && q.productCode.toLowerCase().includes(searchLower))
      );
    }
    
    if (this.selectedStatus) {
      filtered = filtered.filter(q => q.policyStatus === this.selectedStatus);
    }
    
    this.filteredQuotes = filtered;
    this.displayedQuotes = filtered.slice(0, this.maxRows);
  }

  executeCommand(quote: Quote, commandNumber: number): void {
    let id = quote.id;
    if (commandNumber === 1) {
      this.policyService.getPf(quote.policyNr || '', 'policy').subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `policy_${quote.policyNr || 'document'}.pdf`;
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          window.URL.revokeObjectURL(url);
        },
        error: (error) => {
          console.error('Error downloading policy:', error);
        }
      });
    }

    if (commandNumber === 2) {
      this.policyService.getPf(quote.policyNr || '', 'kid').subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `policy_${quote.policyNr || 'kid'}.pdf`;
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          window.URL.revokeObjectURL(url);
        },
        error: (error) => {
          console.error('Error downloading policy:', error);
        }
      });
    }
  }
}
