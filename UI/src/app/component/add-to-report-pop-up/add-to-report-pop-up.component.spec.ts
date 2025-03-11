import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddToReportPopUpComponent } from './add-to-report-pop-up.component';

describe('AddToReportPopUpComponent', () => {
  let component: AddToReportPopUpComponent;
  let fixture: ComponentFixture<AddToReportPopUpComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AddToReportPopUpComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddToReportPopUpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
