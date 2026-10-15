import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { PolicyDraftStore } from '../../state/policy-draft.store';
import { Person } from '../../../../shared/models/person.model';
import { Contacts } from '../../../../shared/models/contacts.model';
import { Identifier } from '../../../../shared/models/identifier.model';
import { Address } from '../../../../shared/models/address.model';

export const DOCUMENT_TYPE_OPTIONS: { value: string; label: string }[] = [
  { value: 'PASSPORT', label: 'Паспорт' }
];

@Component({
  selector: 'app-policy-holder-person',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatCardModule],
  templateUrl: './policy-holder-person.component.html',
  styleUrl: './policy-holder-person.component.scss'
})
export class PolicyHolderPersonComponent {
  readonly store = inject(PolicyDraftStore);
  readonly documentTypes = DOCUMENT_TYPE_OPTIONS;

  readonly person = computed(() => this.store.policy().policyHolder.person);
  readonly contacts = computed(() => this.store.policy().policyHolder.contacts);
  readonly document = computed(() => this.store.policy().policyHolder.identifiers?.[0] ?? null);
  readonly address = computed(() => this.store.policy().policyHolder.addresses?.[0] ?? null);

  onPersonField(field: 'firstName' | 'lastName' | 'middleName', event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.store.patchPolicyHolder(holder => {
      if (!holder.person) {
        holder.person = new Person();
      }
      holder.person[field] = value;
    });
  }

  onBirthDate(event: Event): void {
    const iso = (event.target as HTMLInputElement).value || '';
    this.store.patchPolicyHolder(holder => {
      if (!holder.person) {
        holder.person = new Person();
      }
      holder.person.birthDate = iso;
    });
  }

  onContactField(field: 'phone' | 'email', event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.store.patchPolicyHolder(holder => {
      if (!holder.contacts) {
        holder.contacts = new Contacts();
      }
      holder.contacts[field] = value;
    });
  }

  onDocType(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.store.patchPolicyHolder(holder => {
      this.ensureIdentifier(holder).typeCode = value;
    });
  }

  onDocField(field: 'number' | 'whom' | 'divisionCode', event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.store.patchPolicyHolder(holder => {
      this.ensureIdentifier(holder)[field] = value;
    });
  }

  onDocIssueDate(event: Event): void {
    const iso = (event.target as HTMLInputElement).value || '';
    this.store.patchPolicyHolder(holder => {
      this.ensureIdentifier(holder).dateIssue = iso;
    });
  }

  onAddress(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.store.patchPolicyHolder(holder => {
      if (!holder.addresses?.length) {
        holder.addresses = [new Address()];
      }
      holder.addresses[0].addressStr = value;
    });
  }

  private ensureIdentifier(holder: { identifiers?: Identifier[] }): Identifier {
    if (!holder.identifiers?.length) {
      holder.identifiers = [new Identifier({ typeCode: 'PASSPORT' })];
    }
    return holder.identifiers[0];
  }
}
