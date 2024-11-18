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
import { RaiseAccessRequestComponent } from './raise-access-request/raise-access-request.component';
import { ProfileComponent } from './profile.component';
import { ViewRequestsComponent } from './view-requests/view-requests.component';
import { RequestStatusComponent } from './request-status/request-status.component';
import { RoleGuard } from '../../services/role.guard';
import { UserMgmtComponent } from './user-mgmt/user-mgmt.component';
import { MyprofileComponent } from './myprofile/myprofile.component';
import { AccessMgmtComponent } from './access-mgmt/access-mgmt.component';
import { AutoApprovalComponent } from './auto-approval/auto-approval.component';
import { ViewNewUserAuthRequestComponent } from './view-new-user-auth-request/view-new-user-auth-request.component';
import { FeatureGuard } from 'src/app/services/feature.guard';

export const ProfileRoutes: Routes = [
    {
        path: '',
        component: ProfileComponent,
        canActivateChild: [FeatureGuard],
        children: [
            {
                path: '',
                redirectTo: 'MyProfile',
                pathMatch: 'full'
            },
            {
                path: 'MyProfile',
                component: MyprofileComponent,
                data: {
                    feature: "MyProfile"
                }
            },
            {
                path: 'GrantRequests',
                component: ViewRequestsComponent,
                canActivate: [RoleGuard],
                data: {
                    feature: "GrantRequests"
                }
            },
            {
                path: 'GrantNewUserAuthRequests',
                component: ViewNewUserAuthRequestComponent,
                canActivate: [RoleGuard],
                data: {
                    feature: "GrantNewUserAuthRequests"
                }
            },
            {
                path: 'RaiseRequest',
                component: RaiseAccessRequestComponent,
                data: {
                    feature: "RaiseRequest"
                }
            },
            {
                path: 'RequestStatus',
                component: RequestStatusComponent,
                data: {
                    feature: "RequestStatus"
                }
            },
            {
                path: 'AccessMgmt',
                component: AccessMgmtComponent,
                canActivate: [RoleGuard],
                data: {
                    feature: "AccessMgmt"
                }
            },
            {
                path: 'UserSettings',
                component: UserMgmtComponent,
                data: {
                    feature: "UserSettings"
                }
            },
            {
                path: 'AutoApprove',
                component: AutoApprovalComponent,
                data: {
                    feature: "AutoApprove"
                }
            }
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(ProfileRoutes)],
    exports: [RouterModule],
    providers: [RoleGuard]
})

export class ProfileRoutingModule { }
