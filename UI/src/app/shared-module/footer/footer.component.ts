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

import { Component, ElementRef, OnInit } from '@angular/core';
import { HttpService } from 'src/app/services/http.service';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent implements OnInit {
  currentYear: number;
  currentversion: string;
  isSide: boolean;

  constructor(private httpService: HttpService, private elementRef: ElementRef) { }

  ngOnInit() {
    this.currentYear = (new Date()).getFullYear();
    this.getMatchVersions();
  }

  ngAfterViewInit() {
    const footerElement = this.elementRef.nativeElement.querySelector('.footer');
    if(footerElement){
      footerElement.setAttribute('aria-description', `Application footer with version ${this.currentversion} and copyright information for Publicis Sapient`);
    }
  }

  // getting the version details from server
  getMatchVersions() {
    this.httpService.getMatchVersions().subscribe((filterData) => {
      if (filterData && filterData.versionDetailsMap) {
        this.currentversion = filterData.versionDetailsMap.currentVersion;

        const footerElement = this.elementRef.nativeElement.querySelector('footer');
        if (footerElement) {
          footerElement.setAttribute('aria-label', `Page Footer - Version ${this.currentversion}`);
        }
      }
    });
  }
}
