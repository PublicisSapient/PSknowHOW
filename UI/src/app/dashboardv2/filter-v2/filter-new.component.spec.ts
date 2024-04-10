import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FilterNewComponent } from './filter-new.component';

describe('FilterNewComponent', () => {
  let component: FilterNewComponent;
  let fixture: ComponentFixture<FilterNewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FilterNewComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FilterNewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
