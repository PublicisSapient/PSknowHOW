import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardV2Component } from './dashboard-v2.component';

describe('DashboardV2Component', () => {
  let component: DashboardV2Component;
  let fixture: ComponentFixture<DashboardV2Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DashboardV2Component ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DashboardV2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
