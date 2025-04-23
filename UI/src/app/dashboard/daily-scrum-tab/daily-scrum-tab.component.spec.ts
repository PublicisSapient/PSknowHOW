import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DailyScrumTabComponent } from './daily-scrum-tab.component';
import { SharedService } from 'src/app/services/shared.service';
import { DailyScrumComponent } from '../daily-scrum/daily-scrum.component';
import { ActivatedRoute, Router } from '@angular/router';

describe('DailyScrumTabComponent', () => {
  let component: DailyScrumTabComponent;
  let fixture: ComponentFixture<DailyScrumTabComponent>;
  let sharedService: SharedService;
  const routerMock = {
    navigate: jasmine.createSpy('navigate'),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DailyScrumTabComponent, DailyScrumComponent],
      providers: [
        SharedService,
        { provide: ActivatedRoute, useValue: { snapshot: { params: {} } } },
        { provide: Router, useValue: routerMock },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DailyScrumTabComponent);
    component = fixture.componentInstance;
    sharedService = TestBed.inject(SharedService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set visible side bar to false on init', () => {
    spyOn(sharedService, 'setVisibleSideBar');
    component.ngOnInit();
    expect(sharedService.setVisibleSideBar).toHaveBeenCalledWith(false);
  });

  it('should set side nav to false on init', () => {
    spyOn(sharedService, 'setSideNav');
    component.ngOnInit();
    expect(sharedService.setSideNav).toHaveBeenCalledWith(false);
  });

  it('should set displayModal to false on closeModal', () => {
    component.closeModal();
    expect(component.displayModal).toBeFalse();
  });

  it('should update the assigneeList and set the displayModal to the provided value', () => {
    const assigneeList = [
      { name: 'John' },
      { name: 'Jane' },
      { name: 'Alice' },
    ];
    component.assigneeList = assigneeList;
    const e = true;
    component.setExpandView(e);
    expect(component.assigneeList).toEqual(assigneeList);
    expect(component.displayModal).toBe(e);
  });

  it('should toggle the showLess property', () => {
    component.showLess = true;
    component.onShowLessOrMore();
    expect(component.showLess).toBe(false);
    component.onShowLessOrMore();
    expect(component.showLess).toBe(true);
  });

  it('should update the filters property with the provided filters', () => {
    const filters = { column1: 'value1', column2: 'value2' };
    component.onFilterChange(filters);
    expect(component.filters).toEqual(filters);
  });

  it('should update the selectedUser to "Overall" if it matches the provided selectedUser', () => {
    const selectedUser = 'John';
    component.selectedUser = 'John';
    component.onSelectedUserChange(selectedUser);
    expect(component.selectedUser).toBe('Overall');
  });

  it('should update the selectedUser to the provided selectedUser if it does not match', () => {
    const selectedUser = 'Jane';
    component.selectedUser = 'John';
    component.onSelectedUserChange(selectedUser);
    expect(component.selectedUser).toBe('Jane');
  });

  it('should emit the reloadKPITab event with the provided event data', () => {
    const event = { data: 'some-data' };
    spyOn(component.reloadKPITab, 'emit');
    component.reloadKPI(event);
    expect(component.reloadKPITab.emit).toHaveBeenCalledWith(event);
  });
});
