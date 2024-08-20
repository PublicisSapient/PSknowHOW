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
import { FormControl, FormGroup, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpService } from 'src/app/services/http.service';

@Component({
  selector: 'app-config-settings',
  templateUrl: './config-settings.component.html',
  styleUrls: ['./config-settings.component.css']
})
export class ConfigSettingsComponent implements OnInit {

  configOptions: { tab: string; tabValue: string; }[];
  selectedTab: string = 'projectControls';
  tab: any;
  selectedToolName: string = null;
  constructor(private route: ActivatedRoute, public router: Router, public httpService: HttpService) {
    this.configOptions = [
      {
        'tab': 'Project Controls',
        'tabValue': 'projectControls'
      },
      {
        'tab': 'Available Connections',
        'tabValue': 'availableConnections'
      },
      {
        'tab': 'Project Configuration',
        'tabValue': 'projectConfig'
      }
    ]

    this.route.queryParams.subscribe(params => {
      this.selectedToolName = params['toolName'];
      this.tab = Number(params['tab']);
      switch (this.tab) {
        case 1:
          this.selectedTab = 'availableConnections';
          break;
        case 2:
          this.selectedTab = 'projectConfig';
          break;
        default:
          this.selectedTab = 'projectControls';
          break;
      }
    });

  }

  ngOnInit(): void {

  }

  onTabChange() {
    if (this.selectedTab === 'projectConfig') {
      this.router.navigate(['.'], { queryParams: { 'tab': 2 }, relativeTo: this.route });
    } else if (this.selectedTab === 'availableConnections') {
      this.router.navigate(['.'], { queryParams: { 'tab': 1 }, relativeTo: this.route });
    } else {
      this.router.navigate(['.'], { queryParams: { 'tab': 0 }, relativeTo: this.route });
    }
  }

}
