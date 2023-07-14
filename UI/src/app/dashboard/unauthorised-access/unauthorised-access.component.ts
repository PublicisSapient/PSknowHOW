import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-unauthorised-access',
  templateUrl: './unauthorised-access.component.html',
  styleUrls: ['./unauthorised-access.component.css']
})
export class UnauthorisedAccessComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

  reloadApp(){
    window.location.reload();
  }

}
