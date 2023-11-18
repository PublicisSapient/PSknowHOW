import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KpiModalComponent } from './kpi-modal.component';

describe('KpiModalComponent', () => {
  let component: KpiModalComponent;
  let fixture: ComponentFixture<KpiModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ KpiModalComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(KpiModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
