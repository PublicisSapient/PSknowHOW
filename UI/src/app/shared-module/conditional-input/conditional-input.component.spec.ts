import { SimpleChange } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConditionalInputComponent } from './conditional-input.component';

describe('ConditionalInputComponent', () => {
  let component: ConditionalInputComponent;
  let fixture: ComponentFixture<ConditionalInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ConditionalInputComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ConditionalInputComponent);
    component = fixture.componentInstance;
    component.fieldConfig = {
      options: [
        {
          labelValue: 'p1',
          operator: '<',
          countValue: 3,
          maxValue: 10,
          minValue: 1,
        },
        {
          labelValue: 'p2',
          operator: '<',
          countValue: 2,
          maxValue: 10,
          minValue: 1,
        },
        {
          labelValue: 'p3',
          operator: '<',
          countValue: 2,
          maxValue: 10,
          minValue: 1,
        },
        {
          labelValue: 'p4',
          operator: '<',
          countValue: 2,
          maxValue: 10,
          minValue: 1,
        },
        {
          labelValue: 'p5',
          operator: '<',
          countValue: 2,
          maxValue: 10,
          minValue: 1,
        },
      ],
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize templateLabels, templateData, finalValue, and set countValue for each element in valueObj', () => {
    component.valueObj = [
      { labelValue: 'p1', countValue: 10 },
      { labelValue: 'p2', countValue: 5 },
    ];

    component.ngOnChanges({
      valueObj: new SimpleChange(null, component.valueObj, true),
    });

    expect(component.templateLabels).toEqual(['p1', 'p2']);
    expect(component.templateData).toEqual([
      {
        labelValue: 'p1',
        operator: '<',
        countValue: 10,
        maxValue: 10,
        minValue: 1,
      },
      {
        labelValue: 'p2',
        operator: '<',
        countValue: 5,
        maxValue: 10,
        minValue: 1,
      },
    ]);
    expect(component.finalValue).toEqual([
      {
        labelValue: 'p1',
        operator: '<',
        countValue: 10,
        maxValue: 10,
        minValue: 1,
      },
      {
        labelValue: 'p2',
        operator: '<',
        countValue: 5,
        maxValue: 10,
        minValue: 1,
      },
    ]);
    expect(component.fieldConfig.options[0].countValue).toEqual(10);
    expect(component.fieldConfig.options[1].countValue).toEqual(5);
  });

  it('should not initialize templateLabels, templateData, finalValue, and countValue if valueObj is empty', () => {
    component.valueObj = [];

    expect(component.templateLabels).toEqual([]);
    expect(component.templateData).toEqual([]);
    expect(component.finalValue).toEqual([]);
  });

  it('should update templateLabels, templateData, and set countValue to minValue for selected option', () => {
    component.templateLabels = ['p1', 'p2'];
    component.templateData = [
      {
        labelValue: 'p1',
        operator: '<',
        countValue: 3,
        maxValue: 10,
        minValue: 1,
      },
      {
        labelValue: 'p2',
        operator: '<',
        countValue: 2,
        maxValue: 10,
        minValue: 1,
      },
    ];
    const event = {
      value: [
        { labelValue: 'p1', countValue: 3, minValue: 1 },
        { labelValue: 'p2', countValue: 2, minValue: 1 },
        { labelValue: 'p3', countValue: 5, minValue: 1 },
      ],
      itemValue: { labelValue: 'p3', countValue: 5, minValue: 1 },
    };

    component.setValue(event);

    expect(component.templateLabels).toEqual(['p1', 'p2', 'p3']);
    expect(component.templateData).toEqual([
      {
        labelValue: 'p1',
        operator: '<',
        countValue: 3,
        maxValue: 10,
        minValue: 1,
      },
      {
        labelValue: 'p2',
        operator: '<',
        countValue: 2,
        maxValue: 10,
        minValue: 1,
      },
      {
        labelValue: 'p3',
        operator: '<',
        countValue: 1,
        maxValue: 10,
        minValue: 1,
      },
    ]);
  });

  it('should call setOutput method', () => {
    spyOn(component, 'setOutput');
    component.fieldConfig = {
      options: [
        { labelValue: 'Label 1', minValue: 5 },
        { labelValue: 'Label 2', minValue: 10 },
        { labelValue: 'Label 3', minValue: 15 },
      ],
    };
    component.templateLabels = ['Label 1', 'Label 2'];
    component.templateData = [
      { labelValue: 'Label 1', minValue: 5 },
      { labelValue: 'Label 2', minValue: 10 },
    ];
    const event = {
      value: [
        { labelValue: 'Label 1', minValue: 5 },
        { labelValue: 'Label 2', minValue: 10 },
      ],
      itemValue: { labelValue: 'Label 1', minValue: 5 },
    };

    component.setValue(event);

    expect(component.setOutput).toHaveBeenCalled();
  });

  it('should add new option to templateData if it does not exist', () => {
    spyOn(component, 'setOutput');
    component.templateData = [
      { labelValue: 'Label 1', countValue: 5 },
      { labelValue: 'Label 2', countValue: 10 },
    ];
    const event = { value: 15 };
    const option = { labelValue: 'Label 3' };

    component.setCounter(event, option);

    expect(component.templateData).toEqual([
      { labelValue: 'Label 1', countValue: 5 },
      { labelValue: 'Label 2', countValue: 10 },
      { labelValue: 'Label 3', countValue: 15 },
    ]);
    expect(component.setOutput).toHaveBeenCalled();
  });

  it('should update countValue of existing option in templateData', () => {
    spyOn(component, 'setOutput');
    component.templateData = [
      { labelValue: 'Label 1', countValue: 5 },
      { labelValue: 'Label 2', countValue: 10 },
    ];
    const event = { value: 15 };
    const option = { labelValue: 'Label 2' };

    component.setCounter(event, option);

    expect(component.templateData).toEqual([
      { labelValue: 'Label 1', countValue: 5 },
      { labelValue: 'Label 2', countValue: 15 },
    ]);
    expect(component.setOutput).toHaveBeenCalled();
  });

  it('should call blur() on the event target', () => {
    const mockEvent = {
      target: jasmine.createSpyObj('HTMLElement', ['blur']),
    };

    component.removeFocus(mockEvent);

    expect(mockEvent.target.blur).toHaveBeenCalled();
  });
});
