import { AfterViewInit, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpService } from 'src/app/services/http.service';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
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
    this.route.queryParams.subscribe(params => {
      authToken = params['authToken'];
    });
    this.validateUser(authToken);
  }

  validateUser(authToken){
    let obj = {
      'resource': environment.RESOURCE,
      'authToken': authToken
    };
    
    this.http.getUserValidation(obj).subscribe((response) => {
      if(response && response['success']){
        this.router.navigate(['./dashboard/iteration']);
      }else{
        this.router.navigateByUrl('/pageNotFound');
      }
    })
  }
}
