import { ComponentFixture, TestBed } from '@angular/core/testing';
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

const getDashConfData = require('../../../test/resource/boardConfigNew.json');

describe('NavNewComponent', () => {
  let component: NavNewComponent;
  let fixture: ComponentFixture<NavNewComponent>;
  let getAuth: GetAuthService;
  let httpService: HttpService
  let sharedService: SharedService;
  let helperService: HelperService;
  let messageService: MessageService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NavNewComponent],
      imports: [RouterTestingModule, HttpClientModule, BrowserAnimationsModule],
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
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });



  it('should set items and activeItem correctly when response is successful', () => {
    const response = { success: true, data: getDashConfData.data };
    spyOn(httpService, 'getShowHideOnDashboard').and.returnValue(of(response));
    const setDashConfigSpy = spyOn(sharedService, 'setDashConfigData');
    component.getBoardConfig();
    expect(setDashConfigSpy).toHaveBeenCalledWith(getDashConfData.data);
    expect(component.items).toEqual([
      {
        "label": "My KnowHOW",
        "slug": "mydashboard",
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
        "label": "DORA",
        "slug": "dora",
        command: jasmine.any(Function),
      },
      {
        "label": "Backlog",
        "slug": "backlog",
        command: jasmine.any(Function),
      },
      {
        "label": "KPI Maturity",
        "slug": "Maturity",
        command: jasmine.any(Function),
      }
    ]);
  });
});
