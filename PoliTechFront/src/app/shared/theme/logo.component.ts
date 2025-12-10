import { Component } from '@angular/core';
import { ThemeService } from './theme.service';

@Component({
  selector: 'app-logo',
  template: `<img class="logo" [src]="logoUrl" alt="logo">`
})
export class LogoComponent {
  logoUrl = this.theme.getLogoUrl();
  constructor(private theme: ThemeService) {}
}
