import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TooltipV2Component } from './tooltip-v2.component';

describe('TooltipV2Component', () => {
  let component: TooltipV2Component;
  let fixture: ComponentFixture<TooltipV2Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TooltipV2Component],
    }).compileComponents();

    fixture = TestBed.createComponent(TooltipV2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
