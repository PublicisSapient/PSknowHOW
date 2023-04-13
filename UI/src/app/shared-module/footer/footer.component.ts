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
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent implements OnInit {
  currentYear: number;
  currentversion : string;
  isSide : boolean;

  constructor(private httpService : HttpService,private sharedService : SharedService,public router : Router) { }

  ngOnInit() {
    this.currentYear = (new Date()).getFullYear();
    this.getMatchVersions();
    this.sharedService.isSideNav.subscribe(flag=>{
       this.isSide = flag;
    })
  }

  // getting the version details from server
  getMatchVersions() {
    this.httpService.getMatchVersions().subscribe((filterData) => {
      if (filterData && filterData.versionDetailsMap) {
        this.currentversion = filterData.versionDetailsMap.currentVersion;
      }
    });
  }

  styleObj():object{
    let marginLeft = this.isSide ? '16rem' : '5rem'; 
    const urlArray = this.router.url.split('/');
    if(urlArray.includes('Help') || urlArray.includes('Config') || urlArray.includes('Error') || urlArray[urlArray.length-1].includes('login') || urlArray.includes('register')){
      marginLeft = '0rem'
    }
    return {'margin-left': marginLeft}

  }

}
