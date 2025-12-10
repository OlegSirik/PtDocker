import { Component } from '@angular/core';
import { ThemeService } from './theme.service';

@Component({
  selector: 'app-theme-toggle',
  template: `
    <mat-slide-toggle [checked]="isDark" (change)="toggle($event.checked)">
      Dark
    </mat-slide-toggle>
  `
})
export class ThemeToggleComponent {
  isDark = document.documentElement.classList.contains('dark');
  constructor(private theme: ThemeService) {}

  toggle(dark: boolean) {
    const currentCfgRaw = localStorage.getItem('ui_config');
    const cfg = currentCfgRaw ? JSON.parse(currentCfgRaw) : {};
    cfg.darkMode = dark;
    this.theme.applyTheme(cfg);
    this.isDark = dark;
  }
}
