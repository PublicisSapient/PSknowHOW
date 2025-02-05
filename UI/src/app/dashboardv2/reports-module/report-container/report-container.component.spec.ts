import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportContainerComponent } from './report-container.component';

describe('ReportContainerComponent', () => {
  let component: ReportContainerComponent;
  let fixture: ComponentFixture<ReportContainerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReportContainerComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReportContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
