import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MultilineV2Component } from './multiline-v2.component';

describe('MultilineV2Component', () => {
  let component: MultilineV2Component;
  let fixture: ComponentFixture<MultilineV2Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MultilineV2Component ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MultilineV2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
