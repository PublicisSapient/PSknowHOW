import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
} from '@angular/core';

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.css'],
})
export class TableComponent implements OnInit, OnChanges {
  @Input() cols;
  @Input() data;
  @Input() showMarker = false;
  @Input() showMarkerColumnNumber;
  @Input() trendBoxColorObj;
  constructor() {}

  ngOnChanges(changes: SimpleChanges) {
    // only run when property "data" changed
    if (changes['data']) {
      this.data = this.sortAlphabetically(this.data);
    }
  }

  ngOnInit(): void {
    this.data = this.sortAlphabetically(this.data);
  }

  sortAlphabetically(objArray) {
    objArray?.sort((a, b) => a.name.localeCompare(b.name));
    return objArray;
  }

  getColorForRow(rowName: string): string {
    if (!this.trendBoxColorObj) return '';
    const matchingKey = Object.keys(this.trendBoxColorObj).find(
      (key) => this.trendBoxColorObj[key].nodeName === rowName,
    );
    return matchingKey ? this.trendBoxColorObj[matchingKey].color : '';
  }
}
