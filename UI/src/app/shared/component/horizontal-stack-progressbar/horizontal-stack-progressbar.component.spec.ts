import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HorizontalStackProgressbarComponent } from './horizontal-stack-progressbar.component';

describe('HorizontalStackProgressbarComponent', () => {
  let component: HorizontalStackProgressbarComponent;
  let fixture: ComponentFixture<HorizontalStackProgressbarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HorizontalStackProgressbarComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HorizontalStackProgressbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
