import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PrimaryFilterComponent } from './primary-filter.component';

describe('PrimaryFilterComponent', () => {
  let component: PrimaryFilterComponent;
  let fixture: ComponentFixture<PrimaryFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PrimaryFilterComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PrimaryFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
