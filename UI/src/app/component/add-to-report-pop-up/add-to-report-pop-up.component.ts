import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-add-to-report-pop-up',
  templateUrl: './add-to-report-pop-up.component.html',
  styleUrls: ['./add-to-report-pop-up.component.css']
})
export class AddToReportPopUpComponent implements OnInit {
  @Input() reportObj: any;

  constructor() { }

  ngOnChanges() {
    // this.reportObj.metadata = JSON.parse(this.reportObj.metadata);
    this.reportObj.metadata.trendColors = this.removeDuplicateKeys(this.reportObj.metadata.trendColors);
  }

  ngOnInit(): void {
  }

  objectValues(obj): any[] {
    // return this.helperService.getObjectKeys(obj)
    let result = [];
    if (obj && Object.keys(obj)?.length) {
      Object.keys(obj).forEach((x) => {
        result.push(obj[x]);
      });
    }
    return result;
  }

  objectKeys(obj) {
    return Object.keys(obj);
  }

  canonicalize(obj) {
    if (obj === null || typeof obj !== 'object') {
      return JSON.stringify(obj);
    }

    if (Array.isArray(obj)) {
      // For arrays, canonicalize each element.
      return '[' + obj.map(this.canonicalize).join(',') + ']';
    }

    // For objects, sort keys, then canonicalize.
    const sortedKeys = Object.keys(obj).sort();
    const sortedObj = {};
    sortedKeys.forEach(key => {
      sortedObj[key] = obj[key];
    });
    return JSON.stringify(sortedObj);
  }

  /**
   * Removes keys from an object whose values (after deep canonicalization) are duplicates.
   * @param {object} inputObj - The input object.
   * @returns {object} - A new object containing only unique value entries.
   */
  removeDuplicateKeys(inputObj) {
    const seen = new Set();
    const result = {};

    Object.keys(inputObj).forEach(key => {
      // Create a canonical fingerprint for deep comparison.
      const fingerprint = this.canonicalize(inputObj[key]);
      if (!seen.has(fingerprint)) {
        seen.add(fingerprint);
        result[key] = inputObj[key];
      }
    });

    return result;
  }

}
