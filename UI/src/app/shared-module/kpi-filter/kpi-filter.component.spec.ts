import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KpiFilterComponent } from './kpi-filter.component';

describe('KpiFilterComponent', () => {
  let component: KpiFilterComponent;
  let fixture: ComponentFixture<KpiFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ KpiFilterComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(KpiFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
