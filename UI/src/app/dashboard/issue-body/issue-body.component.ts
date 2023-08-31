import { Component, OnInit ,Input} from '@angular/core';

@Component({
  selector: 'app-issue-body',
  templateUrl: './issue-body.component.html',
  styleUrls: ['./issue-body.component.css']
})
export class IssueBodyComponent implements OnInit {
  @Input() issueData;
  constructor() { }

  ngOnInit(): void {
  }

}
