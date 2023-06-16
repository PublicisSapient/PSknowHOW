import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormGroup, ReactiveFormsModule, FormsModule, FormBuilder } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AppConfig, APP_CONFIG } from 'src/app/services/app.config';
import { MessageService } from 'primeng/api';
import { HttpService } from '../../services/http.service';
import { AdSettingsComponent } from './ad-settings.component';
import { InputNumberModule } from 'primeng/inputnumber';
import { CheckboxModule } from 'primeng/checkbox';
import { environment } from 'src/environments/environment';
import { of,throwError } from 'rxjs';
import { SharedService } from 'src/app/services/shared.service';

describe('AdSettingsComponent', () => {
  let component: AdSettingsComponent;
  let fixture: ComponentFixture<AdSettingsComponent>;
  let httpService: HttpService;
  const baseUrl = environment.baseUrl;
  let httpMock;
  let msgService;
  let shared ;

  const fakeLoginSettings = {
    message: 'types of authentication config',
    success: true,
    data: {
      authTypeStatus: {
        standardLogin: false,
        adLogin: true
      },
      adServerDetail: {
        username: 'testUser',
        password: '****',
        host: 'testhost.net',
        port: 639,
        rootDn: 'DC=global,DC=testhost,DC=net',
        domain: 'testdomian.net'
      }
    }
  };
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AdSettingsComponent],
      imports: [FormsModule,
        ReactiveFormsModule,
        RouterTestingModule,
        HttpClientTestingModule,
        InputNumberModule,
        CheckboxModule
      ],
      providers: [
        HttpService,
        MessageService,
        SharedService,
        { provide: APP_CONFIG, useValue: AppConfig }
      ]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AdSettingsComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    httpService = TestBed.inject(HttpService);
    msgService = TestBed.inject(MessageService);
    shared = TestBed.inject(SharedService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get Login config on load', () => {
    component.ngOnInit();
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/auth-types')[0].flush(fakeLoginSettings);
    expect(component.adSettingsForm.controls['domain'].value).toEqual(fakeLoginSettings['data']['adServerDetail']['domain']);
  });

  it('should submit AD config', () => {
    component.ngOnInit();
    fixture.detectChanges();
    for (const obj in fakeLoginSettings['data']['adServerDetail']) {
      if (obj !== 'password') {
        if (component.adSettingsForm && component.adSettingsForm.controls[obj]) {
          component.adSettingsForm.controls[obj].setValue(fakeLoginSettings['data']['adServerDetail'][obj]);
        }
      }
    }
    component.adSettingsForm.controls['password'].setValue('testPassword');
    expect(component.adSettingsForm.valid).toBeTrue();
    const fakeSubmitResponse = {
      message: 'created and updated active directory user',
      success: true,
      data: {
        username: 'test-username',
        password: '***',
        host: 'test-host-name',
        port: 639,
        rootDn: 'test-root',
        domain: 'testdomain.net'
      }
    };
    component.submit();
    httpMock.match(baseUrl + '/api/auth-types')[0].flush(fakeSubmitResponse);
  });
   

  it("should adsetting form valid",()=>{
    component.initializeFields();
    component.adSettingsForm.controls['username'].setValue("abc@gmail.com")
    component.adSettingsForm.controls['password'].setValue("***")
    component.adSettingsForm.controls['host'].setValue("abc@gmail.com")
    component.adSettingsForm.controls['port'].setValue("abc@gmail.com")
    component.adSettingsForm.controls['rootDn'].setValue("abc@gmail.com")
    component.adSettingsForm.controls['domain'].setValue("abc@gmail.com")
    component.selectedTypes = [
      {
        name: 'adLogin',
        label: 'KnowHOW Local Authentication',
      }
    ];
    component.submit();
    expect(component.adSettingsForm.invalid).toBeFalsy();
  })

  it("should adsetting form invalid",()=>{
    component.initializeFields();
    component.selectedTypes = [
      {
        name: 'adLogin',
        label: 'KnowHOW Local Authentication',
      }
    ];
    component.submit();
    expect(component.adSettingsForm.invalid).toBeTruthy();
  })

  it("should give success response when form submit",()=>{
    component.initializeFields();
    component.selectedTypes = [
      {
        name: 'standard',
        label: 'KnowHOW Local Authentication',
      }
    ];
    const spyMessageService  = spyOn(msgService,'add');
    spyOn(httpService,'setAuthConfig').and.returnValue(of({success : true}));
    component.submit();
    expect(spyMessageService).toHaveBeenCalled();
  })

  it("should give failure response when form submit",()=>{
    component.initializeFields();
    component.selectedTypes = [
      {
        name: 'standard',
        label: 'KnowHOW Local Authentication',
      }
    ];
    const spyMessageService  = spyOn(msgService,'add');
    spyOn(httpService,'setAuthConfig').and.returnValue(of({success : false}));
    component.submit();
    expect(spyMessageService).toHaveBeenCalled();
  })

  it("should visible atleast one login tab",()=>{
    component.selectedTypes = [];
    component.checkValues();
    expect(component.selectedTypes.length).toBeGreaterThan(0)
  })

});
