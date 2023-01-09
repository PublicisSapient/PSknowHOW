import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { catchError, first, map, mergeMap } from 'rxjs/operators';
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
      return this.getSSOUserAuthInfo();
    }
  }

  getSSOUserAuthInfo() {
    return this.httpService.getSSOUserAuthInfo().pipe(mergeMap(res => {
      console.log('Response -->',res);
      console.log('response Headers --> ',res['headers']);
      console.log('username from response headers -->',res['headers']?.get('username'));
      if (res['status'] === 200 && res['headers']?.get('username')) {
        const userName = res['headers']?.get('username');
        console.log('userName -->',res['headers']?.get('username'));
        const checkifUserAlreadyLoggedIn = localStorage.getItem('user_name') ? (localStorage.getItem('user_name') === userName) : false;
        if(!checkifUserAlreadyLoggedIn){
          console.log('calling user info api using username');
            return this.getSSOUserInfo(userName);
        }else{
          if (this.redirectToProfile()) {
            this.router.navigate(['./dashboard/Config/Profile']);
            return of(false);
          } else {
            this.router.navigate(['./dashboard/']);
            return of(false);
          }
        }
      }
    }),
      catchError((error) => {
        console.log('error -->',error);
        this.router.navigate(['./authentication-fail']);
        return of(false);
      }), first());
  }

  getSSOUserInfo(userName) {
    return this.httpService.getSSOUserInfo(userName).pipe(map(response =>{
      console.log('response from user info call',response);
      if (response['success'] || response['authenticated']) {
        if(response['success']){
          console.log('setting localstorage');
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
      }
    }));
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
