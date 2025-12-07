import { NgModule } from '@angular/core';
import { ThemeToggleComponent } from './theme-toggle.component';
import { LogoComponent } from './logo.component';
import { MaterialModule } from './material.module';
import { CommonModule } from '@angular/common';

@NgModule({
  declarations: [ThemeToggleComponent, LogoComponent],
  imports: [CommonModule, MaterialModule],
  exports: [ThemeToggleComponent, LogoComponent]
})
export class ThemeModule {}
