import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StickyHeaderV2Component } from './sticky-header-v2.component';
import { SharedService } from '../../services/shared.service';

describe('StickyHeaderV2Component', () => {
  let component: StickyHeaderV2Component;
  let fixture: ComponentFixture<StickyHeaderV2Component>;
  let sharedService: SharedService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ StickyHeaderV2Component ],
      providers: [SharedService]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StickyHeaderV2Component);
    component = fixture.componentInstance;
    sharedService = TestBed.inject(SharedService);
    fixture.detectChanges();
  });

  xit('should create', () => {
    expect(component).toBeTruthy();
  });
});
