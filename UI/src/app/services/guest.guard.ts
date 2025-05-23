import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { SharedService } from './shared.service';
@Injectable()
export class GuestGuard implements CanActivate {
  constructor(private sharedService: SharedService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    if (!this.sharedService.getCurrentUserDetails('authorities')) {
      return false;
    }
    if (
      this.sharedService
        .getCurrentUserDetails('authorities')
        .includes('ROLE_GUEST')
    ) {
      this.router.navigate(['/dashboard/iteration']);
      return false;
    } else {
      return true;
    }
  }
}
