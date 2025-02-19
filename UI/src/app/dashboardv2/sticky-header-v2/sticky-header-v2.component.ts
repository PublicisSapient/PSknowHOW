import { AfterViewChecked, AfterViewInit, Component, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Subscription } from 'rxjs';
import { HelperService } from 'src/app/services/helper.service';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-sticky-header',
  templateUrl: './sticky-header-v2.component.html',
  styleUrls: ['./sticky-header-v2.component.css']
})
export class StickyHeaderV2Component implements AfterViewChecked, OnDestroy {

  fields: Map<string, string> = new Map();
  //isIteration:boolean = false;
  subscriptions: Subscription[] = [];
  colorObj: any = {};
  constructor( public service: SharedService, private helperService: HelperService, private cdr: ChangeDetectorRef) { 
    this.subscriptions.push(
      this.service.onTabSwitch.subscribe((data)=>{
        this.fields.set('Selected Dashboard ', JSON.parse(JSON.stringify(data.selectedBoard)));
      }))
  }

  ngAfterViewChecked(): void {
    this.subscriptions.push(this.service.mapColorToProjectObs.subscribe((data) => {
      if (Object.keys(data).length > 0) {
        this.colorObj = data;
      }
      let colorsArr = ['#6079C5', '#FFB587', '#D48DEF', '#A4F6A5', '#FBCF5F', '#9FECFF']
      for (let i = 0; i < data?.length; i++) {
        if (data[i]?.nodeId) {
          this.colorObj[data[i].nodeId] = { nodeName: data[i].nodeName, color: colorsArr[i], nodeId: data[i].nodeId, labelName: data[i].labelName }
        }
      }
    }));
    this.cdr.detectChanges();
  }

  objectKeys(obj){
    return this.helperService?.getObjectKeys(obj)
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

}
