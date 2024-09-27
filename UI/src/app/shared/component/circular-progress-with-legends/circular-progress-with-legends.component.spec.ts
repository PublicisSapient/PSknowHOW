import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CircularProgressWithLegendsComponent } from './circular-progress-with-legends.component';

describe('CircularProgressWithLegendsComponent', () => {
  let component: CircularProgressWithLegendsComponent;
  let fixture: ComponentFixture<CircularProgressWithLegendsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CircularProgressWithLegendsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CircularProgressWithLegendsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
