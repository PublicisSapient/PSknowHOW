import { AfterViewInit, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpService } from 'src/app/services/http.service';
import { Router } from '@angular/router';
@Component({
  selector: 'app-verify',
  templateUrl: './verify.component.html',
  styleUrls: ['./verify.component.css']
})
export class VerifyComponent implements OnInit, AfterViewInit {

  constructor(
    private http: HttpService,
    private route: ActivatedRoute,
    public router: Router,
  ) { }

  ngOnInit(): void {
   
  }

  ngAfterViewInit(){
    let authToken:string = '';
    let redirect_uri:string = '';
    this.route.queryParams.subscribe(params => {
      authToken = params['authToken'];
      redirect_uri = params['authToken'];
    });
    this.validateUser(authToken, redirect_uri);
  }

  validateUser(authToken, redirect_uri){
    // let obj = {
    //   'resource': 'knowhow',
    //   'authToken': authToken
    // };
    
    this.http.getUserValidation(authToken).subscribe((response) => {
      if(response && response['success']){
        this.router.navigateByUrl(redirect_uri);
      }else{
        this.router.navigateByUrl('/pageNotFound');
      }
    })
  }
}
