import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
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
  ],
  templateUrl: './clients-page.component.html',
  styleUrls: ['./clients-page.component.scss']
})
export class ClientsPageComponent implements OnInit {
  displayedColumns: string[] = ['id', 'clientId', 'name', 'status', 'actions'];
  clients: Client[] = [];
  loading = false;

  constructor(
    private clientsService: ClientsService,
    private router: Router,
    private route: ActivatedRoute,
    private auth: AuthService
  ) {}

  ngOnInit() {
    this.loadClients();
  }

  loadClients() {
    this.loading = true;
    this.clientsService.getAll().subscribe({
      next: (apiClients: Client[]) => {
        this.clients = apiClients;
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

  editClient(client: Client, event?: Event) {
    if (event) {
      event.stopPropagation();
    }
    if (client.id) {
      const tenantCode = this.route.snapshot.params['tenantId'] || this.auth.tenant || '';
      this.router.navigate(['/', tenantCode, 'admin', 'clients', client.id.toString()]);
    }
  }


}
