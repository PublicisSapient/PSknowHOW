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
  subscription: Subscription;
  constructor( public service: SharedService) { }

  ngOnInit(): void {
    this.subscription = this.service.currentSelectedSprintSub.subscribe((item)=>{
      this.fields.set('Sprint', item?.nodeName);
    })
  }

  ngAfterViewInit(): void {
    this.fields.set('Selected Tab', this.service?.selectedTab);
    this.fields.set('Project Type', this.service?.selectedtype);
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

}
