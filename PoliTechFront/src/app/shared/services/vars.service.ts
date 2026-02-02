import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, BehaviorSubject } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { AuthService } from './auth.service';

export interface LobVar {
    varCode: string;
    varType: string;
    varPath: string;
    varName: string;
    varDataType: string;
    varValue: string;
    varCdm: string
    varNr: number;
  }

@Injectable({
    providedIn: 'root'
  })

export class VarsService {
  private allVars: LobVar[] = [];
  private varsLoaded$ = new BehaviorSubject<boolean>(false);

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {
    // Load vars from API on startup
    this.loadVarsFromApi();
  }

  /**
   * Load PvVars from API
   * GET /api/v1/{tenantCode}/admin/products/vars
   */
  private loadVarsFromApi(): void {
    const url = `${this.authService.baseApiUrl}/admin/products/vars`;
    this.http.get<LobVar[]>(url).pipe(
      catchError(error => {
        console.error('Error loading vars from API:', error);
        return of([]);
      })
    ).subscribe(vars => {
      this.allVars = vars || [];
      this.varsLoaded$.next(true);
    });
  }

  /**
   * Get all vars from API (Observable)
   */
  getAPIVars(): Observable<LobVar[]> {
    const url = `${this.authService.baseApiUrl}/admin/products/vars`;
    return this.http.get<LobVar[]>(url);
  }

  /**
   * Get all cached vars (sync)
   */
  getAllVars(): LobVar[] {
    return this.allVars;
  }

  /**
   * Check if vars are loaded
   */
  isVarsLoaded(): Observable<boolean> {
    return this.varsLoaded$.asObservable();
  }

  getPhCategories(type: string): string[] | any[] {
    if (type === 'person') {
      return [
        "person",
        "contacts",
        "organization",
        "identifiers",
        "addresses",
        "additionalProperties"
      ];
    }
    if (type === 'organization') {
      return [
        "organization",
        "identifiers",
        "addresses",
        "additionalProperties"
      ];
    }
    return [];
  }

  getPhDefVars(type: string): LobVar[] {
    if (type === 'person') {
      return this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.person')).
      concat(this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.contacts'))).
      concat(this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.organization'))).
      concat(this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.identifiers'))).
      concat(this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.addresses'))).
      concat(this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.additionalProperties')));
    }
    if (type === 'organization') {
      return this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.organization')).
      concat(this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.identifiers'))).
      concat(this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.contacts'))).
      concat(this.allVars.filter(v => v.varCdm?.startsWith('policyHolder.addresses')));
    }
    return [];
  }

  getIoDefVars(type: string): LobVar[] | any[]{
    if (type === 'person') {

        return [
        ...this.getIoVars(),
        ...this.getIoPersonVars(),
        ...this.getIoOrganizationVars(),
        ...this.getIoContactsVars(),
        ...this.getIoPersonIdentifiersVars(),
        ...this.getIoAddressVars(),
        ...this.getIoRiskFactorsVars()
      ];

    }
    if (type === 'device') {
        return [
          ...this.getIoVars(),
          ...this.getIoDeviceVars()
        ];
    }
  
    if (type === 'property') {
        return [
          ...this.getIoVars()
                ];
    }
    if (type === 'avia-ns') {
      return [
        ...this.getIoVars(),
        ...this.getIoPersonVars(),
        ...this.getIoContactsVars(),
        ...this.getIoTravelSegmentsVars()
      ];
  }
  return [];
  }

  getIoCategories(type: string): string[] | any[] {
    if (type === 'person') {
      return [
        "person",
        "contacts",
        "organization",
        "identifiers",
        "addresses",
        "additionalProperties",
        "riskFactors"
      ];
    }
    if (type === 'device') {
      return [
        "device",
        "additionalProperties",
        "riskFactors"
      ];
    }
    if (type === 'property') {
      return [
        "property",
        "additionalProperties",
        "riskFactors"
      ];
    }
    if (type === 'avia-ns') {
      return [
        "person",
        "contacts",
        "travelSegments",
        "additionalProperties",
        "riskFactors"
      ];
    }
    return [];
  }

  getIoPersonVars(): LobVar[] {
    return this.allVars.filter(v => v.varCdm?.startsWith('insuredObject.person'));
  }
  getIoOrganizationVars(): LobVar[] {
    return this.allVars.filter(v => v.varCdm?.startsWith('insuredObject.organization'));
  }
  getIoContactsVars(): LobVar[] {
    return this.allVars.filter(v => v.varCdm?.startsWith('insuredObject.contacts'));
  }
  getIoPersonIdentifiersVars(): LobVar[] {
    return this.allVars.filter(v => v.varCdm?.startsWith('insuredObject.identifiers'));
  }
  getIoAddressVars(): LobVar[] {
    return this.allVars.filter(v => v.varCdm?.startsWith('insuredObject.addresses'));
  }
  getIoTravelSegmentsVars(): LobVar[] {
    return this.allVars.filter(v => v.varCdm?.startsWith('insuredObject.travelSegments'));
  }

  getIoVars(): LobVar[] {
    return this.allVars.filter(v => 
      v.varCdm?.startsWith('insuredObject') && 
      (v.varCdm?.split('.').length - 1) === 1
    );
  }
  getIoDeviceVars(): LobVar[] {
    return this.allVars.filter(v => v.varCdm?.startsWith('insuredObject.device'));
  }
  getIoPropertyVars(): LobVar[] {
    return this.allVars.filter(v => v.varCdm?.startsWith('insuredObject.property'));
  }  
  getIoRiskFactorsVars(): LobVar[] {
    return this.allVars.filter(v => v.varCdm?.startsWith('insuredObject.riskFactors'));
  }  
}