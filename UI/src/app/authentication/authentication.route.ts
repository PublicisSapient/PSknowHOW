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
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';

/* Create nested routing for login and registration. */
export const LoginRoutes: Routes = [
    {
        path: '',
        children: [
            { path: '', redirectTo: 'login', pathMatch: 'full' },
            { path: 'login', component: LoginComponent, pathMatch: 'full' },
            { path: 'register', component: RegisterComponent, pathMatch: 'full' },
            { path: 'forgetPasswordEmail', component: ForgotPasswordComponent, pathMatch: 'full' },
            { path: 'resetPassword', component: ResetPasswordComponent, pathMatch: 'full' }
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(LoginRoutes)],
    exports: [RouterModule]
})

export class AuthenticationRoutingModule { }
