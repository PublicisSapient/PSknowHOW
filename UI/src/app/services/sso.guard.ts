import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, first, map } from 'rxjs/operators';
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
      return this.getSSOUserInfo();
    }
  }

  getSSOUserInfo() {
    return this.httpService.getSSOUserInfo().pipe(map(response =>{
      console.log('response from user info call',response);
      if (response['success']) {
          localStorage.setItem('user_name', response['data']?.username);
          localStorage.setItem('projectsAccess', JSON.stringify(response['data']['projectsAccess']));
          localStorage.setItem('authorities', this.aesEncryption.convertText(JSON.stringify(response['data']['authorities']), 'encrypt'));

         //navigate to profile or dashboard screen
          if (this.redirectToProfile()) {
            this.router.navigate(['./dashboard/Config/Profile']);
            return false;
          } else {
            this.router.navigate(['./dashboard/']);
            return false;
          }
      }
    }),
    catchError((error) => {
      console.log('error -->',error);
      this.router.navigate(['./authentication-fail']);
      return of(false);
    }),
    first());
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
