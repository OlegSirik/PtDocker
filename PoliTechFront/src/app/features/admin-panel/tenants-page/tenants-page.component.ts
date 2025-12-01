import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChip } from '@angular/material/chips';
import { TenantService, Tenant } from '../../../shared/services/tenant.service';
import { AuthService } from '../../../shared/services/auth/auth.service';

@Component({
  selector: 'app-tenants-page',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatChip
  ],
  templateUrl: './tenants-page.component.html',
  styleUrls: ['./tenants-page.component.scss']
})
export class TenantsPageComponent implements OnInit {
  displayedColumns: string[] = ['id', 'name', 'trusted_email', 'status', 'createdAt'];
  tenants: Tenant[] = [];
  loading = false;

  constructor(
    private tenantService: TenantService,
    private router: Router,
    private auth: AuthService
  ) {}

  ngOnInit() {
    //if (!this.ensurePrivileges()) {
    //  return;
    //}
    this.loadTenants();
  }

  loadTenants() {
    this.loading = true;
    this.tenantService.getTenants().subscribe({
      next: (tenants) => {
        this.tenants = tenants;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading tenants:', error);
        this.loading = false;
      }
    });
  }

  addTenant() {
    this.router.navigate(['/admin/tenants/edit']);
  }

  editTenant(tenant: Tenant) {
    if (tenant.id) {
      this.router.navigate(['/admin/tenants/edit', tenant.id]);
    }
  }

  
  private ensurePrivileges(): boolean {
    const profile = this.auth.profile;
    if (!profile) {
      this.router.navigate(['/']);
      return false;
    }

    const hasAccess = this.auth.hasAccountType('SYS_ADMIN') || this.auth.hasAnyRole(['SYS_ADMIN']);
    if (!hasAccess) {
      this.router.navigate(['/']);
      return false;
    }

    return true;
  }
    
}

