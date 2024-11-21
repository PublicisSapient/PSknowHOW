import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PsKpiCardFilterComponent } from './ps-kpi-card-filter.component';

describe('PsKpiCardFilterComponent', () => {
  let component: PsKpiCardFilterComponent;
  let fixture: ComponentFixture<PsKpiCardFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PsKpiCardFilterComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PsKpiCardFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
