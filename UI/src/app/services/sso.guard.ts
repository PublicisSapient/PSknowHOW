import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { first, mergeMap } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { HttpService } from './http.service';
import { TextEncryptionService } from './text.encryption.service';

@Injectable({
  providedIn: 'root'
})
export class SSOGuard implements CanActivate {

  constructor(private aesEncryption: TextEncryptionService ,private router: Router,private httpService: HttpService){}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    if (!environment.SSO_LOGIN) {
      return true;
    } else {
      //fetch token and user name
      this.getSSOUserAuthInfo().pipe(first(), mergeMap(res => {
        if (res['status'] === 200 && res.headers.get('username')) {
          const userName = res.headers.get('username');
          const checkifUserAlreadyLoggedIn = localStorage.getItem('user_name') ? (localStorage.getItem('user_name') === userName)  : false;

          if(!checkifUserAlreadyLoggedIn){
            return this.getSSOUserInfo(userName);
          }else{
            return of({userName, authenticated : true});
          }
        }else{
          return throwError('Authentication Failed!!!');
        }

      })).subscribe(response =>{
        if (response['success'] || response['authenticated']) {
          if(response['success']){
            localStorage.setItem('user_name', response['data']?.username);
            localStorage.setItem('projectsAccess', JSON.stringify(response['data']['projectsAccess']));
            localStorage.setItem('authorities', this.aesEncryption.convertText(JSON.stringify(response['data']['authorities']), 'encrypt'));
          }

          //navigate to profile or dashboard screen
          if (this.redirectToProfile()) {
            this.router.navigate(['./dashboard/Config/Profile']);
            return false;
          } else {
            this.router.navigate(['./dashboard/']);
            return false;
          }
          return false;

        } else {
          this.router.navigate(['./authentication-fail']);
          return false;
        }
      },
      error =>{
        this.router.navigate(['./authentication-fail']);
        return false;
      });
    }
  }


  getSSOUserInfo(userName) {
    return this.httpService.getSSOUserInfo(userName);
  }

  getSSOUserAuthInfo(){
    return this.httpService.getSSOUserAuthInfo();

    // return of({});
  }

  redirectToProfile() {
    // if (!localStorage.getItem('user_email') || localStorage.getItem('user_email') === '') {
    //     return true;
    // }
    const decryptedText = this.aesEncryption.convertText(localStorage.getItem('authorities'), 'decrypt');
    if (decryptedText && JSON.parse(decryptedText).includes('ROLE_SUPERADMIN')) {
        return false;
    } else if (localStorage.getItem('projectsAccess') === 'undefined' || !localStorage.getItem('projectsAccess').length) {
        return true;
    }


}
}
