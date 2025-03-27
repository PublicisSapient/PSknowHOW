import { ComponentFixture, TestBed } from '@angular/core/testing';
import { IssueCardComponent } from './issue-card.component';
import { SharedService } from 'src/app/services/shared.service';
import { BehaviorSubject } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';

describe('IssueCardComponent', () => {
  let component: IssueCardComponent;
  let fixture: ComponentFixture<IssueCardComponent>;
  let sharedService: SharedService;
  const routerMock = {
    navigate: jasmine.createSpy('navigate'),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [IssueCardComponent],
      providers: [
        SharedService,
        { provide: ActivatedRoute, useValue: { snapshot: { params: {} } } },
        { provide: Router, useValue: routerMock },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IssueCardComponent);
    component = fixture.componentInstance;
    sharedService = TestBed.inject(SharedService);
    component.issueData = {
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
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnChanges', () => {
    it('should set isOverViewSelected to true', () => {
      const changes = {
        issueData: {
          currentValue: {},
          previousValue: {},
          firstChange: true,
          isFirstChange: () => true,
        },
      };
      component.ngOnChanges(changes);
      expect(component.isOverViewSelected).toBeTrue();
    });
  });

  describe('getNameInitials', () => {
    it('should return the initials of a name', () => {
      const name = 'John Doe';
      const result = component.getNameInitials('John Doe');
      expect(result).toBe('JD');
    });

    it('should return the first two initials if there are more than two words in the name', () => {
      const name = 'John Doe Smith';
      const result = component.getNameInitials(name);
      expect(result).toBe('JD');
    });

    it('should return the initials in uppercase', () => {
      const name = 'john doe';
      const result = component.getNameInitials(name);
      expect(result).toBe('JD');
    });
  });

  describe('Service Integration', () => {
    it('should set isOverViewSelected to true and update issueData when service data changes', () => {
      const newData = {
        statusLogGroup: {},
        workLogGroup: {},
        assigneeLogGroup: {
          '2023-12-20': ['Pawan Kandpal'],
        },
        timeWithUser: '1d 2h',
        timeWithStatus: ' 23h',
        loggedWorkInSeconds: 0,
        epicName:
          'KnowHOW | Client enhancements and rollout support in Q1 2024',
        spill: false,
        preClosed: false,
        'Issue Id': 'DTS-30163',
        'Issue URL': 'https://publicissapient.atlassian.net/browse/DTS-30163',
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
      };

      sharedService.currentIssue.next(newData);
      fixture.detectChanges();
      expect(component.isOverViewSelected).toBeTrue();
      expect(component.issueData).toEqual(newData);
    });
  });
});
