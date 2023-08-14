import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DoraComponent } from './dora.component';

describe('DoraComponent', () => {
  let component: DoraComponent;
  let fixture: ComponentFixture<DoraComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DoraComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DoraComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
