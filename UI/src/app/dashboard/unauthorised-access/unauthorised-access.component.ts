import { Component, OnInit } from '@angular/core';
import { HelperService } from 'src/app/services/helper.service';

@Component({
  selector: 'app-unauthorised-access',
  templateUrl: './unauthorised-access.component.html',
  styleUrls: ['./unauthorised-access.component.css'],
})
export class UnauthorisedAccessComponent {
  constructor(private helperService: HelperService) {}

  reloadApp() {
    this.helperService.windowReload();
  }
}
