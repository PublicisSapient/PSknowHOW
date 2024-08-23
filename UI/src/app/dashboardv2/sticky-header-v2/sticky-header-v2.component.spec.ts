import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StickyHeaderV2Component } from './sticky-header-v2.component';

describe('StickyHeaderV2Component', () => {
  let component: StickyHeaderV2Component;
  let fixture: ComponentFixture<StickyHeaderV2Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ StickyHeaderV2Component ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StickyHeaderV2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
