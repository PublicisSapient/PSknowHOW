import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FieldMappingFieldComponent } from './field-mapping-field.component';

describe('FieldMappingFieldComponent', () => {
  let component: FieldMappingFieldComponent;
  let fixture: ComponentFixture<FieldMappingFieldComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FieldMappingFieldComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FieldMappingFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
