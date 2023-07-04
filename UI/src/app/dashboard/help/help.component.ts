import { Component, OnInit } from '@angular/core';
import { HttpService } from 'src/app/services/http.service';
import { SharedService } from 'src/app/services/shared.service';

declare let require: any;

@Component({
  selector: 'app-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.css']
})

export class HelpComponent implements OnInit {
  selectedValue = '';
  overallSummary: any;
  area;
  landingInfo;
  
  verticalArray: Array<string> = [];
  summaryItems: any = [];
  tableHeadingArr: any = [];
  totalsArray: any = [];
  totalProjects: any = 0;
  totalProjects30Days: any = 0;
  totalUsers: any = 0;
  newUsers: any = 0;
  isProducer: Boolean = false;
  userName : string;

  constructor(private httpService: HttpService,private sharedService : SharedService) { }

  ngOnInit(): void {
  }

  

 
  getTotalUsersCount(){
    this.httpService.getUsersCount().subscribe((response)=>{
      if(response.data){
        this.totalUsers = response.data['Total Users'];
        this.newUsers = response.data['New Users Added in last 30 days'];
      }
    }, error=>{
      console.log(error);
    });
  }

}
