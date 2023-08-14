import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DoraComponent } from './dora.component';
import { SharedService } from '../../services/shared.service';
import { HttpService } from 'src/app/services/http.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { APP_CONFIG, AppConfig } from 'src/app/services/app.config';
import { HelperService } from 'src/app/services/helper.service';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { DatePipe } from '@angular/common';

describe('DoraComponent', () => {
  let component: DoraComponent;
  let fixture: ComponentFixture<DoraComponent>;
  let service: SharedService;
  let httpService: HttpService;

  beforeEach(async () => {
    service = new SharedService();
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
      ],
      declarations: [ DoraComponent ],
      providers: [
        HelperService,
        HttpService,
        { provide: APP_CONFIG, useValue: AppConfig },
        { provide: SharedService, useValue: service },
        DatePipe
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DoraComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
