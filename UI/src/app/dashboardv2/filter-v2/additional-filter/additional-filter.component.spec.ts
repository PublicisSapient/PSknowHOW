import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdditionalFilterComponent } from './additional-filter.component';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterTestingModule } from '@angular/router/testing';
import { SharedService } from '../../../services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { GetAuthService } from '../../../services/getauth.service';
import { HttpClientModule } from '@angular/common/http';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpService } from '../../../services/http.service';

describe('AdditionalFilterComponent', () => {
  let component: AdditionalFilterComponent;
  let fixture: ComponentFixture<AdditionalFilterComponent>;
  let getAuth: GetAuthService;
  let httpService: HttpService
  let sharedService: SharedService;
  let helperService: HelperService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AdditionalFilterComponent ],
      imports: [RouterTestingModule, HttpClientModule, BrowserAnimationsModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],

      providers: [SharedService, GetAuthService, HttpService, HelperService, CommonModule, DatePipe,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdditionalFilterComponent);
    component = fixture.componentInstance;
    getAuth = TestBed.get(GetAuthService);
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    helperService = TestBed.inject(HelperService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should reset filterData, filterSet, and selectedFilters when selectedTab changes', () => {
    component.filterData = ['Filter 1', 'Filter 2'];
    component.filterSet = new Set(['Filter 1', 'Filter 2']);
    component.selectedFilters = ['Filter 1', 'Filter 2'];

    component.ngOnChanges({ selectedTab: {
      currentValue: 'New Tab', previousValue: 'Old Tab', firstChange: false,
      isFirstChange: function (): boolean {
        return false;
      }
    } });

    expect(component.filterData).toEqual([]);
    expect(component.filterSet.size).toBe(0);
    expect(component.selectedFilters.length).toBe(0);
  });

  // Add more test cases as needed

});