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
import { PageNotFoundComponent } from '../page-not-found/page-not-found.component';
import { environment } from 'src/environments/environment';
/**
 * Route the path to login/registration when user doesn't have authentication token.
 * Route the path to dashboard and it children(Executive/Quatilty....) when user contain
 * authenticate token.
 * Logged/Authguard is used for authentication guard, check token everytime while routing
 */

@NgModule({
  imports: [RouterModule.forRoot([], { useHash: true, relativeLinkResolution: 'legacy', enableTracing: true })],

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


