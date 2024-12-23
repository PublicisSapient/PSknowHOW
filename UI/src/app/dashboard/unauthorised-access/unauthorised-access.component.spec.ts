import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UnauthorisedAccessComponent } from './unauthorised-access.component';
import { SharedService } from 'src/app/services/shared.service';
import { APP_CONFIG, AppConfig } from '../../services/app.config';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ExcelService } from 'src/app/services/excel.service';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs'; // To provide mock data for ActivatedRoute if needed
import { HelperService } from 'src/app/services/helper.service';

describe('UnauthorisedAccessComponent', () => {
  let component: UnauthorisedAccessComponent;
  let fixture: ComponentFixture<UnauthorisedAccessComponent>;
  let helperService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [UnauthorisedAccessComponent],
      imports: [HttpClientTestingModule],
      providers: [
        HelperService,
        SharedService,
        ExcelService,
        DatePipe,
        { provide: APP_CONFIG, useValue: AppConfig },
        {
          provide: ActivatedRoute,
          useValue: {
            // Mock any properties or observables used in HelperService
            snapshot: {
              paramMap: {
                get: (key: string) => null, // Mock specific params if needed
              },
            },
            params: of({}), // Mock params observable if needed
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UnauthorisedAccessComponent);
    component = fixture.componentInstance;
    // helperService = TestBed.inject(HelperService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should reload window', () => {
    const spyObj = spyOn(helperService, 'windowReload');
    component.reloadApp();
    expect(spyObj).toHaveBeenCalled();
  });
});
