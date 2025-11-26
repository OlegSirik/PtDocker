import { Routes } from '@angular/router';
import { BusinessLineComponent } from './features/business-line/business-line.component';
import { BusinessLineEditComponent } from './features/business-line-edit/business-line-edit.component';
import { ProductsComponent } from './features/products/products.component';
import { ProductComponent } from './features/product/product.component';
import { CalculatorComponent } from './features/calculator/calculator.component';
import { FilesComponent } from './features/files/files.component';
import { TestComponent } from './features/test/test.component';
import { FormlyFormsComponent } from './features/formly-forms/formly-forms.component';
import {authGuard} from './shared/guards/auth.guard';
import {LoginComponent} from './features/login/login.component';
import {ForbiddenComponent} from './features/forbidden/forbidden.component';
import {TenantsPageComponent} from './features/admin-panel/tenants-page/tenants-page.component';
import {ClientsPageComponent} from './features/admin-panel/clients-page/clients-page.component';
import {AccountDetailPageComponent} from './features/admin-panel/account-detail-page/account-detail-page.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'forbidden', component: ForbiddenComponent  },
  { path: '', redirectTo: '/business-line', pathMatch: 'full' },
  { path: 'business-line', component: BusinessLineComponent, canActivate: [authGuard] },
  { path: 'lob-edit', component: BusinessLineEditComponent, canActivate: [authGuard] },
  { path: 'lob-edit/:mpCode', component: BusinessLineEditComponent, canActivate: [authGuard] },
  { path: 'products', component: ProductsComponent, canActivate: [authGuard] },
  { path: 'product/:id/version/:versionNo', component: ProductComponent, canActivate: [authGuard] },
  { path: 'product/new', component: ProductComponent, canActivate: [authGuard] },
  { path: 'product/:product-id/version/:version-no/form', component: FormlyFormsComponent, canActivate: [authGuard] },
  { path: 'products/:productId/versions/:versionNo/packages/:packageNo/calculator', component: CalculatorComponent, canActivate: [authGuard] },
  { path: 'files', component: FilesComponent, canActivate: [authGuard] },
  { path: 'test', component: TestComponent, canActivate: [authGuard] },
  { path: 'admin/tenants', component: TenantsPageComponent, canActivate: [authGuard] },
  { path: 'admin/clients', component: ClientsPageComponent, canActivate: [authGuard] },
  { path: 'admin/accounts/:id', component: AccountDetailPageComponent, canActivate: [authGuard] }
];
