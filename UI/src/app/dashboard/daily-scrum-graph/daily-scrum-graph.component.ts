import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, ViewContainerRef } from '@angular/core';
import * as d3 from 'd3';
import { SharedService } from 'src/app/services/shared.service';
import { setTimeout } from 'timers';

@Component({
  selector: 'app-daily-scrum-graph',
  templateUrl: './daily-scrum-graph.component.html',
  styleUrls: ['./daily-scrum-graph.component.css']
})
export class DailyScrumGraphComponent implements OnChanges, OnDestroy {


  @Input() issueDataList;
  @Input() selectedSprintInfo;
  @Input() standUpStatusFilter;
  elem;

  currentDayIndex;
  displayModal = false;
  constructor(private viewContainerRef: ViewContainerRef, private service: SharedService) { }

  ngOnChanges(changes: SimpleChanges) {
    this.elem = this.viewContainerRef.element.nativeElement;
    window.setTimeout(() => {
      this.draw(this.issueDataList);
    }, 0);
  }

  showLegends() {
    this.displayModal = !this.displayModal;
  }

  //generated dates on MM/DD format
  generateDates() {
    const currentDate = new Date();

    const startDate = new Date(this.selectedSprintInfo.sprintStartDate);
    const endDate = new Date(this.selectedSprintInfo.sprintEndDate);
    const xAxisCoordinates = [];
    const noOfDaysInCurrentSprint = this.getNoOFDayBetweenStartAndEndDate(startDate, endDate);
    for (let i = 0; i < noOfDaysInCurrentSprint; i++) {
      const date = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate() + i);
      if (date.toDateString() === currentDate.toDateString()) {
        this.currentDayIndex = i;
      }
      xAxisCoordinates.push(this.formatDate(date));
    }
    return xAxisCoordinates;
  }

  getNoOFDayBetweenStartAndEndDate(startDate, endDate) {
    const _MS_PER_DAY = 1000 * 60 * 60 * 24;
    return (Date.UTC(endDate.getFullYear(), endDate.getMonth(), endDate.getDate()) - Date.UTC(startDate.getFullYear(), startDate.getMonth(), startDate.getDate())) / _MS_PER_DAY + 1;
  }

  formatDate(date) {
    date = new Date(date);
    return this.pad(date.getMonth() + 1) + '/' + this.pad(date.getDate());
  }

  pad(s) {
    return s < 10 ? '0' + s : s;
  }

  draw(issueList, parentIssue = null, parentStoryClick = false, previousScroll = 0) {
    const chart = d3.select(this.elem).select('#chart');
    const scroller = d3.select(this.elem).select('#scroller');
    chart.selectAll('svg').remove();
    d3.select(this.elem)
      .select('#dateAxis').select('svg').remove();
    d3.select(this.elem)
      .select('#issueAxis').select('*').remove();

    // modify issues list
    let selectedIssueSubtask = [];
    let issueDataList = [];
    if (!parentStoryClick) {
      issueDataList = [...issueList.filter((issue) => {
        if (issue['parentStory'] && issue['parentStory'].length) {
          return !issueList.map((issue2) => issue2['Issue Id']).includes(issue['parentStory'][0])
        } else {
          return issue;
        }
      })];
    } else {
      let otherExpandedStories = issueList.filter((f) => f['IsExpanded'] && f['Issue Id'] !== parentIssue['Issue Id']);
      otherExpandedStories.forEach((element) => {
        delete element['IsExpanded'];
        issueList.splice(issueList.findIndex((obj) => obj['Issue Id'] === element['Issue Id']) + 1, element['subTask'].length);
      });

      issueDataList = [...issueList.filter((issue) => {
        let independentSubtasks = issueList.filter((f) => f['parentStory'] && f['parentStory'].length && !issueList.includes(f['parentStory'][0])).map(m => m['Issue Id']);
        if (issue['parentStory'] && issue['parentStory'].length && issue['parentStory'].includes(parentIssue['Issue Id'])) {
          return issue;
        } else if (independentSubtasks.includes(issue['Issue Id'])) {
          return issue;
        } else if (!issue['parentStory']) {
          return issue;
        }
      })];
    }

    const xCoordinates = this.generateDates();
    const margin = { top: 30, right: 10, bottom: 20, left: 100 };
    let width = chart.node().parentNode.offsetWidth - margin.left - margin.right - 12.5 / 100 * scroller.node().offsetWidth;
    const swimLaneHeight = 75;
    const height = issueList.length * swimLaneHeight + swimLaneHeight;

    const openIssueStatus = this.standUpStatusFilter.find(item => item['filterName'] === 'Open')?.options;

    // if (issueDataList.length > 15) {
    //   width = width + ((issueDataList.length - 14) * 200);
    // }

    const svg = chart
      .append('svg')
      .attr('width', width + margin.left + margin.right)
      .attr('height', height + margin.top + margin.bottom)
      .append('g')
      .attr('transform', `translate(${margin.left}, 0)`);

    const x = d3.scaleBand()
      .domain(xCoordinates)
      .range([0, width])
      .paddingOuter(0)
    // .tickValues(d3.range(xCoordinates[0], xCoordinates[xCoordinates.length - 1], 3));

    const y = d3.scaleLinear()
      .domain([0, issueList.length])
      .range([0, height]);

    const initialCoordinate = x(xCoordinates[1]);

    const issueAxis = d3.select(this.elem)
      .select('#issueAxis')
      .append('svg')
      .attr('width', '100%')
      .attr('height', height + margin.top + margin.bottom)
      .append('g');

    const dateLine = d3.select(this.elem)
      .select('#dateAxis')
      .append('svg')
      .attr('width', width + margin.left + margin.right)
      .attr('height', 30)
      .style('background-color', '#FFF')
      .append('g')
      .attr('transform', `translate(${margin.left},0)`);

    //add X-Axis
    const svgX = dateLine.append('g')
      .attr('class', 'xAxis')
      // .attr('transform', `translate(0, ${height})`)
      .call(d3.axisBottom(x));



    // highlight todays Date
    if (this.currentDayIndex >= 0) {
      svgX.call(g => g.selectAll(`.tick:nth-of-type(${this.currentDayIndex + 1}) text`)
        .style('color', '#2741D3').style('font-weight', 'bold'))
    }

    svg
      .select('.xAxis')
      .selectAll(`.tick text`)

    const getNameInitials = (name) => {
      const initials = name?.split(' ').map(d => d[0]);
      if (initials.length > 2) {
        return initials?.slice(0, 2).join('').toUpperCase();
      }
      return initials?.join('').toUpperCase();
    };

    const showMarkers = (issueList) => {
      let self = this;
      const marker = svg
        .selectAll('circle.some-class')
        .data(issueList)
        .enter()
        .append("g")
        .attr('class', 'box')
        .attr('parent-data', (d) => JSON.stringify(d))
        .attr('transform', function (d, i) { return 'translate(0,' + ((i + 1) * swimLaneHeight) + ')'; });

      let currentIssue = {};
      let parts = marker.selectAll("g.part")
        .data(() => {
          console.log([].concat(...issueList.map((d) => Object.keys(d['statusLogGroup']))));
          return [].concat(...issueList.map((d, index) => Object.keys(d['statusLogGroup'])))
        })
        .enter()
        .append("g")
        .attr("class", "statusPart")

      // we need to show status change only when tooltip data ie... info about the status change is present in currentIssue
      // that is why display property takes into account the currentIssue
      parts.append('circle')
        .attr('cx', function (d, i) {
          currentIssue = (JSON.parse(d3.select(this.parentNode.parentNode).attr('parent-data')));
          let toolTipData = ``;
          if (Object.keys(currentIssue['statusLogGroup']).includes(d)) {
            toolTipData += `<p>${currentIssue['statusLogGroup'][d].join(' --> ')}</p><p>Date: ${d}</>`
            if (Object.keys(currentIssue['statusLogGroup']).includes(d) && toolTipData !== '') {
              return x(self.formatDate(new Date(d))) >= 0 ? x(self.formatDate(new Date(d))) + initialCoordinate / 2 : 0
            } else {
              return -500;
            }
          }
        })
        .attr('cy', (d, i) => issueList.length <= 1 ? swimLaneHeight / 2 : (y(i + 1) - y(i) - 1) / 2)
        .attr('r', 5)
        .style('display', function (d) {
          currentIssue = (JSON.parse(d3.select(this.parentNode.parentNode).attr('parent-data')));
          let toolTipData = ``;
          let display = 'none'
          if (Object.keys(currentIssue['statusLogGroup']).includes(d)) {
            toolTipData += `<p>${currentIssue['statusLogGroup'][d].join(' --> ')}</p><p>Date: ${d}</>`
            if (Object.keys(currentIssue['statusLogGroup']).includes(d) && toolTipData !== '') {
              display = 'block';
            }
          }
          return display;
        })
        .style('stroke-width', 1)
        .attr('stroke', function (d) {
          currentIssue = (JSON.parse(d3.select(this.parentNode.parentNode).attr('parent-data')));
          return openIssueStatus.includes(currentIssue['Issue Status']) ? '#707070' : '#437495'
        })
        .attr('fill', function (d) {
          currentIssue = (JSON.parse(d3.select(this.parentNode.parentNode).attr('parent-data')));
          return openIssueStatus.includes(currentIssue['Issue Status']) ? '#707070' : '#fff';
        })
        .style('cursor', 'pointer')
        .on('mouseover', function (event, i) {
          let d = event.currentTarget.__data__;
          let data = ``;
          currentIssue = (JSON.parse(d3.select(this.parentNode.parentNode).attr('parent-data')));
          if (Object.keys(currentIssue['statusLogGroup']).includes(d)) {
            data += `<p>${currentIssue['statusLogGroup'][d].join(' --> ')}</p><p>Date: ${d}</>`
          }

          showTooltip(data, event.offsetX, event.offsetY);
        })
        .on('mouseout', () => {
          hideTooltip();
        });

      let assigneeParts = marker.selectAll("g.part")
        .data(() => {
          return [].concat(...issueList.map((d, index) => Object.keys(d['assigneeLogGroup'])))
        })
        .enter()
        .append("g")
        .attr("class", "assigneePart")

      let assigneePartsArr = [];
      assigneeParts
        .append('text')
        .attr('class', 'assigneeChangeText')
        .attr('height', 10)
        .attr('width', 100)
        .style('color', '#437495')
        .style('font-weight', 'bold')
        .attr('x', function (d, i) {
          currentIssue = (JSON.parse(d3.select(this.parentNode.parentNode).attr('parent-data')));
          let toolTipData = ``;
          if (Object.keys(currentIssue['assigneeLogGroup']).includes(d)) {
            toolTipData += `<p>${currentIssue['assigneeLogGroup'][d].join(' --> ')}</p><p>Date: ${d}</>`
            if (Object.keys(currentIssue['assigneeLogGroup']).includes(d) && toolTipData !== '') {
              // logic to avoid multiple labels with same text on the same position
              let obj = {
                data: toolTipData,
                id: currentIssue['Issue Id'],
                x: x(self.formatDate(new Date(d))) + initialCoordinate / 2
              };
              let alreadyThere = false;
              assigneePartsArr.forEach((part) => {
                if (part.x === obj.x && part.data === obj.data && part.id === obj.id) {
                  alreadyThere = true;
                }
              });
              if (!alreadyThere) {
                assigneePartsArr.push(obj);
                return x(self.formatDate(new Date(d))) >= 0 ? x(self.formatDate(new Date(d))) + initialCoordinate / 2 : -500
              } else {
                return -500;
              }
            } else {
              return -500;
            }
          }
        })
        .attr('y', (d, i) => issueList.length <= 1 ? swimLaneHeight / 2 + 20 : (y(i + 1) - y(i) - 1) / 2 + 20)
        .style('cursor', 'pointer')
        .text(function (d, i) {
          currentIssue = (JSON.parse(d3.select(this.parentNode.parentNode).attr('parent-data')));
          let textData = ``;
          if (Object.keys(currentIssue['assigneeLogGroup']).includes(d) && currentIssue['assigneeLogGroup'][d].length === 1) {
            textData += `${getNameInitials(currentIssue['assigneeLogGroup'][d][0])}`
            return textData;
          } else {
            if (currentIssue['assigneeLogGroup'][d]) {
              currentIssue['assigneeLogGroup'][d].forEach((element, index) => {
                if (index !== currentIssue['assigneeLogGroup'][d].length - 1) {
                  textData += `${getNameInitials(element)} --> `;
                } else {
                  textData += `${getNameInitials(element)}`;
                }
              });
              return textData;
            }
          }
        })
        .on('mouseover', function (event, d) {
          currentIssue = (JSON.parse(d3.select(this.parentNode.parentNode).attr('parent-data')));
          const data = `<p>${currentIssue['assigneeLogGroup'][d].join('-->')}</p><p>Owned: ${d}</>`;
          showTooltip(data, event.offsetX, event.offsetY);
        })
        .on('mouseout', () => {
          hideTooltip();
        })
        .each(function (d, i) {
          var thisWidth = this.getComputedTextLength()
          console.log(this, thisWidth);
          if (this.getAttribute('x')) {
            this.setAttribute('x', parseInt(this.getAttribute('x')) < 0 ? -500 : parseInt(this.getAttribute('x')) - thisWidth / 2);
          }
        })

      // show 'Due Date Exceeded' if status not closed after dueDate
      marker.append('image')
        .attr('xlink:href', '../../../assets/img/due-date-exceeded.svg')
        .style('display', (d) => self.compareDates(new Date(), d['Due Date']) && !d['Actual-Completion-Date'] ? 'block' : 'none')
        .attr('width', '40px').attr('height', '40px')
        .attr('x', (d) => !d['Due Date'] || d['Due Date'] === '-' ? 0 : x(self.formatDate(new Date())) + initialCoordinate / 2 - 20)
        .attr('y', (d, i) => issueList.length <= 1 ? swimLaneHeight / 2 - 20 : (y(i + 1) - y(i)) / 2 - 20)
        .style('cursor', 'pointer')
        .on('mouseover', (event, i) => {
          let d = event.currentTarget.__data__;
          const data = `<p>Due date exceeded</p><p>Original Due Date: ${self.formatDate(d['Due Date'])}</>`;
          showTooltip(data, event.offsetX, event.offsetY);
        })
        .on('mouseout', () => {
          hideTooltip();
        });

      // show QA completed if it exists
      marker.append('image')
        .attr('xlink:href', '../../../assets/img/qa-completed.svg')
        .style('display', (d) => d['Test-Completed'] !== '-' && self.compareDates(d['Test-Completed'], self.selectedSprintInfo.sprintStartDate) && self.compareDates(self.selectedSprintInfo.sprintEndDate, d['Test-Completed']) ? 'block' : 'none')
        .attr('width', '40px').attr('height', '40px')
        .attr('x', (d) => {
          console.log(!d['Test-Completed'] || d['Test-Completed'] === '-' ? 0 : x(self.formatDate(new Date(d['Test-Completed']))) + initialCoordinate / 2 - 20);
          return !d['Test-Completed'] || d['Test-Completed'] === '-' ? 0 : x(self.formatDate(new Date(d['Test-Completed']))) + initialCoordinate / 2 - 20;
        })
        .attr('y', (d, i) => issueList.length <= 1 ? swimLaneHeight / 2 - 20 : (y(i + 1) - y(i)) / 2 - 20)
        .style('cursor', 'pointer')
        .on('mouseover', (event, i) => {
          let d = event.currentTarget.__data__;
          const data = `<p>QA Completed</p><p>Date: ${self.formatDate(d['Test-Completed'])}</>`;
          showTooltip(data, event.offsetX, event.offsetY);
        })
        .on('mouseout', () => {
          hideTooltip();
        });

      // Show 'Dev Due Date completion' if it exist
      marker.append('image')
        .attr('xlink:href', '../../../assets/img/dev-completed.svg')
        .style('display', (d) => d['Dev-Completion-Date'] !== '-' && self.compareDates(d['Dev-Completion-Date'], self.selectedSprintInfo.sprintStartDate) && self.compareDates(self.selectedSprintInfo.sprintEndDate, d['Dev-Completion-Date']) ? 'block' : 'none')
        .attr('width', '40px').attr('height', '40px')
        .attr('x', (d) => {
          return isNaN(x(self.formatDate(new Date(d['Dev-Completion-Date']))) + initialCoordinate / 2 - 25) ? 0 : x(self.formatDate(new Date(d['Dev-Completion-Date']))) + initialCoordinate / 2 - 20
        })
        .attr('y', (d, i) => issueList.length <= 1 ? swimLaneHeight / 2 - 20 : (y(i + 1) - y(i)) / 2 - 20)
        .style('cursor', 'pointer')
        .on('mouseover', (event, i) => {
          let d = event.currentTarget.__data__;
          const data = `<p>Dev Completed</p><p>Date: ${self.formatDate(d['Dev-Completion-Date'])}</>`;
          showTooltip(data, event.offsetX, event.offsetY);
        })
        .on('mouseout', () => {
          hideTooltip();
        });
    };

    const showTaskDetail = (issue) => {
      this.service.setIssueData(issue);
    }

    const showSubTask = (parentIssue, index,) => {
      selectedIssueSubtask.forEach((task) => {
        let idx = issueDataList.findIndex(obj => obj['Issue Id'] === task['Issue Id']);
        if (idx !== -1) {
          selectedIssueSubtask.splice(selectedIssueSubtask.findIndex(subTask => subTask['Issue Id'] === task['Issue Id']), 1, {});
        }
      });
      selectedIssueSubtask = selectedIssueSubtask.filter((task) => Object.keys(task).length);
      if (selectedIssueSubtask.length) {
        issueDataList.splice(index + 1, 0, ...selectedIssueSubtask);
        parentIssue['IsExpanded'] = true;
        let scrollPosition = scroller.node().scrollTop;
        this.draw(issueDataList, parentIssue, true, scrollPosition);
      }
    };

    // const showIssueIdandStatus = (centerDate, issue, i, isOpenIssue = false) => {
    const showIssueIdandStatus = (issueList) => {
      let self = this;
      //add issue boxes
      const issueSvg = issueAxis
        .selectAll('rect.some-class')
        .data(issueList)
        .enter()
        .append("g")
        .attr('class', 'box')
        .attr('transform', function (d, i) { return 'translate(0,' + ((i + 1) * swimLaneHeight) + ')'; });

      issueSvg.append("rect")
        .attr("width", '100%')
        .attr("height", (d, i) => issueList.length <= 1 ? swimLaneHeight : y(i + 1) - y(i) + 8 <= swimLaneHeight ? y(i + 1) - y(i) + 8 : swimLaneHeight)
        .attr("fill", function (d, i) {
          if (parentIssue && parentIssue['Issue Id']) {
            if (d['Issue Id'] === parentIssue['Issue Id'] || (d['isSubtask'] && d['parentStory'][0] === parentIssue['Issue Id'])) {
              return '#F4F7F9';
            }
          }
          return '#FFF';
        });


      issueSvg
        .append('foreignObject')
        .attr('width', '100%')
        .attr('height', '100%')
        .attr('x', 5)
        .attr('y', 0)
        .append('xhtml:div')
        .html(function (d) {
          if (d['Issue Type'] && d['Issue Type'] === 'Story' && d['subTask']) {
            return `<div><i class="fas ${parentIssue && d['Issue Id'] === parentIssue['Issue Id'] ? 'fa-angle-down' : 'fa-angle-right'} p-mr-1"></i><div style="display: inline;"><div class='issueTypeIcon ${d['Issue Type'].split(' ').join('-')}'></div><a>${d['Issue Id']}</a></div>
                    <div><span class="issueStatus">${d['Issue Status']}</div></div>`;
          } else {
            return `<div><div style="display: inline;"><div class='issueTypeIcon ${d['Issue Type'].split(' ').join('-')}'></div>${d['Issue Id']}</div>
            <div><span class="issueStatus ${openIssueStatus.includes(d['Issue Status']) ? 'in_progress' : 'closed'}">${d['Issue Status']}</div></div>`;
          }
        })
        .style('font-weight', 'bold')
        .style('transform', (d) => `scale(${d['parentStory'] && d['parentStory'].length ? 0.8 : 1})`)
        .style('cursor', 'pointer')
        .style('height', (d, i) => `${issueList.length <= 1 ? swimLaneHeight : y(i + 1) - y(i) - 1}px`)
        .style('display', 'flex')
        .style('align-items', 'center')
        .on('click', function (event, issue) {
          let d = issue;
          if (!d['IsExpanded']) {
            if (d && d['subTask']) {
              selectedIssueSubtask = d['subTask'].map(d => ({ ...d, isSubtask: true }));
              console.log(d['subTask'], selectedIssueSubtask);
              let index = issueList.findIndex(obj => obj['Issue Id'] === issue['Issue Id']);
              showSubTask(d, index);
            }
          } else {
            delete d['IsExpanded'];
            issueList.splice(issueList.findIndex((obj) => obj['Issue Id'] === d['Issue Id']) + 1, d['subTask'].length);

            let scrollPosition = scroller.node().scrollTop;
            self.draw(issueList, null, false, scrollPosition);
          }
          showTaskDetail(d);
        })
    };

    //show tooltip
    const tooltipContainer = d3.select('#chart').select('.tooltip-container');
    const showTooltip = (data, xVal, yVal) => {

      if (xVal + 200 > scroller.node().getBoundingClientRect().right) {
        xVal -= 100;
      } else {
        xVal += 20;
      }

      if (yVal > scroller.node().getBoundingClientRect().bottom) {
        yVal -= 50;
      } else {
        yVal += 20;
      }

      tooltipContainer
        .selectAll('div')
        .data(data)
        .join('div')
        .attr('class', 'tooltip')
        .style('left', xVal + 'px')
        .style('top', yVal + 'px')
        .style('width', '200px')
        .style('height', 'auto')
        .html(data)
        .transition()
        .duration(500)
        .style('display', 'block')
        .style('opacity', 1);
    };
    const hideTooltip = () => {
      tooltipContainer
        .selectAll('.tooltip')
        .transition()
        .duration(500)
        .style('display', 'none')
        .style('opacity', 0);
      tooltipContainer.selectAll('.tooltip').remove();
    };



    const drawLineForIssue = (issueList) => {
      let self = this;
      let line = svg
        .selectAll('rect.some-class')
        .data(issueList)
        .enter()
        .append("g")
        .attr('class', 'box')
        .attr('transform', function (d, i) { return 'translate(0,' + ((i + 1) * 75) + ')'; });

      line.append("rect")
        .attr("width", '100%')
        .attr("height", (d, i) => issueList.length <= 1 ? swimLaneHeight + 5 : y(i + 1) - y(i) + 8 <= swimLaneHeight ? y(i + 1) - y(i) + 8 : swimLaneHeight)
        .attr("fill", function (d, i) {
          if (parentIssue && parentIssue['Issue Id']) {
            if (d['Issue Id'] === parentIssue['Issue Id'] || (d['isSubtask'] && d['parentStory'][0] === parentIssue['Issue Id'])) {
              return '#F4F7F9';
            }
          }
          return '#FFF';
        })
        .attr('x', -100)
        .attr('transform', function (d, i) { return 'translate(0,0)'; });

      line
        .append('g')
        .attr('class', 'line')
        .attr('transform', `translate(0,0)`)
        .append('svg:line')
        .attr('x1', function (d, i) { return d['spill'] ? x(self.getStartAndEndLinePoints(d)['startPoint']) - initialCoordinate : x(self.getStartAndEndLinePoints(d)['startPoint']) + initialCoordinate / 2; })
        .attr('x2', function (d, i) { return x(self.getStartAndEndLinePoints(d)['endPoint']) + initialCoordinate / 2; })
        .attr('y1', (d, i) => issueList.length <= 1 ? swimLaneHeight / 2 : (y(i + 1) - y(i)) / 2)
        .attr('y2', (d, i) => issueList.length <= 1 ? swimLaneHeight / 2 : (y(i + 1) - y(i)) / 2)
        .style('stroke', function (d) { return d['Actual-Completion-Date'] ? '#D8D8D8' : '#437495'; })
        .style('stroke-width', function (d) { return d['isSubtask'] ? 1 : 4; })
        .style('stroke-dasharray', function (d) { return d['spill'] ? '4,4' : '0,0'; })
        .style('fill', 'none')
        .attr('class', 'gridline')
        .style('cursor', 'pointer')
        .on('mouseover', function (event) {
          d3.select(this)
            .style('stroke-width', function (d) { return d['isSubtask'] ? 3 : 6; })
        })
        .on('mouseout', function (event) {
          d3.select(this)
            .style('stroke-width', function (d) { return d['isSubtask'] ? 1 : 4; })
        });

      showMarkers(issueList);
      showIssueIdandStatus(issueList);
    };



    // draw lines for each issues and its subtask
    drawLineForIssue(issueDataList);

    //draw line for todays date if it exist
    if (typeof this.currentDayIndex === 'number') {
      // const line = svg
      //   .append('g')
      //   .attr('transform', `translate(0,0)`)
      //   .append('svg:line')
      //   .attr('x1', x(xCoordinates[this.currentDayIndex]) + initialCoordinate / 2)
      //   .attr('x2', x(xCoordinates[this.currentDayIndex]) + initialCoordinate / 2)
      //   .attr('y1', height)
      //   .attr('y2', 0)
      //   .style('stroke', '#dedede')
      //   .style('fill', 'none')
      //   .attr('class', 'gridline');
    }

    scroller.node().scrollTop = previousScroll;
  }

  // get line start and end coordinates
  getStartAndEndLinePoints(issue) {
    //calculate startDate
    let startPoint;
    if (issue['spill']) {
      startPoint = new Date(this.selectedSprintInfo.sprintStartDate);
    } else {
      startPoint = issue['Actual-Start-Date'] ? new Date(issue['Actual-Start-Date']) : new Date();
    }
    //calculate endDate
    const endPoint = issue['Actual-Completion-Date'] ? new Date(issue['Actual-Completion-Date']) : new Date();

    const noOfDaysBetweenStartandEnd = this.getNoOFDayBetweenStartAndEndDate(startPoint, endPoint);

    const centerDate = new Date(startPoint.getFullYear(), startPoint.getMonth(), startPoint.getDate() + noOfDaysBetweenStartandEnd / 2);

    return { startPoint: this.formatDate(startPoint), endPoint: this.formatDate(endPoint), centerDate: this.formatDate(centerDate) };
  }

  //check if date 1 is greater than date2
  compareDates(date1, date2) {
    const d1 = new Date(date1);
    const d2 = new Date(date2);
    if (d1.toDateString() === d2.toDateString()) {
      return false;
    }
    return d1 > d2;
  }


  ngOnDestroy(): void {
    const chart = d3.select(this.elem).select('#chart');
    chart.select('svg').remove();
  }
}