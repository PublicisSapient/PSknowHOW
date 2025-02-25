import { Component, Input, OnChanges, OnInit, SimpleChanges, ViewContainerRef } from '@angular/core';
import * as d3 from 'd3';
import { ExportExcelComponent } from '../export-excel/export-excel.component';
import { SharedService } from 'src/app/services/shared.service';

@Component({
  selector: 'app-chart-with-filters',
  templateUrl: './chart-with-filters.component.html',
  styleUrls: ['./chart-with-filters.component.css']
})
export class ChartWithFiltersComponent implements OnInit, OnChanges {
  @Input() data;
  dataCopy;
  modifiedData;
  legendData;
  @Input() filters;
  @Input() kpiName;
  @Input() modalHeads;
  @Input() selectedTab;
  @Input() category;
  @Input() kpiId;
  displayModal: boolean = false;
  selectedMainFilter;
  modalDetails;
  selectedFilter2;
  elem;
  selectedMainCategory: string = '';
  psColors = ['#FBCF5F', '#A4F6A5', '#FFB688', '#D38EEC', '#ED8888', '#6079C5', '#9FE8FA', '#D4CEB0', '#99CDA9', '#6079C5'];

  constructor(private viewContainerRef: ViewContainerRef, private service: SharedService) { }

  ngOnInit(): void {
    this.selectedMainFilter = this.service.getKpiSubFilterObj()[this.kpiId] && this.service.getKpiSubFilterObj()[this.kpiId]['selectedMainFilter'] ? 
    this.service.getKpiSubFilterObj()[this.kpiId]['selectedMainFilter'] : 
    this.filters.filterGroup1[0];
    if (this.category && this.category.length && this.category[1]) {
      this.selectedMainCategory = this.service.getKpiSubFilterObj()[this.kpiId] && this.service.getKpiSubFilterObj()[this.kpiId]['selectedMainCategory'] ? 
      this.service.getKpiSubFilterObj()[this.kpiId]['selectedMainCategory'] : 
      this.category[1];
    }
    this.modifiedData = this.groupData(this.data, this.selectedMainFilter.filterKey);
    if(this.selectedMainCategory){this.categorySelect({option:this.selectedMainCategory})}
    this.mainFilterSelect({option:this.selectedMainFilter})
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['data'] || changes['filters']) {
      this.modalHeads = this.data.modalHeads || this.modalHeads;
      this.data = this.data.chartData || this.data;

      if (this.selectedFilter2?.length) {
        this.selectedFilter2.forEach(filter => {
          filter.selectedValue = null;
        });
        this.selectedFilter2 = null;
      }
      this.elem = this.viewContainerRef.element.nativeElement;
      this.modifiedData = this.groupData(this.data, 'Issue Status');
      this.dataCopy = Object.assign([], this.data);

      this.selectedMainFilter = this.service.getKpiSubFilterObj()[this.kpiId] && this.service.getKpiSubFilterObj()[this.kpiId]['selectedMainFilter'] ? 
      this.service.getKpiSubFilterObj()[this.kpiId]['selectedMainFilter'] : 
      this.filters.filterGroup1[0];
      if (this.category && this.category.length && this.category[1]) {
        this.selectedMainCategory = this.service.getKpiSubFilterObj()[this.kpiId] && this.service.getKpiSubFilterObj()[this.kpiId]['selectedMainCategory'] ? 
        this.service.getKpiSubFilterObj()[this.kpiId]['selectedMainCategory'] : 
        this.category[1];
      }
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
    var color = d3.scaleOrdinal([...this.psColors, ...d3.schemeTableau10]);

    var data_ready = Object.entries(data).map(([key, value]) => ({ key, value }));
    let pie_input = [];
    data_ready.forEach(d => {
      pie_input.push({
        key: d.key,
        value: d.value[Object.keys(d.value)[0]],
        name: Object.keys(d.value)[0]
      })
    })
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
      // .style("opacity", 0.7)
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
    var color = d3.scaleOrdinal([...this.psColors, ...d3.schemeTableau10]);
    arr.forEach(element => {
      this.legendData.push({
        key: Object.keys(element)[0],
        value: element[Object.keys(element)[0]],
        percentage: Math.round(element[Object.keys(element)[0]] / total * 100 * 100) / 100,
        color: color(Object.keys(element)[0])
      })
    });

    this.legendData.sort((a, b) => b.percentage - a.percentage);
  }

  mainFilterSelect(event) {
    this.selectedMainFilter = event.option;
    this.modifiedData = this.groupData(this.dataCopy, this.selectedMainFilter.filterKey);
    this.draw(this.modifiedData);
    this.legendData = [];
    this.populateLegend(this.modifiedData);
    this.service.setKpiSubFilterObj({ [this.kpiId]: { selectedMainCategory: this.selectedMainCategory,  selectedMainFilter: this.selectedMainFilter } });

  }

  exploreData(filterVal) {
    this.modalDetails = {
      header: this.kpiName + ' ' + this.selectedMainFilter.filterName,
      tableHeadings: this.modalHeads.map(item => (item === 'Defect ID' ? 'Issue Id' : item)),
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
    this.filters.filterGroup2?.forEach(element => {
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

  categorySelect(event) {
    this.dataCopy = this.data.filter(issue => issue.Category.includes(event.option.categoryName));
    this.modifiedData = this.groupData(this.dataCopy, this.selectedMainFilter.filterKey);
    setTimeout(() => {this.draw(this.modifiedData);}, 0);
    this.legendData = [];
    this.populateLegend(this.modifiedData);
    this.service.setKpiSubFilterObj({ [this.kpiId]: { selectedMainCategory: event.option,  selectedMainFilter: this.selectedMainFilter } });
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
