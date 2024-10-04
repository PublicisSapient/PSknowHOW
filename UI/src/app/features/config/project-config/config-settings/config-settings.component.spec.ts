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
import { RouterTestingModule } from '@angular/router/testing';
import { HttpService } from 'src/app/core/services/http.service';
import { SharedService } from 'src/app/core/services/shared.service';

describe('ConfigSettingsComponent', () => {
  let component: ConfigSettingsComponent;
  let fixture: ComponentFixture<ConfigSettingsComponent>;
  let mockSharedService: jasmine.SpyObj<SharedService>;
  let mockHttpService: jasmine.SpyObj<HttpService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockActivatedRoute;

  beforeEach(async () => {
    // Set up spies for services
    mockSharedService = jasmine.createSpyObj('SharedService', ['getSelectedProject']);
    mockHttpService = jasmine.createSpyObj('HttpService', ['']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    // ActivatedRoute with mock query parameters
    mockActivatedRoute = {
      queryParams: of({
        toolName: 'TestTool',
        tab: '2'
      })
    };

    await TestBed.configureTestingModule({
      declarations: [ConfigSettingsComponent],
      imports: [RouterTestingModule], // Importing the RouterTestingModule to test navigation
      providers: [
        { provide: SharedService, useValue: mockSharedService },
        { provide: HttpService, useValue: mockHttpService },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigSettingsComponent);
    component = fixture.componentInstance;

    // Mocking return values
    mockSharedService.getSelectedProject.and.returnValue({ type: 'Project' });

    fixture.detectChanges(); // Run ngOnInit
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize configOptions correctly', () => {
    expect(component.configOptions.length).toBe(3);
    expect(component.configOptions[0].tab).toBe('Project Settings');
    expect(component.configOptions[1].tab).toBe('Available Connections');
    expect(component.configOptions[2].tab).toBe('Project Configuration');
  });

  it('should set selectedToolName from queryParams', () => {
    expect(component.selectedToolName).toBe('TestTool');
  });

  it('should set selectedTab based on queryParams (tab value 2)', () => {
    expect(component.selectedTab).toBe('projectConfig');
  });

  it('should get selectedProject on ngOnInit', () => {
    expect(component.selectedProject).toEqual({ type: 'Project' });
    expect(mockSharedService.getSelectedProject).toHaveBeenCalled();
  });

  it('should navigate on onTabChange - projectConfig case', () => {
    component.selectedTab = 'projectConfig';
    component.selectedProject = { type: 'Project' };

    component.onTabChange();

    expect(mockRouter.navigate).toHaveBeenCalledWith(
      ['.'],
      { queryParams: { type: 'project', tab: 2 }, relativeTo: mockActivatedRoute }
    );
  });

  it('should navigate on onTabChange - availableConnections case', () => {
    component.selectedTab = 'availableConnections';
    component.onTabChange();

    expect(mockRouter.navigate).toHaveBeenCalledWith(
      ['.'],
      { queryParams: { tab: 1 }, relativeTo: mockActivatedRoute }
    );
  });

  it('should navigate on onTabChange - default case (projectSettings)', () => {
    component.selectedTab = 'projectSettings';
    component.selectedProject = { type: 'Project' };

    component.onTabChange();

    expect(mockRouter.navigate).toHaveBeenCalledWith(
      ['.'],
      { queryParams: { type: 'project', tab: 0 }, relativeTo: mockActivatedRoute }
    );
  });
});

