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
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { first } from 'rxjs/operators';
import { Router } from '@angular/router';
@Component({
    selector: 'app-forgot-password',
    templateUrl: './forgot-password.component.html',
    styleUrls: ['../login/login.component.css']
})
export class ForgotPasswordComponent implements OnInit {

    emailForm: UntypedFormGroup;
    loading = false;
    submitted = false;
    returnUrl: string;
    error = '';
    success = '';
    isPasswordUpdated = false;
    constructor(private formBuilder: UntypedFormBuilder, private httpService: HttpService, private router: Router) { }

    ngOnInit() {
        // Set validation for ForgotPasswordComponent-form elements
        this.emailForm = this.formBuilder.group({
            email: ['', [Validators.required, Validators.maxLength(80), Validators.pattern('[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$')]]
        }, {});

    }

    // convenience getter for easy access to form fields
    get f() {
 return this.emailForm.controls;
}

    onSubmit() {
        this.error = '';
        this.submitted = true;
        this.isPasswordUpdated = false;
        // stop here if form is invalid
        if (this.emailForm.invalid) {
            return;
        }

        // start spinner
        this.loading = true;

        // call emailForm service
        this.httpService.forgotPassword(this.f.email.value)
            .pipe(first())
            .subscribe(
                data => {
                    // stop spinner
                    this.loading = false;
                    // check email is not register sent else mail send exception else successful result
                    if (data['success'] === true) {

                        this.success = 'Link to reset password has been sent to your registered email address';
                        this.isPasswordUpdated = true;
                    } else if (!data['success'] || data['success'] === false) {
                        this.error = 'Link could not be sent to the entered email id. Please check if the email id is valid and registered.';
                        this.isPasswordUpdated = false;
                        this.router.navigate([this.router.url]);
                    }
                },
                error => {

                    this.loading = false; // stop spinner
                    this.isPasswordUpdated = false;
                    if (error.status === 0) {// in case of connection timeout
                        this.error = 'Could not send email, connection timed out!';
                    } else {
                        this.error = 'Please check your email/notification setup';
                    }
                    this.router.navigate([this.router.url]);
                }

            );
    }
}
