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
import { HttpService } from '../../services/http.service';
import { Router, ActivatedRoute } from '@angular/router';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { first } from 'rxjs/operators';
import { SharedService } from '../../services/shared.service';
import { GoogleAnalyticsService } from 'src/app/services/google-analytics.service';
@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
    loginForm: UntypedFormGroup;
    adLoginForm: UntypedFormGroup;
    loading = false;
    submitted = false;
    returnUrl: string;
    error = '';
    sessionMsg = '';
    adLogin = true;
    loginConfig = {};



    constructor(private formBuilder: UntypedFormBuilder, private route: ActivatedRoute, private router: Router, private httpService: HttpService, private sharedService: SharedService, private ga: GoogleAnalyticsService) {
    }

    ngOnInit() {
        this.getLoginConfig();
        this.adLogin = !localStorage.getItem('loginType') || localStorage.getItem('loginType') === 'AD';


        /* if token exists for user then redirect to dashboard route(Executive page)*/
        this.submitted = false;
        this.route.queryParams.subscribe(params => {
            this.sessionMsg = params['sessionExpire'];
        });

        /*Set required validation for login elements*/
        this.loginForm = this.formBuilder.group({
            username: ['', Validators.required],
            password: ['', Validators.required]

        });

        /*Set required validation for AD login elements*/
        this.adLoginForm = this.formBuilder.group({
            username: ['', Validators.required],
            password: ['', Validators.required]

        });

        /* get return url from route parameters or default to '/' */
        this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
    }

    getLoginConfig() {
        this.httpService.getLoginConfig().subscribe(response => {
            if(response.success) {
               this.loginConfig = response.data;
            } else {
                this.loginConfig = {
                    standardLogin: true,
                    adLogin: false
                };
            }
        });
    }

    /* convenience getter for easy access to form fields*/
    get f() {
 return this.loginForm.controls;
}

    /* convenience getter for easy access to form fields*/
    get adf() {
 return this.adLoginForm.controls;
}

   

    onSubmit(loginType) {
        this.submitted = true;
        this.error = '';
        /* stop here if form is invalid*/
        if (loginType === 'standard') {
            if (this.loginForm.invalid) {
                return;
            }
        } else if (loginType === 'AD') {
            if (this.adLoginForm.invalid) {
                return;
            }
        }
        /*start the spinner*/
        this.loading = true;
        /*call login service*/
        if (loginType === 'standard') {
            this.httpService.login('', this.f.username.value, this.f.password.value)
                .pipe(first())
                .subscribe(
                    data => {
                        this.performLogin(data, this.f.username.value, this.f.password.value, 'standard');
                    });
        } else if (loginType === 'AD') {
            this.httpService.login('LDAP', this.adf.username.value, this.adf.password.value)
                .pipe(first())
                .subscribe(
                    data => {
                        this.performLogin(data, this.adf.username.value, this.adf.password.value, 'AD');
                    });
        }
    }

    redirectToProfile() {
        if (!this.sharedService.getCurrentUserDetails('user_email') || this.sharedService.getCurrentUserDetails('user_email') === '') {
            return true;
        }
        if (this.sharedService.getCurrentUserDetails('authorities')?.includes('ROLE_SUPERADMIN')) {
            return false;
        } else if (this.sharedService.getCurrentUserDetails('projectsAccess') === 'undefined' || !this.sharedService.getCurrentUserDetails('projectsAccess')?.length) {
            return true;
        }


    }

    performLogin(data, username, password, loginType) {
        /*stop loading of spinner*/
        this.loading = false;
        /*
        check 401 status and display server message to
        */
        if (data['status'] === 401) {
            this.f.password.setValue('');
            this.submitted = false;
            this.error = data['error'].message;
        } else if (data['status'] === 0) {
            this.error = 'Internal Server Error';

        } else if (data['status'] === 200) {
            /*After successfully login redirect form to dashboard router(Executive page)*/
            localStorage.setItem('loginType', loginType);
            this.adLogin = loginType;
            this.ga.setLoginMethod(data.body, loginType);
            if (this.redirectToProfile()) {
                this.router.navigate(['./dashboard/Config/Profile']);
            } else {
                this.router.navigate(['./dashboard/']);
            }
        }
    }
}
