import { Component, OnInit,OnDestroy } from '@angular/core';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-filter-list',
  templateUrl: './filter-list.component.html',
  styleUrls: ['./filter-list.component.css']
})
export class FilterListComponent implements OnInit {

  selectedTab = 'mydashboard';
  subscriptions: any[] = [];
  showChart = true
  selectedFilterArray : any;

  constructor(private service : SharedService) { 
    this.selectedTab = (this.service.getSelectedTab() || 'mydashboard');

    this.subscriptions.push(this.service.onTabRefresh.subscribe((selectedTab) => {
      this.selectedTab = selectedTab;
  }));
  }

  ngOnInit(): void {
    this.service.setShowTableView(this.showChart);
    this.subscriptions.push(this.service.selectedFilterListObs.subscribe(list=>{
    // console.log("came on filter -list inside suscribe"+list)
    this.selectedFilterArray = list
   }));
  }


  showChartToggle(val){
    this.showChart = val;
    this.service.setShowTableView(this.showChart);
}


ngOnDestroy() {
  this.subscriptions.forEach(subscription => subscription.unsubscribe());
}

}
