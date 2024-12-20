import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-unauthorised-access',
  templateUrl: './unauthorised-access.component.html',
  styleUrls: ['./unauthorised-access.component.css']
})
export class UnauthorisedAccessComponent {

  constructor() { }

  reloadApp(){
    window.location.reload();
  }

}
