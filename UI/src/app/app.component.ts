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

import { Component, HostListener, OnInit } from '@angular/core';
import { SharedService } from './services/shared.service';
import { GetAuthService } from './services/getauth.service';
import { HttpService } from './services/http.service';
import { GoogleAnalyticsService } from './services/google-analytics.service';
import { GetAuthorizationService } from './services/get-authorization.service';
import { Router, RouteConfigLoadStart, RouteConfigLoadEnd, NavigationEnd, ActivatedRoute } from '@angular/router';
import { PrimeNGConfig } from 'primeng/api';
import { Location } from '@angular/common';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})



export class AppComponent implements OnInit {
  loadingRouteConfig: boolean;
  authorized = <boolean>true;
  refreshCounter: number = 0;
  self: any = this;
  selectedTab: string = '';

  @HostListener('window:scroll', ['$event'])
  onScroll(event) {
    const header = document.querySelector('.header');
    if (window.scrollY > 200) { // adjust the scroll position threshold as needed
      header?.classList.add('scrolled');
    } else {
      header?.classList.remove('scrolled');
    }
  }

  constructor(public router: Router, private service: SharedService, private getAuth: GetAuthService, private httpService: HttpService, private primengConfig: PrimeNGConfig,
    public ga: GoogleAnalyticsService, private authorisation: GetAuthorizationService, private route: ActivatedRoute, private location: Location) {
    this.authorized = this.getAuth.checkAuth();
  }

  ngOnInit() {
    localStorage.removeItem('newUI');

    this.primengConfig.ripple = true;
    this.authorized = this.getAuth.checkAuth();
    this.router.events.subscribe(event => {
      if (event instanceof RouteConfigLoadStart) {
        this.loadingRouteConfig = true;
      } else if (event instanceof RouteConfigLoadEnd) {
        this.loadingRouteConfig = false;
      }

      // insert page in dataLayer
      if (event instanceof NavigationEnd) {
        this.loadingRouteConfig = false;
        const data = {
          url: event.urlAfterRedirects + '/' + (this.service.getSelectedType() || 'Scrum'),
          userRole: this.authorisation.getRole(),
          version: this.httpService.currentVersion,
          uiType: 'New'
        };
        this.ga.setPageLoad(data);
      }

    });

    const url = localStorage.getItem('shared_link');
    let currentUserProjectAccess = JSON.parse(localStorage.getItem('currentUserDetails'))?.projectsAccess?.length ? JSON.parse(localStorage.getItem('currentUserDetails'))?.projectsAccess: [];
    currentUserProjectAccess = currentUserProjectAccess.flatMap(row => row.projects);
    const ifSuperAdmin = JSON.parse(localStorage.getItem('currentUserDetails'))?.authorities?.includes('ROLE_SUPERADMIN');
    if (url) {
      // Extract query parameters
      const queryParams = new URLSearchParams(url.split('?')[1]);
      const stateFilters = queryParams.get('stateFilters');
      const kpiFilters = queryParams.get('kpiFilters');

      if (stateFilters && stateFilters.length > 0) {
        let decodedStateFilters: string = '';

        if (stateFilters?.length <= 8) {
          this.httpService.handleRestoreUrl(stateFilters, kpiFilters)
            .pipe(
              catchError((error) => {
                this.router.navigate(['/dashboard/Error']);
                setTimeout(() => {
                  this.service.raiseError({
                    status: 900,
                    message: error.message || 'Invalid URL.',
                  });
                }, 100);
                return throwError(error);  // Re-throw the error so it can be caught by a global error handler if needed
              })
            )
            .subscribe((response: any) => {
              if (response.success) {
                const longStateFiltersString = response.data['longStateFiltersString'];
                decodedStateFilters = atob(longStateFiltersString);
                this.urlRedirection(decodedStateFilters, currentUserProjectAccess, url, ifSuperAdmin);
              }
            });
        } else {
          decodedStateFilters = atob(stateFilters);
          this.urlRedirection(decodedStateFilters, currentUserProjectAccess, url, ifSuperAdmin);
        }
      }

    } else {
      this.router.navigate(['./dashboard/']);
    }
  }

  urlRedirection(decodedStateFilters, currentUserProjectAccess, url, ifSuperAdmin) {
    const stateFiltersObjLocal = JSON.parse(decodedStateFilters);

    let stateFilterObj = [];
    let projectLevelSelected = false;
    if (typeof stateFiltersObjLocal['parent_level'] === 'object' && stateFiltersObjLocal['parent_level'] && Object.keys(stateFiltersObjLocal['parent_level']).length > 0) {
      stateFilterObj = [stateFiltersObjLocal['parent_level']];
    } else {
      stateFilterObj = stateFiltersObjLocal['primary_level'];
    }

    projectLevelSelected = stateFilterObj?.length && stateFilterObj[0]?.labelName?.toLowerCase() === 'project';

    // Check if user has access to all project in stateFiltersObjLocal['primary_level']
    const hasAllProjectAccess = stateFilterObj?.every(filter =>
      currentUserProjectAccess?.some(project => project.projectId === filter.basicProjectConfigId)
    );

    // Superadmin have all project access hence no need to check project for superadmin
    const hasAccessToAll = ifSuperAdmin || hasAllProjectAccess;

    if (projectLevelSelected) {
      if (hasAccessToAll) {
        this.router.navigate([url]);
      } else {
        this.router.navigate(['/dashboard/Error']);
        this.service.raiseError({
          status: 901,
          message: 'No project access.',
        });
      }
    }
  }

}
