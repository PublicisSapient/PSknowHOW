import { ComponentFixture, TestBed } from '@angular/core/testing';
import { IssueBodyComponent } from './issue-body.component';

describe('IssueBodyComponent', () => {
  let component: IssueBodyComponent;
  let fixture: ComponentFixture<IssueBodyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [IssueBodyComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IssueBodyComponent);
    component = fixture.componentInstance;
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

  describe('convertToHoursIfTime', () => {
    it('should return the value as is if it is "-"', () => {
      const result = component.convertToHoursIfTime('-', 'hour');
      expect(result).toBe('-');
    });

    it('should return the value as is if it is not a number', () => {
      const result = component.convertToHoursIfTime('abc', 'hour');
      expect(result).toBe('abc');
    });

    it('should return the value as is if unit is not "day"', () => {
      const result = component.convertToHoursIfTime(3600, 'hour');
      expect(result).toBe(3600);
    });

    it('should return the value as "0d" if the value is 0', () => {
      const result = component.convertToHoursIfTime(0, 'day');
      expect(result.trim()).toBe('0d');
    });

    it('should convert the value to days if unit is "day"', () => {
      const result = component.convertToHoursIfTime(7200, 'day');
      expect(result.trim()).toBe('2h');
    });

    it('should add a "-" sign if the value is less than 0', () => {
      const result = component.convertToHoursIfTime(-3600, 'day');
      expect(result).toBe('-1h ');
    });

    it('should return "0d" if the value is an empty string', () => {
      const result = component.convertToHoursIfTime('', 'day');
      expect(result.trim()).toBe('0d');
    });
  });

  describe('convertToDays', () => {
    it('should convert minutes and hours to days', () => {
      const result = component.convertToDays(30, 16);
      expect(result).toBe('2d ');
    });

    it('should not include hours if it is 0', () => {
      const result = component.convertToDays(30, 8);
      expect(result.trim()).toBe('1d');
    });

    it('should not include days if it is 0', () => {
      const result = component.convertToDays(30, 4);
      expect(result.trim()).toBe('4h');
    });
  });
});
