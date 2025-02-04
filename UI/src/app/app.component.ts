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

  constructor(public router: Router, private service: SharedService, private getAuth: GetAuthService, private httpService: HttpService, private primengConfig: PrimeNGConfig,
    public ga: GoogleAnalyticsService, private authorisation: GetAuthorizationService, private route: ActivatedRoute, private helperService: HelperService, private location: Location) {
    this.authorized = this.getAuth.checkAuth();
  }

  ngOnInit() {
    localStorage.removeItem('newUI');

    /** Fetch projectId and sprintId from query param and save it to global object */
    this.route.queryParams
      .subscribe(params => {
        if (!this.refreshCounter) {
          let stateFiltersParam = params['stateFilters'];
          let kpiFiltersParam = params['kpiFilters'];
          // console.log('params', stateFiltersParam, kpiFiltersParam);

          if (stateFiltersParam?.length) {
            let selectedTab = this.location.path();
            selectedTab = selectedTab?.split('/')[2] ? selectedTab?.split('/')[2] : 'iteration';
            selectedTab = selectedTab?.split(' ').join('-').toLowerCase();
            this.selectedTab = selectedTab.split('?statefilters=')[0];
            this.service.setSelectedBoard(this.selectedTab);

            if (stateFiltersParam?.length <= 8 && kpiFiltersParam?.length <= 8) {
              this.httpService.handleRestoreUrl(stateFiltersParam, kpiFiltersParam).subscribe((response: any) => {
                console.log('response', response);
                try {
                  if (response.success) {
                    const longKPIFiltersString = response.data['longKPIFiltersString'];
                    const longStateFiltersString = response.data['longStateFiltersString'];
                    stateFiltersParam = atob(longStateFiltersString);
                    // stateFiltersParam = stateFiltersParam.replace(/###/gi, '___');

                    // const kpiFiltersParam = params['kpiFilters'];
                    if (longKPIFiltersString) {
                      const kpiFilterParamDecoded = atob(longKPIFiltersString);
                      console.log('kpiFilterParamDecoded', kpiFilterParamDecoded);
                      const kpiFilterValFromUrl = (kpiFilterParamDecoded && JSON.parse(kpiFilterParamDecoded)) ? JSON.parse(kpiFilterParamDecoded) : this.service.getKpiSubFilterObj();
                      this.service.setKpiSubFilterObj(kpiFilterValFromUrl);
                    }

                    this.service.setBackupOfFilterSelectionState(JSON.parse(stateFiltersParam));
                    this.refreshCounter++;
                  } else {
                    this.router.navigate(['/dashboard/Error']); // Redirect to the error page
                    setTimeout(() => {
                      this.service.raiseError({
                        status: 900,
                        message: response.message || 'Invalid URL.'
                      });
                    });
                  }
                } catch (error) {
                  this.router.navigate(['/dashboard/Error']); // Redirect to the error page
                  setTimeout(() => {
                    this.service.raiseError({
                      status: 900,
                      message: 'Invalid URL.'
                    });
                  })
                }
              });
            } else {
              try {
                // let selectedTab = this.location.path();
                // selectedTab = selectedTab?.split('/')[2] ? selectedTab?.split('/')[2] : 'iteration';
                // selectedTab = selectedTab?.split(' ').join('-').toLowerCase();
                // this.selectedTab = selectedTab.split('?statefilters=')[0];
                // this.service.setSelectedBoard(this.selectedTab);

                stateFiltersParam = atob(stateFiltersParam);
                console.log('stateFiltersParam', stateFiltersParam);
                // stateFiltersParam = stateFiltersParam.replace(/###/gi, '___');

                // const kpiFiltersParam = params['kpiFilters'];
                if (kpiFiltersParam) {
                  const kpiFilterParamDecoded = atob(kpiFiltersParam);
                  const kpiFilterValFromUrl = (kpiFilterParamDecoded && JSON.parse(kpiFilterParamDecoded)) ? JSON.parse(kpiFilterParamDecoded) : this.service.getKpiSubFilterObj();
                  this.service.setKpiSubFilterObj(kpiFilterValFromUrl);
                }

                this.service.setBackupOfFilterSelectionState(JSON.parse(stateFiltersParam));
                this.refreshCounter++;
              } catch (error) {
                this.router.navigate(['/dashboard/Error']); // Redirect to the error page
                setTimeout(() => {
                  this.service.raiseError({
                    status: 900,
                    message: 'Invalid URL.'
                  });
                }, 100);
              }
            }
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
          url: event.urlAfterRedirects + '/' + (this.service.getSelectedType() || 'Scrum'),
          userRole: this.authorisation.getRole(),
          version: this.httpService.currentVersion,
          uiType: 'New'
        };
        this.ga.setPageLoad(data);
      }

    });

    const url = localStorage.getItem('shared_link');
    const currentUserProjectAccess = JSON.parse(localStorage.getItem('currentUserDetails'))?.projectsAccess[0]?.projects;
    const ifSuperAdmin = JSON.parse(localStorage.getItem('currentUserDetails'))?.authorities?.includes('ROLE_SUPERADMIN');
    if (url) {
      // Extract query parameters
      const queryParams = new URLSearchParams(url.split('?')[1]);
      const stateFilters = queryParams.get('stateFilters');
      const kpiFilters = queryParams.get('kpiFilters');

      if (stateFilters && stateFilters.length > 0) {
        let decodedStateFilters: string = '';
        let stateFiltersObj: Object = {};

        if (stateFilters?.length <= 8) {
          this.httpService.handleRestoreUrl(stateFilters, kpiFilters).subscribe((response: any) => {
            if (response.success) {
              const longStateFiltersString = response.data['longStateFiltersString'];
              decodedStateFilters = atob(longStateFiltersString);
              this.urlRedirection(decodedStateFilters, stateFiltersObj, currentUserProjectAccess, url, ifSuperAdmin);
            } else {
              this.router.navigate(['/dashboard/Error']);
              setTimeout(() => {
                this.service.raiseError({
                  status: 900,
                  message: response.message || 'Invalid URL.',
                });
              }, 100);
            }
          });
        } else {
          decodedStateFilters = atob(stateFilters);
          this.urlRedirection(decodedStateFilters, stateFiltersObj, currentUserProjectAccess, url, ifSuperAdmin);
        }
      }

    } else {
      this.router.navigate(['./dashboard/']);
    }
  }

  urlRedirection(decodedStateFilters, stateFiltersObj, currentUserProjectAccess, url, ifSuperAdmin) {
    let stateFiltersObjLocal = stateFiltersObj;

    stateFiltersObjLocal = JSON.parse(decodedStateFilters);

    let stateFilterObj = [];
    let projectLevelSelected = false;
    if (typeof stateFiltersObjLocal['parent_level'] === 'object' && Object.keys(stateFiltersObjLocal['parent_level']).length > 0) {
      stateFilterObj = [stateFiltersObjLocal['parent_level']];
    } else {
      stateFilterObj = stateFiltersObjLocal['primary_level'];
    }

    projectLevelSelected = stateFilterObj?.length && stateFilterObj[0]?.labelName?.toLowerCase() === 'project';


    // Check if user has access to all project in stateFiltersObjLocal['primary_level']
    const hasAllProjectAccess = stateFilterObj.every(filter =>
      currentUserProjectAccess?.some(project => project.projectId === filter.basicProjectConfigId)
    );

    // Superadmin have all project access hence no need to check project for superadmin
    const hasAccessToAll = ifSuperAdmin || hasAllProjectAccess;

    if (projectLevelSelected) {
      if (hasAccessToAll) {
        this.router.navigate([JSON.parse(JSON.stringify(url))]);
      } else {
        this.router.navigate(['/dashboard/Error']);
        setTimeout(() => {
          this.service.raiseError({
            status: 901,
            message: 'No project access.',
          });
        }, 100);
      }
    }
  }

}
