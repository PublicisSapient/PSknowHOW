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
import { HttpService } from '../../../services/http.service';
import { Router, ActivatedRoute } from '@angular/router';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-user-mgmt',
  templateUrl: './user-mgmt.component.html',
  styleUrls: ['./user-mgmt.component.css', '../profile.component.css']
})
export class UserMgmtComponent implements OnInit {
  changePasswordForm: UntypedFormGroup;
  submitted = false;
  error = '';
  success = '';
  constructor(
    private formBuilder: UntypedFormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private httpService: HttpService,
    private messageService: MessageService) { }

  ngOnInit() {
    // Set validation for registration-form elements
    this.changePasswordForm = this.formBuilder.group({
      password: ['', [
        Validators.required,
        Validators.pattern('(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[$@$!%*?&])[A-Za-z\d$@$!%*?&].{7,}'),
        Validators.maxLength(30)
      ]],
      confirmpassword: ['', Validators.required],
      oldpassword: ['', Validators.required]
    }, {
      validator: [this.checkPasswords
        // ,this.checkOldPassword
      ]
    });
  }

  // Validation for confirm-password
  checkPasswords(group: UntypedFormGroup) {
    const pass = group.controls.password.value;
    const confirmPass = group.controls.confirmpassword.value;
    return pass === confirmPass ? null : { notSame: true };
  }

  // convenience getter for easy access to form fields
  get f() {
 return this.changePasswordForm.controls;
}

  onSubmit() {
    this.submitted = true;
    // stop here if form is invalid
    if (this.changePasswordForm.invalid) {
      return;
    }
    // call service
    this.httpService.changePassword(this.f.oldpassword.value, this.f.password.value)
      .subscribe(
        response => {
          if (response.success) {
            this.success = 'Password changed successfully';
            this.messageService.add({ severity: 'success', summary: `Password changed successfully` });
          } else {
            this.error = 'Change password request failed!';
            this.messageService.add({ severity: 'error', summary: response.message && response.message.length ? response.message : `Change password request failed! Please try again with valid password` });
          }
        }
      );
  }
}
