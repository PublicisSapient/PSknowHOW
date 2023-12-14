import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ParentFilterComponent } from './parent-filter.component';

describe('ParentFilterComponent', () => {
  let component: ParentFilterComponent;
  let fixture: ComponentFixture<ParentFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ParentFilterComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ParentFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
