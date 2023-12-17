import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MultilineStyleV2Component } from './multiline-style-v2.component';

describe('MultilineStyleV2Component', () => {
  let component: MultilineStyleV2Component;
  let fixture: ComponentFixture<MultilineStyleV2Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MultilineStyleV2Component ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MultilineStyleV2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
