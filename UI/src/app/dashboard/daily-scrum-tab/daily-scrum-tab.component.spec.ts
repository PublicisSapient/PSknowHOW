import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DailyScrumTabComponent } from './daily-scrum-tab.component';
import { SharedService } from 'src/app/services/shared.service';
import { DailyScrumComponent } from '../daily-scrum/daily-scrum.component';

describe('DailyScrumTabComponent', () => {
  let component: DailyScrumTabComponent;
  let fixture: ComponentFixture<DailyScrumTabComponent>;
  let sharedService: SharedService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DailyScrumTabComponent, DailyScrumComponent ],
      providers: [ SharedService ]
    })
    .compileComponents();
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

  // Add more test cases as needed
  xit('should expand view', () => {
    const event = {};
    component.assigneeList = [],
    component.displayModal = false;
    component.setExpandView(event);
  })

  it('should show less or more', ()=>{
    component.showLess = false;
    component.onShowLessOrMore();
    expect(component.showLess).toBeTrue();
  })

  xit('should handle filter change', () => {
    const filters = {};
    component.onFilterChange(filters);
    expect(component.filters).toEqual(filters);
  })

  it('should handle user change', () => {
    const selectedUser = 'testuser';
    component.selectedUser = 'XXXXXXX';
    component.onSelectedUserChange(selectedUser);
    expect(component.selectedUser).toEqual(selectedUser);
  })
});
