import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DefaultReportComponent } from './default-report.component';

describe('DefaultReportComponent', () => {
  let component: DefaultReportComponent;
  let fixture: ComponentFixture<DefaultReportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DefaultReportComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DefaultReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
