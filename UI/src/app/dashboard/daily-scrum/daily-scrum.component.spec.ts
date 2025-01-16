/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

/*********************************************
File contains test cases for daily scrum component.
@author bhagyashree, rishabh
*******************************/

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SharedService } from 'src/app/services/shared.service';
import { DailyScrumComponent } from './daily-scrum.component';
import { TableModule } from 'primeng/table';
import { SimpleChange, SimpleChanges } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

const assigneeList = [
  {
    "assigneeId": "712020:ef53b477-a4b6-4c00-9af9-fecbcc5e6e53",
    "assigneeName": "Akshat Shrivastav",
    "role": "Backend Developer",
    "cardDetails": {
      "Remaining Capacity": {
        "value": "-",
        "unit": "day"
      },
      "Remaining Estimate": {
        "value": "0",
        "unit": "day"
      },
      "Remaining Work": {
        "value": "1",
        "value1": "0.0",
        "unit1": "SP"
      },
      "Delay": {
        "value": "-",
        "unit": "day"
      }
    }
  },
  {
    "assigneeId": "6347ee76db32d9ce175d569f",
    "assigneeName": "Hirenkumar Babariya",
    "role": "Backend Developer",
    "cardDetails": {
      "Remaining Capacity": {
        "value": "-",
        "unit": "day"
      },
      "Remaining Estimate": {
        "value": "0",
        "unit": "day"
      },
      "Remaining Work": {
        "value": "2",
        "value1": "0.0",
        "unit1": "SP"
      },
      "Delay": {
        "value": "-",
        "unit": "day"
      }
    }
  },
  {
    "assigneeId": "63bd2c83713349bea186fcad",
    "assigneeName": "Kunal Kamble",
    "role": "Backend Developer",
    "cardDetails": {
      "Remaining Capacity": {
        "value": "-",
        "unit": "day"
      },
      "Remaining Estimate": {
        "value": "0",
        "unit": "day"
      },
      "Remaining Work": {
        "value": "1",
        "value1": "0.0",
        "unit1": "SP"
      },
      "Delay": {
        "value": "-",
        "unit": "day"
      }
    }
  },
  {
    "assigneeId": "622987834160640069caac53",
    "assigneeName": "Mamatha Paccha",
    "role": "Unassigned",
    "cardDetails": {
      "Remaining Capacity": {
        "value": "-",
        "unit": "day"
      },
      "Remaining Estimate": {
        "value": "2400",
        "unit": "day"
      },
      "Remaining Work": {
        "value": "3",
        "value1": "5.0",
        "unit1": "SP"
      },
      "Delay": {
        "value": "-",
        "unit": "day"
      }
    }
  },
  {
    "assigneeId": "712020:be14b4ea-748a-4d7a-a54e-b86a11ac162b",
    "assigneeName": "Pawan Kandpal",
    "role": "Unassigned",
    "cardDetails": {
      "Remaining Capacity": {
        "value": "-",
        "unit": "day"
      },
      "Remaining Estimate": {
        "value": "0",
        "unit": "day"
      },
      "Remaining Work": {
        "value": "1",
        "value1": "20.0",
        "unit1": "SP"
      },
      "Delay": {
        "value": "-",
        "unit": "day"
      }
    }
  },
  {
    "assigneeId": "63d37bfef386bda5dcac38e5",
    "assigneeName": "Purushottam Gupta",
    "role": "Backend Developer",
    "cardDetails": {
      "Remaining Capacity": {
        "value": "-",
        "unit": "day"
      },
      "Remaining Estimate": {
        "value": "0",
        "unit": "day"
      },
      "Remaining Work": {
        "value": "1",
        "value1": "0.0",
        "unit1": "SP"
      },
      "Delay": {
        "value": "-",
        "unit": "day"
      }
    }
  },
  {
    "assigneeId": "62c515fcfa577c57c3b6cba1",
    "assigneeName": "Shivani .",
    "role": "Backend Developer",
    "cardDetails": {
      "Remaining Capacity": {
        "value": "-",
        "unit": "day"
      },
      "Remaining Estimate": {
        "value": "0",
        "unit": "day"
      },
      "Remaining Work": {
        "value": "1",
        "value1": "0.0",
        "unit1": "SP"
      },
      "Delay": {
        "value": "-",
        "unit": "day"
      }
    }
  },
  {
    "assigneeId": "634918c848be855a65ed8350",
    "assigneeName": "Sumit Goyal",
    "role": "Tester",
    "cardDetails": {
      "Remaining Capacity": {
        "value": "-",
        "unit": "day"
      },
      "Remaining Estimate": {
        "value": "-",
        "unit": "day"
      },
      "Remaining Work": {
        "value": "-",
        "unit1": "SP"
      },
      "Delay": {
        "value": "-",
        "unit": "day"
      }
    }
  }
];

const issueData = [
  {
    "statusLogGroup": {},
    "workLogGroup": {},
    "assigneeLogGroup": {
      "2023-12-20": [
        "Pawan Kandpal"
      ]
    },
    "timeWithUser": "1d 2h",
    "timeWithStatus": " 23h",
    "loggedWorkInSeconds": 0,
    "epicName": "KnowHOW | Client enhancements and rollout support in Q1 2024",
    "spill": false,
    "preClosed": false,
    "Issue Id": "DTS-30162",
    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-30162",
    "Issue Description": "Security fixed for onboarding of Marriott International",
    "Issue Status": "Open",
    "Issue Type": "Story",
    "Size(story point/hours)": "20.0",
    "Logged Work": "0d",
    "Original Estimate": "0d",
    "Priority": "P2 - Critical",
    "Due Date": "-",
    "Remaining Estimate": "-",
    "Predicted Completion Date": "-",
    "Overall Delay": "-",
    "Dev Due Date": "-",
    "Assignee": "Pawan Kandpal",
    "Change Date": "2023-12-20",
    "Labels": [],
    "Created Date": "2023-12-04",
    "Root Cause List": [
      "None"
    ],
    "Owner Full Name": [
      "Pawan Kandpal"
    ],
    "Sprint Name": "KnowHOW | PI_16| ITR_1",
    "Release Name": "KnowHOW PI-16",
    "Updated Date": "2023-12-20",
    "Dev-Completion-Date": "-"
  },
  {
    "statusLogGroup": {
      "2023-12-20": [
        "Open"
      ],
      "2023-12-21": [
        "In Analysis"
      ]
    },
    "workLogGroup": {},
    "assigneeLogGroup": {
      "2023-12-20": [
        "Shivani .",
        "Akshat Shrivastav"
      ]
    },
    "timeWithUser": "1d 1h",
    "timeWithStatus": " 2h",
    "loggedWorkInSeconds": 0,
    "epicName": "KnowHOW | Client enhancements and rollout support in Q1 2024",
    "spill": false,
    "preClosed": false,
    "Issue Id": "DTS-31130",
    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-31130",
    "Issue Description": "Albertsons: 'Sonar Processor' Code Quality requirements",
    "Issue Status": "In Analysis",
    "Issue Type": "Story",
    "Logged Work": "0d",
    "Original Estimate": "0d",
    "Priority": "P2 - Critical",
    "Due Date": "-",
    "Remaining Estimate": "-",
    "Predicted Completion Date": "-",
    "Overall Delay": "-",
    "Dev Due Date": "-",
    "Assignee": "Akshat Shrivastav",
    "Change Date": "2023-12-21",
    "Labels": [
      "Albertsons"
    ],
    "Created Date": "2023-12-20",
    "Root Cause List": [
      "None"
    ],
    "Owner Full Name": [
      "Akshat Shrivastav"
    ],
    "Sprint Name": "KnowHOW | PI_16| ITR_1",
    "Release Name": "KnowHOW v8.3.0",
    "Updated Date": "2023-12-21",
    "Dev-Completion-Date": "-"
  },
  {
    "statusLogGroup": {
      "2023-12-20": [
        "Open"
      ]
    },
    "workLogGroup": {},
    "assigneeLogGroup": {
      "2023-12-20": [
        "Hirenkumar Babariya"
      ]
    },
    "timeWithUser": " 20h",
    "timeWithStatus": " 20h",
    "loggedWorkInSeconds": 0,
    "spill": false,
    "preClosed": false,
    "Issue Id": "DTS-31139",
    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-31139",
    "Issue Description": "Auth&Auth_Warning message should be displayed when a SSO Logged-in User, try to login using Standard Login",
    "Issue Status": "Open",
    "Issue Type": "Defect",
    "Logged Work": "0d",
    "Original Estimate": "0d",
    "Priority": "P3 - Major",
    "Due Date": "-",
    "Remaining Estimate": "-",
    "Predicted Completion Date": "-",
    "Overall Delay": "-",
    "Dev Due Date": "-",
    "Assignee": "Hirenkumar Babariya",
    "Change Date": "2023-12-20",
    "Labels": [],
    "Created Date": "2023-12-20",
    "Root Cause List": [
      "None"
    ],
    "Owner Full Name": [
      "Hirenkumar Babariya"
    ],
    "Sprint Name": "KnowHOW | PI_16| ITR_1",
    "Release Name": "KnowHOW v8.3.0",
    "Updated Date": "2023-12-20",
    "Dev-Completion-Date": "-"
  },
  {
    "statusLogGroup": {
      "2023-12-20": [
        "Open"
      ]
    },
    "workLogGroup": {},
    "assigneeLogGroup": {
      "2023-12-20": [
        "Purushottam Gupta",
        "Kunal Kamble"
      ]
    },
    "timeWithUser": "1d 1h",
    "timeWithStatus": "1d 1h",
    "loggedWorkInSeconds": 0,
    "epicName": "KnowHOW | Client enhancements and rollout support in Q1 2024",
    "spill": false,
    "preClosed": false,
    "Issue Id": "DTS-31128",
    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-31128",
    "Issue Description": "Albertsons: 'Github Processor' Code Quality requirements",
    "Issue Status": "Open",
    "Issue Type": "Story",
    "Logged Work": "0d",
    "Original Estimate": "0d",
    "Priority": "P2 - Critical",
    "Due Date": "-",
    "Remaining Estimate": "-",
    "Predicted Completion Date": "-",
    "Overall Delay": "-",
    "Dev Due Date": "-",
    "Assignee": "Kunal Kamble",
    "Change Date": "2023-12-20",
    "Labels": [
      "Albertsons"
    ],
    "Created Date": "2023-12-20",
    "Root Cause List": [
      "None"
    ],
    "Owner Full Name": [
      "Kunal Kamble"
    ],
    "Sprint Name": "KnowHOW | PI_16| ITR_1",
    "Release Name": "KnowHOW v8.3.0",
    "Updated Date": "2023-12-20",
    "Dev-Completion-Date": "-"
  },
  {
    "statusLogGroup": {
      "2023-12-20": [
        "Open"
      ]
    },
    "workLogGroup": {},
    "assigneeLogGroup": {
      "2023-12-20": [
        "Kunal Kamble",
        "Shivani ."
      ]
    },
    "timeWithUser": "1d 1h",
    "timeWithStatus": "1d 1h",
    "loggedWorkInSeconds": 0,
    "epicName": "KnowHOW | Client enhancements and rollout support in Q1 2024",
    "spill": false,
    "preClosed": false,
    "Issue Id": "DTS-31129",
    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-31129",
    "Issue Description": "Albertsons: 'Custom API' Code Quality requirements",
    "Issue Status": "Open",
    "Issue Type": "Story",
    "Logged Work": "0d",
    "Original Estimate": "0d",
    "Priority": "P2 - Critical",
    "Due Date": "-",
    "Remaining Estimate": "-",
    "Predicted Completion Date": "-",
    "Overall Delay": "-",
    "Dev Due Date": "-",
    "Assignee": "Shivani .",
    "Change Date": "2023-12-20",
    "Labels": [
      "Albertsons"
    ],
    "Created Date": "2023-12-20",
    "Root Cause List": [
      "None"
    ],
    "Owner Full Name": [
      "Shivani ."
    ],
    "Sprint Name": "KnowHOW | PI_16| ITR_1",
    "Release Name": "KnowHOW v8.3.0",
    "Updated Date": "2023-12-20",
    "Dev-Completion-Date": "-"
  },
  {
    "statusLogGroup": {
      "2023-12-21": [
        "Closed"
      ]
    },
    "workLogGroup": {},
    "assigneeLogGroup": {},
    "timeWithUser": "-",
    "timeWithStatus": "-",
    "loggedWorkInSeconds": 10800,
    "epicName": "KnowHOW | Integration with Central Auth & Hierarchy",
    "spill": true,
    "remainingEstimateInSeconds": 0,
    "preClosed": false,
    "Issue Id": "DTS-30422",
    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-30422",
    "Issue Description": "CA-KnowHow || FE || Create if else for old login and central login to work using a switch",
    "Issue Status": "Closed",
    "Issue Type": "Story",
    "Size(story point/hours)": "5.0",
    "Logged Work": "3h ",
    "Original Estimate": "0d",
    "Priority": "P3 - Major",
    "Due Date": "-",
    "Remaining Estimate": "0d",
    "Remaining Days": "0d",
    "Predicted Completion Date": "-",
    "Overall Delay": "-",
    "Dev Due Date": "-",
    "Assignee": "Sumit Goyal",
    "Change Date": "2023-12-21",
    "Labels": [
      "UI"
    ],
    "Created Date": "2023-12-11",
    "Root Cause List": [
      "None"
    ],
    "Owner Full Name": [
      "Sumit Goyal"
    ],
    "Sprint Name": "KnowHOW | PI_16| ITR_1",
    "Resolution": "Fixed or Completed",
    "Updated Date": "2023-12-21",
    "Dev-Completion-Date": "-",
    "Actual-Completion-Date": "2023-12-21T03:45:41.008"
  },
  {
    "statusLogGroup": {
      "2023-12-20": [
        "In Testing"
      ]
    },
    "workLogGroup": {},
    "assigneeLogGroup": {},
    "timeWithUser": " 23h",
    "timeWithStatus": " 18h",
    "loggedWorkInSeconds": 0,
    "epicName": "KnowHOW | Client enhancements and rollout support in Q1 2024",
    "spill": true,
    "preClosed": false,
    "Issue Id": "DTS-31079",
    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-31079",
    "Issue Description": "Backlog Epic Progress - For DRP Discovery project, epic links are not redirected to correct link in Jira",
    "Issue Status": "In Testing",
    "Issue Type": "Defect",
    "Logged Work": "0d",
    "Original Estimate": "0d",
    "Priority": "P3 - Major",
    "Due Date": "-",
    "Remaining Estimate": "-",
    "Predicted Completion Date": "-",
    "Overall Delay": "-",
    "Dev Due Date": "-",
    "Assignee": "Mamatha Paccha",
    "Change Date": "2023-12-20",
    "Labels": [
      "Prod_Defect"
    ],
    "Created Date": "2023-12-18",
    "Root Cause List": [
      "None"
    ],
    "Owner Full Name": [
      "Mamatha Paccha"
    ],
    "Sprint Name": "KnowHOW | PI_16| ITR_1",
    "Release Name": "KnowHOW v8.3.0",
    "Updated Date": "2023-12-20",
    "Dev-Completion-Date": "-"
  },
  {
    "statusLogGroup": {
      "2023-12-20": [
        "Ready for Testing",
        "In Testing",
        "Closed"
      ]
    },
    "workLogGroup": {},
    "assigneeLogGroup": {
      "2023-12-20": [
        "Mamatha Paccha"
      ]
    },
    "timeWithUser": "-",
    "timeWithStatus": "-",
    "loggedWorkInSeconds": 37800,
    "epicName": "KnowHOW | Client Requests Q4 2023",
    "spill": true,
    "remainingEstimateInSeconds": 0,
    "preClosed": false,
    "Issue Id": "DTS-30102",
    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-30102",
    "Issue Description": "Zephyr Server connection - Token support is required for Zephyr connection to fetch data (Basic auth is disabled for Jira server)",
    "Issue Status": "Closed",
    "Issue Type": "Story",
    "Logged Work": "1d 2h 30m",
    "Original Estimate": "0d",
    "Priority": "P2 - Critical",
    "Due Date": "07-Dec-2023",
    "Remaining Estimate": "0d",
    "Remaining Days": "0d",
    "Predicted Completion Date": "-",
    "Overall Delay": "-",
    "Dev Due Date": "-",
    "Assignee": "Mamatha Paccha",
    "Change Date": "2023-12-20",
    "Labels": [
      "UI"
    ],
    "Created Date": "2023-11-30",
    "Root Cause List": [
      "None"
    ],
    "Owner Full Name": [
      "Mamatha Paccha"
    ],
    "Sprint Name": "KnowHOW | PI_16| ITR_1",
    "Resolution": "Fixed or Completed",
    "Release Name": "KnowHOW v8.3.0",
    "Updated Date": "2023-12-20",
    "Dev-Completion-Date": "-",
    "Actual-Completion-Date": "2023-12-20T12:19:33.583"
  },
  {
    "statusLogGroup": {
      "2023-12-20": [
        "Open"
      ]
    },
    "workLogGroup": {},
    "assigneeLogGroup": {
      "2023-12-20": [
        "Manoj Kumar Srivastava",
        "Purushottam Gupta"
      ]
    },
    "timeWithUser": "1d 1h",
    "timeWithStatus": "1d 2h",
    "loggedWorkInSeconds": 0,
    "epicName": "KnowHOW | Client enhancements and rollout support in Q1 2024",
    "spill": false,
    "preClosed": false,
    "Issue Id": "DTS-31125",
    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-31125",
    "Issue Description": "Albertsons: 'Jira Processor' Code Quality requirements ",
    "Issue Status": "Open",
    "Issue Type": "Story",
    "Logged Work": "0d",
    "Original Estimate": "0d",
    "Priority": "P2 - Critical",
    "Due Date": "-",
    "Remaining Estimate": "-",
    "Predicted Completion Date": "-",
    "Overall Delay": "-",
    "Dev Due Date": "-",
    "Assignee": "Purushottam Gupta",
    "Change Date": "2023-12-20",
    "Labels": [
      "Albertsons"
    ],
    "Created Date": "2023-12-20",
    "Root Cause List": [
      "None"
    ],
    "Owner Full Name": [
      "Purushottam Gupta"
    ],
    "Sprint Name": "KnowHOW | PI_16| ITR_1",
    "Release Name": "KnowHOW v8.3.0",
    "Updated Date": "2023-12-20",
    "Dev-Completion-Date": "-"
  },
  {
    "statusLogGroup": {},
    "workLogGroup": {},
    "assigneeLogGroup": {
      "2023-12-20": [
        "Mamatha Paccha"
      ]
    },
    "timeWithUser": "1d",
    "timeWithStatus": " 23h",
    "loggedWorkInSeconds": 0,
    "epicName": "KnowHOW Enabler | Improvement in Code Quality & Test Automation to enable smoother releases",
    "spill": false,
    "remainingEstimateInSeconds": 144000,
    "originalEstimateInSeconds": 144000,
    "preClosed": false,
    "Issue Id": "DTS-30697",
    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-30697",
    "Issue Description": "QE|| Automation - Update script - Dashbord config - show/hide project level",
    "Issue Status": "Open",
    "Issue Type": "Story",
    "Size(story point/hours)": "5.0",
    "Logged Work": "0d",
    "Original Estimate": "5d ",
    "Priority": "P3 - Major",
    "Due Date": "-",
    "Remaining Estimate": "5d ",
    "Remaining Days": "5d ",
    "Predicted Completion Date": "-",
    "Overall Delay": "-",
    "Dev Due Date": "-",
    "Assignee": "Mamatha Paccha",
    "Change Date": "2023-12-21",
    "Labels": [
      "QA"
    ],
    "Created Date": "2023-12-12",
    "Root Cause List": [
      "None"
    ],
    "Owner Full Name": [
      "Mamatha Paccha"
    ],
    "Sprint Name": "KnowHOW | PI_16| ITR_1",
    "Updated Date": "2023-12-21",
    "Dev-Completion-Date": "-"
  },
  {
    "statusLogGroup": {
      "2023-12-20": [
        "Open",
        "In Investigation"
      ]
    },
    "workLogGroup": {},
    "assigneeLogGroup": {
      "2023-12-20": [
        "Hirenkumar Babariya"
      ]
    },
    "timeWithUser": " 20h",
    "timeWithStatus": " 16h",
    "loggedWorkInSeconds": 0,
    "spill": false,
    "preClosed": false,
    "Issue Id": "DTS-31140",
    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-31140",
    "Issue Description": "After user verify email, user approval request is not visible in knowhow 'request' button",
    "Issue Status": "In Investigation",
    "Issue Type": "Defect",
    "Logged Work": "0d",
    "Original Estimate": "0d",
    "Priority": "P3 - Major",
    "Due Date": "-",
    "Remaining Estimate": "-",
    "Predicted Completion Date": "-",
    "Overall Delay": "-",
    "Dev Due Date": "-",
    "Assignee": "Hirenkumar Babariya",
    "Change Date": "2023-12-20",
    "Labels": [],
    "Created Date": "2023-12-20",
    "Root Cause List": [
      "None"
    ],
    "Owner Full Name": [
      "Hirenkumar Babariya"
    ],
    "Sprint Name": "KnowHOW | PI_16| ITR_1",
    "Release Name": "KnowHOW v8.3.0",
    "Updated Date": "2023-12-20",
    "Dev-Completion-Date": "-"
  },
  {
    "statusLogGroup": {},
    "workLogGroup": {},
    "assigneeLogGroup": {},
    "timeWithUser": " 23h",
    "timeWithStatus": " 23h",
    "loggedWorkInSeconds": 10800,
    "epicName": "KnowHOW | Client Requests Q4 2023",
    "spill": true,
    "remainingEstimateInSeconds": 0,
    "originalEstimateInSeconds": 7200,
    "preClosed": false,
    "Issue Id": "DTS-30187",
    "Issue URL": "https://publicissapient.atlassian.net/browse/DTS-30187",
    "Issue Description": "DSV - In progress filter on chart should work as per configuration in settings (Status)",
    "Issue Status": "In Testing",
    "Issue Type": "Story",
    "Logged Work": "3h ",
    "Original Estimate": "2h ",
    "Priority": "P3 - Major",
    "Due Date": "08-Dec-2023",
    "Remaining Estimate": "0d",
    "Remaining Days": "0d",
    "Predicted Completion Date": "21-Dec-2023",
    "Potential Delay(in days)": "9d",
    "Dev Due Date": "-",
    "Assignee": "Mamatha Paccha",
    "Change Date": "2023-12-20",
    "Labels": [
      "DSV"
    ],
    "Created Date": "2023-12-05",
    "Root Cause List": [
      "None"
    ],
    "Owner Full Name": [
      "Mamatha Paccha"
    ],
    "Sprint Name": "KnowHOW | PI_16| ITR_1",
    "Release Name": "KnowHOW v9.0.0",
    "Updated Date": "2023-12-20",
    "Dev-Completion-Date": "-"
  }
];

describe('DailyScrumComponent', () => {
  let component: DailyScrumComponent;
  let fixture: ComponentFixture<DailyScrumComponent>;
  let sharedService: SharedService;
  const routerMock = {
    navigate: jasmine.createSpy('navigate')
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DailyScrumComponent],
      providers: [
        SharedService,
        { provide: ActivatedRoute, useValue: { snapshot: { params: {} } } },
        { provide: Router, useValue: routerMock }
      ],
      imports: [TableModule]
    })
      .compileComponents();

    fixture = TestBed.createComponent(DailyScrumComponent);
    sharedService = TestBed.inject(SharedService);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set selected user', () => {
    const spySelecteUserChange = spyOn(component.onSelectedUserChange, 'emit');
    component.assigneeList = [
      {assigneeId : 'dummyUserId'}
    ]
    component.setSelectedUser('dummyUserId', 'dummy');
    expect(spySelecteUserChange).toHaveBeenCalled();

  });

  it('should set selected user', () => {
    const spySelecteUserChange = spyOn(component.onSelectedUserChange, 'emit');
    component.setSelectedUser('Overall', 'dummy');
    expect(component.activeIndex2).toBe(0);

  });

  it('should set selected user when assigneeid is blank', () => {
    const spySelecteUserChange = spyOn(component.onSelectedUserChange, 'emit');
    spyOn(component, 'getCurrentAssigneeIssueData');
    component.setSelectedUser('', 'dummy');
    expect(component.activeIndex2).toBe(0);

  });

  it('should set showLess', () => {
    const spysetShowLess = spyOn(component.onShowLessOrMore, 'emit');
    component.setShowLess();
    expect(spysetShowLess).toHaveBeenCalled();

  });

  it('should set showLess', () => {
    const spyhandleViewExpandCollapse = spyOn(component.onExpandOrCollapse, 'emit');
    component.handleViewExpandCollapse();
    expect(spyhandleViewExpandCollapse).toHaveBeenCalled();

  });

  it('should convert to hours', () => {
    let result = component.convertToHoursIfTime(25, 'hours');
    expect(result).toEqual(25);

    result = component.convertToHoursIfTime(65, 'hours');
    expect(result).toEqual(65);

    result = component.convertToHoursIfTime(60, 'hours');
    expect(result).toEqual(60);
  });

  it('should convert to day', () => {
    let result = component.convertToHoursIfTime(25, 'day');
    expect(result.trim()).toEqual('0d');

    result = component.convertToHoursIfTime(480, 'day');
    expect(result.trim()).toEqual('1d');

    result = component.convertToHoursIfTime(0, 'day');
    expect(result.trim()).toEqual('0d');
  });

  /**AI Generated */
  it('should sort assigneeList by assigneeName in ascending order', () => {
    component.assigneeList = [
      { assigneeName: 'John Doe' },
      { assigneeName: 'Jane Doe' },
      { assigneeName: 'Alice Smith' },
    ];
    const event = { field: 'Team Member', order: 1 };
    component.customSort(event);
    expect(component.assigneeList).toEqual([
      { assigneeName: 'Alice Smith' },
      { assigneeName: 'Jane Doe' },
      { assigneeName: 'John Doe' },
    ]);
  });

  it('should sort assigneeList by assigneeName in descending order', () => {
    component.assigneeList = [
      { assigneeName: 'John Doe' },
      { assigneeName: 'Jane Doe' },
      { assigneeName: 'Alice Smith' },
    ];
    const event = { field: 'Team Member', order: -1 };
    component.customSort(event);
    expect(component.assigneeList).toEqual([
      { assigneeName: 'John Doe' },
      { assigneeName: 'Jane Doe' },
      { assigneeName: 'Alice Smith' },
    ]);
  });

  it('should filter assigneeList based on selected value', () => {
    component.allAssignee = [
      { assigneeId: '1', assigneeName: 'John Doe', team: 'A' },
      { assigneeId: '2', assigneeName: 'Jane Doe', team: 'B' },
      { assigneeId: '3', assigneeName: 'Alice Smith', team: 'A' },
    ];
    component.onFullScreen = true;
    const event = { value: 'A' };
    const filterKey = 'team';
    component.handleSingleSelectChange(event, filterKey);
    fixture.detectChanges();
    expect(component.assigneeList).toEqual([
      { assigneeId: '1', assigneeName: 'John Doe', team: 'A' },
      { assigneeId: '3', assigneeName: 'Alice Smith', team: 'A' },
    ]);
  });

  it('should reset assigneeList when no value is selected', () => {
    component.allAssignee = [
      { assigneeId: '1', assigneeName: 'John Doe', team: 'A' },
      { assigneeId: '2', assigneeName: 'Jane Doe', team: 'B' },
      { assigneeId: '3', assigneeName: 'Alice Smith', team: 'A' },
    ];
    const event = { value: null };
    const filterKey = 'team';
    component.assigneeList = [{ assigneeId: '1', assigneeName: 'John Doe', team: 'A' }];
    component.handleSingleSelectChange(event, filterKey);
    expect(component.assigneeList).toEqual([
      { assigneeId: '1', assigneeName: 'John Doe', team: 'A' },
      { assigneeId: '2', assigneeName: 'Jane Doe', team: 'B' },
      { assigneeId: '3', assigneeName: 'Alice Smith', team: 'A' },
    ]);
  });

  it('should calculate totals and emit filter change event', () => {
    component.allAssignee = assigneeList;
    component.columns = [
      "Remaining Capacity",
      "Remaining Estimate",
      "Remaining Work",
      "Delay"
    ];
    const event = { value: 'B' };
    const filterKey = 'team';
    spyOn(component, 'calculateTotal');
    spyOn(component.onFilterChange, 'emit');
    component.handleSingleSelectChange(event, filterKey);
    fixture.detectChanges();
    expect(component.calculateTotal).toHaveBeenCalled();
    expect(component.onFilterChange.emit).toHaveBeenCalledWith(component.filters);
  });

  it('should not filter assigneeList if allAssignee is empty', () => {
    component.allAssignee = [];
    const event = { value: 'A' };
    const filterKey = 'team';
    component.handleSingleSelectChange(event, filterKey);
    expect(component.assigneeList).toEqual([]);
  });

  it('should update allAssignee when assigneeList changes', () => {
    component.allAssignee = [];
    const changes: SimpleChanges = {
      assigneeList: {
        currentValue: [
          { assigneeId: '1', assigneeName: 'John Doe', team: 'A' },
          { assigneeId: '2', assigneeName: 'Jane Doe', team: 'B' },
        ],
        previousValue: undefined,
        firstChange: false,
        isFirstChange: function (): boolean {
          return false;
        }
      }
    };
    component.ngOnChanges(changes);
    fixture.detectChanges();
    expect(component.allAssignee).toEqual(changes.assigneeList.currentValue);
  });

  it('should update assigneeList when filters change', () => {
    component.allAssignee = [
      { assigneeId: '1', assigneeName: 'John Doe', team: 'A' },
      { assigneeId: '2', assigneeName: 'Jane Doe', team: 'B' },
    ];
    component.filters = {};
    const changes: SimpleChanges = { filters: { currentValue: { team: 'B' }, previousValue: undefined, firstChange: true, isFirstChange: () => true } };
    component.ngOnChanges(changes);
    fixture.detectChanges();
    expect(component.assigneeList).toEqual([{ assigneeId: '2', assigneeName: 'Jane Doe', team: 'B' }]);
  });

  it('should calculate totals and get current assignee issue data', () => {
    const changes: SimpleChanges = {
      "assigneeList": {
        "previousValue": [
          {
            "assigneeId": "712020:ef53b477-a4b6-4c00-9af9-fecbcc5e6e53",
            "assigneeName": "Akshat Shrivastav",
            "role": "Backend Developer",
            "cardDetails": {
              "Remaining Capacity": {
                "value": "-",
                "unit": "day"
              },
              "Remaining Estimate": {
                "value": "0",
                "unit": "day"
              },
              "Remaining Work": {
                "value": "1",
                "value1": "0.0",
                "unit1": "SP"
              },
              "Delay": {
                "value": "-",
                "unit": "day"
              }
            }
          },
          {
            "assigneeId": "6347ee76db32d9ce175d569f",
            "assigneeName": "Hirenkumar Babariya",
            "role": "Backend Developer",
            "cardDetails": {
              "Remaining Capacity": {
                "value": "-",
                "unit": "day"
              },
              "Remaining Estimate": {
                "value": "0",
                "unit": "day"
              },
              "Remaining Work": {
                "value": "2",
                "value1": "0.0",
                "unit1": "SP"
              },
              "Delay": {
                "value": "-",
                "unit": "day"
              }
            }
          },
          {
            "assigneeId": "63bd2c83713349bea186fcad",
            "assigneeName": "Kunal Kamble",
            "role": "Backend Developer",
            "cardDetails": {
              "Remaining Capacity": {
                "value": "-",
                "unit": "day"
              },
              "Remaining Estimate": {
                "value": "0",
                "unit": "day"
              },
              "Remaining Work": {
                "value": "1",
                "value1": "0.0",
                "unit1": "SP"
              },
              "Delay": {
                "value": "-",
                "unit": "day"
              }
            }
          },
          {
            "assigneeId": "622987834160640069caac53",
            "assigneeName": "Mamatha Paccha",
            "role": "Unassigned",
            "cardDetails": {
              "Remaining Capacity": {
                "value": "-",
                "unit": "day"
              },
              "Remaining Estimate": {
                "value": "2400",
                "unit": "day"
              },
              "Remaining Work": {
                "value": "3",
                "value1": "5.0",
                "unit1": "SP"
              },
              "Delay": {
                "value": "-",
                "unit": "day"
              }
            }
          },
          {
            "assigneeId": "712020:be14b4ea-748a-4d7a-a54e-b86a11ac162b",
            "assigneeName": "Pawan Kandpal",
            "role": "Unassigned",
            "cardDetails": {
              "Remaining Capacity": {
                "value": "-",
                "unit": "day"
              },
              "Remaining Estimate": {
                "value": "0",
                "unit": "day"
              },
              "Remaining Work": {
                "value": "1",
                "value1": "20.0",
                "unit1": "SP"
              },
              "Delay": {
                "value": "-",
                "unit": "day"
              }
            }
          },
          {
            "assigneeId": "63d37bfef386bda5dcac38e5",
            "assigneeName": "Purushottam Gupta",
            "role": "Backend Developer",
            "cardDetails": {
              "Remaining Capacity": {
                "value": "-",
                "unit": "day"
              },
              "Remaining Estimate": {
                "value": "0",
                "unit": "day"
              },
              "Remaining Work": {
                "value": "1",
                "value1": "0.0",
                "unit1": "SP"
              },
              "Delay": {
                "value": "-",
                "unit": "day"
              }
            }
          },
          {
            "assigneeId": "62c515fcfa577c57c3b6cba1",
            "assigneeName": "Shivani .",
            "role": "Backend Developer",
            "cardDetails": {
              "Remaining Capacity": {
                "value": "-",
                "unit": "day"
              },
              "Remaining Estimate": {
                "value": "0",
                "unit": "day"
              },
              "Remaining Work": {
                "value": "1",
                "value1": "0.0",
                "unit1": "SP"
              },
              "Delay": {
                "value": "-",
                "unit": "day"
              }
            }
          },
          {
            "assigneeId": "634918c848be855a65ed8350",
            "assigneeName": "Sumit Goyal",
            "role": "Tester",
            "cardDetails": {
              "Remaining Capacity": {
                "value": "-",
                "unit": "day"
              },
              "Remaining Estimate": {
                "value": "-",
                "unit": "day"
              },
              "Remaining Work": {
                "value": "-",
                "unit1": "SP"
              },
              "Delay": {
                "value": "-",
                "unit": "day"
              }
            }
          }
        ],
        "currentValue": [
          {
            "assigneeId": "712020:ef53b477-a4b6-4c00-9af9-fecbcc5e6e53",
            "assigneeName": "Akshat Shrivastav",
            "role": "Backend Developer",
            "cardDetails": {
              "Remaining Capacity": {
                "value": "-",
                "unit": "day"
              },
              "Remaining Estimate": {
                "value": "0",
                "unit": "day"
              },
              "Remaining Work": {
                "value": "1",
                "value1": "0.0",
                "unit1": "SP"
              },
              "Delay": {
                "value": "-",
                "unit": "day"
              }
            }
          },
          {
            "assigneeId": "6347ee76db32d9ce175d569f",
            "assigneeName": "Hirenkumar Babariya",
            "role": "Backend Developer",
            "cardDetails": {
              "Remaining Capacity": {
                "value": "-",
                "unit": "day"
              },
              "Remaining Estimate": {
                "value": "0",
                "unit": "day"
              },
              "Remaining Work": {
                "value": "2",
                "value1": "0.0",
                "unit1": "SP"
              },
              "Delay": {
                "value": "-",
                "unit": "day"
              }
            }
          },
          {
            "assigneeId": "63bd2c83713349bea186fcad",
            "assigneeName": "Kunal Kamble",
            "role": "Backend Developer",
            "cardDetails": {
              "Remaining Capacity": {
                "value": "-",
                "unit": "day"
              },
              "Remaining Estimate": {
                "value": "0",
                "unit": "day"
              },
              "Remaining Work": {
                "value": "1",
                "value1": "0.0",
                "unit1": "SP"
              },
              "Delay": {
                "value": "-",
                "unit": "day"
              }
            }
          },
          {
            "assigneeId": "622987834160640069caac53",
            "assigneeName": "Mamatha Paccha",
            "role": "Unassigned",
            "cardDetails": {
              "Remaining Capacity": {
                "value": "-",
                "unit": "day"
              },
              "Remaining Estimate": {
                "value": "2400",
                "unit": "day"
              },
              "Remaining Work": {
                "value": "3",
                "value1": "5.0",
                "unit1": "SP"
              },
              "Delay": {
                "value": "-",
                "unit": "day"
              }
            }
          },
          {
            "assigneeId": "712020:be14b4ea-748a-4d7a-a54e-b86a11ac162b",
            "assigneeName": "Pawan Kandpal",
            "role": "Unassigned",
            "cardDetails": {
              "Remaining Capacity": {
                "value": "-",
                "unit": "day"
              },
              "Remaining Estimate": {
                "value": "0",
                "unit": "day"
              },
              "Remaining Work": {
                "value": "1",
                "value1": "20.0",
                "unit1": "SP"
              },
              "Delay": {
                "value": "-",
                "unit": "day"
              }
            }
          },
          {
            "assigneeId": "63d37bfef386bda5dcac38e5",
            "assigneeName": "Purushottam Gupta",
            "role": "Backend Developer",
            "cardDetails": {
              "Remaining Capacity": {
                "value": "-",
                "unit": "day"
              },
              "Remaining Estimate": {
                "value": "0",
                "unit": "day"
              },
              "Remaining Work": {
                "value": "1",
                "value1": "0.0",
                "unit1": "SP"
              },
              "Delay": {
                "value": "-",
                "unit": "day"
              }
            }
          },
          {
            "assigneeId": "62c515fcfa577c57c3b6cba1",
            "assigneeName": "Shivani .",
            "role": "Backend Developer",
            "cardDetails": {
              "Remaining Capacity": {
                "value": "-",
                "unit": "day"
              },
              "Remaining Estimate": {
                "value": "0",
                "unit": "day"
              },
              "Remaining Work": {
                "value": "1",
                "value1": "0.0",
                "unit1": "SP"
              },
              "Delay": {
                "value": "-",
                "unit": "day"
              }
            }
          },
          {
            "assigneeId": "634918c848be855a65ed8350",
            "assigneeName": "Sumit Goyal",
            "role": "Tester",
            "cardDetails": {
              "Remaining Capacity": {
                "value": "-",
                "unit": "day"
              },
              "Remaining Estimate": {
                "value": "-",
                "unit": "day"
              },
              "Remaining Work": {
                "value": "-",
                "unit1": "SP"
              },
              "Delay": {
                "value": "-",
                "unit": "day"
              }
            }
          }
        ],
        "firstChange": false,
        isFirstChange: function (): boolean {
          return false;
        }
      },
      "selectedUser": {
        "previousValue": "Overall",
        "currentValue": "712020:ef53b477-a4b6-4c00-9af9-fecbcc5e6e53",
        "firstChange": false,
        isFirstChange: function (): boolean {
          return false;
        }
      },
      "columns": {
        "previousValue": undefined,
        "currentValue": [
          "Remaining Capacity",
          "Remaining Estimate",
          "Remaining Work",
          "Delay"
        ],
        "firstChange": false,
        isFirstChange: function (): boolean {
          return false;
        }
      }
    };
    component.assigneeList = assigneeList;
    component.columns = [
      "Remaining Capacity",
      "Remaining Estimate",
      "Remaining Work",
      "Delay"
    ];
    component.filters = {
      "role": null
    };
    component.totals = {};
    component.issueData = issueData;
    component.selectedUser = '712020:ef53b477-a4b6-4c00-9af9-fecbcc5e6e53';
    spyOn(component, 'calculateTotal');
    spyOn(component, 'getCurrentAssigneeIssueData');
    component.ngOnChanges(changes);
    expect(component.calculateTotal).toHaveBeenCalled();
    expect(component.getCurrentAssigneeIssueData).toHaveBeenCalledWith('Akshat Shrivastav');
  });

  it('should not update assigneeList if assigneeList is not changed', () => {
    component.assigneeList = [
      { assigneeId: '1', assigneeName: 'John Doe' },
      { assigneeId: '2', assigneeName: 'Jane Doe' },
    ];
    component.ngOnChanges({});
    fixture.detectChanges();
    expect(component.assigneeList).toEqual([
      { assigneeId: '1', assigneeName: 'John Doe' },
      { assigneeId: '2', assigneeName: 'Jane Doe' },
    ]);
  });


  it('should calculate totals correctly', () => {
    component.assigneeList = assigneeList;
    component.columns = [
      "Remaining Capacity",
      "Remaining Estimate",
      "Remaining Work",
      "Delay"
    ];
    component.calculateTotal();
    expect(component.totals).toEqual(JSON.parse(
      `{"Team Member":"8 Members",
      "Remaining Capacity":{"value":"0d","unit":"day","value1":"0.00"},
      "Remaining Estimate":{"value":"5d ","unit":"day","value1":"0.00"},
      "Remaining Work":{"value":"10","value1":"25.00","unit1":"SP"},
      "Delay":{"value":"0d","unit":"day","value1":"0.00"}}`
    ));

  });

  it('should calculate totals correctly', () => {
    component.assigneeList = [
      {
        "assigneeId": "712020:ef53b477-a4b6-4c00-9af9-fecbcc5e6e53",
        "assigneeName": "Akshat Shrivastav",
        "role": "Backend Developer",
        "cardDetails": {
          "Remaining Capacity": {
            "value": "-",
            "unit": "day",
            "unit1": "day"
          },
          "Remaining Estimate": {
            "value": "0",
            "unit": "day",
            "unit1": "day"
          },
          "Remaining Work": {
            "value": "1",
            "value1": "0.0",
            "unit1": "SP"
          },
          "Delay": {
            "value": "-",
            "unit": "day"
          }
        }
      },
    ];
    component.columns = [
      "Remaining Capacity",
      "Remaining Estimate",
      "Remaining Work",
      "Delay"
    ];

    spyOn(component,'convertToHoursIfTime')
    component.calculateTotal();

  });

  it('should set loader to true and emit reloadKPITab event', () => {
    spyOn(component.reloadKPITab, 'emit');
    component.loader = false;
    component.reloadKPI('event');
    expect(component.loader).toBeTrue();
    expect(component.reloadKPITab.emit).toHaveBeenCalledWith('event');
  });


  it('should set onFullScreen to false and emit closeModal event', () => {
    spyOn(component.closeModal, 'emit');
    component.onFullScreen = true;
    component.backToIterationTab();
    expect(component.onFullScreen).toBeFalse();
    expect(component.closeModal.emit).toHaveBeenCalledWith(false);
  });

  /**AI Generated */

  describe('YourComponent', () => {

    it('should initialize the filters object with null values for new filter keys', () => {

      component.filterData = [
        { filterKey: 'name' },
        { filterKey: 'age' },
        { filterKey: 'gender' }
      ];
      component.ngOnInit();
      expect(component.filters).toEqual({
        name: null,
        age: null,
        gender: null
      });
    });

    it('should not overwrite existing filter values in the filters object', () => {

      component.filterData = [
        { filterKey: 'name' },
        { filterKey: 'age' },
        { filterKey: 'gender' }
      ];
      component.filters = {
        name: 'John',
        age: 30,
        gender: 'male'
      };
      component.ngOnInit();
      expect(component.filters).toEqual({
        name: 'John',
        age: 30,
        gender: 'male'
      });
    });

    it('should not modify the filters object if filterData is undefined', () => {

      component.filterData = undefined;
      component.filters = {
        name: 'John',
        age: 30,
        gender: 'male'
      };
      component.ngOnInit();
      expect(component.filters).toEqual({
        name: 'John',
        age: 30,
        gender: 'male'
      });
    });
  });

  it('should call setSelectedUser with the provided id and name', () => {
    const id = 1;
    const name = 'John';
    spyOn(component, 'setSelectedUser');
    component.handleTabChange(id, name);
    expect(component.setSelectedUser).toHaveBeenCalledWith(id, name);
  });

  it('should return the hours with "h" appended', () => {
    const rminutes = 0;
    const rhours = 2;
    const result = component.convertToHours(rminutes, rhours);
    expect(result).toBe('2h');
  });

  describe('customSort', () => {

    beforeEach(() => {
      component.assigneeList = [
        {
          assigneeName: 'John',
          cardDetails: {
            field1: { value: 'value1' },
            field2: { value: 'value2' },
            field3: { value: 'value3' }
          }
        },
        {
          assigneeName: 'Jane',
          cardDetails: {
            field1: { value: 'value4' },
            field2: { value: 'value5' },
            field3: { value: 'value6' }
          }
        },
        {
          assigneeName: 'Bob',
          cardDetails: {
            field1: { value: 'value7' },
            field2: { value: 'value8' },
            field3: { value: 'value9' }
          }
        }
      ];
    });

    it('should sort the assigneeList by the specified field in ascending order', () => {

      const event = { field: 'field2', order: 1 };
      component.assigneeList = [
        {
          assigneeName: 'John',
          cardDetails: {
            field1: { value: 'value1' },
            field2: { value: '-' },
            field3: { value: 'value3' }
          }
        },
        {
          assigneeName: 'Jane',
          cardDetails: {
            field1: { value: 'value4' },
            field2: { value: '-' },
            field3: { value: 'value6' }
          }
        },
      ];
      component.customSort(event);
      expect(component.assigneeList).toBeDefined();
    });

    it('should sort the assigneeList by the specified field in ascending order', () => {

      const event = { field: 'field2', order: 1 };
      component.customSort(event);
      expect(component.assigneeList).toEqual([
        {
          assigneeName: 'John',
          cardDetails: {
            field1: { value: 'value1' },
            field2: { value: 'value2' },
            field3: { value: 'value3' }
          }
        },
        {
          assigneeName: 'Jane',
          cardDetails: {
            field1: { value: 'value4' },
            field2: { value: 'value5' },
            field3: { value: 'value6' }
          }
        },
        {
          assigneeName: 'Bob',
          cardDetails: {
            field1: { value: 'value7' },
            field2: { value: 'value8' },
            field3: { value: 'value9' }
          }
        }
      ]);
    });

    it('should sort the assigneeList by the specified field in descending order', () => {

      const event = { field: 'field2', order: -1 };
      component.customSort(event);
      expect(component.assigneeList).toEqual([
        {
          assigneeName: 'Bob',
          cardDetails: {
            field1: { value: 'value7' },
            field2: { value: 'value8' },
            field3: { value: 'value9' }
          }
        },
        {
          assigneeName: 'Jane',
          cardDetails: {
            field1: { value: 'value4' },
            field2: { value: 'value5' },
            field3: { value: 'value6' }
          }
        },
        {
          assigneeName: 'John',
          cardDetails: {
            field1: { value: 'value1' },
            field2: { value: 'value2' },
            field3: { value: 'value3' }
          }
        }
      ]);
    });
  });

  describe('getNameInitials', () => {
    it('should return the initials of a name with two or fewer words', () => {

      const name = 'John Doe';
      const result = component.getNameInitials(name);
      expect(result).toBe('JD');
    });

    it('should return the first two initials of a name with more than two words', () => {

      const name = 'John Doe Smith';
      const result = component.getNameInitials(name);
      expect(result).toBe('JD');
    });

    it('should return the uppercase initials of a name', () => {

      const name = 'john doe abc';
      const result = component.getNameInitials(name);
      expect(result).toBe('JD');
    });
  });

  describe('getCurrentAssigneeIssueData', () => {

    beforeEach(() => {
      component.issueData = [
        {
          Assignee: 'John',
          subTask: ['subtask1', 'subtask2']
        },
        {
          Assignee: 'Jane',
          subTask: ['subtask3', 'subtask4']
        },
        {
          Assignee: 'Bob',
          subTask: ['subtask5', 'subtask6']
        }
      ];
    });

    it('should filter the issueData by the specified assigneeName', () => {

      const assigneeName = 'John';
      component.getCurrentAssigneeIssueData(assigneeName);
      expect(component.currentAssigneeissueData).toBeDefined();
    });

    it('should update the subTask property if it is a string', () => {

      const assigneeName = 'John';
      component.getCurrentAssigneeIssueData(assigneeName);
      expect(component.currentAssigneeissueData).toBeDefined();
    });

    it('should not update the subTask property if it is already an object', () => {
      const assigneeName = 'Jane';
      component.issueData[1].subTask = [
        {
          subTaskName: 'subtask3',
          subTaskDetails: { field1: 'value1', field2: 'value2' }
        },
        {
          subTaskName: 'subtask4',
          subTaskDetails: { field1: 'value3', field2: 'value4' }
        }
      ];
      component.getCurrentAssigneeIssueData(assigneeName);
      expect(component.currentAssigneeissueData).toBeDefined();
    });


    it('should not update when issueData is undefined', () => {
      const assigneeName = 'Jane';
      component.issueData = undefined
      component.getCurrentAssigneeIssueData(assigneeName);
      expect(component.currentAssigneeissueData).toBeUndefined();
    });
  });

});
