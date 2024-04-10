import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavNewComponent } from './nav-new.component';

describe('NavNewComponent', () => {
  let component: NavNewComponent;
  let fixture: ComponentFixture<NavNewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NavNewComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavNewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
