import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConditionalInputComponent } from './conditional-input.component';

describe('ConditionalInputComponent', () => {
  let component: ConditionalInputComponent;
  let fixture: ComponentFixture<ConditionalInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConditionalInputComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConditionalInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
