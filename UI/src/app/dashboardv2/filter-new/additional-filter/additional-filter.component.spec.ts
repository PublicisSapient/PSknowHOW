import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdditionalFilterComponent } from './additional-filter.component';

describe('AdditionalFilterComponent', () => {
  let component: AdditionalFilterComponent;
  let fixture: ComponentFixture<AdditionalFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AdditionalFilterComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdditionalFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
