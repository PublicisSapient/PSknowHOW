import { Component, OnInit, ElementRef } from '@angular/core';
import * as d3 from 'd3';
import iterationCommitment from '../../../assets/data/iteration-commitment-v2.json';

@Component({
  selector: 'kpi-card-v3',
  templateUrl: './kpi-card-v3.component.html',
  styleUrls: ['./kpi-card-v3.component.css']
})

export class KpiCardV3Component implements OnInit {
    private data = [iterationCommitment];
    categoryValues = {};
      private svg: any;
      private margin = 50;
      private width = 750 - (this.margin * 2);
      private height = 400 - (this.margin * 2);
    
      constructor(private elRef: ElementRef) {}
    
      ngOnInit(): void {
        this.data[0].issueData.forEach(issue => {
          const category = issue["Category"][0]; // Assuming "Category" is always an array
          const value = 0;//issue["Value"];
        
          if (this.categoryValues[category]) {
            this.categoryValues[category] += value; // Sum values for the same category
          } else {
            this.categoryValues[category] = value;
          }
        });
        
        const chartData = Object.entries(this.categoryValues).map(([key, value]) => ({
          category: key,
          totalValue: value,
        }));
        console.log(chartData)
       
        this.createSvg();
       this.drawBars(chartData);
      }

      private createSvg(): void {
        this.svg = d3.select(this.elRef.nativeElement)
          .select('.chart-container')
          .append('svg')
          .attr('width', this.width + (this.margin * 2))
          .attr('height', this.height + (this.margin * 2))
          .append('g')
          .attr('transform', 'translate(' + this.margin + ',' + this.margin + ')');
      }
    
      private drawBars(data: any[]): void {
        const x = d3.scaleLinear()
          .domain([d3.min(data, d => d.value)!, d3.max(data, d => d.value)!])
          .range([0, this.width]);
    
        this.svg.append('g')
          .attr('transform', 'translate(0,' + this.height / 2 + ')')
          .call(d3.axisBottom(x));
    
        this.svg.selectAll('bars')
          .data(data)
          .enter()
          .append('rect')
          .attr('x', d => x(Math.min(0, d.value)))
          .attr('y', this.height / 3)
          .attr('width', d => Math.abs(x(d.value) - x(0)))
          .attr('height', this.height / 4)
          .attr('fill', d => d.color);
    
        this.svg.selectAll('labels')
          .data(data)
          .enter()
          .append('text')
          .text(d => d.value)
          .attr('x', d => x(d.value) + (d.value > 0 ? -20 : 10))
          .attr('y', this.height / 2 - 10)
          .attr('fill', 'white');
      }
    
      filterData(filter: string) {
        // Implement your filtering logic here
        //const filteredData = this.data.filter(d => d.Category.includes(filter));
        //this.svg.selectAll('*').remove(); // Clear previous chart
        //this.drawBars(filteredData);
      }
}