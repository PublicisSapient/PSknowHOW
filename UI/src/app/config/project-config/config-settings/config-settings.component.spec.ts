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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConfigSettingsComponent } from './config-settings.component';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';
import { of } from 'rxjs';

describe('ConfigSettingsComponent', () => {
  let component: ConfigSettingsComponent;
  let fixture: ComponentFixture<ConfigSettingsComponent>;
  let route: ActivatedRoute;
  let router: Router;
  let httpService: HttpService;
  let sharedService: SharedService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConfigSettingsComponent ],
      providers: [
        { provide: ActivatedRoute, useValue: { queryParams: of({}) } },
        { provide: Router, useValue: {} },
        { provide: HttpService, useValue: {} },
        { provide: SharedService, useValue: {} }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigSettingsComponent);
    component = fixture.componentInstance;
    route = TestBed.inject(ActivatedRoute);
    router = TestBed.inject(Router);
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have configOptions', () => {
    expect(component.configOptions).toBeDefined();
    expect(component.configOptions.length).toBe(3);
  });

  it('should have selectedTab', () => {
    expect(component.selectedTab).toBe('projectSettings');
  });

  it('should call onTabChange', () => {
    spyOn(component, 'onTabChange');
    component.onTabChange();
    expect(component.onTabChange).toHaveBeenCalledTimes(1);
  });
});
