import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-sticky-header',
  templateUrl: './sticky-header-v2.component.html',
  styleUrls: ['./sticky-header-v2.component.css']
})
export class StickyHeaderV2Component implements OnInit,AfterViewInit, OnDestroy {

  fields: Map<string, string> = new Map();
  subscriptions: Subscription[] = [];
  constructor( public service: SharedService) { }

  ngOnInit(): void {
    this.subscriptions.push(
      this.service.currentSelectedSprintSub.subscribe((item)=>{
        this.fields.set('Sprint', item?.nodeName);
      })
    )
  }

  ngAfterViewInit(): void {
    this.subscriptions.push(
    this.service.onTypeOrTabRefresh.subscribe((data)=>{
      this.fields.set('Selected Tab', data.selectedTab);
    this.fields.set('Project Type', data.selectedType);
      
    }))
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

}
