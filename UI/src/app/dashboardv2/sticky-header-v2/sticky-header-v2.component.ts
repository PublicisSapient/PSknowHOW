import { AfterViewInit, Component, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { HelperService } from 'src/app/services/helper.service';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-sticky-header',
  templateUrl: './sticky-header-v2.component.html',
  styleUrls: ['./sticky-header-v2.component.css']
})
export class StickyHeaderV2Component implements AfterViewInit, OnDestroy {

  fields: Map<string, string> = new Map();
  //isIteration:boolean = false;
  subscriptions: Subscription[] = [];
  colorObj: any = {};
  constructor( public service: SharedService, private helperService: HelperService) { 
    this.subscriptions.push(
      this.service.onTypeOrTabRefresh.subscribe((data)=>{
      //  this.isIteration = data.selectedTab === 'iteration'?true:false;
        this.fields.set('Selected Dashboard ', data.selectedTab);
      }))
  }

  ngAfterViewInit(): void {
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

  }

  objectKeys(obj){
    return this.helperService?.getObjectKeys(obj)
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

}
