import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OverlappedProgressbarComponent } from './overlapped-progressbar.component';

describe('OverlappedProgressbarComponent', () => {
  let component: OverlappedProgressbarComponent;
  let fixture: ComponentFixture<OverlappedProgressbarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OverlappedProgressbarComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OverlappedProgressbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
