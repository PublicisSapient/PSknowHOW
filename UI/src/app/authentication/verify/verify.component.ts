import { AfterViewInit, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpService } from 'src/app/services/http.service';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { SharedService } from '../../services/shared.service';
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
    private sharedService: SharedService
  ) { }

  ngOnInit(): void {
   
  }

  ngAfterViewInit(){
    let authToken:string = '';
    authToken = this.sharedService.getAuthToken()
    // this.validateUser(authToken);
  }

  validateUser(authToken){
    let obj = {
      'resource': environment.RESOURCE,
      'authToken': authToken
    };
    
    this.http.getUserValidation(obj).subscribe((response) => {
      if(response && response['success']){
        this.sharedService.setCurrentUserDetails(response?.['data'])
        localStorage.setItem("user_name", response?.['data']?.user_name);
        localStorage.setItem("user_email", response?.['data']?.user_email);
        this.router.navigate(['/dashboard/iteration']);
      }
      else{
        this.router.navigate(['/pageNotFound']);
      }
    })
  }
}
