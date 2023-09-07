import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DailyScrumComponent } from './daily-scrum.component';

describe('DailyScrumComponent', () => {
  let component: DailyScrumComponent;
  let fixture: ComponentFixture<DailyScrumComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DailyScrumComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(DailyScrumComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set selected user', () => {
    const spySelecteUserChange = spyOn(component.onSelectedUserChange, 'emit');
    component.setSelectedUser('dummyUserId');
    expect(spySelecteUserChange).toHaveBeenCalledWith('dummyUserId');

  });

  it('should set showLess', () => {
    const spysetShowLess = spyOn(component.onShowLessOrMore, 'emit');
    component.setShowLess();
    expect(spysetShowLess).toHaveBeenCalled();

  });

  it('should set showLess', () => {
    const spyhandleViewExpandCollapse = spyOn(component.onExpandOrCollapse, 'emit');
    component.handleViewExpandCollapse();
    expect(spyhandleViewExpandCollapse).toHaveBeenCalled();

  });

  it('should convert to hours', () => {
    let result = component.convertToHoursIfTime(25, 'hours');
    expect(result).toEqual(25);

    result = component.convertToHoursIfTime(65, 'hours');
    expect(result).toEqual(65);

    result = component.convertToHoursIfTime(60, 'hours');
    expect(result).toEqual(60);
  });

  it('should convert to day', () => {
    let result = component.convertToHoursIfTime(25, 'day');
    expect(result.trim()).toEqual('25m');

    result = component.convertToHoursIfTime(480, 'day');
    expect(result.trim()).toEqual('1d');

    result = component.convertToHoursIfTime(0, 'day');
    expect(result.trim()).toEqual('0d');
  });

});
