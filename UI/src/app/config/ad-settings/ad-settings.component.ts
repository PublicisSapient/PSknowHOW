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
import { FormBuilder, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { HttpService } from '../../services/http.service';
import { RsaEncryptionService } from '../../services/rsa.encryption.service';


@Component({
  selector: 'app-ad-settings',
  templateUrl: './ad-settings.component.html',
  styleUrls: ['./ad-settings.component.css']
})
export class AdSettingsComponent implements OnInit {
  authSettingsForm: UntypedFormGroup;
  adSettingsFormObj: any;
  ssoFormObj: any;
  // standardLoginForm: UntypedFormGroup;
  // standardLoginFormObj: any;
  submitted = false;
  loginSettingsTypes = [{
    name: 'standardLogin',
    label: 'KnowHOW Local Authentication'
  },
  {
    name: 'adLogin',
    label: 'AD Authentication'
  },
  {
      name: 'ssoLogin',
      label: 'Login with SSO'
  }];

  selectedTypes: any[] = [{
    name: 'standardLogin',
    label: 'KnowHOW Local Authentication'
  }];
  disableSave = false;

  constructor(private formBuilder: UntypedFormBuilder, private http: HttpService, private messenger: MessageService, private rsa: RsaEncryptionService) { }

  ngOnInit(): void {
    this.getAuthSettings();
  }

  // get Auth configuration
  getAuthSettings() {
    this.initializeFields();
    // this.standardLoginForm = this.formBuilder.group(this.standardLoginFormObj);
    // this.authSettingsForm = this.formBuilder.group(this.ssoFormObj);
    this.http.getAuthConfig().subscribe(response => {
      if (response && response.success) {
        if (response && response.data && response.data.authTypeStatus) {
          this.selectedTypes = [];
          if (response.data.authTypeStatus.standardLogin) {
            this.selectedTypes.push({
              name: 'standardLogin',
              label: 'KnowHOW Local Authentication'
            });
          }

          if (response.data.authTypeStatus.adLogin) {
            this.selectedTypes.push({
              name: 'adLogin',
              label: 'AD Authentication'
            });
          }

          if (response.data.authTypeStatus.ssoLogin) {
            this.selectedTypes.push({
              name: 'ssoLogin',
              label: 'Login with SSO'
            });
          }
        } else {
          this.selectedTypes.push({
            name: 'standardLogin',
            label: 'KnowHOW Local Authentication'
          });
        }
        if (response.success && response.data && response.data.adServerDetail) {
          for (const obj in response.data.adServerDetail) {
            if (obj !== 'password') {
              if (this.authSettingsForm && this.adForm[obj]) {
                this.adForm[obj].setValue(response.data.adServerDetail[obj]);
              }
            }
          }
        }
        if (response.success && response.data && response.data.ssoLoginConfig) {
          for (const obj in response.data.ssoLoginConfig) {
            if (obj !== 'secretRef') {
              if (this.authSettingsForm && this.ssoForm[obj]) {
                this.ssoForm[obj].setValue(response.data.ssoLoginConfig[obj]);
              }
            }
          }
        }
      }
    });
  }

  typeNotSelected(formName) {
    if (!this.selectedTypes.map((type) => type.name).includes(formName)) {
      return true;
    } else {
      return false;
    }
  }

  checkValues(event) {
    console.log(event);
    
    if (!this.selectedTypes.length) {
      this.selectedTypes = [{
        name: 'standardLogin',
        label: 'KnowHOW Local Authentication'
      }];
    }
   
    // if (!this.selectedTypes.length) {
    //   this.disableSave = true;
    // } else {
    //   this.disableSave = false;
    // }
  }

  // convenience getter for easy access to form fields
  get adForm() {
    return this.authSettingsForm.get('adLogin')['controls'];
  }
  get ssoForm() {
    return this.authSettingsForm.get('ssoLogin')['controls'];
  }

  // get standardLogin() {
  //   return this.standardLoginForm.controls;
  // }

  // get pingForm() {
  //   return this.pingForm.controls;
  // }


  initializeFields() {
    let formElems = {};
    // if(type?.toLowerCase() === 'adlogin'){
      this.adSettingsFormObj = {
        username: ['', Validators.required],
        password: ['', Validators.required],
        host: ['', Validators.required],
        port: [null, Validators.required],
        rootDn: ['', Validators.required],
        domain: ['', Validators.required],
      };
      formElems['adLogin'] = this.formBuilder.group(this.adSettingsFormObj);
    // }
    // if(type?.toLowerCase() === 'ssologin'){
      this.ssoFormObj = {
        clientID: ['', Validators.required],
        secretRef: ['', Validators.required],
        environment: ['', Validators.required],
        discoveryURI: ['', Validators.required],
        callbackUri: ['', Validators.required],
        cookieDomain: ['', Validators.required],
        jwksUrl: ['', Validators.required],
        issuer: ['', Validators.required],
        cookiePassRef: ['', Validators.required]
      }
      formElems['ssoLogin'] = this.formBuilder.group(this.ssoFormObj);
    // }
   
    this.authSettingsForm = this.formBuilder.group(formElems);
  }

  submit() {
    this.submitted = true;
    const submitData = {};
    submitData['authTypeStatus'] = {};
    this.selectedTypes.forEach((item) => {
      submitData['authTypeStatus'][item.name] = true;
    });

    if (this.selectedTypes.filter((type) => type.name === 'adLogin').length) {
      // return if form is invalid
      if (this.authSettingsForm.invalid) {
        return;
      } else {
        submitData['adServerDetail'] = {};
        for (const obj in this.adForm) {
          if (obj === 'password') {
            submitData['adServerDetail'][obj] = this.rsa.encrypt(this.adForm[obj].value);
          } else {
            submitData['adServerDetail'][obj] = this.adForm[obj].value;
          }
        }
      }
    }

    if (this.selectedTypes.filter((type) => type.name === 'ssoLogin').length) {
      // return if form is invalid
      if (this.authSettingsForm.invalid) {
        return;
      } else {
        submitData['ssoLoginConfig'] = {};
        for (const obj in this.ssoForm) {
          if (obj === 'secretRef') {
            submitData['ssoLoginConfig'][obj] = this.rsa.encrypt(this.ssoForm[obj].value);
          } else {
            submitData['ssoLoginConfig'][obj] = this.ssoForm[obj].value;
          }
        }
      }
    }
    console.log(submitData);
    
    this.http.setAuthConfig(submitData).subscribe(response => {
      if (response && response['success']) {
        this.messenger.add({
          severity: 'success',
          summary: 'Saved successfully!',
          detail: ''
        });
      } else {
        this.messenger.add({
          severity: 'error',
          summary: 'Some error occurred!'
        });
      }
    }, err => {
      this.messenger.add({
        severity: 'error',
        summary: err.error.message
      });
    });
  }
}
