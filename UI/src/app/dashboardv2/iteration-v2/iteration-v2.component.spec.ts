import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IterationV2Component } from './iteration-v2.component';

describe('IterationV2Component', () => {
  let component: IterationV2Component;
  let fixture: ComponentFixture<IterationV2Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ IterationV2Component ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(IterationV2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
