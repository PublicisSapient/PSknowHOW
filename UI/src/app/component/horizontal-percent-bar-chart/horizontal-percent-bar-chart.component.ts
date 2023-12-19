import { Component, HostListener, Input, OnChanges, SimpleChanges, Type, ViewChild, ViewContainerRef } from '@angular/core';
import * as d3 from 'd3';
declare const require: any;

@Component({
  selector: 'app-horizontal-percent-bar-chart',
  templateUrl: './horizontal-percent-bar-chart.component.html',
  styleUrls: ['./horizontal-percent-bar-chart.component.css']
})


export class HorizontalPercentBarChartComponent implements OnChanges {
  @Input() data = [];
  unmodifiedDataCopy = [];
  @Input() isDrilledDown: boolean = false;
  @Input() filter;
  selectedGroup = '';
  @ViewChild('popupHost', { read: ViewContainerRef })
  popupHost!: ViewContainerRef;
  elem: any;
  selectedNode: {};
  @Input() kpiId:string = '';
  @Input() activeTab?: number = 0;
  @Input() kpiWidth:string = '';
  loader: boolean = false;
  constructor(public viewContainerRef: ViewContainerRef) { }

  ngOnChanges(changes: SimpleChanges) {
    this.loader = true;
    d3.select(this.elem).select('#back_icon').attr('class', 'p-d-none');
    if (changes['data']) {
      this.isDrilledDown = false;
      this.elem = this.viewContainerRef.element.nativeElement;
      if (this.data) {
        if (!this.isDrilledDown) {
          this.data = this.data;
          this.unmodifiedDataCopy = JSON.parse(JSON.stringify(this.data));
        }
        this.draw(this.data);
      }
      d3.select(this.elem).select('.tooltip-chart-container').select('app-horizontal-percent-bar-chart').remove();
    }
    if(changes['activeTab']){
      /** settimeout applied because dom is loading late */
      this.isDrilledDown = false;
      setTimeout(() => {
          this.draw(this.data);
      }, 0);
    }
    if (changes['filter']) {
      d3.select(this.elem).select('#back_icon').attr('class', 'p-d-none');
    }
  }


  ngAfterViewInit(){
    this.draw(this.data);
  }

  draw(data, selectedNode = '') {
    if(data && data?.length > 0){
      if(document.getElementById('chart-'+this.kpiId)?.offsetWidth){

        let self = this;
        const elem = this.elem;
        self.selectedNode = selectedNode;
        let isLong: boolean = false;
        data?.forEach(x => {
          if(x.kpiGroup?.length > 15){
            isLong = true;
          }
        })

        const margin = { top: 10, right: 22, bottom: 20, left: this.kpiWidth == '100' && isLong ? 260 : 100};
        let tempWidth:any = (document.getElementById('chart-'+this.kpiId)?.offsetWidth ? document.getElementById('chart-'+this.kpiId)?.offsetWidth : 485)
        let chartContainerWidth = tempWidth;

        // chartContainerWidth = chartContainerWidth <= 490 ? chartContainerWidth : chartContainerWidth - 70;
        const chart = d3.select(elem).select('#chart-'+this.kpiId);
        chart.select('.chart-container').select('svg').remove();
        chart.select('.chart-container').remove();

        const width = chartContainerWidth - margin.left - margin.right;
        const barsTotalWidth = data.length > 7 ? data.length*30 : 180;
        const height = !this.isDrilledDown ? barsTotalWidth - margin.top - margin.bottom : 100;

        // append the svg object to the body of the page
        const svg = chart
          .append('div')
          .attr('class', 'chart-container')
          .append("svg")
          .attr("width", width + margin.left + margin.right)
          .attr("height", height + margin.top + margin.bottom)
          .append("g")
          .attr("transform", `translate(${margin.left},${margin.top})`);


        // let subgroups = Object.keys(data[0]['value']);
        const groups = this.isDrilledDown ? [data.kpiGroup] : data.map(d => d.kpiGroup);
        let subgroups = [];
        if (!this.isDrilledDown) {
          data[0]['value']?.forEach((element) => {
            subgroups.push(element['subFilter']);
          });
        } else {
          data[0]?.forEach((element) => {
            subgroups.push(element['subFilter']);
          });
        }

        const y = d3.scaleBand()
          .domain(groups)
          .range([height, 0])
          .padding([0.4]);

        const x = d3.scaleLinear()
          .domain([0, 100])
          .range([0, width]);

        // Add X axis
        const xAxis = svg.append('g')
          .attr('transform', `translate(10, ${height})`)
          .attr('class', 'xAxis')
          .call(d3.axisBottom(x).tickSize(0).tickFormat(d => d + '%').ticks(6));
        xAxis.selectAll('path')
          .style('display', 'none');

        //Add vertical gridlines for each x tick
        svg.append('g')
          .attr('transform', `translate(10, ${height})`).selectAll('line.gridline').data(x.ticks(6)).enter()
          .append('svg:line')
          .attr('x1', d => x(d))
          .attr('x2', d => x(d))
          .attr('y1', 0)
          .attr('y2', -height)
          .style('stroke', '#BDBDBD')
          .style('fill', 'none')
          .attr('class', 'gridline');

        // Add Y axis
        const yAxis = svg.append('g')
          .attr('class', 'yAxis')
          .call(d3.axisLeft(y).tickSize(0));

        // Creating y-axis text as hyperlink
        yAxis.selectAll("text")
        .classed("link", (d)=>{
          const url = data.find(de=>de.kpiGroup === d)?.url;
          return (url ? true : false) 
        })
        .on("click", function(event,d){
          const url = data.find(de=>de.kpiGroup === d)?.url;
          if(url){
            window.open(url, "_blank");
          }
        });

        yAxis.selectAll('text')
          .style('font-size', '10px')
          .call(this.wrap, this.kpiWidth);

        yAxis.select('path')
          .style('display', 'none')
          .style('font-family', 'inherit');

        // color palette = one color per subgroup
        const color = d3.scaleOrdinal()
          .domain(subgroups)
          .range(subgroups.length > 3 ? d3.schemePaired : ['#4472C4', '#F4AA46', '#7FBD7F'])
          .unknown("#ccc");

        // Normalize the data -> sum of each group must be 100!
        if (!self.isDrilledDown) {
          data?.forEach((d) => {
            let tot = 0;
            for (const i in subgroups) {
              const name = subgroups[i];
              tot += +d['value'].filter((x) => x.subFilter === name)[0]['value'];
            }
            for (const i in subgroups) {
              const name = subgroups[i];
              if (tot > 0) {
                d[name] = d['value'].filter((x) => x.subFilter === name)[0]['value'] / tot * 100;
              } else {
                d[name] = 0;
              }
            }
          });
        } else {
          data?.forEach((d) => {
            let tot = 0;
            for (const i in subgroups) {
              const name = subgroups[i];
              tot += +d.filter((x) => x.subFilter === name)[0]['value'];
            }
            for (const i in subgroups) {
              const name = subgroups[i];
              d[name] = d.filter((x) => x.subFilter === name)[0]['value'] / tot * 100;
            }
          });
        }

        //stack the data? --> stack per subgroup
        const stackedData = d3.stack()
          .keys(subgroups)
          (data);

        // Show the bars
        svg.append('g')
          .attr('transform', `translate(10, 0)`)
          .attr('class', 'bars')
          .selectAll('g')
          .data(stackedData)
          .join('g')
          .attr('fill', d => {
            return color(d.key);
          })
          .on('click', (event, d) => {
            if (!self.isDrilledDown && event.target.__data__.data && event.target.__data__.data.kpiGroup) {
              self.isDrilledDown = true;
              let key = d['key'];
              let kpiGroup = event.target.__data__.data.kpiGroup;
              let selectedNode = d.filter((x) => x.data['kpiGroup'] === kpiGroup)[0];
              if (selectedNode) {
                data = [selectedNode.data.value.filter((val) => val.subFilter === key)[0].drillDown];
                if (data && data.length && data[0]) {
                  data.kpiGroup = key;
                  this.draw(data, selectedNode);
                  d3.select(elem).select('#back_icon').attr('class', 'p-d-flex')
                    .on('click', (event, d) => {
                      this.isDrilledDown = false;
                      this.draw(this.unmodifiedDataCopy);
                      d3.select(elem).select('#back_icon').attr('class', 'p-d-none');
                    });
                } else {
                  event.preventDefault();
                  event.stopPropagation();
                }
              }
            }
          })
          .selectAll('rect')
          .data(d => d)
          .join('rect')
          .attr('x', d => x(d[0]))
          .attr('y', d => y(d.data.kpiGroup))
          .on('mouseover', (event, d) => {
            this.selectedGroup = d.data.kpiGroup;
            if (d.data && d.data && d.data.kpiGroup) {
              let toolTipDataCollection = JSON.parse(JSON.stringify(self.data));
              const tooltipData = toolTipDataCollection.filter(tooltip => tooltip.kpiGroup === this.selectedGroup)[0];
              d3.select(elem).select('#chart-'+this.kpiId).select('#legendContainer').selectAll('div').remove();
              this.showTooltip(subgroups, width, margin, color, tooltipData, elem, height);
            }
          })
          .on('mouseout', (event, d) => {
            if(this.data.length >1){
              d3.select(elem).select('#legendContainer').selectAll('div').remove();
              this.showLegend(subgroups, width, margin, color, elem, data, height);
            }
          })
          .attr('height', y.bandwidth());

        if (this.isDrilledDown) {
          svg.selectAll('rect')
            .transition()
            .ease(d3.easeLinear)
            .duration(200)
            .delay(function (d, i) { return i * 200 }) //a different delay for each rect
            .attr('width', d => x(d[1]) - x(d[0]))
            .style('cursor', this.isDrilledDown ? 'default' : 'pointer')
        } else {
          svg.selectAll('rect')
            .transition()
            .ease(d3.easeLinear)
            .duration(200)
            .attr('width', d => x(d[1]) - x(d[0]))
            .style('cursor', this.isDrilledDown ? 'default' : 'pointer')
        }

       this.loader = false;
        if(this.data.length > 1 || this.isDrilledDown){
          this.showLegend(subgroups, width, margin, color, elem, data, height);
        }
        if(this.data.length == 1 && !this.isDrilledDown){
          this.showTooltip(subgroups, width, margin, color, data[0], elem, height);
        }
      }
    }
  }

  showLegend(subgroups, width, margin, color, elem, data, height) {
    let legendDiv;
    legendDiv = d3.select(elem).select('#chart-'+this.kpiId).select('#legendContainer');

    legendDiv
      .style('width', '95%')
      .style('margin', '0 auto')
      .transition()
      .duration(200)
      .style('opacity', 1)
      .attr('class', 'p-d-flex p-flex-wrap p-justify-center normal-legend');

    let htmlString = '';
    if (!this.isDrilledDown) {
      subgroups.forEach((d, i) => {
        htmlString += `<div class="legend_item"><div class="legend_color_indicator" style="background-color: ${color(d)}"></div> <span class="p-m-1" style="font-weight:bold">: ${d}</span></div>`;
      });
    } else {
      subgroups.forEach((d, i) => {
        htmlString += `<div class="legend_item"><div class="legend_color_indicator" style="background-color: ${color(d)}"></div> <span class="p-m-1" style="font-size: 10px; font-weight:bold;">: ${d}
        (${data[0].filter((val) => val.subFilter === d)[0]['value']} | <span style="font-weight:bold">${data[0][d].toFixed(2).replace(/\.00$/, '')}%</span>)
        </span></div>`;
      });
    }

    legendDiv.html(htmlString)
    legendDiv.style('bottom', 40 + 'px');
  }

  showTooltip(subgroups, width, margin, color, tooltipData, elem, height) {
    const legendDiv = d3.select(elem).select('#chart-'+this.kpiId).select('#legendContainer')
      .style('margin', '0 auto')
      .style('width', '95%');

    legendDiv.transition()
      .duration(200)
      .style('opacity', 1)
      .attr('class', 'p-d-flex p-flex-column p-align-center normal-legend');

    let htmlString = `<div class="p-text-center">${tooltipData.kpiGroup}</div><div class="p-d-flex p-flex-wrap">`;

    subgroups.forEach((d, i) => {
      htmlString += `<div class="legend_item" style="flex-direction:column; align-items:start; margin-right:1.5rem">
                    <div style="margin-bottom:0.5rem" class="p-d-flex p-align-center">
                      <div class="legend_color_indicator" style="background-color: ${color(d)}">
                      </div>
                      <span class="p-m-1" style="font-weight:bold">: ${d}</span>
                    </div>
                    <div style="font-size: 0.75rem;">Value: <span style="color:${color(d)}; font-weight:bold">${tooltipData['value'].filter((x) => x.subFilter === d)[0]['value']}</span></div>
                    <div style="font-size: 0.75rem;">Percentage: <span style="color:${color(d)} ; font-weight:bold">${tooltipData[d].toFixed(2).replace(/\.00$/, '')}%</span></div>
                    </div>`;
    });
    htmlString += '</div>'
    legendDiv.html(htmlString)
      .style('bottom', 30 + 'px');
  }

  // Required for dynamic component only; not in use right now
  showDistributionChartOnTooltip(event, d) {
    let variableWidthOffset = document.documentElement.clientWidth > 1500 ? -200 : 200;
    // position the tooltip
    const xPosition = event.screenX;
    const yPosition = event.screenY;
    d3.select('.tooltip-chart-container')
      .style('display', 'block')
      .style('height', 'auto')
      .style('min-height', '280px')
      .style('position', 'absolute')
      .style('width', 'auto')
      .style('top', -yPosition + 450 + 'px')
      .style('left', xPosition - variableWidthOffset + 'px')
      .style('padding-right', '50px')
      .style('z-index', '1001');
    // Create component dynamically inside the ng-template
    this.popupHost.clear();
    const popupComponentRef = this.popupHost.createComponent(HorizontalPercentBarChartComponent);
    popupComponentRef.setInput('data', [d]);
    popupComponentRef.setInput('isOnTooltip', true);
  }

  wrap(text, kpiWidth) {
    let textLength = 15;
    if(kpiWidth == '100'){
      textLength = 48;
    }
    text.each(function () {
      let text = d3.select(this);
      if (text.text().length > textLength) {
          text.text(text.text().substring(0, textLength) + '...');
        }
    });
  }

  ngOnDestroy() {
    // this is used for removing svg already made when value is updated
    d3.select(this.elem).select('#chart-'+this.kpiId).select('.chart-container').remove();
    this.data = [];
  }
}
