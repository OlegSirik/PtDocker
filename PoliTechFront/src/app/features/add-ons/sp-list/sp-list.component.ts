import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChip } from '@angular/material/chips';
import { AuthService } from '../../../shared/services/auth.service';
import { AddonProvidersService, ProviderListDto } from '../../../shared/services/api/addon-providers.service';
import { AddonPricelistService, PricelistListDto } from '../../../shared/services/api/addon-pricelist.service';

@Component({
  selector: 'app-sp-list',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './sp-list.component.html',
  styleUrls: ['./sp-list.component.scss']
})
export class SpListComponent implements OnInit {
  displayedColumns: string[] = ['id', 'name', 'status', 'actions'];
  providers: ProviderListDto[] = [];
  loading = false;
  selectedProvider: ProviderListDto | null = null;
  pricelists: PricelistListDto[] = [];
  loadingPricelists = false;
  pricelistColumns: string[] = ['id', 'code', 'name', 'price', 'status', 'actions'];

  constructor(
    private providersService: AddonProvidersService,
    private pricelistService: AddonPricelistService,
    private router: Router,
    private route: ActivatedRoute,
    private auth: AuthService
  ) {}

  ngOnInit() {
    this.loadProviders();
  }

  loadProviders() {
    this.loading = true;
    this.providersService.getProviders().subscribe({
      next: (list) => {
        this.providers = list;
        this.loading = false;
      },
      error: (error: unknown) => {
        console.error('Error loading providers:', error);
        this.loading = false;
      }
    });
  }

  addProvider() {
    const tenantCode = this.route.snapshot.params['tenantId'] || this.auth.tenant || '';
    this.router.navigate(['/', tenantCode, 'admin', 'addon', 'sp-provider', 'edit']);
  }

  editProvider(provider: ProviderListDto, event?: Event) {
    if (event) {
      event.stopPropagation();
    }
    const tenantCode = this.route.snapshot.params['tenantId'] || this.auth.tenant || '';
    this.router.navigate(['/', tenantCode, 'admin', 'addon', 'sp-provider', provider.id.toString()]);
  }

  selectProvider(provider: ProviderListDto) {
    this.selectedProvider = this.selectedProvider?.id === provider.id ? null : provider;
    if (this.selectedProvider) {
      this.loadPricelists();
    } else {
      this.pricelists = [];
    }
  }

  loadPricelists() {
    if (!this.selectedProvider) return;
    this.loadingPricelists = true;
    this.pricelistService.getPricelists(this.selectedProvider.id).subscribe({
      next: (list) => {
        this.pricelists = list;
        this.loadingPricelists = false;
      },
      error: () => {
        this.loadingPricelists = false;
      }
    });
  }

  addPricelist() {
    const tenantCode = this.route.snapshot.params['tenantId'] || this.auth.tenant || '';
    this.router.navigate(['/', tenantCode, 'admin', 'addon', 'pricelist-edit', 'edit'], {
      queryParams: { spId: this.selectedProvider?.id }
    });
  }

  editPricelist(pl: PricelistListDto, event?: Event) {
    if (event) {
      event.stopPropagation();
    }
    const tenantCode = this.route.snapshot.params['tenantId'] || this.auth.tenant || '';
    this.router.navigate(['/', tenantCode, 'admin', 'addon', 'pricelist-edit', pl.id.toString()]);
  }

  selectPricelist(pl: PricelistListDto) {
    this.editPricelist(pl);
  }

  getStatusClass(status: string): string {
    if (!status) return 'status-active';
    const s = (status || '').toUpperCase();
    if (s === 'ACTIVE') return 'status-active';
    if (s === 'SUSPENDED' || s === 'ORANGE') return 'status-suspended';
    return 'status-active';
  }
}
