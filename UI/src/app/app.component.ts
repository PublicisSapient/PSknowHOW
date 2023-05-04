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

import { Component, OnInit } from '@angular/core';
import { SharedService } from './services/shared.service';
import { GetAuthService } from './services/getauth.service';
import { HttpService } from './services/http.service';
import { GoogleAnalyticsService } from './services/google-analytics.service';
import { GetAuthorizationService } from './services/get-authorization.service';
import { Router, RouteConfigLoadStart, RouteConfigLoadEnd, NavigationEnd } from '@angular/router';
import { PrimeNGConfig } from 'primeng/api';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})



export class AppComponent implements OnInit {

  loadingRouteConfig: boolean;

  authorized = <boolean>true;

  constructor(private router: Router, private service: SharedService, private getAuth: GetAuthService, private httpService: HttpService, private primengConfig: PrimeNGConfig,
    private ga: GoogleAnalyticsService, private authorisation: GetAuthorizationService) {
    this.authorized = this.getAuth.checkAuth();
  }

  ngOnInit() {
    // load google Analytics script on all instances except local and if customAPI property is true
    this.httpService.getAnalyticsFlag()
      .subscribe(flag => {
        if (flag['success'] && flag['data'] && flag['data']['analyticsSwitch']) {
          if (window.location.origin.indexOf('localhost') === -1) {
            this.ga.load('gaTagManager').then(data => {
              console.log('script loaded ', data);
            }).catch(error => console.log(error));
          }
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
          url: event.urlAfterRedirects + '/' + (this.service.getSelectedType() ? this.service.getSelectedType() : 'Scrum'),
          userRole: this.authorisation.getRole(),
          version: this.httpService.currentVersion
        };
        this.ga.setPageLoad(data);
      }

    });
  }
}
