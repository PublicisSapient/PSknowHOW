import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KpiTableComponent } from './kpi-table.component';

describe('KpiTableComponent', () => {
  let component: KpiTableComponent;
  let fixture: ComponentFixture<KpiTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ KpiTableComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(KpiTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
