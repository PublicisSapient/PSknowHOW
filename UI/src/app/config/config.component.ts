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
import { GetAuthorizationService } from '../services/get-authorization.service';
import { Router, NavigationEnd } from '@angular/router';
import { SharedService } from '../services/shared.service';

declare let $: any;

@Component({
    selector: 'app-config',
    templateUrl: './config.component.html',
    styleUrls: ['./config.component.css']
})
export class ConfigComponent implements OnInit {
    items = [];
    hasAccess = <boolean>false;
    activeTab: any;

    constructor(private getAuthorizationService: GetAuthorizationService, private router: Router,private sharedService: SharedService) {
    }

    ngOnInit() {
        if (this.getAuthorizationService.checkIfSuperUser() || (this.sharedService.getCurrentUserDetails('projectsAccess') && this.sharedService.getCurrentUserDetails('projectsAccess') !== 'undefined' && this.sharedService.getCurrentUserDetails('projectsAccess').length)) {
            if (!this.getAuthorizationService.checkIfSuperUser()) {
                if (!this.getAuthorizationService.checkIfProjectAdmin()) {
                    this.hasAccess = false;
                } else {
                    this.hasAccess = true;
                }
            } else {
                this.hasAccess = true;
            }
        } else {
            this.hasAccess = false;
        }

        this.router.events.subscribe(event => {
            if (event instanceof NavigationEnd) {
                this.setActiveTabOnClick(event.urlAfterRedirects);
            }
        });

        this.items = [
            { label: 'Projects', icon: 'fas fa-wrench', routerLink: '/dashboard/Config/ProjectList', id: 'Layout-KPIConfig', routerLinkActiveOptions: '{ exact: true }' },
            { label: 'Connections', icon: 'fas fa-plug', routerLink: '/dashboard/Config/connection-list', id: 'Layout-ConnectionsConfig', routerLinkActiveOptions: '{ exact: true }' },
            { label: 'Profile Mgmt.', icon: 'fas fa-user-circle', routerLink: '/dashboard/Config/Profile/MyProfile', id: 'Layout-ProfileMgmt', routerLinkActiveOptions: '{ exact: true }' },
        ];

        if (this.hasAccess) {
            // logged in as SuperAdmin or ProjectAdmin
            this.items.push(
                { label: 'Dashboard Config.', icon: 'fas fa-life-ring', routerLink: '/dashboard/Config/Dashboardconfig', id: 'Layout-DashboardConfig', routerLinkActiveOptions: '{ exact: true }' },
                // { label: 'Validation', icon: 'fas fa-chart-line', routerLink: '/dashboard/Config/DataValidation', id: 'Layout-DataValidation', routerLinkActiveOptions: '{ exact: true }' }
            );
        }

        if (this.hasAccess) {
            this.items.push(
                { label: 'Misc. Settings', icon: 'fa fa-fw fa-cog', routerLink: '/dashboard/Config/AdvancedSettings', id: 'Layout-AdvanceSettings', routerLinkActiveOptions: '{ exact: true }' },
                { label: 'Upload Data', icon: 'fa fa-fw fa-upload', routerLink: '/dashboard/Config/Upload', id: 'Layout-Upload', routerLinkActiveOptions: '{ exact: true }' },
                { label: 'Capacity Planning', icon: 'fa fa-regular fa-users', routerLink: '/dashboard/Config/Capacity', id: 'Layout-Capacity', routerLinkActiveOptions: '{ exact: true }' }
            );
        }

        this.setActiveTabOnClick(this.router.url);
    }

    setActiveTabOnClick(url) {
        this.activeTab = this.items.filter((item) => item.routerLink === url)[0];
    }

    ngOnDestroy() {
        this.sharedService.setSideNav(false);
    }
}
