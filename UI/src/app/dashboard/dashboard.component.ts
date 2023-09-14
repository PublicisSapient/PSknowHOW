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
  OnInit,
  ChangeDetectorRef,
  AfterContentInit,
  HostListener,
  Output,
  Renderer2,
  ViewChild,
} from '@angular/core';
import { SharedService } from '../services/shared.service';
import { GetAuthService } from '../services/getauth.service';
import { HttpService } from '../services/http.service';
import { NavigationEnd, Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
})

/**
 Route the path from app-route and redirect to dashboard
 */
export class DashboardComponent implements OnInit, AfterContentInit {
  displayModal = false;
  modalDetails={
    header:'User Request Approved',
    content:'Click on "Continue" to reflect the changes happened from requested Role change.'
  };

  @ViewChild('header',{ static: true }) header;
  authorized = true;
  isApply = false;
  headerStyle;
  sideNavStyle;

  constructor(
    public cdRef: ChangeDetectorRef,
    public router: Router,
    private service: SharedService,
    private getAuth: GetAuthService,
    private httpService: HttpService,
    private renderer: Renderer2,
  ) {
    this.sideNavStyle ={ 'toggled' :this.isApply};
    this.renderer.listen('document', 'click', (e: Event) => {

      // setting document click event data to identify outside click for show/hide kpi filter
      this.service.setClickedItem(e?.target);
    });
    this.authorized = this.getAuth.checkAuth();
  }

  ngOnInit() {
    this.setPageContentWrapperHeight();
    // this.authorized = this.getAuth.checkAuth();

    this.httpService.loadApp.subscribe(data=>{
      if(this.httpService.createdProjectName){
        this.modalDetails.header =`Project Created`;
        this.modalDetails.content =`The project "${this.httpService.createdProjectName}" has been created successfully and you have gained admin rights for it.`;
      }
      this.displayModal =data;
    });
    this.service.isSideNav.subscribe((flag) => {
      this.isApply = flag;
      this.sideNavStyle ={ 'toggled' : this.isApply };
    });

    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.setPageContentWrapperHeight();
      }
    });
  }

  setPageContentWrapperHeight(){
    setTimeout(()=>{
      this.headerStyle={height: 'calc(100vh - '+this.header.nativeElement.offsetHeight+'px)',top:'calc('+this.header.nativeElement.offsetHeight+'px'+' - '+'0px)'};
    },0);
  }

  reloadApp(){
    this.displayModal=false;
    this.router.navigate(['./dashboard/mydashboard']).then(success =>{
    window.location.reload();
    });

  }

  ngAfterContentInit() {
    this.cdRef.detectChanges();
  }

  ngOnDestroy() {
    this.isApply = false;
    this.sideNavStyle ={ 'toggled' : this.isApply };
    this.service.setSideNav(false);
  }
}
