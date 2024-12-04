import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PsKpiCardHeaderComponent } from './ps-kpi-card-header.component';

describe('PsKpiCardHeaderComponent', () => {
  let component: PsKpiCardHeaderComponent;
  let fixture: ComponentFixture<PsKpiCardHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PsKpiCardHeaderComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PsKpiCardHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
