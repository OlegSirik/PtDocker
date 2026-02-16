import { Routes } from '@angular/router';
import { BusinessLineComponent } from './features/business-line/business-line.component';
import { BusinessLineEditComponent } from './features/business-line-edit/business-line-edit.component';
import { ProductsComponent } from './features/products/products.component';
import { ProductComponent } from './features/product/product.component';
import { CalculatorComponent } from './features/calculator/calculator.component';

import { FormlyFormsComponent } from './features/formly-forms/formly-forms.component';
import {authGuard} from './shared/guards/auth.guard';
import {LoginComponent} from './features/login/login.component';
import {ForbiddenComponent} from './features/forbidden/forbidden.component';
import {TenantsPageComponent} from './features/admin-panel/tenants-page/tenants-page.component';
import {ClientsPageComponent} from './features/admin-panel/clients-page/clients-page.component';
import {ClientEditComponent} from './features/admin-panel/client-edit/client-edit.component';
import {AccountDetailPageComponent} from './features/admin-panel/account-detail-page/account-detail-page.component';
import { SpListComponent } from './features/add-ons/sp-list/sp-list.component';
import { SpProviderComponent } from './features/add-ons/sp-provider/sp-provider.component';
import { PricelistEditComponent } from './features/add-ons/pricelist-edit/pricelist-edit.component';
import { TenantGuard } from './shared/guards/tenant.guard';
import { QuotesComponent } from './features/quotes/quotes.component';
import { UserProfileComponent } from './features/user-profile/user-profile.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';


export const routes: Routes = [
  {
    path: 'forbidden',
    component: ForbiddenComponent
  },
  {
    path: ':tenantId',
    canActivate: [TenantGuard],
    children: [
  { path: 'login', component: LoginComponent },
  { path: 'forbidden', component: ForbiddenComponent  },
//  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'business-line', component: BusinessLineComponent, canActivate: [authGuard] },
  { path: 'lob-edit', component: BusinessLineEditComponent, canActivate: [authGuard] },
  { path: 'lob-edit/:id', component: BusinessLineEditComponent, canActivate: [authGuard] },
  { path: 'products', component: ProductsComponent, canActivate: [authGuard] },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'product/:id/version/:versionNo', component: ProductComponent, canActivate: [authGuard] },
  { path: 'product/new', component: ProductComponent, canActivate: [authGuard] },
  { path: 'product/:product-id/version/:version-no/form', component: FormlyFormsComponent, canActivate: [authGuard] },
  { path: 'products/:productId/versions/:versionNo/packages/:packageNo/calculator', component: CalculatorComponent, canActivate: [authGuard] },
  { path: 'lk/quotes', component: QuotesComponent, canActivate: [authGuard] },
  
  { path: 'admin/tenants', component: TenantsPageComponent, canActivate: [authGuard] },
  { path: 'admin/clients', component: ClientsPageComponent, canActivate: [authGuard] },
  { path: 'admin/clients/edit', component: ClientEditComponent, canActivate: [authGuard] },
  { path: 'admin/clients/:client-id', component: ClientEditComponent, canActivate: [authGuard] },
  { path: 'admin/accounts/:id', component: AccountDetailPageComponent, canActivate: [authGuard] },
  { path: 'admin/addon/sp-list', component: SpListComponent, canActivate: [authGuard] },
  { path: 'admin/addon/sp-provider/edit', component: SpProviderComponent, canActivate: [authGuard] },
  { path: 'admin/addon/sp-provider/:id', component: SpProviderComponent, canActivate: [authGuard] },
  { path: 'admin/addon/pricelist-edit/edit', component: PricelistEditComponent, canActivate: [authGuard] },
  { path: 'admin/addon/pricelist-edit/:id', component: PricelistEditComponent, canActivate: [authGuard] },
  { path: 'user_profile', component: UserProfileComponent, canActivate: [authGuard] }
]},
{
  path: '',
  redirectTo: 'forbidden',  // можно указать дефолтный tenant
  pathMatch: 'full'
},
{
  path: '**',
  redirectTo: 'not-found'
}
];
