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
import { Router, CanActivate } from '@angular/router';
import { GetAuthorizationService } from './get-authorization.service';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

@Injectable()
export class RoleGuard implements CanActivate {

    constructor(private router: Router, private getAuthorization: GetAuthorizationService) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        if(route.url[0].path === 'GrantRequests'  && this.getAuthorization.checkIfProjectAdmin()) {
            return true;
        }
        if (this.getAuthorization.checkIfSuperUser()) {
            // logged in as SuperUser so return true
            return true;
        } else {
            // not logged in so redirect to raise request
            this.router.navigate(['./dashboard/Config/Profile/RaiseRequest']);
            return false;
        }
    }
}
