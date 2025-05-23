/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

import { Injectable } from '@angular/core';
import {
  Router,
  CanActivate,
  UrlTree,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
} from '@angular/router';
import { GetAuthService } from './getauth.service';
import { SharedService } from './shared.service';
import { HttpService } from './http.service';
import { Observable, pipe } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';

@Injectable()
export class AuthGuard implements CanActivate {
  constructor(
    private router: Router,
    private getAuth: GetAuthService,
    private sharedService: SharedService,
    private httpService: HttpService,
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot,
  ):
    | Observable<boolean | UrlTree>
    | Promise<boolean | UrlTree>
    | boolean
    | UrlTree {
    const currentUserDetails = this.httpService.currentUserDetails;

    if (currentUserDetails) {
      if (currentUserDetails['authorities']) {
        return true;
      } else {
        if (environment.AUTHENTICATION_SERVICE) {
          /** redirect to central login url*/
          if (environment.CENTRAL_LOGIN_URL) {
            window.location.href = environment.CENTRAL_LOGIN_URL;
          }
        } else {
          const queryParams = route.queryParams;
          this.router.navigate(['./authentication/login'], {
            queryParams: queryParams,
          });
        }
        return false;
      }
    } else {
      return this.httpService.getCurrentUserDetails().pipe(
        map((details) => {
          if (details['success']) {
            this.httpService.setCurrentUserDetails(details['data']);
            if (details['data']['authorities']) {
              return true;
            }
            if (environment.AUTHENTICATION_SERVICE) {
              /** redirect to central login url*/
              if (environment.CENTRAL_LOGIN_URL) {
                window.location.href = environment.CENTRAL_LOGIN_URL;
              }
            } else {
              const queryParams = route.queryParams;
              this.router.navigate(['./authentication/login'], {
                queryParams: queryParams,
              });
            }
            return false;
          }
        }),
      );
    }
  }
}
