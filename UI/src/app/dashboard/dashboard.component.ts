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

import { Component, OnInit, ChangeDetectorRef, AfterContentInit, HostListener, Output, Renderer2 } from '@angular/core';
import { SharedService } from '../services/shared.service';
import { GetAuthService } from '../services/getauth.service';
import { HttpService } from '../services/http.service';
import { Router } from '@angular/router';


@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})

/**
 Route the path from app-route and redirect to dashboard
 */
export class DashboardComponent implements OnInit, AfterContentInit {

  authorized = <boolean>true;
  headerFixed = <boolean>false;
  scrollOffset = <number>150;
  constructor(public cdRef: ChangeDetectorRef, public router: Router, private service: SharedService, private getAuth: GetAuthService, private httpService: HttpService, private renderer: Renderer2) {
    this.renderer.listen('document', 'click',(e: Event)=>{
      // setting document click event data to identify outside click for show/hide kpi filter
      this.service.setClickedItem(e?.target);
  });
    this.authorized = this.getAuth.checkAuth();
  }

  ngOnInit() {
    // this.authorized = this.getAuth.checkAuth();

  }

  // for making the header sticky on scroll
  @HostListener('window:scroll', [])
  onWindowScroll() {
    if (this.router.url.indexOf('/Config/') === -1 && this.router.url !== '/dashboard/Maturity' && this.router.url !== '/dashboard/EngineeringMaturity') {
      this.headerFixed = (window.pageYOffset
        || document.documentElement.scrollTop
        || document.body.scrollTop || 0
      ) > this.scrollOffset;
    } else {
      this.headerFixed = false;
    }
  }

  ngAfterContentInit() {
    this.cdRef.detectChanges();
  }

}
