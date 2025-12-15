import { Injectable } from '@angular/core';


export interface TenantTheme {
id: string; // e.g. "tenant-a"
name?: string;
palette?: {
primary?: string;
accent?: string;
warn?: string;
background?: string;
surface?: string;
card?: string;
text?: string;
};
logoUrl?: string;
fontFamily?: string;
}


@Injectable({ providedIn: 'root' })
export class ThemeService {
  private styleElement: HTMLStyleElement | null = null;

  applyCustomTheme(theme: TenantTheme) {
    console.log('Applying theme:', theme);
    
    // Remove any previous tenant-* classes to avoid conflicts
    Array.from(document.body.classList)
      .filter(c => c.startsWith('tenant-') || c === 'theme-dark')
      .forEach(c => document.body.classList.remove(c));

    // Remove existing dynamic style element if present
    if (this.styleElement && this.styleElement.parentNode) {
      this.styleElement.parentNode.removeChild(this.styleElement);
      this.styleElement = null;
    }

    // Create a new style element for dynamic theme overrides
    this.styleElement = document.createElement('style');
    this.styleElement.id = 'dynamic-theme-overrides';
    
    const root = document.documentElement;
    const cssRules: string[] = [];

    if (theme.palette) {
      // Angular Material 3 system color variables - override with !important
      if (theme.palette.primary) {
        const primary = theme.palette.primary;
        cssRules.push(`:root { --mat-sys-color-primary: ${primary} !important; }`);
        cssRules.push(`:root { --mdc-theme-primary: ${primary} !important; }`);
        cssRules.push(`:root { --mat-primary: ${primary} !important; }`);
        // Material button primary color
        cssRules.push(`.mat-mdc-button.mat-primary { --mdc-filled-button-container-color: ${primary} !important; }`);
        cssRules.push(`.mat-mdc-raised-button.mat-primary { --mdc-filled-button-container-color: ${primary} !important; }`);
        cssRules.push(`.mat-mdc-unelevated-button.mat-primary { --mdc-filled-button-container-color: ${primary} !important; }`);
        cssRules.push(`.mat-mdc-fab.mat-primary { --mdc-fab-container-color: ${primary} !important; }`);
        cssRules.push(`.mat-mdc-mini-fab.mat-primary { --mdc-fab-container-color: ${primary} !important; }`);
        // Material form field primary color
        cssRules.push(`.mat-mdc-form-field.mat-primary .mat-mdc-text-field-wrapper { --mdc-outlined-text-field-focus-outline-color: ${primary} !important; }`);
        cssRules.push(`.mat-mdc-form-field.mat-primary .mdc-line-ripple::after { --mdc-line-ripple-active-color: ${primary} !important; }`);
        // Material tab active indicator
        cssRules.push(`.mat-mdc-tab-group.mat-primary .mdc-tab-indicator__content--underline { border-color: ${primary} !important; }`);
        cssRules.push(`.mat-mdc-tab-group.mat-primary .mat-mdc-tab-label-active { color: ${primary} !important; }`);
      }
      
      if (theme.palette.accent) {
        const accent = theme.palette.accent;
        cssRules.push(`:root { --mat-sys-color-secondary: ${accent} !important; }`);
        cssRules.push(`:root { --mdc-theme-secondary: ${accent} !important; }`);
        cssRules.push(`:root { --mat-accent: ${accent} !important; }`);
        cssRules.push(`.mat-mdc-button.mat-accent { --mdc-filled-button-container-color: ${accent} !important; }`);
        cssRules.push(`.mat-mdc-raised-button.mat-accent { --mdc-filled-button-container-color: ${accent} !important; }`);
      }
      
      if (theme.palette.warn) {
        const warn = theme.palette.warn;
        cssRules.push(`:root { --mat-sys-color-error: ${warn} !important; }`);
        cssRules.push(`:root { --mdc-theme-error: ${warn} !important; }`);
        cssRules.push(`:root { --mat-warn: ${warn} !important; }`);
        cssRules.push(`.mat-mdc-button.mat-warn { --mdc-filled-button-container-color: ${warn} !important; }`);
        cssRules.push(`.mat-mdc-raised-button.mat-warn { --mdc-filled-button-container-color: ${warn} !important; }`);
      }
      
      if (theme.palette.background) {
        const bg = theme.palette.background;
        cssRules.push(`:root { --mat-sys-color-surface: ${bg} !important; }`);
        cssRules.push(`:root { --mdc-theme-surface: ${bg} !important; }`);

        // toolbar
        cssRules.push(`:root { --mat-app-surface: ${bg} !important; }`);
        //cssRules.push(`:root ( --mat-toolbar-container-background-color: ${bg} !important; }`);

        cssRules.push(`body { background-color: ${bg} !important; }`);
      }
      
      if (theme.palette.surface) {
        cssRules.push(`:root { --mat-sys-color-surface-container: ${theme.palette.surface} !important; }`);
      }
      
      if (theme.palette.card) {
        cssRules.push(`:root { --mat-sys-color-surface-container-high: ${theme.palette.card} !important; }`);
        cssRules.push(`.mat-mdc-card { background-color: ${theme.palette.card} !important; }`);
      }
      
      if (theme.palette.text) {
        cssRules.push(`:root { --mat-sys-color-on-surface: ${theme.palette.text} !important; }`);
        cssRules.push(`body { color: ${theme.palette.text} !important; }`);
      }
      
      // Custom branding variables (for non-Material components)
      if (theme.palette.primary) root.style.setProperty('--branding-primary', theme.palette.primary);
      if (theme.palette.accent) root.style.setProperty('--branding-accent', theme.palette.accent);
      if (theme.palette.warn) root.style.setProperty('--branding-warn', theme.palette.warn);
      if (theme.palette.background) root.style.setProperty('--branding-bg', theme.palette.background);
      if (theme.palette.surface) root.style.setProperty('--branding-surface', theme.palette.surface);
      if (theme.palette.card) root.style.setProperty('--branding-card-bg', theme.palette.card);
      if (theme.palette.text) root.style.setProperty('--branding-text', theme.palette.text);
    }
    
    if (theme.logoUrl) root.style.setProperty('--branding-logo-url', `url('${theme.logoUrl}')`);
    if (theme.fontFamily) {
      root.style.setProperty('--branding-font-family', theme.fontFamily);
      cssRules.push(`body { font-family: ${theme.fontFamily} !important; }`);
    }

    // Inject all CSS rules
    if (cssRules.length > 0) {
      this.styleElement.textContent = cssRules.join('\n');
      document.head.appendChild(this.styleElement);
    }
  }
}