import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BarplotStackedComponent } from './barplot-stacked.component';

describe('BarplotStackedComponent', () => {
  let component: BarplotStackedComponent;
  let fixture: ComponentFixture<BarplotStackedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BarplotStackedComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BarplotStackedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
