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

    /* get return url from route parameters or default to '/' */
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  /* convenience getter for easy access to form fields*/
  get f() {
    return this.loginForm.controls;
  }

  onSubmit() {
    this.submitted = true;
    this.error = '';
    /* stop here if form is invalid*/
    if (this.loginForm.invalid) {
      return;
    }

    /*start the spinner*/
    this.loading = true;
    /*call login service*/
    this.httpService.login('', this.f.username.value, this.f.password.value)
      .pipe(first())
      .subscribe(
        data => {
          this.performLogin(data, this.f.username.value, this.f.password.value);
        });

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

  performLogin(data, username, password) {
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
      this.ga.setLoginMethod(data.body, 'standard');
      if (this.redirectToProfile()) {
        this.router.navigate(['./dashboard/Config/Profile']);
      } else {
        const url = localStorage.getItem('shared_link');
        const currentUserProjectAccess = JSON.parse(localStorage.getItem('currentUserDetails'))?.projectsAccess[0]?.projects;
        if (url) {
          // Extract query parameters
          const queryParams = new URLSearchParams(url.split('?')[1]);
          const stateFilters = queryParams.get('stateFilters');

          if (stateFilters) {
            try {
              const decodedStateFilters = atob(stateFilters);
              const stateFiltersObj = JSON.parse(decodedStateFilters);

              // console.log('Decoded State Filters Object:', stateFiltersObj);
              let stateFilterObj = [];

              if (typeof stateFiltersObj['parent_level'] === 'object' && Object.keys(stateFiltersObj['parent_level']).length > 0) {
                stateFilterObj = [stateFiltersObj['parent_level']];
              } else {
                stateFilterObj = stateFiltersObj['primary_level'];
              }

              // Check if user has access to all project in stateFiltersObj['primary_level']
              const hasAccessToAll = stateFilterObj.every(filter =>
                currentUserProjectAccess.some(project => project.projectId === filter.basicProjectConfigId)
              );

              if (hasAccessToAll) {
                this.router.navigate([JSON.parse(JSON.stringify(url))]);
              } else {
                this.router.navigate(['/dashboard/Error']);
                setTimeout(() => {
                  this.sharedService.raiseError({
                    status: 901,
                    message: 'No project access.',
                  });
                }, 100);
              }
            } catch (error) {
              this.router.navigate(['/dashboard/Error']);
              setTimeout(() => {
                this.sharedService.raiseError({
                  status: 900,
                  message: 'Invalid URL.'
                });
              }, 100);
            }
          }
        } else {
          this.router.navigate(['./dashboard/']);
        }
      }
    }
  }
}
