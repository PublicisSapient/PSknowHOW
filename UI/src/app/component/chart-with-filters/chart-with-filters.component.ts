import { Component, EventEmitter, Input, OnChanges, SimpleChanges, ViewChild, ViewContainerRef } from '@angular/core';
import * as d3 from 'd3';
import { ExportExcelComponent } from '../export-excel/export-excel.component';

@Component({
  selector: 'app-chart-with-filters',
  templateUrl: './chart-with-filters.component.html',
  styleUrls: ['./chart-with-filters.component.css']
})
export class ChartWithFiltersComponent implements OnChanges {
  @Input() data;
  dataCopy;
  modifiedData;
  legendData;
  @Input() filters;
  @Input() kpiName;
  @Input() modalHeads;
  displayModal: boolean = false;
  selectedMainFilter;
  modalDetails;
  selectedFilter2;
  elem;

  constructor(private viewContainerRef: ViewContainerRef) { }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['data'] || changes['filters']) {
      if (this.selectedFilter2?.length) {
        this.selectedFilter2.forEach(filter => {
          filter.selectedValue = null;
        });
        this.selectedFilter2 = null;
      }
      this.elem = this.viewContainerRef.element.nativeElement;
      this.modifiedData = this.groupData(this.data, 'Issue Status');
      this.dataCopy = Object.assign([], this.data);

      this.selectedMainFilter = this.filters.filterGroup1[0];
      this.populateLegend(this.modifiedData);

      this.modalDetails = {
        header: this.kpiName + ' ' + this.selectedMainFilter.filterName,
        tableHeadings: this.modalHeads,
        tableValues: this.data,
      };

      this.populateAdditionalFilters();
      setTimeout(() => this.draw(this.modifiedData), 100);
    }
  }

  draw(data) {
    d3.select(this.elem).select('svg').remove();
    let width = 250,
      height = 250,
      margin = 20;

    // The radius of the pieplot is half the width or half the height (smallest one).
    let radius = Math.min(width, height) / 2 - margin;

    let svg = d3.select(this.elem).select('#chart')
      .append("svg")
      .attr("width", width)
      .attr("height", height)
      .append("g")
      .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");
    var color = d3.scaleOrdinal(d3.schemeTableau10);

    var data_ready = Object.entries(data).map(([key, value]) => ({ key, value }));
    let pie_input = [];
    data_ready.forEach(d => {
      pie_input.push({
        key: d.key,
        value: d.value[Object.keys(d.value)[0]],
        name: Object.keys(d.value)[0]
      })
    })
    console.log(pie_input);
    let pie = d3.pie().value(function (d) { return d.value; });
    data_ready = pie(pie_input);
    // Build the pie chart: Basically, each part of the pie is a path that we build using the arc function.
    svg
      .selectAll('whatever')
      .data(data_ready)
      .enter()
      .append('path')
      .attr('d', d3.arc()
        .innerRadius(radius / 1.5)         // This is the size of the donut hole
        .outerRadius(radius)
      )
      .attr('fill', function (d) { return (color(d.data.key)) })
      .attr("stroke", function (d) { return (color(d.data.key)) })
      .style("stroke-width", "1px")
      .style("opacity", 0.7)
      .style('cursor', 'pointer')
      .on('click', (event, d) => this.exploreData(d.data.name));
  }

  groupData(arr, property) {
    if (arr?.length) {
      // Create an object to store the grouped counts
      const groupedCounts = {};

      // Iterate through the array
      arr.forEach(item => {
        // Get the value of the specified property
        const propValue = item[property];

        // If the value is already in the groupedCounts object, increment its count
        if (groupedCounts[propValue]) {
          groupedCounts[propValue]++;
        } else {
          // Otherwise, initialize the count for this value to 1
          groupedCounts[propValue] = 1;
        }
      });

      // Convert the groupedCounts object to an array of objects
      const result = Object.keys(groupedCounts).map(key => ({
        [key]: groupedCounts[key]
      }));

      return result;
    } else {
      return [];
    }
  }

  populateLegend(arr) {
    this.legendData = [];
    let total = 0;
    arr.forEach(element => {
      total += element[Object.keys(element)[0]];
    });
    console.log(total);
    var color = d3.scaleOrdinal(d3.schemeTableau10);
    arr.forEach(element => {
      this.legendData.push({
        key: Object.keys(element)[0],
        value: element[Object.keys(element)[0]],
        percentage: Math.round(element[Object.keys(element)[0]] / total * 100 * 100) / 100,
        color: color(Object.keys(element)[0])
      })
    });
  }

  mainFilterSelect(event) {
    this.selectedMainFilter = event.option;
    this.modifiedData = this.groupData(this.dataCopy, this.selectedMainFilter.filterKey);
    console.log(this.modifiedData);
    this.draw(this.modifiedData);
    this.legendData = [];
    this.populateLegend(this.modifiedData);
  }

  exploreData(filterVal) {
    this.modalDetails = {
      header: this.kpiName + ' ' + this.selectedMainFilter.filterName,
      tableHeadings: this.modalHeads,
      tableValues: this.dataCopy.filter((d) => {
        if (this.selectedMainFilter.filterType === 'Single') {
          return d[this.selectedMainFilter.filterKey] === filterVal;
        } else {
          return d[this.selectedMainFilter.filterKey].includes(filterVal);
        }
      })
    }
    this.displayModal = true;
  }

  clearModalDataOnClose() {
    this.displayModal = false;
    this.modalDetails = {
      header: '',
      tableHeadings: [],
      tableValues: [],
    };
  }

  checkIfArray(arr) {
    return Array.isArray(arr);
  }

  populateAdditionalFilters() {
    this.filters.filterGroup2.forEach(element => {
      element.values = this.getUniquePropertyValues(this.data, element.filterKey)
    });
  }

  getUniquePropertyValues(arr: any[], propertyName: string): any[] {
    const uniqueValues = new Set();

    arr.forEach(item => {
      if (Array.isArray(item[propertyName])) {
        item[propertyName].forEach(element => {
          uniqueValues.add(element);
        });
      } else if (item.hasOwnProperty(propertyName)) {
        uniqueValues.add(item[propertyName]);
      }
    });
    return Array.from(uniqueValues);
  }

  applyAdditionalFilters(event) {
    this.modifiedData = event.modifiedData;
    this.dataCopy = event.dataCopy;
    this.populateLegend(this.modifiedData);
    setTimeout(() => this.draw(this.modifiedData), 100);
  }

  clearAdditionalFilters() {
    this.dataCopy = Object.assign([], this.data);
    this.modifiedData = this.groupData(this.data, this.selectedMainFilter.filterKey);
    this.selectedFilter2.forEach(filter => {
      filter.selectedValue = null;
    });
    this.selectedFilter2 = null;
    this.populateLegend(this.modifiedData);
    setTimeout(() => this.draw(this.modifiedData), 100);
  }
}
