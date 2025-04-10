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
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { ConfigSettingsComponent } from './config-settings.component';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { APP_CONFIG } from 'src/app/services/app.config';

describe('ConfigSettingsComponent', () => {
  let component: ConfigSettingsComponent;
  let fixture: ComponentFixture<ConfigSettingsComponent>;
  let sharedService: SharedService;
  let router: Router;
  let activatedRoute: ActivatedRoute;

  const mockAppConfig = { apiEndpoint: 'http://mock-api.com' }; // Mock configuration

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ConfigSettingsComponent],
      imports: [RouterTestingModule, HttpClientTestingModule], // Added HttpClientTestingModule here
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({ toolName: 'Jira', tab: '2' }),
          },
        },
        SharedService,
        HttpService,
        { provide: APP_CONFIG, useValue: mockAppConfig }, // Provide mock config
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ConfigSettingsComponent);
    component = fixture.componentInstance;
    sharedService = TestBed.inject(SharedService);
    router = TestBed.inject(Router);
    activatedRoute = TestBed.inject(ActivatedRoute);
    spyOn(sharedService, 'getSelectedProject').and.returnValue({
      type: 'Agile',
      id: 1,
    });
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize configOptions and default selectedTab', () => {
    expect(component.configOptions).toEqual([
      { tab: 'Project Settings', tabValue: 'projectSettings' },
      { tab: 'Available Connections', tabValue: 'availableConnections' },
      { tab: 'Project Configuration', tabValue: 'projectConfig' },
    ]);
    expect(component.selectedTab).toBe('projectConfig');
  });

  it('should set selectedTab to "availableConnections" when tab is 1', () => {
    // Modify queryParams to simulate tab = 1
    activatedRoute.queryParams = of({ toolName: 'Jira', tab: '1' });
    fixture = TestBed.createComponent(ConfigSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.selectedTab).toBe('availableConnections');
  });

  it('should set selectedTab to "projectConfig" when tab is 2', () => {
    // Modify queryParams to simulate tab = 2
    activatedRoute.queryParams = of({ toolName: 'Jira', tab: '2' });
    fixture = TestBed.createComponent(ConfigSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.selectedTab).toBe('projectConfig');
  });

  it('should set selectedTab to "projectSettings" when tab is not 1 or 2 (default case)', () => {
    // Modify queryParams to simulate a value other than 1 or 2
    activatedRoute.queryParams = of({ toolName: 'Jira', tab: '3' });
    fixture = TestBed.createComponent(ConfigSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.selectedTab).toBe('projectSettings');
  });

  it('should update selectedTab based on queryParams', () => {
    expect(component.selectedToolName).toBe('Jira');
    expect(component.tab).toBe(2);
    expect(component.selectedTab).toBe('projectConfig');
  });

  it('should call getSelectedProject on ngOnInit and assign it to selectedProject', () => {
    component.ngOnInit();
    expect(sharedService.getSelectedProject).toHaveBeenCalled();
    expect(component.selectedProject).toEqual({ type: 'Agile', id: 1 });
  });

  it('should navigate with correct queryParams on onTabChange for projectConfig tab', () => {
    component.selectedTab = 'projectConfig';
    const navigateSpy = spyOn(router, 'navigate');

    component.onTabChange();

    expect(navigateSpy).toHaveBeenCalledWith(['.'], {
      queryParams: { type: 'agile', tab: 2 },
      relativeTo: activatedRoute,
    });
  });

  it('should navigate with correct queryParams on onTabChange for availableConnections tab', () => {
    component.selectedTab = 'availableConnections';
    const navigateSpy = spyOn(router, 'navigate');

    component.onTabChange();

    expect(navigateSpy).toHaveBeenCalledWith(['.'], {
      queryParams: { tab: 1 },
      relativeTo: activatedRoute,
    });
  });

  it('should navigate with correct queryParams on onTabChange for projectSettings tab', () => {
    component.selectedTab = 'projectSettings';
    const navigateSpy = spyOn(router, 'navigate');

    component.onTabChange();

    expect(navigateSpy).toHaveBeenCalledWith(['.'], {
      queryParams: { type: 'agile', tab: 0 },
      relativeTo: activatedRoute,
    });
  });
});
