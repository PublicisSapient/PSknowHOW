import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { SharedService } from './shared.service';
import { HttpService } from './http.service';
import { ActivatedRoute, Router, Routes } from '@angular/router';
import { FeatureFlagsService } from './feature-toggle.service';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { GoogleAnalyticsService } from './google-analytics.service';
import { ExecutiveComponent } from '../dashboard/executive/executive.component';
import { MaturityComponent } from '../dashboard/maturity/maturity.component';
import { ErrorComponent } from '../dashboard/error/error.component';
import { IterationComponent } from '../dashboard/iteration/iteration.component';
import { DeveloperComponent } from '../dashboard/developer/developer.component';
import { DashboardComponent } from '../dashboard/dashboard.component';
import { AccessGuard } from '../services/access.guard';
import { BacklogComponent } from '../dashboard/backlog/backlog.component';
import { UnauthorisedAccessComponent } from '../dashboard/unauthorised-access/unauthorised-access.component';
import { MilestoneComponent } from '../dashboard/milestone/milestone.component';
import { DoraComponent } from '../dashboard/dora/dora.component';
import { FeatureGuard } from '../services/feature.guard';
import { PageNotFoundComponent } from '../page-not-found/page-not-found.component';
@Injectable({
  providedIn: 'root'
})
export class AppInitializerService {

  constructor(private sharedService: SharedService, private httpService: HttpService, private router: Router, private featureToggleService: FeatureFlagsService, private http: HttpClient, private route: ActivatedRoute, private ga: GoogleAnalyticsService) {
  }

  routes: Routes = [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard', component: DashboardComponent,
        canActivateChild : [FeatureGuard],
        children: [
          { path: '', redirectTo: 'iteration', pathMatch: 'full' },
          {
            path: 'mydashboard', component: IterationComponent, pathMatch: 'full', canActivate: [AccessGuard],
            data: {
              feature: "My Dashboard"
            }
          },
          {
            path: 'iteration', component: IterationComponent, pathMatch: 'full',
            data: {
              feature: "Iteration"
            }
          },
          {
            path: 'developer', component: DeveloperComponent, pathMatch: 'full', canActivate: [AccessGuard],
            data: {
              feature: "Developer"
            }
          },
          {
            path: 'Maturity', component: MaturityComponent, pathMatch: 'full', canActivate: [AccessGuard],
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
          },
          { path: 'Error', component: ErrorComponent, pathMatch: 'full' },
          { path: 'unauthorized-access', component: UnauthorisedAccessComponent, pathMatch: 'full' },
          {
            path: 'Config',
            loadChildren: () => import('../config/config.module').then(m => m.ConfigModule), canLoad: [FeatureGuard],
            data: {
              feature: "Config"
            }
          },
          { path: ':boardName', component: ExecutiveComponent, pathMatch: 'full' },
    
        ],
      },
      { path: 'pageNotFound', component: PageNotFoundComponent },
      { path: '**', redirectTo: 'pageNotFound' }
    ];

  checkFeatureFlag() {
    return new Promise<void>((resolve, reject) => {
      if (!environment['production']) {
        alert("inside app initializer prod" + environment['production'])
        this.featureToggleService.config = this.featureToggleService.loadConfig().then((res) => res);
        this.validateToken();
      } else {
        const env$ = this.http.get('assets/env.json').pipe(
          tap(env => {
            alert("inside app initializer auth " + env['AUTHENTICATION_SERVICE'])
            alert("inside app initializer central url " + env['CENTRAL_LOGIN_URL'])
            alert("inside app initializer sso " + env['SSO_LOGIN'])
            environment['baseUrl'] = env['baseUrl'] || '';
            environment['SSO_LOGIN'] = env['SSO_LOGIN'] || false;
            environment['AUTHENTICATION_SERVICE'] = env['AUTHENTICATION_SERVICE'] || false;
            environment['CENTRAL_LOGIN_URL'] = env['CENTRAL_LOGIN_URL'] || '';
            environment['MAP_URL'] = env['MAP_URL'] || '';
            environment['RETROS_URL'] = env['RETROS_URL'] || '';
            this.validateToken();
          }));
        env$.toPromise().then(async res => {
          this.featureToggleService.config = this.featureToggleService.loadConfig().then((res) => res);
        });
      }

      

      // load google Analytics script on all instances except local and if customAPI property is true
      let addGAScript = this.featureToggleService.isFeatureEnabled('GOOGLE_ANALYTICS');
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

  validateToken() {
    return new Promise<void>((resolve, reject) => {
      alert("inside validate token func" + environment['AUTHENTICATION_SERVICE']);
      // setTimeout(() => {
        if (environment['AUTHENTICATION_SERVICE']) {
          this.router.resetConfig(this.routes);
          let url = window.location.href;
          let authToken = url.split("authToken=")?.[1]?.split("&")?.[0];
          if (authToken) {
            this.sharedService.setAuthToken(authToken);
          }
          let obj = {
            'resource': environment.RESOURCE,
            'authToken': authToken
          };
          // Make API call or initialization logic here...
          this.httpService.getUserValidation(obj).subscribe((response) => {
            if (response?.['success']) {
              this.sharedService.setCurrentUserDetails(response?.['data']);
              localStorage.setItem("user_name", response?.['data']?.user_name);
              localStorage.setItem("user_email", response?.['data']?.user_email);
              const redirect_uri = localStorage.getItem('redirect_uri');
              if (authToken) {
                this.ga.setLoginMethod(response?.['data'], response?.['data']?.authType);
              }
              if (redirect_uri) {
                if (redirect_uri.startsWith('#')) {
                  this.router.navigate([redirect_uri.split('#')[1]])
                } else {
                  this.router.navigate([redirect_uri]);
                }
                localStorage.removeItem('redirect_uri');
              } else {
                this.router.navigate(['/dashboard/iteration'], { queryParamsHandling: 'merge' });
              }
            }
          }, error => {
            console.log(error);
          })
        }
        resolve();
      // }, 5000)
      
    })

  }
}

