import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UnauthorisedAccessComponent } from './unauthorised-access.component';
import { HelperService } from 'src/app/core/services/helper.service';
import { SharedService } from 'src/app/core/services/shared.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ExcelService } from 'src/app/core/services/excel.service';
import { DatePipe } from '@angular/common';
import { APP_CONFIG, AppConfig } from 'src/app/core/configs/app.config';

describe('UnauthorisedAccessComponent', () => {
  let component: UnauthorisedAccessComponent;
  let fixture: ComponentFixture<UnauthorisedAccessComponent>;
  let helperService 

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UnauthorisedAccessComponent ],
      imports : [HttpClientTestingModule],
      providers : [HelperService,SharedService,ExcelService,DatePipe,
        { provide: APP_CONFIG, useValue: AppConfig }]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UnauthorisedAccessComponent);
    component = fixture.componentInstance;
    helperService = TestBed.inject(HelperService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should reload window',()=>{
    const spyObj = spyOn(helperService,'windowReload')
    component.reloadApp();
    expect(spyObj).toHaveBeenCalled();
  })
});
