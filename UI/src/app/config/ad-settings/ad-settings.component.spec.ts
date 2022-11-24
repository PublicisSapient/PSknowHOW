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

describe('AdSettingsComponent', () => {
  let component: AdSettingsComponent;
  let fixture: ComponentFixture<AdSettingsComponent>;
  let httpService: HttpService;
  const baseUrl = environment.baseUrl;
  let httpMock;

  const fakeLoginSettings = {
    message: 'types of authentication config',
    success: true,
    data: {
      authTypeStatus: {
        standardLogin: false,
        adLogin: true
      },
      adServerDetail: {
        username: 'svc-glbl-khADintgrt',
        password: '',
        host: 'lladldap.hk.publicisgroupe.net',
        port: 639,
        rootDn: 'DC=global,DC=publicisgroupe,DC=net',
        domain: 'publicisgroupe.net'
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
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get Login config on load', () => {
    component.ngOnInit();
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/auth-types')[0].flush(fakeLoginSettings);
    expect(component.authSettingsForm.controls['domain'].value).toEqual(fakeLoginSettings['data']['adServerDetail']['domain']);
  });

  it('should submit AD config', () => {
    component.ngOnInit();
    fixture.detectChanges();
    for (const obj in fakeLoginSettings['data']['adServerDetail']) {
      if (obj !== 'password') {
        if (component.authSettingsForm && component.authSettingsForm.controls[obj]) {
          component.authSettingsForm.controls[obj].setValue(fakeLoginSettings['data']['adServerDetail'][obj]);
        }
      }
    }
    component.authSettingsForm.controls['password'].setValue('testPassword');
    expect(component.authSettingsForm.valid).toBeTrue();
    const fakeSubmitResponse = {
      message: 'created and updated active directory user',
      success: true,
      data: {
        username: 'test-username',
        password: '8HvZjOM5y5T2c6ROq5CN7Z/IaAk0Q/cuubrN9sPOXmWdzEwlwNu9i48pHMPuvbAH',
        host: 'test-host-name',
        port: 639,
        rootDn: 'test-root',
        domain: 'testdomain.net'
      }
    };
    component.submit();
    httpMock.match(baseUrl + '/api/auth-types')[0].flush(fakeSubmitResponse);
  });
});
