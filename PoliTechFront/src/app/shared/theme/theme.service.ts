import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface UiConfig {
  primaryColor?: string;    // hex
  secondaryColor?: string;
  toolbarColor?: string;
  background?: string;
  textColor?: string;
  logoUrl?: string;
  darkMode?: boolean;
  buttonRadius?: string;
}

@Injectable({ providedIn: 'root' })
export class ThemeService {

  private readonly STORAGE_KEY = 'ui_config';

  constructor(private http: HttpClient) {}

  // apply from server or saved config
  applyTheme(cfg: UiConfig) {
    const root = document.documentElement;

    if (cfg.primaryColor) root.style.setProperty('--primary-500', cfg.primaryColor);
    if (cfg.secondaryColor) root.style.setProperty('--accent-500', cfg.secondaryColor);
    if (cfg.toolbarColor) root.style.setProperty('--toolbar-bg', cfg.toolbarColor);
    if (cfg.background) root.style.setProperty('--bg', cfg.background);
    if (cfg.textColor) root.style.setProperty('--text', cfg.textColor);
    if (cfg.logoUrl) root.style.setProperty('--logo-url', `url('${cfg.logoUrl}')`) ;

    if (cfg.buttonRadius) root.style.setProperty('--button-radius', cfg.buttonRadius);

    // dark mode toggle via class for easier scoping
    if (cfg.darkMode) {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }

    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(cfg));
  }

  // load persisted config
  loadSaved() {
    const raw = localStorage.getItem(this.STORAGE_KEY);
    if (raw) {
      try {
        const cfg: UiConfig = JSON.parse(raw);
        this.applyTheme(cfg);
      } catch (e) { /* ignore */ }
    }
  }

  // convenience: get logo URL (CSS var stores string "url('...')" so parse)
  getLogoUrl(): string {
    const raw = getComputedStyle(document.documentElement).getPropertyValue('--logo-url') || '';
    const m = raw.match(/url\(['"]?(.*?)['"]?\)/);
    return m ? m[1] : raw.trim() || '/assets/logo-default.svg';
  }

  // helper: fetch ui config from backend for current tenant
  getUiConfigForTenant(tenant: string) {
    return this.http.get<UiConfig>(`/api/v1/${tenant}/ui-config`);
  }
}
