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
import { HelperService } from './services/helper.service';
import { Location } from '@angular/common';
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

  constructor(private router: Router, private service: SharedService, private getAuth: GetAuthService, private httpService: HttpService, private primengConfig: PrimeNGConfig,
    public ga: GoogleAnalyticsService, private authorisation: GetAuthorizationService, private route: ActivatedRoute, private helperService: HelperService, private location: Location) {
    this.authorized = this.getAuth.checkAuth();
  }

  ngOnInit() {
    localStorage.removeItem('newUI');
    /** Fetch projectId and sprintId from query param and save it to global object */
    this.route.queryParams
      .subscribe(params => {
        let nodeId = params.projectId;
        let sprintId = params.sprintId;
        if (nodeId) {
          this.service.setProjectQueryParamInFilters(nodeId)
        }
        if (sprintId) {
          this.service.setSprintQueryParamInFilters(sprintId)
        }
      });

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

        if (!this.refreshCounter) {
          ++this.refreshCounter;

          let selectedTab = this.location.path();
          selectedTab = selectedTab?.split('/')[2] ? selectedTab?.split('/')[2] : 'iteration';
          selectedTab = selectedTab?.split(' ').join('-').toLowerCase();
          this.selectedTab = selectedTab.split('?statefilters=')[0];
          this.service.setSelectedBoard(this.selectedTab);

          const urlPath = decodeURIComponent(window.location.hash);
          const queryParamsIndex = urlPath.indexOf('?');

          if (queryParamsIndex !== -1) {
            const queryString = urlPath.slice(queryParamsIndex + 1);

            // Parse query string into key-value pairs
            const urlParams = new URLSearchParams(queryString);

            let param = urlParams.get('stateFilters');
            param = atob(param);
            console.log('param', param);
            // param = param.replace(/###/gi, '___');

            this.helperService.setBackupOfUrlFilters(param);
          }
        }
      }

    });
  }

}
