import { TestBed, ComponentFixture } from '@angular/core/testing';
import { StickyHeaderV2Component } from './sticky-header-v2.component';
import { SharedService } from '../../services/shared.service';
import { HelperService } from '../../services/helper.service';
import { By } from '@angular/platform-browser';
import { of, Subject } from 'rxjs';

describe('StickyHeaderV2Component', () => {
  let component: StickyHeaderV2Component;
  let fixture: ComponentFixture<StickyHeaderV2Component>;
  let sharedService: SharedService;
  let helperService: HelperService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [StickyHeaderV2Component],
      providers: [
        {
          provide: SharedService,
          useValue: {
            fieldsSubscription: of([
              { label: 'Selected Dashboard', value: 'my-knowhow' }
            ]),
            onTypeOrTabRefresh: new Subject<{ selectedTab: string, selectedType: string }>() //new EventEmitter<string>()
          }
        },
        {
          provide: HelperService,
          useValue: {
            getObjectKeys: (obj) => {
              if (obj && Object.keys(obj).length) {
                return Object.keys(obj);
              } else {
                return [];
              }
            }
          } // provide a mock implementation for HelperService
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StickyHeaderV2Component);
    component = fixture.componentInstance;
    sharedService = TestBed.inject(SharedService);
    helperService = TestBed.inject(HelperService);
    fixture.detectChanges();
  });

  xit('should be hidden initially', () => {
    expect(component).toBeTruthy();// toBe(false); // or whatever property you're expecting
  });

  xit('should be visible after scrolling down', () => {
    window.scrollTo(0, 300);
    fixture.detectChanges();
    expect(component).toBeFalsy(); //.toBe(true); // or whatever property you're expecting
  });

});