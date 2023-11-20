import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KpiModalComponent } from './kpi-modal.component';
import { SharedService } from 'src/app/services/shared.service';

describe('KpiModalComponent', () => {
  let component: KpiModalComponent;
  let fixture: ComponentFixture<KpiModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ KpiModalComponent ],
      providers: [SharedService]
    })
    .compileComponents();

    fixture = TestBed.createComponent(KpiModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  xit('should create', () => {
    expect(component).toBeTruthy();
  });
});
