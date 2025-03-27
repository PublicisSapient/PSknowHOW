import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-issue-body',
  templateUrl: './issue-body.component.html',
  styleUrls: ['./issue-body.component.css'],
})
export class IssueBodyComponent {
  @Input() issueData;

  convertToHoursIfTime(val, unit) {
    if (val === '-' || isNaN(val)) {
      return val;
    }
    const isLessThanZero = val < 0;
    val = Math.abs(val);
    const hours = val / (60 * 60);
    const rhours = Math.floor(hours);
    const minutes = (hours - rhours) * 60;
    const rminutes = Math.round(minutes);
    if (unit?.toLowerCase() === 'day') {
      if (val !== 0) {
        val = this.convertToDays(rminutes, rhours);
      } else {
        val = '0d';
      }
    }
    if (isLessThanZero) {
      val = '-' + val;
    }
    if (val === '') {
      val = '0d';
    }
    return val;
  }

  convertToDays(rminutes, rhours) {
    const days = rhours / 8;
    const rdays = Math.floor(days);
    rhours = (days - rdays) * 8;
    return `${rdays !== 0 ? rdays + 'd ' : ''}${
      rhours !== 0 ? rhours + 'h ' : ''
    }`;
  }
}
