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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FooterComponent } from './footer.component';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';
import { HttpTestingController, HttpClientTestingModule } from '@angular/common/http/testing';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { CommonModule } from '@angular/common';
// import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

describe('FooterComponent', () => {
  let component: FooterComponent;
  let fixture: ComponentFixture<FooterComponent>;
  let httpService ;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ FooterComponent],
      imports : [HttpClientTestingModule,CommonModule,RouterTestingModule],
      providers : [HttpService,SharedService,  { provide: APP_CONFIG, useValue: AppConfig }]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FooterComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should App version response come',()=>{
    const fakeRespose = {
      versionDetailsMap :{
        currentVersion : "6.3-SNAPSHOT"
      }
    }
    spyOn(httpService,'getMatchVersions').and.returnValue(of(fakeRespose));
    component.getMatchVersions();
    expect(component.currentversion).not.toBe('');
  })
});
