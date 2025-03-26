import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KpiTableComponent } from './kpi-table.component';
import { SharedService } from 'src/app/services/shared.service';
import { SimpleChange, SimpleChanges } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

describe('KpiTableComponent', () => {
  let component: KpiTableComponent;
  let fixture: ComponentFixture<KpiTableComponent>;
  let sharedService: SharedService;
  const routerMock = {
    navigate: jasmine.createSpy('navigate'),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [KpiTableComponent],
      providers: [
        SharedService,
        { provide: ActivatedRoute, useValue: { snapshot: { params: {} } } },
        { provide: Router, useValue: routerMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(KpiTableComponent);
    component = fixture.componentInstance;
    sharedService = TestBed.inject(SharedService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show tooltip on mouse enter', () => {
    const event = {
      pageY: '560',
      pageX: '600',
    };
    const field = 'frequency';
    const data = {
      hoverText: ['1-sprint1', '2-sprint2', '3-sprint3'],
    };
    component.mouseEnter(event, field, data);
    expect(component.showToolTip).toBe(true);
  });

  it('should hide tooltip on mouse leave', () => {
    component.mouseLeave();
    expect(component.showToolTip).toBe(false);
    expect(component.toolTipHtml).toBe('');
  });

  it('should assign colors to node', () => {
    component.tabs = [];
    component.colorObj = {
      AddingIterationProject_64e739541426ba469c39c102: {
        nodeName: 'AddingIterationProject',
        color: '#079FFF',
      },
    };
    component.assignColorToNodes();
    expect(Object.keys(component.nodeColors)?.length).toEqual(
      Object.keys(component.colorObj)?.length,
    );
    expect(component.tabs?.length).toEqual(
      Object.keys(component.nodeColors)?.length,
    );
  });

  describe('YourComponent', () => {
    beforeEach(() => {
      spyOn(component, 'assignColorToNodes');
    });

    it('should update kpiData if it has changed', () => {
      const changes: SimpleChanges = {
        kpiData: new SimpleChange({ value: 5 }, { value: 10 }, false),
      };
      component.ngOnChanges(changes);
      expect(component.kpiData).toEqual({ value: 10 });
    });

    it('should not update kpiData if it has not changed', () => {
      const changes: SimpleChanges = {
        kpiData: new SimpleChange({ value: 10 }, { value: 10 }, false),
      };
      component.ngOnChanges(changes);
      expect(component.kpiData).toEqual({ value: 10 });
    });

    it('should call assignColorToNodes if colorObj has changed', () => {
      const changes: SimpleChanges = {
        colorObj: new SimpleChange({ color: 'blue' }, { color: 'red' }, false),
      };
      component.ngOnChanges(changes);
      expect(component.assignColorToNodes).toHaveBeenCalled();
    });

    it('should update kpiConfigData if it has changed', () => {
      const changes: SimpleChanges = {
        kpiConfigData: new SimpleChange(
          { config: 'B' },
          { config: 'A' },
          false,
        ),
      };
      component.ngOnChanges(changes);
      expect(component.kpiConfigData).toEqual({ config: 'A' });
    });

    it('should not update kpiConfigData if it has not changed', () => {
      const changes: SimpleChanges = {
        kpiConfigData: new SimpleChange(
          { config: 'A' },
          { config: 'A' },
          false,
        ),
      };
      component.ngOnChanges(changes);
      expect(component.kpiConfigData).toEqual({ config: 'A' });
    });

    it('should set kpi loader', () => {
      sharedService.setMaturiyTableLoader(true);
      component.ngOnInit();
      expect(component.loader).toBeTruthy();
    });
  });
});
