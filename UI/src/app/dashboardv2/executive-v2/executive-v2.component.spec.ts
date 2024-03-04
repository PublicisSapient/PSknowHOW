import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExecutiveV2Component } from './executive-v2.component';

describe('ExecutiveV2Component', () => {
  let component: ExecutiveV2Component;
  let fixture: ComponentFixture<ExecutiveV2Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ExecutiveV2Component ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExecutiveV2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
