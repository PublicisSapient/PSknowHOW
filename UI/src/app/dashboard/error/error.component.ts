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

import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { SharedService } from '../../services/shared.service';
import { Router } from '@angular/router';
import { timer } from 'rxjs/internal/observable/timer';

@Component({
  selector: 'app-error',
  templateUrl: './error.component.html',
  styleUrls: ['./error.component.css']
})
export class ErrorComponent implements OnInit, OnDestroy {
  errorMsg = '';
  redirectButtonText = 'Go to homepage';
  redirectButtonRoute = '/';
  timeLeft: 60;
  interval = null;
  source = null;

  constructor(private service: SharedService, private router: Router) {
    // this.pollForAvailability('/');
  }

    ngOnInit() {
      // for getting error from Shared service
      this.service.passErrorToErrorPage.subscribe((error) => {
        switch (error.status) {
          case 0: this.errorMsg = 'Server not available';
            this.redirectButtonText = 'Go to homepage';
            this.redirectButtonRoute = '/';
            this.pollForAvailability(this.redirectButtonRoute);
            break;
          // case 400: this.errorMsg = 'Some error occurred';
          //   this.redirectButtonText = 'Go to homepage';
          //   this.redirectButtonRoute = '/';
          //   break;
          case 401:
            this.service.setCurrentUserDetails({});
            this.errorMsg = 'Session Expired';
            this.redirectButtonText = 'Go to Login';
            this.redirectButtonRoute = './authentication/login';
            break;
          case 403: this.errorMsg = 'Unauthorised action';
            this.redirectButtonText = 'Go to homepage';
            this.redirectButtonRoute = '/';
            this.pollForAvailability(this.redirectButtonRoute);
            break;
          case 404: this.errorMsg = 'API Not Found';
            this.redirectButtonText = 'Go to homepage';
            this.redirectButtonRoute = '/';
            this.pollForAvailability(this.redirectButtonRoute);
            break;
          case 500: this.errorMsg = 'Internal Server error';
            this.redirectButtonText = 'Go to homepage';
            this.redirectButtonRoute = '/';
            this.pollForAvailability(this.redirectButtonRoute);
            break;
          default: this.errorMsg = 'Some error occurred';
            this.redirectButtonText = 'Go to homepage';
            this.redirectButtonRoute = '/';
            this.pollForAvailability(this.redirectButtonRoute);
            break;
        }

      });
    }

  // disabling timer
  @HostListener('window:beforeunload')
  ngOnDestroy() {
    if (this.source) {
      this.source.unsubscribe();
    }
  }

  pollForAvailability(redirectButtonRoute) {
    if (!this.source) {
      this.source = timer(1000, 1000).subscribe(val => {
        if (this.timeLeft > 0) {
          this.timeLeft--;
        } else {
          this.timeLeft = 60;
        }
        if (val === 60) {
          this.redirectButtonText = 'Redirecting';
          this.router.navigate([redirectButtonRoute]);
        }
      }
      );
    }

  }
}

