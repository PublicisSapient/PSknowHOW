import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupedColumnPlusLineChartV2Component } from './grouped-column-plus-line-chart-v2.component';

describe('GroupedColumnPlusLineChartV2Component', () => {
  let component: GroupedColumnPlusLineChartV2Component;
  let fixture: ComponentFixture<GroupedColumnPlusLineChartV2Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GroupedColumnPlusLineChartV2Component ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GroupedColumnPlusLineChartV2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
