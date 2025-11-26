import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChip } from '@angular/material/chips';
import { TenantService } from '../../../shared/services/tenant.service';
import { AuthService } from '../../../shared/services/auth/auth.service';
import { ClientsService, Client } from '../../../shared/services/api/clients.service';
@Component({
  selector: 'app-clients-page',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatChip
  ],
  templateUrl: './clients-page.component.html',
  styleUrls: ['./clients-page.component.scss']
})
export class ClientsPageComponent implements OnInit {
  displayedColumns: string[] = ['code', 'name', 'trusted_email', 'status', 'createdAt'];
  clients: Client[] = [];
  loading = false;

  constructor(
    private tenantService: ClientsService,
    private router: Router,
    private auth: AuthService
  ) {}

  ngOnInit() {
//    if (!this.ensurePrivileges()) {
//      return;
//    }
    this.loadClients();
  }

  loadClients() {
    this.loading = true;
    this.tenantService.getAll().subscribe({
      next: (clients: Client[]) => {
        this.clients = clients;
        this.loading = false;
      },
      error: (error: unknown) => {
        console.error('Error loading clients:', error);
        this.loading = false;
      }
    });
  }

  addClient() {
    this.router.navigate(['/admin/clients/edit']);
  }

  editClient(client: Client) {
    if (client.id) {
      this.router.navigate(['/admin/clients/edit', client.id]);
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
