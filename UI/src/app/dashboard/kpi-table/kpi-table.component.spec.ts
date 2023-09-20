import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KpiTableComponent } from './kpi-table.component';
import { SharedService } from 'src/app/services/shared.service';

describe('KpiTableComponent', () => {
  let component: KpiTableComponent;
  let fixture: ComponentFixture<KpiTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ KpiTableComponent ],
      providers : [SharedService]
    })
    .compileComponents();

    fixture = TestBed.createComponent(KpiTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show tooltip on mouse enter', () =>{
    const event = {
      'pageY': '560',
      'pageX': '600'
    };
    const field = 'frequency';
    const data = {
      'hoverText': ["1-sprint1", "2-sprint2", "3-sprint3"]
    }
    component.mouseEnter(event, field, data);
    expect(component.showToolTip).toBe(true);
  })

  it('should hide tooltip on mouse leave', () => {
    component.mouseLeave();
    expect(component.showToolTip).toBe(false);
    expect(component.toolTipHtml).toBe('');
  })

  it('should assign colors to node', ()=> {
    component.tabs = [];
    component.colorObj = {
      "AddingIterationProject_64e739541426ba469c39c102": {
          "nodeName": "AddingIterationProject",
          "color": "#079FFF"
      }
    };
    component.assignColorToNodes();
    expect(Object.keys(component.nodeColors)?.length).toEqual(Object.keys(component.colorObj)?.length);
    expect(component.tabs?.length).toEqual(Object.keys(component.nodeColors)?.length);
  })
});
