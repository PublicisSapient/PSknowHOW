import { Component, Input, OnInit } from '@angular/core';
import { HttpService } from 'src/app/services/http.service';

@Component({
  selector: 'app-recommendations',
  templateUrl: './recommendations.component.html',
  styleUrls: ['./recommendations.component.css']
})
export class RecommendationsComponent implements OnInit {
  displayModal:boolean = false;
  modalDetails = {
    tableHeadings: [],
    tableValues: [],
    kpiId: ''
  };

  constructor(private httpService: HttpService) { }

  ngOnInit(): void {
  }

  handleClick(){
    this.displayModal = true;
    let obj = {
      
    }
    this.httpService.getRecommendations(obj).subscribe((response) => {

    })
  }
}
