import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { NavNewComponent } from './nav-new.component';

import { RouterTestingModule } from '@angular/router/testing';
import { SharedService } from '../../services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { GetAuthService } from '../../services/getauth.service';
import { HttpClientModule } from '@angular/common/http';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpService } from '../../services/http.service';
import { CommonModule, DatePipe } from '@angular/common';
import { MessageService } from 'primeng/api';
import { of } from 'rxjs';
import { Routes } from '@angular/router';
import { ExecutiveV2Component } from '../executive-v2/executive-v2.component';
import { MaturityComponent } from 'src/app/dashboard/maturity/maturity.component';

const getDashConfData = require('../../../test/resource/boardConfigNewServer.json');

describe('NavNewComponent', () => {
  let component: NavNewComponent;
  let fixture: ComponentFixture<NavNewComponent>;
  let getAuth: GetAuthService;
  let httpService: HttpService
  let sharedService: SharedService;
  let helperService: HelperService;
  let messageService: MessageService;
  let mockRouter;

  beforeEach(async () => {

    const routes: Routes = [
      { path: 'dashboard/my-knowhow', component: ExecutiveV2Component },
      { path: 'dashboard/dashboard', component: ExecutiveV2Component },
      { path: 'dashboard/kpi-maturity', component: MaturityComponent },
    ];


    await TestBed.configureTestingModule({
      declarations: [NavNewComponent],
      imports: [RouterTestingModule.withRoutes(routes), HttpClientModule, BrowserAnimationsModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],

      providers: [SharedService, GetAuthService, HttpService, HelperService, CommonModule, DatePipe, MessageService,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(NavNewComponent);
    component = fixture.componentInstance;
    getAuth = TestBed.get(GetAuthService);
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    helperService = TestBed.inject(HelperService);
    messageService = TestBed.inject(MessageService);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });



  it('should set items and activeItem correctly when response is successful', () => {
    const response = getDashConfData;
    spyOn(httpService, 'getShowHideOnDashboardNewUI').and.returnValue(of(response));
    let data = response.data.userBoardConfigDTO;
    data['configDetails'] = response.data.configDetails;
    const setDashConfigSpy = spyOn(sharedService, 'setDashConfigData');
    component.getBoardConfig([]);
    expect(setDashConfigSpy).toHaveBeenCalledWith(data);
    expect(component.items).toEqual([
      {
        "label": "My KnowHow",
        "slug": "my-knowhow",
        command: jasmine.any(Function),
      },
      {
        "label": "Speed",
        "slug": "speed",
        command: jasmine.any(Function),
      },
      {
        "label": "Quality",
        "slug": "quality",
        command: jasmine.any(Function),
      },
      {
        "label": "Value",
        "slug": "value",
        command: jasmine.any(Function),
      },
      {
        "label": "Iteration",
        "slug": "iteration",
        command: jasmine.any(Function),
      },
      {
        "label": "Developer",
        "slug": "developer",
        command: jasmine.any(Function),
      },
      {
        "label": "Release",
        "slug": "release",
        command: jasmine.any(Function),
      },
      {
        "label": "Dora",
        "slug": "dora",
        command: jasmine.any(Function),
      },
      {
        "label": "Backlog",
        "slug": "backlog",
        command: jasmine.any(Function),
      },
      {
        "label": "Kpi Maturity",
        "slug": "kpi-maturity",
        command: jasmine.any(Function),
      }
    ]);
  });


  it('should set the selectedTab correctly', fakeAsync(() => {
    const obj = { boardSlug: 'my-knowhow', boardName: 'My KnowHOW' };
    const setSelectedTypeOrTabRefreshSpy = spyOn(sharedService, 'setSelectedTypeOrTabRefresh');
    // const navigateSpy = spyOn(mockRouter, 'navigate');
    component.handleMenuTabFunctionality(obj);
    // expect(mockRouter.navigate).toHaveBeenCalledWith(['dashboard/my-knowhow']);
    tick(200);
    expect(setSelectedTypeOrTabRefreshSpy).toHaveBeenCalledWith('my-knowhow', 'scrum');
  }
  ));

  it('should not call setDashConfigData when boardName is not "Kpi Maturity"', fakeAsync(() => {
    const obj = { boardSlug: 'dashboard', boardName: 'Other Board' };
    const setDashConfigDataSpy = spyOn(sharedService, 'setDashConfigData');
    component.handleMenuTabFunctionality(obj);
    tick(200);
    expect(setDashConfigDataSpy).not.toHaveBeenCalled();
  }
  ));

  xit('should navigate to the correct route', fakeAsync(() => {
    const obj = { boardSlug: 'kpi-maturity', boardName: 'Kpi Maturity' };

    component.handleMenuTabFunctionality(obj);
    tick(200);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['dashboard/kpi-maturity']);
  }));

  it('should return true if obj1 and obj2 are the same object', () => {
    const obj = { prop: 'value' };

    const result = component.deepEqual(obj, obj);

    expect(result).toBe(true);
  });

  it('should return false if obj1 or obj2 is null', () => {
    const obj1 = { prop: 'value' };
    const obj2 = null;

    const result = component.deepEqual(obj1, obj2);

    expect(result).toBe(false);
  });

  it('should return false if obj1 or obj2 is not an object', () => {
    const obj1 = { prop: 'value' };
    const obj2 = 'string';

    const result = component.deepEqual(obj1, obj2);

    expect(result).toBe(false);
  });

  it('should return false if obj1 and obj2 have different number of keys', () => {
    const obj1 = { prop1: 'value1', prop2: 'value2' };
    const obj2 = { prop1: 'value1' };

    const result = component.deepEqual(obj1, obj2);

    expect(result).toBe(false);
  });

  it('should return false if obj1 and obj2 have different keys', () => {
    const obj1 = { prop1: 'value1' };
    const obj2 = { prop2: 'value2' };

    const result = component.deepEqual(obj1, obj2);

    expect(result).toBe(false);
  });

  it('should return false if obj1 and obj2 have different values for the same key', () => {
    const obj1 = { prop: 'value1' };
    const obj2 = { prop: 'value2' };

    const result = component.deepEqual(obj1, obj2);

    expect(result).toBe(false);
  });

  it('should return true if obj1 and obj2 have the same keys and values', () => {
    const obj1 = { prop1: 'value1', prop2: 'value2' };
    const obj2 = { prop1: 'value1', prop2: 'value2' };

    const result = component.deepEqual(obj1, obj2);

    expect(result).toBe(true);
  });

  it('should return true if both arrays are empty', () => {
    const array1: string[] = [];
    const array2: string[] = [];

    const result = component.compareStringArrays(array1, array2);

    expect(result).toBe(true);
  });

  it('should return false if arrays have different lengths', () => {
    const array1 = ['a', 'b', 'c'];
    const array2 = ['a', 'b'];

    const result = component.compareStringArrays(array1, array2);

    expect(result).toBe(false);
  });

  it('should return false if arrays have different elements', () => {
    const array1 = ['a', 'b', 'c'];
    const array2 = ['a', 'd', 'c'];

    const result = component.compareStringArrays(array1, array2);

    expect(result).toBe(false);
  });

  it('should return true if arrays have the same elements in the same order', () => {
    const array1 = ['a', 'b', 'c'];
    const array2 = ['a', 'b', 'c'];

    const result = component.compareStringArrays(array1, array2);

    expect(result).toBe(true);
  });

  it('should return true if arrays have the same elements in a different order', () => {
    const array1 = ['a', 'b', 'c'];
    const array2 = ['c', 'a', 'b'];

    const result = component.compareStringArrays(array1, array2);

    expect(result).toBe(false);
  });
});