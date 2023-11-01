import { AfterViewInit, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpService } from 'src/app/services/http.service';

@Component({
  selector: 'app-verify',
  templateUrl: './verify.component.html',
  styleUrls: ['./verify.component.css']
})
export class VerifyComponent implements OnInit, AfterViewInit {

  constructor(
    private http: HttpService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
   
  }

  ngAfterViewInit(){
    let authToken:string = '';
    this.route.queryParams.subscribe(params => {
      authToken = params['authToken'];
    });
    this.validateUser(authToken);
  }

  validateUser(authToken){
    let obj = {
      'resource': 'knowhow',
      'authToken': authToken
    };
    
    this.http.getUserValidation(obj).subscribe((response) => {
      console.log(response);
    })
  }
}
