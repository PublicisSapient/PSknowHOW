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
import { RouterModule } from '@angular/router';
import { AuthGuard } from '../../core/guards/auth.guard';
import { Logged } from '../../core/guards/logged.guard';
import { GuestGuard } from '../../core/guards/guest.guard';
import { FeatureGuard } from '../../core/guards/feature.guard';
import { AccessGuard } from '../../core/guards/access.guard';

/**
 * Route the path to login/registration when user doesn't have authentication token.
 * Route the path to dashboard and it children(Executive/Quatilty....) when user contain
 * authenticate token.
 * Logged/Authguard is used for authentication guard, check token everytime while routing
 */

@NgModule({
  imports: [RouterModule.forRoot([], { useHash: true, relativeLinkResolution: 'legacy' })],

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


