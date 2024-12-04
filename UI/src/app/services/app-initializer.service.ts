import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { SharedService } from './shared.service';
import { HttpService } from './http.service';
import { ActivatedRoute, Router, Routes } from '@angular/router';
import { FeatureFlagsService } from './feature-toggle.service';
import { HttpClient } from '@angular/common/http';
import { GoogleAnalyticsService } from './google-analytics.service';
import { tap } from 'rxjs/operators';
import { Logged } from '../services/logged.guard';
import { SSOGuard } from '../services/sso.guard';
import { FeatureGuard } from '../services/feature.guard';
import { AccessGuard } from '../services/access.guard';
import { DashboardComponent } from '../dashboard/dashboard.component';
import { IterationComponent } from '../dashboard/iteration/iteration.component';
import { DeveloperComponent } from '../dashboard/developer/developer.component';
import { MaturityComponent } from '../dashboard/maturity/maturity.component';
import { BacklogComponent } from '../dashboard/backlog/backlog.component';
import { MilestoneComponent } from '../dashboard/milestone/milestone.component';
import { DoraComponent } from '../dashboard/dora/dora.component';
import { ExecutiveComponent } from '../dashboard/executive/executive.component';
import { ErrorComponent } from '../dashboard/error/error.component';
import { UnauthorisedAccessComponent } from '../dashboard/unauthorised-access/unauthorised-access.component';
import { AuthGuard } from './auth.guard';
import { SsoAuthFailureComponent } from '../component/sso-auth-failure/sso-auth-failure.component';
import { PageNotFoundComponent } from '../page-not-found/page-not-found.component';
import { DashboardV2Component } from '../dashboardv2/dashboard-v2/dashboard-v2.component';
import { ExecutiveV2Component } from '../dashboardv2/executive-v2/executive-v2.component';

@Injectable({
    providedIn: 'root'
})
export class AppInitializerService {

    constructor(private sharedService: SharedService, private httpService: HttpService, private router: Router, private featureToggleService: FeatureFlagsService, private http: HttpClient, private route: ActivatedRoute, private ga: GoogleAnalyticsService) {
    }
    commonRoutes: Routes = localStorage.getItem('newUI') ? [
        { path: '', redirectTo: 'iteration', pathMatch: 'full' },
        // {
        //     // path: 'iteration', component: IterationComponent, pathMatch: 'full', canActivate: [AccessGuard],
        //     // data: {
        //     //     feature: "Iteration"
        //     // }
        // },
        {
            path: 'kpi-maturity', component: MaturityComponent, pathMatch: 'full', canActivate: [AccessGuard],
            data: {
                feature: "Maturity"
            }
        }
    ] : [
        { path: '', redirectTo: 'iteration', pathMatch: 'full' },
        {
            path: 'my-knowhow', component: ExecutiveComponent, pathMatch: 'full', canActivate: [AccessGuard],
            data: {
                feature: "My Dashboard"
            }
        },
        // {
        //     // path: 'iteration', component: IterationComponent, pathMatch: 'full', canActivate: [AccessGuard],
        //     // data: {
        //     //     feature: "Iteration"
        //     // }
        // },
        {
            path: 'developer', component: DeveloperComponent, pathMatch: 'full', canActivate: [AccessGuard],
            data: {
                feature: "Developer"
            }
        },
        {
            path: 'kpi-maturity', component: MaturityComponent, pathMatch: 'full', canActivate: [AccessGuard],
            data: {
                feature: "Maturity"
            }
        },
        {
            path: 'backlog', component: BacklogComponent, pathMatch: 'full', canActivate: [AccessGuard],
            data: {
                feature: "Backlog"
            }
        },
        {
            path: 'release', component: MilestoneComponent, pathMatch: 'full', canActivate: [AccessGuard],
            data: {
                feature: "Release"
            }
        },
        {
            path: 'dora', component: DoraComponent, pathMatch: 'full', canActivate: [AccessGuard],
            data: {
                feature: "Dora"
            }
        }
    ];
    routes: Routes = [
        { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
        {
            path: 'authentication',
            loadChildren: () => import('../../app/authentication/authentication.module').then(m => m.AuthenticationModule),
            resolve: [Logged],
            canActivate: [SSOGuard]
        },
        {
            path: 'dashboard', component: !localStorage.getItem('newUI') ? DashboardComponent : DashboardV2Component,
            canActivateChild: [FeatureGuard],
            children: [
                ...this.commonRoutes,
                {
                    path: 'Config',
                    loadChildren: () => import('../../app/config/config.module').then(m => m.ConfigModule),
                    data: {
                        feature: "Config"
                    }
                },
                { path: ':boardName', component: !localStorage.getItem('newUI') ? ExecutiveComponent : ExecutiveV2Component, pathMatch: 'full' },
                { path: 'Error', component: ErrorComponent, pathMatch: 'full' },
                { path: 'unauthorized-access', component: UnauthorisedAccessComponent, pathMatch: 'full' },

            ], canActivate: [AuthGuard],
        },
        { path: 'authentication-fail', component: SsoAuthFailureComponent },
        { path: '**', redirectTo: 'authentication' }
    ];

    routesAuth: Routes = [
        { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
        {
            path: 'dashboard', component: !localStorage.getItem('newUI') ? DashboardComponent : DashboardV2Component,
            children: [
            ...this.commonRoutes,
                { path: 'Error', component: ErrorComponent, pathMatch: 'full' },
                { path: 'unauthorized-access', component: UnauthorisedAccessComponent, pathMatch: 'full' },
                {
                    path: 'Config',
                    loadChildren: () => import('../../app/config/config.module').then(m => m.ConfigModule),
                    data: {
                        feature: "Config"
                    }
                },
                { path: ':boardName', component: !localStorage.getItem('newUI') ? ExecutiveComponent : ExecutiveV2Component, pathMatch: 'full' },

            ], canActivate: [AuthGuard],
        },
        { path: 'pageNotFound', component: PageNotFoundComponent },
        { path: '**', redirectTo: 'pageNotFound' }
    ];

    async checkFeatureFlag() {
        let loc = window.location.hash ? JSON.parse(JSON.stringify(window.location.hash?.split('#')[1])) : '';
        return new Promise<void>(async (resolve, reject) => {
            if (!environment['production']) {
                this.featureToggleService.config = this.featureToggleService.loadConfig().then((res) => res);
                this.validateToken(loc);
            } else {
                const env$ = this.http.get('assets/env.json').pipe(
                    tap(env => {
                        environment['baseUrl'] = env['baseUrl'] || '';
                        environment['SSO_LOGIN'] = env['SSO_LOGIN'] === 'true' ? true : false;
                        environment['AUTHENTICATION_SERVICE'] = env['AUTHENTICATION_SERVICE'] === 'true' ? true : false;
                        environment['CENTRAL_LOGIN_URL'] = env['CENTRAL_LOGIN_URL'] || '';
                        environment['CENTRAL_API_URL'] = env['CENTRAL_API_URL'] || '';
                        environment['MAP_URL'] = env['MAP_URL'] || '';
                        environment['RETROS_URL'] = env['RETROS_URL'] || '';
                        environment['SPEED_SUITE'] = env['SPEED_SUITE'] === 'true' ? true : false;
                        this.validateToken(loc);
                    }));
                env$.toPromise().then(async res => {
                    this.featureToggleService.config = this.featureToggleService.loadConfig().then((res) => res);
                });
            }



            // load google Analytics script on all instances except local and if customAPI property is true
            let addGAScript = await this.featureToggleService.isFeatureEnabled('GOOGLE_ANALYTICS');
            if (addGAScript) {
                if (window.location.origin.indexOf('localhost') === -1) {
                    this.ga.load('gaTagManager').then(data => {
                        console.log('script loaded ', data);
                    })
                }
            }
            resolve();
        })
    }

  validateToken(location) {
    return new Promise<void>((resolve, reject) => {
        if (!environment['AUTHENTICATION_SERVICE']) {
            this.router.resetConfig([...this.routes]);
            this.router.navigate([location]);
        } else {
            // Make API call or initialization logic here...
            this.httpService.getUserDetailsForCentral().subscribe((response) => {
                  if (response?.['success']) {
                      this.sharedService.setCurrentUserDetails(response?.['data']);
                      this.router.resetConfig([...this.routesAuth]);
                      localStorage.setItem("user_name", response?.['data']?.user_name);
                      localStorage.setItem("user_email", response?.['data']?.user_email);
                      this.ga.setLoginMethod(response?.['data'], response?.['data']?.authType);
                  }

                  if (location) {
                      let redirect_uri = JSON.parse(localStorage.getItem('redirect_uri'));
                      if (redirect_uri) {
                          localStorage.removeItem('redirect_uri');
                      }
                      this.router.navigateByUrl(location);
                  } else {
                      this.router.navigate(['/dashboard/iteration']);
                  }
                }, error => {
                    console.log(error);
                });


            }
            resolve();

        })

    }
}
