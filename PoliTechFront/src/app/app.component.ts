import {Component, inject} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToolbarComponent } from './shared/components/toolbar/toolbar.component';
import {AuthService} from './shared/services/auth.service';

@Component({
    selector: 'app-root',
    imports: [RouterOutlet, ToolbarComponent],
    templateUrl: './app.component.html',
    styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'ng-front4';

  private authService = inject(AuthService);
  ngOnInit() {
    this.authService.initializeAuthState()?.subscribe();
  }
}
