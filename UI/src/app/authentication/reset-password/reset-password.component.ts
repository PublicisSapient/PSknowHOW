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
import { ActivatedRoute } from '@angular/router';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { first } from 'rxjs/operators';
@Component({
    selector: 'app-reset-password',
    templateUrl: './reset-password.component.html',
    styleUrls: ['../login/login.component.css']
})
export class ResetPasswordComponent implements OnInit {

    resetPasswordForm: UntypedFormGroup;
    loading = false;
    submitted = false;
    returnUrl: string;
    error = '';
    message = '';
    resetToken = '';
    isPasswordUpdated = false;
    constructor(private formBuilder: UntypedFormBuilder, private route: ActivatedRoute, private httpService: HttpService) { }

    ngOnInit() {
        // get the token value from url
        this.route.queryParams.subscribe(params => {
            this.resetToken = params['resetToken'];
        });

        // Set validation for reset-form elements
        this.resetPasswordForm = this.formBuilder.group({
            password: ['', [
                Validators.required,
                Validators.pattern('(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[$@$!%*?&])[A-Za-z\d$@$!%*?&].{7,}'),
                Validators.maxLength(30)
            ]],
            confirmpassword: ['', Validators.required],
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
 return this.resetPasswordForm.controls;
}

    onSubmit() {
        this.error = '';
        this.submitted = true;
        this.isPasswordUpdated = false;

        // stop here if form is invalid
        if (this.resetPasswordForm.invalid) {
            return;
        }

        // start spinner
        this.loading = true;

        // call resetPassword service
        this.httpService.updatePassword(this.f.password.value, this.resetToken)
            .pipe(first())
            .subscribe(
                data => {
                    // stop spinner
                    this.loading = false;
                    // check successfull result else show error message
                    if (data['success']) {
                        this.message = 'Password successfully saved';
                        this.isPasswordUpdated = true;
                    } else if (!data['success']) {
                        this.error = data['message'];
                    }
                });
    }
}
