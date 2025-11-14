import { Routes } from '@angular/router';
import { BusinessLineComponent } from './features/business-line/business-line.component';
import { BusinessLineEditComponent } from './features/business-line-edit/business-line-edit.component';
import { ProductsComponent } from './features/products/products.component';
import { ProductComponent } from './features/product/product.component';
import { CalculatorComponent } from './features/calculator/calculator.component';
import { FilesComponent } from './features/files/files.component';
import { TestComponent } from './features/test/test.component';
import { FormlyFormsComponent } from './features/formly-forms/formly-forms.component';

export const routes: Routes = [
  { path: '', redirectTo: '/business-line', pathMatch: 'full' },
  { path: 'business-line', component: BusinessLineComponent },
  { path: 'lob-edit', component: BusinessLineEditComponent },
  { path: 'lob-edit/:mpCode', component: BusinessLineEditComponent },
  { path: 'products', component: ProductsComponent },
  { path: 'product/:id/version/:versionNo', component: ProductComponent },
  { path: 'product/new', component: ProductComponent },
  { path: 'product/:product-id/version/:version-no/form', component: FormlyFormsComponent },
  { path: 'products/:productId/versions/:versionNo/packages/:packageNo/calculator', component: CalculatorComponent },
  { path: 'files', component: FilesComponent },
  { path: 'test', component: TestComponent }
];
