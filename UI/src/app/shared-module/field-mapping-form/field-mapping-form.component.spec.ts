import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FieldMappingFormComponent } from './field-mapping-form.component';

describe('FieldMappingFormComponent', () => {
  let component: FieldMappingFormComponent;
  let fixture: ComponentFixture<FieldMappingFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FieldMappingFormComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FieldMappingFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
