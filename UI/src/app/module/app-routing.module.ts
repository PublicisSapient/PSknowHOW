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

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ExecutiveComponent } from '../dashboard/executive/executive.component';
import { MaturityComponent } from '../dashboard/maturity/maturity.component';
import { ErrorComponent } from '../dashboard/error/error.component';
import { IterationComponent } from '../dashboard/iteration/iteration.component';
import { DeveloperComponent } from '../dashboard/developer/developer.component';
import { DashboardComponent } from '../dashboard/dashboard.component';
import { AuthGuard } from '../services/auth.guard';
import { Logged } from '../services/logged.guard';
import { AccessGuard } from '../services/access.guard';
import { GuestGuard } from '../services/guest.guard';
import { BacklogComponent } from '../dashboard/backlog/backlog.component';
import { SSOGuard } from '../services/sso.guard';
import { SsoAuthFailureComponent } from '../component/sso-auth-failure/sso-auth-failure.component';
import { UnauthorisedAccessComponent } from '../dashboard/unauthorised-access/unauthorised-access.component';
import { MilestoneComponent } from '../dashboard/milestone/milestone.component';
import { DoraComponent } from '../dashboard/dora/dora.component';
import { FeatureGuard } from '../services/feature.guard';
/**
 * Route the path to login/registration when user doesn't have authentication token.
 * Route the path to dashboard and it children(Executive/Quatilty....) when user contain
 * authenticate token.
 * Logged/Authguard is used for authentication guard, check token everytime while routing
 */


const routes: Routes = [
  { path: '', redirectTo: 'authentication', pathMatch: 'full' },
  {
    path: 'authentication',
    loadChildren: () => import('../authentication/authentication.module').then(m => m.AuthenticationModule),
    resolve: [Logged],
    canActivate: [SSOGuard]
  },
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
        path: 'iteration', component: IterationComponent, pathMatch: 'full', canActivate: [AccessGuard], 
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

    ], canActivate: [AuthGuard]
  },
  { path: 'authentication-fail', component: SsoAuthFailureComponent },
  { path: '**', redirectTo: 'authentication' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true, relativeLinkResolution: 'legacy' })],

  exports: [RouterModule],
  providers: [
    AuthGuard,
    Logged,
    AccessGuard,
    GuestGuard,
    FeatureGuard
  ]
})
export class AppRoutingModule { }


