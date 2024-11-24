import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AssigneeBoardComponent } from './assignee-board.component';
import { SharedService } from 'src/app/services/shared.service';
import { of } from 'rxjs';

describe('AssigneeBoardComponent', () => {
  let component: AssigneeBoardComponent;
  let fixture: ComponentFixture<AssigneeBoardComponent>;
  let sharedServiceSpy: jasmine.SpyObj<SharedService>;
  let service;
  const issueData = [
    {
      statusLogGroup: {},
      workLogGroup: {},
      assigneeLogGroup: {
        '2023-12-20': ['Pawan Kandpal'],
      },
      timeWithUser: '1d 2h',
      timeWithStatus: ' 23h',
      loggedWorkInSeconds: 0,
      epicName: 'KnowHOW | Client enhancements and rollout support in Q1 2024',
      spill: false,
      preClosed: false,
      'Issue Id': 'DTS-30162',
      'Issue URL': 'https://publicissapient.atlassian.net/browse/DTS-30162',
      'Issue Description':
        'Security fixed for onboarding of Marriott International',
      'Issue Status': 'Open',
      'Issue Type': 'Story',
      'Size(story point/hours)': '20.0',
      'Logged Work': '0d',
      'Original Estimate': '0d',
      Priority: 'P2 - Critical',
      'Due Date': '-',
      'Remaining Estimate': '-',
      'Predicted Completion Date': '-',
      'Overall Delay': '-',
      'Dev Due Date': '-',
      Assignee: 'Pawan Kandpal',
      'Change Date': '2023-12-20',
      Labels: [],
      'Created Date': '2023-12-04',
      'Root Cause List': ['None'],
      'Owner Full Name': ['Pawan Kandpal'],
      'Sprint Name': 'KnowHOW | PI_16| ITR_1',
      'Release Name': 'KnowHOW PI-16',
      'Updated Date': '2023-12-20',
      'Dev-Completion-Date': '-',
    },
    {
      statusLogGroup: {
        '2023-12-20': ['Open'],
        '2023-12-21': ['In Analysis'],
      },
      workLogGroup: {},
      assigneeLogGroup: {
        '2023-12-20': ['Shivani .', 'Akshat Shrivastav'],
      },
      timeWithUser: '1d 1h',
      timeWithStatus: ' 2h',
      loggedWorkInSeconds: 0,
      epicName: 'KnowHOW | Client enhancements and rollout support in Q1 2024',
      spill: false,
      preClosed: false,
      'Issue Id': 'DTS-31130',
      'Issue URL': 'https://publicissapient.atlassian.net/browse/DTS-31130',
      'Issue Description':
        "Albertsons: 'Sonar Processor' Code Quality requirements",
      'Issue Status': 'In Analysis',
      'Issue Type': 'Story',
      'Logged Work': '0d',
      'Original Estimate': '0d',
      Priority: 'P2 - Critical',
      'Due Date': '-',
      'Remaining Estimate': '-',
      'Predicted Completion Date': '-',
      'Overall Delay': '-',
      'Dev Due Date': '-',
      Assignee: 'Akshat Shrivastav',
      'Change Date': '2023-12-21',
      Labels: ['Albertsons'],
      'Created Date': '2023-12-20',
      'Root Cause List': ['None'],
      'Owner Full Name': ['Akshat Shrivastav'],
      'Sprint Name': 'KnowHOW | PI_16| ITR_1',
      'Release Name': 'KnowHOW v8.3.0',
      'Updated Date': '2023-12-21',
      'Dev-Completion-Date': '-',
    },
    {
      statusLogGroup: {
        '2023-12-20': ['Open'],
      },
      workLogGroup: {},
      assigneeLogGroup: {
        '2023-12-20': ['Hirenkumar Babariya'],
      },
      timeWithUser: ' 20h',
      timeWithStatus: ' 20h',
      loggedWorkInSeconds: 0,
      spill: false,
      preClosed: false,
      'Issue Id': 'DTS-31139',
      'Issue URL': 'https://publicissapient.atlassian.net/browse/DTS-31139',
      'Issue Description':
        'Auth&Auth_Warning message should be displayed when a SSO Logged-in User, try to login using Standard Login',
      'Issue Status': 'Open',
      'Issue Type': 'Defect',
      'Logged Work': '0d',
      'Original Estimate': '0d',
      Priority: 'P3 - Major',
      'Due Date': '-',
      'Remaining Estimate': '-',
      'Predicted Completion Date': '-',
      'Overall Delay': '-',
      'Dev Due Date': '-',
      Assignee: 'Hirenkumar Babariya',
      'Change Date': '2023-12-20',
      Labels: [],
      'Created Date': '2023-12-20',
      'Root Cause List': ['None'],
      'Owner Full Name': ['Hirenkumar Babariya'],
      'Sprint Name': 'KnowHOW | PI_16| ITR_1',
      'Release Name': 'KnowHOW v8.3.0',
      'Updated Date': '2023-12-20',
      'Dev-Completion-Date': '-',
    },
    {
      statusLogGroup: {
        '2023-12-20': ['Open'],
      },
      workLogGroup: {},
      assigneeLogGroup: {
        '2023-12-20': ['Purushottam Gupta', 'Kunal Kamble'],
      },
      timeWithUser: '1d 1h',
      timeWithStatus: '1d 1h',
      loggedWorkInSeconds: 0,
      epicName: 'KnowHOW | Client enhancements and rollout support in Q1 2024',
      spill: false,
      preClosed: false,
      'Issue Id': 'DTS-31128',
      'Issue URL': 'https://publicissapient.atlassian.net/browse/DTS-31128',
      'Issue Description':
        "Albertsons: 'Github Processor' Code Quality requirements",
      'Issue Status': 'Open',
      'Issue Type': 'Story',
      'Logged Work': '0d',
      'Original Estimate': '0d',
      Priority: 'P2 - Critical',
      'Due Date': '-',
      'Remaining Estimate': '-',
      'Predicted Completion Date': '-',
      'Overall Delay': '-',
      'Dev Due Date': '-',
      Assignee: 'Kunal Kamble',
      'Change Date': '2023-12-20',
      Labels: ['Albertsons'],
      'Created Date': '2023-12-20',
      'Root Cause List': ['None'],
      'Owner Full Name': ['Kunal Kamble'],
      'Sprint Name': 'KnowHOW | PI_16| ITR_1',
      'Release Name': 'KnowHOW v8.3.0',
      'Updated Date': '2023-12-20',
      'Dev-Completion-Date': '-',
    },
    {
      statusLogGroup: {
        '2023-12-20': ['Open'],
      },
      workLogGroup: {},
      assigneeLogGroup: {
        '2023-12-20': ['Kunal Kamble', 'Shivani .'],
      },
      timeWithUser: '1d 1h',
      timeWithStatus: '1d 1h',
      loggedWorkInSeconds: 0,
      epicName: 'KnowHOW | Client enhancements and rollout support in Q1 2024',
      spill: false,
      preClosed: false,
      'Issue Id': 'DTS-31129',
      'Issue URL': 'https://publicissapient.atlassian.net/browse/DTS-31129',
      'Issue Description': "Albertsons: 'Custom API' Code Quality requirements",
      'Issue Status': 'Open',
      'Issue Type': 'Story',
      'Logged Work': '0d',
      'Original Estimate': '0d',
      Priority: 'P2 - Critical',
      'Due Date': '-',
      'Remaining Estimate': '-',
      'Predicted Completion Date': '-',
      'Overall Delay': '-',
      'Dev Due Date': '-',
      Assignee: 'Shivani .',
      'Change Date': '2023-12-20',
      Labels: ['Albertsons'],
      'Created Date': '2023-12-20',
      'Root Cause List': ['None'],
      'Owner Full Name': ['Shivani .'],
      'Sprint Name': 'KnowHOW | PI_16| ITR_1',
      'Release Name': 'KnowHOW v8.3.0',
      'Updated Date': '2023-12-20',
      'Dev-Completion-Date': '-',
    },
    {
      statusLogGroup: {
        '2023-12-21': ['Closed'],
      },
      workLogGroup: {},
      assigneeLogGroup: {},
      timeWithUser: '-',
      timeWithStatus: '-',
      loggedWorkInSeconds: 10800,
      epicName: 'KnowHOW | Integration with Central Auth & Hierarchy',
      spill: true,
      remainingEstimateInSeconds: 0,
      preClosed: false,
      'Issue Id': 'DTS-30422',
      'Issue URL': 'https://publicissapient.atlassian.net/browse/DTS-30422',
      'Issue Description':
        'CA-KnowHow || FE || Create if else for old login and central login to work using a switch',
      'Issue Status': 'Closed',
      'Issue Type': 'Story',
      'Size(story point/hours)': '5.0',
      'Logged Work': '3h ',
      'Original Estimate': '0d',
      Priority: 'P3 - Major',
      'Due Date': '-',
      'Remaining Estimate': '0d',
      'Remaining Days': '0d',
      'Predicted Completion Date': '-',
      'Overall Delay': '-',
      'Dev Due Date': '-',
      Assignee: 'Sumit Goyal',
      'Change Date': '2023-12-21',
      Labels: ['UI'],
      'Created Date': '2023-12-11',
      'Root Cause List': ['None'],
      'Owner Full Name': ['Sumit Goyal'],
      'Sprint Name': 'KnowHOW | PI_16| ITR_1',
      Resolution: 'Fixed or Completed',
      'Updated Date': '2023-12-21',
      'Dev-Completion-Date': '-',
      'Actual-Completion-Date': '2023-12-21T03:45:41.008',
    },
    {
      statusLogGroup: {
        '2023-12-20': ['In Testing'],
      },
      workLogGroup: {},
      assigneeLogGroup: {},
      timeWithUser: ' 23h',
      timeWithStatus: ' 18h',
      loggedWorkInSeconds: 0,
      epicName: 'KnowHOW | Client enhancements and rollout support in Q1 2024',
      spill: true,
      preClosed: false,
      'Issue Id': 'DTS-31079',
      'Issue URL': 'https://publicissapient.atlassian.net/browse/DTS-31079',
      'Issue Description':
        'Backlog Epic Progress - For DRP Discovery project, epic links are not redirected to correct link in Jira',
      'Issue Status': 'In Testing',
      'Issue Type': 'Defect',
      'Logged Work': '0d',
      'Original Estimate': '0d',
      Priority: 'P3 - Major',
      'Due Date': '-',
      'Remaining Estimate': '-',
      'Predicted Completion Date': '-',
      'Overall Delay': '-',
      'Dev Due Date': '-',
      Assignee: 'Mamatha Paccha',
      'Change Date': '2023-12-20',
      Labels: ['Prod_Defect'],
      'Created Date': '2023-12-18',
      'Root Cause List': ['None'],
      'Owner Full Name': ['Mamatha Paccha'],
      'Sprint Name': 'KnowHOW | PI_16| ITR_1',
      'Release Name': 'KnowHOW v8.3.0',
      'Updated Date': '2023-12-20',
      'Dev-Completion-Date': '-',
    },
    {
      statusLogGroup: {
        '2023-12-20': ['Ready for Testing', 'In Testing', 'Closed'],
      },
      workLogGroup: {},
      assigneeLogGroup: {
        '2023-12-20': ['Mamatha Paccha'],
      },
      timeWithUser: '-',
      timeWithStatus: '-',
      loggedWorkInSeconds: 37800,
      epicName: 'KnowHOW | Client Requests Q4 2023',
      spill: true,
      remainingEstimateInSeconds: 0,
      preClosed: false,
      'Issue Id': 'DTS-30102',
      'Issue URL': 'https://publicissapient.atlassian.net/browse/DTS-30102',
      'Issue Description':
        'Zephyr Server connection - Token support is required for Zephyr connection to fetch data (Basic auth is disabled for Jira server)',
      'Issue Status': 'Closed',
      'Issue Type': 'Story',
      'Logged Work': '1d 2h 30m',
      'Original Estimate': '0d',
      Priority: 'P2 - Critical',
      'Due Date': '07-Dec-2023',
      'Remaining Estimate': '0d',
      'Remaining Days': '0d',
      'Predicted Completion Date': '-',
      'Overall Delay': '-',
      'Dev Due Date': '-',
      Assignee: 'Mamatha Paccha',
      'Change Date': '2023-12-20',
      Labels: ['UI'],
      'Created Date': '2023-11-30',
      'Root Cause List': ['None'],
      'Owner Full Name': ['Mamatha Paccha'],
      'Sprint Name': 'KnowHOW | PI_16| ITR_1',
      Resolution: 'Fixed or Completed',
      'Release Name': 'KnowHOW v8.3.0',
      'Updated Date': '2023-12-20',
      'Dev-Completion-Date': '-',
      'Actual-Completion-Date': '2023-12-20T12:19:33.583',
    },
    {
      statusLogGroup: {
        '2023-12-20': ['Open'],
      },
      workLogGroup: {},
      assigneeLogGroup: {
        '2023-12-20': ['Manoj Kumar Srivastava', 'Purushottam Gupta'],
      },
      timeWithUser: '1d 1h',
      timeWithStatus: '1d 2h',
      loggedWorkInSeconds: 0,
      epicName: 'KnowHOW | Client enhancements and rollout support in Q1 2024',
      spill: false,
      preClosed: false,
      'Issue Id': 'DTS-31125',
      'Issue URL': 'https://publicissapient.atlassian.net/browse/DTS-31125',
      'Issue Description':
        "Albertsons: 'Jira Processor' Code Quality requirements ",
      'Issue Status': 'Open',
      'Issue Type': 'Story',
      'Logged Work': '0d',
      'Original Estimate': '0d',
      Priority: 'P2 - Critical',
      'Due Date': '-',
      'Remaining Estimate': '-',
      'Predicted Completion Date': '-',
      'Overall Delay': '-',
      'Dev Due Date': '-',
      Assignee: 'Purushottam Gupta',
      'Change Date': '2023-12-20',
      Labels: ['Albertsons'],
      'Created Date': '2023-12-20',
      'Root Cause List': ['None'],
      'Owner Full Name': ['Purushottam Gupta'],
      'Sprint Name': 'KnowHOW | PI_16| ITR_1',
      'Release Name': 'KnowHOW v8.3.0',
      'Updated Date': '2023-12-20',
      'Dev-Completion-Date': '-',
    },
    {
      statusLogGroup: {},
      workLogGroup: {},
      assigneeLogGroup: {
        '2023-12-20': ['Mamatha Paccha'],
      },
      timeWithUser: '1d',
      timeWithStatus: ' 23h',
      loggedWorkInSeconds: 0,
      epicName:
        'KnowHOW Enabler | Improvement in Code Quality & Test Automation to enable smoother releases',
      spill: false,
      remainingEstimateInSeconds: 144000,
      originalEstimateInSeconds: 144000,
      preClosed: false,
      'Issue Id': 'DTS-30697',
      'Issue URL': 'https://publicissapient.atlassian.net/browse/DTS-30697',
      'Issue Description':
        'QE|| Automation - Update script - Dashbord config - show/hide project level',
      'Issue Status': 'Open',
      'Issue Type': 'Story',
      'Size(story point/hours)': '5.0',
      'Logged Work': '0d',
      'Original Estimate': '5d ',
      Priority: 'P3 - Major',
      'Due Date': '-',
      'Remaining Estimate': '5d ',
      'Remaining Days': '5d ',
      'Predicted Completion Date': '-',
      'Overall Delay': '-',
      'Dev Due Date': '-',
      Assignee: 'Mamatha Paccha',
      'Change Date': '2023-12-21',
      Labels: ['QA'],
      'Created Date': '2023-12-12',
      'Root Cause List': ['None'],
      'Owner Full Name': ['Mamatha Paccha'],
      'Sprint Name': 'KnowHOW | PI_16| ITR_1',
      'Updated Date': '2023-12-21',
      'Dev-Completion-Date': '-',
    },
    {
      statusLogGroup: {
        '2023-12-20': ['Open', 'In Investigation'],
      },
      workLogGroup: {},
      assigneeLogGroup: {
        '2023-12-20': ['Hirenkumar Babariya'],
      },
      timeWithUser: ' 20h',
      timeWithStatus: ' 16h',
      loggedWorkInSeconds: 0,
      spill: false,
      preClosed: false,
      'Issue Id': 'DTS-31140',
      'Issue URL': 'https://publicissapient.atlassian.net/browse/DTS-31140',
      'Issue Description':
        "After user verify email, user approval request is not visible in knowhow 'request' button",
      'Issue Status': 'In Investigation',
      'Issue Type': 'Defect',
      'Logged Work': '0d',
      'Original Estimate': '0d',
      Priority: 'P3 - Major',
      'Due Date': '-',
      'Remaining Estimate': '-',
      'Predicted Completion Date': '-',
      'Overall Delay': '-',
      'Dev Due Date': '-',
      Assignee: 'Hirenkumar Babariya',
      'Change Date': '2023-12-20',
      Labels: [],
      'Created Date': '2023-12-20',
      'Root Cause List': ['None'],
      'Owner Full Name': ['Hirenkumar Babariya'],
      'Sprint Name': 'KnowHOW | PI_16| ITR_1',
      'Release Name': 'KnowHOW v8.3.0',
      'Updated Date': '2023-12-20',
      'Dev-Completion-Date': '-',
    },
    {
      statusLogGroup: {},
      workLogGroup: {},
      assigneeLogGroup: {},
      timeWithUser: ' 23h',
      timeWithStatus: ' 23h',
      loggedWorkInSeconds: 10800,
      epicName: 'KnowHOW | Client Requests Q4 2023',
      spill: true,
      remainingEstimateInSeconds: 0,
      originalEstimateInSeconds: 7200,
      preClosed: false,
      'Issue Id': 'DTS-30187',
      'Issue URL': 'https://publicissapient.atlassian.net/browse/DTS-30187',
      'Issue Description':
        'DSV - In progress filter on chart should work as per configuration in settings (Status)',
      'Issue Status': 'In Testing',
      'Issue Type': 'Story',
      'Logged Work': '3h ',
      'Original Estimate': '2h ',
      Priority: 'P3 - Major',
      'Due Date': '08-Dec-2023',
      'Remaining Estimate': '0d',
      'Remaining Days': '0d',
      'Predicted Completion Date': '21-Dec-2023',
      'Potential Delay(in days)': '9d',
      'Dev Due Date': '-',
      Assignee: 'Mamatha Paccha',
      'Change Date': '2023-12-20',
      Labels: ['DSV'],
      'Created Date': '2023-12-05',
      'Root Cause List': ['None'],
      'Owner Full Name': ['Mamatha Paccha'],
      'Sprint Name': 'KnowHOW | PI_16| ITR_1',
      'Release Name': 'KnowHOW v9.0.0',
      'Updated Date': '2023-12-20',
      'Dev-Completion-Date': '-',
    },
  ];
  beforeEach(async () => {
    sharedServiceSpy = jasmine.createSpyObj('SharedService', ['setIssueData']);
    sharedServiceSpy.currentData = of({
      statusLogGroup: {},
      workLogGroup: {},
      assigneeLogGroup: {
        '2023-12-20': ['Mamatha Paccha'],
      },
      timeWithUser: '1d 5h',
      timeWithStatus: '1d 4h',
      loggedWorkInSeconds: 0,
      epicName:
        'KnowHOW Enabler | Improvement in Code Quality & Test Automation to enable smoother releases',
      spill: false,
      remainingEstimateInSeconds: 144000,
      originalEstimateInSeconds: 144000,
      preClosed: false,
      'Issue Id': 'DTS-30697',
      'Issue URL': 'https://publicissapient.atlassian.net/browse/DTS-30697',
      'Issue Description':
        'QE|| Automation - Update script - Dashbord config - show/hide project level',
      'Issue Status': 'Open',
      'Issue Type': 'Story',
      'Size(story point/hours)': '5.0',
      'Logged Work': '0d',
      'Original Estimate': '5d ',
      Priority: 'P3 - Major',
      'Due Date': '-',
      'Remaining Estimate': '5d ',
      'Remaining Days': '5d ',
      'Predicted Completion Date': '-',
      'Overall Delay': '-',
      'Dev Due Date': '-',
      Assignee: 'Mamatha Paccha',
      'Change Date': '2023-12-21',
      Labels: ['QA'],
      'Created Date': '2023-12-12',
      'Root Cause List': ['None'],
      'Owner Full Name': ['Mamatha Paccha'],
      'Sprint Name': 'KnowHOW | PI_16| ITR_1',
      'Updated Date': '2023-12-21',
      'Dev-Completion-Date': '-',
    });
    await TestBed.configureTestingModule({
      declarations: [AssigneeBoardComponent],
      providers: [{ provide: SharedService, useValue: sharedServiceSpy }],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssigneeBoardComponent);
    component = fixture.componentInstance;
    component.issueDataList = issueData;
    component.filteredIssueDataList = issueData;
    component.standUpStatusFilter = [
      {
        filterName: 'In Progress',
        filterType: 'button',
        options: [
          'In Progress',
          'In Progress',
          'In Analysis',
          'In Development',
          'In Progress',
          'In Review',
          'Ready for Testing',
          'In Testing',
          'In Progress',
          'Ready for Sign-off',
          'On Hold',
          'Ready for Delivery',
          'Approved',
          'Rejected',
          'Reviewing',
          'At Risk',
          'Missed',
          'More info',
          'In triage',
          'Code Review',
          'Escalate',
          'Ready for Sprint Planning',
          'Requirement Signed Off',
          'Pending Owner Action',
          'In Intake',
          'Pending User Response',
          'In Investigation',
          'In Backlog Refinement',
          'Analyzing',
          'Implementing',
        ],
        isShown: true,
        order: 1,
      },
      {
        filterName: 'Open',
        filterType: 'button',
        options: [
          'To Do',
          'Open',
          'Backlog',
          'Funnel',
          'To Do',
          'To Do',
          'To Do',
          'Open',
        ],
        isShown: false,
      },
      {
        filterName: 'Done',
        filterType: 'button',
        options: ['Done', 'Dropped', 'Closed', 'Live', 'Done', 'Done', 'Done'],
        isShown: true,
        order: 2,
      },
    ];
    service = TestBed.inject(SharedService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set showIssueDetails and graphWidth when currentData is not empty', () => {
    expect(component.showIssueDetails).toBeTrue();
    expect(component.graphWidth).toBe(75);
  });

  xit('should set showIssueDetails and graphWidth when currentData is empty', () => {
    service.setIssueData({});
    fixture.detectChanges();
    expect(component.showIssueDetails).toBeFalse();
    expect(component.graphWidth).toBe(100);
  });

  it('should set currentSprint on ngOnInit', () => {
    sharedServiceSpy.currentSelectedSprint = {
      nodeId: '602_PSKnowHOW_6577d731cd826638f6964f4d',
      nodeName: 'KnowHOW | PI_16| ITR_1_PSKnowHOW',
      sprintStartDate: '2023-12-20T07:10:18.850Z',
      sprintEndDate: '2024-01-09T15:00:00.000Z',
      path: [
        'PSKnowHOW_6577d731cd826638f6964f4d###ACE20001_port###ADQ Financial Services LLC_acc###Financial Services_ver###Government Services_bu',
      ],
      labelName: 'sprint',
      parentId: ['PSKnowHOW_6577d731cd826638f6964f4d'],
      sprintState: 'ACTIVE',
      level: 6,
    };
    component.ngOnInit();
    expect(component.currentSprint).toBe(
      sharedServiceSpy.currentSelectedSprint,
    );
  });

  it('should set currentIssueIndex and filteredIssueDataList on ngOnChanges', () => {
    component.currentIssueIndex = 1;
    component.filteredIssueDataList = [{ id: 1, title: 'Issue 1' }];
    component.ngOnChanges({
      issueDataList: {
        currentValue: [{ id: 2, title: 'Issue 2' }],
        previousValue: undefined,
        firstChange: false,
        isFirstChange: function (): boolean {
          return false;
        },
      },
    });
    fixture.detectChanges();
    expect(component.currentIssueIndex).toBe(0);
    expect(component.filteredIssueDataList).toEqual([
      { id: 2, title: 'Issue 2' },
    ]);
  });

  it('should set currentIssueIndex and call setIssueData with the correct argument on onPreviousIssue', () => {
    component.currentIssueIndex = 1;
    component.filteredIssueDataList = [
      { id: 1, title: 'Issue 1', 'Issue Status': 'Open' },
      { id: 2, title: 'Issue 2', 'Issue Status': 'In Progress' },
      { id: 3, title: 'Issue 3', 'Issue Status': 'Done' },
    ];
    component.onPreviousIssue();
    expect(component.currentIssueIndex).toBe(0);
    expect(sharedServiceSpy.setIssueData).toHaveBeenCalledWith({
      id: 1,
      title: 'Issue 1',
      'Issue Status': 'Open',
    });
  });

  it('should not set currentIssueIndex and call setIssueData when currentIssueIndex is 0 on onPreviousIssue', () => {
    component.currentIssueIndex = 0;
    component.onPreviousIssue();
    expect(component.currentIssueIndex).toBe(0);
    expect(sharedServiceSpy.setIssueData).not.toHaveBeenCalled();
  });

  it('should set currentIssueIndex and call setIssueData with the correct argument on onNextIssue', () => {
    component.currentIssueIndex = 0;
    component.filteredIssueDataList = [
      { id: 1, title: 'Issue 1', 'Issue Status': 'Open' },
      { id: 2, title: 'Issue 2', 'Issue Status': 'In Progress' },
      { id: 3, title: 'Issue 3', 'Issue Status': 'Done' },
    ];
    component.onNextIssue();
    expect(component.currentIssueIndex).toBe(1);
    expect(sharedServiceSpy.setIssueData).toHaveBeenCalledWith({
      id: 2,
      title: 'Issue 2',
      'Issue Status': 'In Progress',
    });
  });

  it('should not set currentIssueIndex and call setIssueData when currentIssueIndex is at the last index on onNextIssue', () => {
    component.currentIssueIndex = 2;
    component.filteredIssueDataList = [
      { id: 1, title: 'Issue 1', 'Issue Status': 'Open' },
      { id: 2, title: 'Issue 2', 'Issue Status': 'In Progress' },
      { id: 3, title: 'Issue 3', 'Issue Status': 'Done' },
    ];
    component.onNextIssue();
    expect(component.currentIssueIndex).toBe(2);
    expect(sharedServiceSpy.setIssueData).not.toHaveBeenCalled();
  });

  it('should set selectedTaskStatusFilter and call filterTasksByStatus on taskFilterSelected', () => {
    component.issueDataList = [
      { id: 1, title: 'Issue 1', 'Issue Status': 'Open' },
      { id: 2, title: 'Issue 2', 'Issue Status': 'In Progress' },
      { id: 3, title: 'Issue 3', 'Issue Status': 'Done' },
    ];
    component.taskFilterSelected('Open');
    fixture.detectChanges();
    expect(component.selectedTaskStatusFilter).toBe('Open');
    expect(component.filteredIssueDataList).toEqual([
      { id: 1, title: 'Issue 1', 'Issue Status': 'Open' },
    ]);
  });

  it('should set selectedTaskStatusFilter to empty string and call filterTasksByStatus when no filter is selected on taskFilterSelected', () => {
    component.taskFilterSelected(null);
    expect(component.selectedTaskStatusFilter).toBe('');
    expect(component.filteredIssueDataList).toEqual(component.issueDataList);
  });

  it('should set filteredIssueDataList to a deep copy of issueDataList when no filter is selected on filterTasksByStatus', () => {
    component.selectedTaskStatusFilter = '';
    component.filterTasksByStatus();
    expect(component.filteredIssueDataList).toEqual(component.issueDataList);
  });

  it('should emit reloadKPITab event on reloadKPI', () => {
    const event = { fieldMappingUpdated: true };
    spyOn(component.reloadKPITab, 'emit');
    component.reloadKPI(event);
    expect(component.reloadKPITab.emit).toHaveBeenCalledWith(event);
  });
});
