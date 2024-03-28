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
import { Router, RouteConfigLoadStart, RouteConfigLoadEnd, NavigationEnd, ActivatedRoute } from '@angular/router';
import { PrimeNGConfig } from 'primeng/api';
import { environment } from 'src/environments/environment';
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})



export class AppComponent implements OnInit {

  loadingRouteConfig: boolean;

  authorized = <boolean>true;

  constructor(public router: Router, private service: SharedService, private getAuth: GetAuthService, private httpService: HttpService, private primengConfig: PrimeNGConfig,
    public ga: GoogleAnalyticsService, private authorisation: GetAuthorizationService, private route: ActivatedRoute) {
    this.authorized = this.getAuth.checkAuth();
  }

  ngOnInit() {
    this.handleValidateToken();
    /** Fetch projectId and sprintId from query param and save it to global object */
    this.route.queryParams
    .subscribe(params => {
        let nodeId = params.projectId;
        let sprintId = params.sprintId;
        if(nodeId){
          this.service.setProjectQueryParamInFilters(nodeId)
        }
        if(sprintId){
          this.service.setSprintQueryParamInFilters(sprintId)
        }
      }
    );

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

  handleValidateToken(){
    if (environment['AUTHENTICATION_SERVICE']) {
      let url = window.location.href;
      // let redirect_uri = url.split("?")?.[0]
      let authToken = url.split("authToken=")?.[1]?.split("&")?.[0];
      if (authToken) {
        this.service.setAuthToken(authToken);
      }
      let obj = {
        'resource': environment.RESOURCE,
        'authToken': authToken
      };
      console.log("inside handle validate token");
      // this.router.navigateByUrl(redirect_uri);
      // Make API call or initialization logic here...
      this.httpService.getUserValidation(obj).subscribe((response) => {
        if (response?.['success']) {
          this.service.setCurrentUserDetails(response?.['data']);
          localStorage.setItem("user_name", response?.['data']?.user_name);
          localStorage.setItem("user_email", response?.['data']?.user_email);
          // const redirect_uri = localStorage.getItem('redirect_uri');
          if (authToken) {
            this.ga.setLoginMethod(response?.['data'], response?.['data']?.authType);
          }
          // if (redirect_uri) {
          //   if (redirect_uri.startsWith('#')) {
          //     this.router.navigate([redirect_uri.split('#')[1]])
          //   } else {
          //     this.router.navigate([redirect_uri]);
          //   }
          //   localStorage.removeItem('redirect_uri');
          // } 
          // else {
          
          // }
        }
      }, error => {
        console.log(error);
      })
    }
  }
}
