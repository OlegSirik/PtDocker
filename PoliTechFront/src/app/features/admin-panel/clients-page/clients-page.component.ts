import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChip } from '@angular/material/chips';
import { TenantService } from '../../../shared/services/tenant.service';
import { AuthService } from '../../../shared/services/auth.service';
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
    MatChip,
    DatePipe
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
    private route: ActivatedRoute,
    private auth: AuthService
  ) {}

  ngOnInit() {
    this.loadClients();
  }

  loadClients() {
    this.loading = true;
    this.tenantService.getAll().subscribe({
      next: (apiClients: any[]) => {
        // Map API response to component interface
        this.clients = apiClients.map((apiClient: any) => ({
          id: apiClient.id,
          tid: apiClient.tid || 0,
          clientId: apiClient.clientId || '',
          name: apiClient.name || '',
          description: apiClient.description || '',
          trusted_email: apiClient.trusted_email || '',
          status: apiClient.isDeleted ? 'DELETED' : (apiClient.status || 'ACTIVE'),
          accountId: apiClient.accountId || apiClient.defaultAccountId,
          createdAt: apiClient.createdAt,
          updateAt: apiClient.updatedAt || apiClient.updateAt
        } as Client));
        this.loading = false;
      },
      error: (error: unknown) => {
        console.error('Error loading clients:', error);
        this.loading = false;
      }
    });
  }

  addClient() {
    const tenantCode = this.route.snapshot.params['tenantId'] || this.auth.tenant || '';
    this.router.navigate(['/', tenantCode, 'admin', 'clients', 'edit']);
  }

  editClient(client: Client) {
    if (client.id) {
      const tenantCode = this.route.snapshot.params['tenantId'] || this.auth.tenant || '';
      this.router.navigate(['/', tenantCode, 'admin', 'clients', client.id.toString()]);
    }
  }


}
