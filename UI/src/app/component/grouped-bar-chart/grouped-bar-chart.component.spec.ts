import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupedBarChartComponent } from './grouped-bar-chart.component';

describe('GroupedBarChartComponent', () => {
  let component: GroupedBarChartComponent;
  let fixture: ComponentFixture<GroupedBarChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GroupedBarChartComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GroupedBarChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
