import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DailyScrumComponent } from './daily-scrum.component';

describe('DailyScrumComponent', () => {
  let component: DailyScrumComponent;
  let fixture: ComponentFixture<DailyScrumComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DailyScrumComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DailyScrumComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set selected user',()=>{
    const spySelecteUserChange = spyOn(component.onSelectedUserChange,'emit');
    component.setSelectedUser('dummyUserId');
    expect(spySelecteUserChange).toHaveBeenCalledWith('dummyUserId');

  });

  it('should set showLess',()=>{
    const spysetShowLess = spyOn(component.onShowLessOrMore,'emit');
    component.setShowLess();
    expect(spysetShowLess).toHaveBeenCalled();

  });

  it('should set showLess',()=>{
    const spyhandleViewExpandCollapse = spyOn(component.onExpandOrCollapse,'emit');
    component.handleViewExpandCollapse();
    expect(spyhandleViewExpandCollapse).toHaveBeenCalled();

  });

});
