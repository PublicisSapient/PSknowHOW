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
import { Router } from '@angular/router';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
@Component({
    selector: 'app-register',
    templateUrl: './register.component.html',
    styleUrls: ['../login/login.component.css']
})
export class RegisterComponent implements OnInit {

    registorForm: UntypedFormGroup;
    loading = false;
    submitted = false;
    error = '';
    success = '';
    constructor(
        private formBuilder: UntypedFormBuilder,
        private router: Router,
        private httpService: HttpService) { }
    ngOnInit() {
        // Set validation for registration-form elements
        this.registorForm = this.formBuilder.group({
            username: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(30), Validators.pattern('^$|^[A-Za-z0-9]+')]],
            password: ['', [
                Validators.required,
                Validators.pattern('(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[$@$!%*?&])[A-Za-z\d$@$!%*?&].{7,}'),
                Validators.maxLength(30)
            ]],
            confirmpassword: ['', Validators.required],
            email: ['', [Validators.required, Validators.maxLength(200), Validators.pattern('[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$')]]
        }, { validator: this.checkPasswords });
    }

    // Validation for confirm-password
    checkPasswords(group: UntypedFormGroup) {
        const pass = group.controls.password.value;
        const confirmPass = group.controls.confirmpassword.value;
        return pass === confirmPass ? null : { notSame: true };
    }

    // convenience getter for easy access to form fields
    get f() {
 return this.registorForm.controls;
}

    onSubmit() {
        this.submitted = true;
        // stop here if form is invalid
        if (this.registorForm.invalid) {
            return;
        }

        // start spinner
        this.loading = true;
        this.error = '';
        this.success =  '';
        // call registration service
        this.httpService.register(this.f.username.value, this.f.password.value, this.f.email.value)
            .subscribe(
                (data: any) => {
                    // stop spinner
                    this.loading = false;
                    if(data.success) {
                        // After successfully registration redirect form to dashboard router(Executive page)

                        this.success = data.message;
                        this.router.navigate(['./dashboard/']);
                    } else {
                        this.error = data.message;
                    }
                },
                error => {
                    this.loading = false; // stop spinner
                    // check 422 status if user already exist else mail is already registered
                    if (error.status === 422) {
                        this.error = 'Cannot complete the registration process, Try with different username/email';
                    } else if (error.status === 0) {// in case of connection timeout
                        this.error = 'Could not register user, connection timed out!';
                    }

                    this.router.navigate([this.router.url]);
                }
            );
    }
}
