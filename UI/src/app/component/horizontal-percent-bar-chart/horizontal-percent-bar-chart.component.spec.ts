import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HorizontalPercentBarChartComponent } from './horizontal-percent-bar-chart.component';

describe('HorizontalPercentBarChartComponent', () => {
  let component: HorizontalPercentBarChartComponent;
  let fixture: ComponentFixture<HorizontalPercentBarChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HorizontalPercentBarChartComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HorizontalPercentBarChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
