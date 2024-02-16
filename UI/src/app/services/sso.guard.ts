import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, first, map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { HttpService } from './http.service';
import { SharedService } from './shared.service';

@Injectable({
  providedIn: 'root'
})
export class SSOGuard implements CanActivate {

  constructor(private router: Router,private httpService: HttpService, private sharedService: SharedService){}

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
    return this.httpService.getSSOUserInfo().pipe(map(response => {
      console.log('response from user info call', response);
      if (response['success']) {
        this.sharedService.setCurrentUserDetails({ user_name: response['data']?.username });
        this.sharedService.setCurrentUserDetails({ projectsAccess: response['data']['projectsAccess'] });
        this.sharedService.setCurrentUserDetails({ authorities:response['data']['authorities'] });
        this.httpService.getAuthDetails();
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
        console.log('error -->', error);
        this.router.navigate(['./authentication-fail']);
        return of(false);
      }),
      first());
  }

  redirectToProfile() {
    // if (!localStorage.getItem('user_email') || localStorage.getItem('user_email') === '') {
    //     return true;
    // }
    const authorities = this.sharedService.getCurrentUserDetails('authorities') ? this.sharedService.getCurrentUserDetails('authorities') : [];
    if (authorities && authorities.includes('ROLE_SUPERADMIN')) {
        return false;
    } else if (this.sharedService.getCurrentUserDetails('projectsAccess') === 'undefined' || !this.sharedService.getCurrentUserDetails('projectsAccess').length) {
        return true;
    }


}
}
