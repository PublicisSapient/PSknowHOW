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

import {
  Component,
  ChangeDetectorRef,
  AfterContentInit,
} from '@angular/core';
import { GetAuthService } from '../../services/getauth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard-v2',
  templateUrl: './dashboard-v2.component.html',
  styleUrls: ['./dashboard-v2.component.css'],
})

/**
 Route the path from app-route and redirect to dashboard
 */
export class DashboardV2Component implements AfterContentInit {
  displayModal = false;
  modalDetails = {
    header: 'User Request Approved',
    content: 'Click on "Continue" to reflect the changes happened from requested Role change.'
  };

  authorized = true;
  isApply = false;
  headerStyle;
  sideNavStyle;
  newUI = false;
  goToTopButton: HTMLElement;

  constructor(
    public cdRef: ChangeDetectorRef,
    public router: Router,
    private getAuth: GetAuthService,
  ) {
    this.sideNavStyle = { 'toggled': this.isApply };
    this.authorized = this.getAuth.checkAuth();
  }

  ngAfterContentInit() {
    this.cdRef.detectChanges();

    this.goToTopButton = document.getElementById('go-to-top');
    this.goToTopButton.addEventListener('click', () => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  }

  ngOnDestroy() {
    this.isApply = false;
    this.goToTopButton.removeEventListener('click', () => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  }
}
