import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { ThemeService, TenantTheme } from '../../shared/theme/theme.service';

@Component({
    selector: 'app-test',
    imports: [
        MatCardModule,
        MatButtonModule
    ],
    templateUrl: './test.component.html',
    styleUrls: ['./test.component.scss']
})
export class TestComponent {
  constructor(private themeService: ThemeService) {}

  applyTheme1(): void {
    const theme1: TenantTheme = {
      id: 'theme-blue',
      name: 'Blue Theme',
      palette: {
        primary: '#2196F3',
        accent: '#03A9F4',
        warn: '#F44336',
        background: '#E3F2FD',
        surface: '#FFFFFF',
        card: '#BBDEFB',
        text: '#1565C0'
      },
      fontFamily: 'Roboto, sans-serif'
    };
    this.themeService.applyCustomTheme(theme1);
  }

  applyTheme2(): void {
    const theme2: TenantTheme = {
      id: 'theme-green',
      name: 'Green Theme',
      palette: {
        primary: '#4CAF50',
        accent: '#8BC34A',
        warn: '#FF5722',
        background: '#E8F5E9',
        surface: '#FFFFFF',
        card: '#C8E6C9',
        text: '#2E7D32'
      },
      fontFamily: 'Arial, sans-serif'
    };
    this.themeService.applyCustomTheme(theme2);
  }

  applyTheme3(): void {
    const theme3: TenantTheme = {
      id: 'theme-purple',
      name: 'Purple Theme',
      palette: {
        primary: '#9C27B0',
        accent: '#E91E63',
        warn: '#FF9800',
        background: '#F3E5F5',
        surface: '#FFFFFF',
        card: '#CE93D8',
        text: '#6A1B9A'
      },
      fontFamily: 'Georgia, serif'
    };
    this.themeService.applyCustomTheme(theme3);
  }
}
