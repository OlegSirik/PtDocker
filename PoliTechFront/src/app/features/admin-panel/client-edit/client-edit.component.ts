import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { ClientsService, Client, ClientConfiguration } from '../../../shared/services/api/clients.service';
import { TenantsService, Tenant } from '../../../shared/services/api/tenants.service';
import { AuthService } from '../../../shared/services/auth.service';

@Component({
  selector: 'app-client-edit',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatSelectModule,
    MatOptionModule,
    MatSnackBarModule,
    MatCheckboxModule
  ],
  templateUrl: './client-edit.component.html',
  styleUrls: ['./client-edit.component.scss']
})
export class ClientEditComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private clientsService = inject(ClientsService);
  private tenantsService = inject(TenantsService);
  private snack = inject(MatSnackBar);
  private authService = inject(AuthService);

  client: Client = {
    tid: 0,
    clientId: '',
    name: '',
    isDeleted: false,
    clientConfiguration: {
      paymentGate: '',
      sendEmailAfterBuy: false,
      sendSmsAfterBuy: false
    }
  };

  originalClient: Client | null = null;
  isNewRecord = true;
  hasChanges = false;
  loading = false;

  ngOnInit(): void {

    const clientId = this.route.snapshot.paramMap.get('client-id') || this.route.snapshot.paramMap.get('id');
    if (clientId) {
      this.isNewRecord = false;
      this.loading = true;
      this.clientsService.getById(clientId).subscribe({
        next: (client) => {
          this.client = { 
            ...client,
            clientConfiguration: client.clientConfiguration || {
              paymentGate: '',
              sendEmailAfterBuy: false,
              sendSmsAfterBuy: false
            }
          };
          this.originalClient = { ...this.client };
          this.loading = false;
          this.updateChanges();
        },
        error: (error) => {
          console.error('Error loading client:', error);
          this.snack.open('Ошибка при загрузке client', 'OK', { duration: 2000 });
          this.loading = false;
          this.router.navigate(['/', this.authService.tenant, 'admin', 'clients']);
        }
      });
    } else {
      this.isNewRecord = true;
      if (!this.client.clientConfiguration) {
        this.client.clientConfiguration = {
          paymentGate: '',
          sendEmailAfterBuy: false,
          sendSmsAfterBuy: false
        };
      }
      this.updateChanges();
    }
  }

  updateChanges(): void {
    if (this.isNewRecord) {
      // For new records, check if any required fields are filled
      this.hasChanges = !!(this.client.clientId || this.client.name || this.client.tid);
    } else {
      // For existing records, compare with original
      this.hasChanges = !this.originalClient || 
        JSON.stringify(this.client) !== JSON.stringify(this.originalClient);
    }
  }

  onFieldChange(): void {
    this.updateChanges();
  }

  save(): void {
    this.client.tid = this.authService.tenantId;
    if (!this.client.clientId || !this.client.name || this.client.tid < 0 ) {
      this.snack.open('Заполните обязательные поля (Code, Name)', 'OK', { duration: 2500 });
      return;
    }

    this.loading = true;
    if (this.isNewRecord) {
      

      this.clientsService.create(this.client).subscribe({
        next: (saved) => {
          this.client = { ...saved };
          this.originalClient = { ...saved };
          this.isNewRecord = false;
          this.loading = false;
          this.updateChanges();
          this.snack.open('Сохранено', 'OK', { duration: 2000 });
          // Navigate to the edit page with the new ID
          if (saved.id) {
            this.router.navigate(['/', this.authService.tenant, 'admin', 'clients', saved.id.toString()]);
          }
        },
        error: (error) => {
          console.error('Error creating client:', error);
          this.snack.open('Ошибка при создании client', 'OK', { duration: 2000 });
          this.loading = false;
        }
      });
    } else {
      if (!this.client.id) {
        this.snack.open('ID клиента не найден', 'OK', { duration: 2000 });
        this.loading = false;
        return;
      }
      this.clientsService.update(this.client.id, this.client).subscribe({
        next: (saved) => {
          this.client = { ...saved };
          this.originalClient = { ...saved };
          this.loading = false;
          this.updateChanges();
          this.snack.open('Сохранено', 'OK', { duration: 2000 });
        },
        error: (error) => {
          console.error('Error updating client:', error);
          this.snack.open('Ошибка при обновлении client', 'OK', { duration: 2000 });
          this.loading = false;
        }
      });
    }
  }

  gotoAccount(client: Client) {
    if (client.clientAccountId) {
      this.router.navigate(['/', this.authService.tenant, 'admin', 'accounts', client.clientAccountId.toString()]);
    }
  }
}

