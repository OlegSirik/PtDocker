import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { PolicyDraftStore } from '../../state/policy-draft.store';
import { Person } from '../../../../shared/models/person.model';

@Component({
  selector: 'app-insured02',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatCardModule],
  templateUrl: './insured02.component.html',
  styleUrl: './insured02.component.scss'
})
export class Insured02Component {
  readonly store = inject(PolicyDraftStore);

  readonly person = computed(() => this.store.policy().insuredObjects[0]?.person ?? null);

  onPersonField(field: 'firstName' | 'lastName' | 'middleName', event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.store.patchInsuredObject(0, io => {
      if (!io.person) {
        io.person = new Person();
      }
      io.person[field] = value;
    });
  }

  onBirthDate(event: Event): void {
    const iso = (event.target as HTMLInputElement).value || '';
    this.store.patchInsuredObject(0, io => {
      if (!io.person) {
        io.person = new Person();
      }
      io.person.birthDate = iso;
    });
  }

  onGender(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.store.patchInsuredObject(0, io => {
      if (!io.person) {
        io.person = new Person();
      }
      io.person.gender = value;
    });
  }
}
