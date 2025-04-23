import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpService } from '../../../services/http.service';
import { AutoApprovalComponent } from './auto-approval.component';
import { RouterTestingModule } from '@angular/router/testing';
import { MessageService } from 'primeng/api';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { APP_CONFIG, AppConfig } from '../../../services/app.config';
import { environment } from 'src/environments/environment';
import { SharedService } from 'src/app/services/shared.service';
import {
  UntypedFormControl,
  UntypedFormGroup,
  Validators,
} from '@angular/forms';
import { of, throwError } from 'rxjs';

describe('AutoApprovalComponent', () => {
  let component: AutoApprovalComponent;
  let fixture: ComponentFixture<AutoApprovalComponent>;
  let httpMock;
  let httpService;
  let messageService;
  const baseUrl = environment.baseUrl;
  const fakeRolesData = {
    message: 'Found all roles',
    success: true,
    data: [
      {
        id: '6026576bb975135001bc3487',
        roleName: 'ROLE_PROJECT_VIEWER',
        roleDescription: 'read kpi data at project level',
        createdDate: 1613125483944,
        lastModifiedDate: 1613125483944,
        isDeleted: 'False',
        permissions: [
          {
            id: '6026576bae81aeece081fa7a',
            permissionName: 'View',
            operationName: 'Read',
            resourceName: 'resource4',
            resourceId: '6026576a4ed204e1a35f10bc',
            createdDate: 1613125482976,
            lastModifiedDate: 1613125482976,
            isDeleted: 'False',
          },
        ],
      },
      {
        id: '6026576bb975135001bc3488',
        roleName: 'ROLE_PROJECT_ADMIN',
        roleDescription: 'manage user-roles at project level',
        createdDate: 1613125483944,
        lastModifiedDate: 1613125483944,
        isDeleted: 'False',
        permissions: [
          {
            id: '6026576bae81aeece081fa7a',
            permissionName: 'View',
            operationName: 'Read',
            resourceName: 'resource4',
            resourceId: '6026576a4ed204e1a35f10bc',
            createdDate: 1613125482976,
            lastModifiedDate: 1613125482976,
            isDeleted: 'False',
          },
        ],
      },
      {
        id: '6026576bb975135001bc3489',
        roleName: 'ROLE_SUPERADMIN',
        roleDescription: 'access to every resource in the instance',
        createdDate: 1613125483944,
        lastModifiedDate: 1613125483944,
        isDeleted: 'False',
        permissions: [
          {
            id: '6026576bae81aeece081fa7b',
            permissionName: 'ViewAll',
            operationName: 'Read',
            resourceName: 'resource5',
            resourceId: '6026576a4ed204e1a35f10bd',
            createdDate: 1613125482976,
            lastModifiedDate: 1613125482976,
            isDeleted: 'False',
          },
        ],
      },
    ],
  };
  const fakeAutoApprovedRoles = {
    message: 'Found all roles for auto approval',
    success: true,
    data: [
      {
        id: '632afbe00ad7d900fd19b123',
        enableAutoApprove: 'true',
        roles: [
          {
            id: '632aa1701b10d53bfb2f4552',
            roleName: 'ROLE_PROJECT_VIEWER',
            roleDescription: 'read kpi data at project level',
            createdDate: 1663738224175,
            lastModifiedDate: 1663738224175,
            isDeleted: 'False',
            permissions: [
              {
                id: '632aa16f846dafee81f7824c',
                permissionName: 'View',
                operationName: 'Read',
                resourceName: 'resource4',
                createdDate: 1663738223035,
                lastModifiedDate: 1663738223035,
                isDeleted: 'False',
              },
            ],
            displayName: 'Project Viewer',
          },
        ],
      },
    ],
  };
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AutoApprovalComponent],
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      providers: [
        HttpService,
        MessageService,
        SharedService,
        { provide: APP_CONFIG, useValue: AppConfig },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AutoApprovalComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    httpService = TestBed.inject(HttpService);
    fixture.detectChanges();
    messageService = TestBed.inject(MessageService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get auto-approved roles on load', () => {
    component.ngOnInit();
    fixture.detectChanges();
    httpMock
      .match(baseUrl + '/api/autoapprove')[0]
      .flush(fakeAutoApprovedRoles);
    expect(component.autoApprovedId).toEqual(
      fakeAutoApprovedRoles['data'][0].id,
    );
  });

  it('should load roles data on load', () => {
    component.ngOnInit();
    fixture.detectChanges();
    httpMock.match(baseUrl + '/api/roles')[0].flush(fakeRolesData);
    expect(Object.keys(component.rolesData).length).toEqual(
      Object.keys(fakeRolesData.data).length,
    );
  });

  it('should submit auto-approval list', () => {
    component.ngOnInit();
    fixture.detectChanges();
    component.autoApprovalForm.controls['enableAutoApprove'].setValue(true);
    component.autoApprovalForm.controls['roles'].setValue([
      'ROLE_PROJECT_VIEWER',
      'ROLE_PROJECT_ADMIN',
    ]);
    component.autoApprovedId = fakeAutoApprovedRoles['data'][0].id;
    const fakeUpdateResponse = {
      message: 'modified access_request@632afbe00ad7d900fd19b123',
      success: true,
      data: [
        {
          id: '632afbe00ad7d900fd19b123',
          enableAutoApprove: 'true',
          roles: [
            {
              id: '633ed1906e65e8b58724d161',
              roleName: 'ROLE_PROJECT_VIEWER',
              roleDescription: 'read kpi data at project level',
              createdDate: 1665061264403,
              lastModifiedDate: 1665061264403,
              isDeleted: 'False',
              permissions: [
                {
                  id: '633ed18f768b4170eafadb2b',
                  permissionName: 'View',
                  operationName: 'Read',
                  resourceName: 'resource4',
                  createdDate: 1665061263540,
                  lastModifiedDate: 1665061263540,
                  isDeleted: 'False',
                },
              ],
              displayName: 'Project Viewer',
            },
            {
              id: '633ed1906e65e8b58724d162',
              roleName: 'ROLE_PROJECT_ADMIN',
              roleDescription: 'manage user-roles at project level',
              createdDate: 1665061264403,
              lastModifiedDate: 1665061264403,
              isDeleted: 'False',
              permissions: [
                {
                  id: '633ed18f768b4170eafadb2b',
                  permissionName: 'View',
                  operationName: 'Read',
                  resourceName: 'resource4',
                  createdDate: 1665061263540,
                  lastModifiedDate: 1665061263540,
                  isDeleted: 'False',
                },
              ],
              displayName: 'Project Admin',
            },
          ],
        },
      ],
    };
    component.onSubmit();
    httpMock
      .match(baseUrl + '/api/autoapprove/' + component.autoApprovedId)[0]
      .flush(fakeUpdateResponse);
  });

  it('should handle change when event is false', () => {
    const event = {
      checked: false,
    };
    component.autoApprovalForm = new UntypedFormGroup({
      roles: new UntypedFormControl([]),
    });
    component.handleChange(event);
    expect(component.autoApprovalForm.controls['roles'].value).toEqual([]);
  });

  it('should handle error response and display error message', () => {
    spyOn(httpService, 'getAutoApprovedRoleList').and.returnValue(
      throwError({ error: { message: 'Test Error' } }),
    );
    const spy = spyOn(messageService, 'add');
    component.getAutoApprovedRoles();

    expect(spy).toHaveBeenCalledWith({
      severity: 'error',
      summary: 'Test Error',
    });
  });

  it('should handle error on fetching roles', () => {
    spyOn(httpService, 'getRolesList').and.returnValue(of('Error'));
    spyOn(messageService, 'add');
    component.getRolesList();
    expect(messageService.add).toHaveBeenCalled();
  });

  it('should handle error while submitting', () => {
    component.autoApprovalForm = new UntypedFormGroup({
      roles: new UntypedFormControl(['ROLE_PROJECT_VIEWER', 'ROLE_GUEST']),
      enableAutoApprove: new UntypedFormControl(true, [Validators.required]),
    });
    component.autoApprovedId = '61dee937b316a50f2bee7227';
    component.rolesData = [
      {
        id: '64b4ed7d138ed232323cfb9c',
        roleName: 'ROLE_PROJECT_VIEWER',
        roleDescription: 'read kpi data at project level',
        createdDate: 1689578877955,
        lastModifiedDate: 1689578877955,
        isDeleted: 'False',
        permissions: [
          {
            id: '64b4ed7daca5f8c8952dcdd6',
            permissionName: 'View',
            operationName: 'Read',
            resourceName: 'resource4',
            createdDate: 1689578877756,
            lastModifiedDate: 1689578877756,
            isDeleted: 'False',
          },
        ],
        displayName: 'Project Viewer',
      },
      {
        id: '64b4ed7d138ed232323cfb9d',
        roleName: 'ROLE_PROJECT_ADMIN',
        roleDescription: 'manage user-roles at project level',
        createdDate: 1689578877955,
        lastModifiedDate: 1689578877955,
        isDeleted: 'False',
        permissions: [
          {
            id: '64b4ed7daca5f8c8952dcdd6',
            permissionName: 'View',
            operationName: 'Read',
            resourceName: 'resource4',
            createdDate: 1689578877756,
            lastModifiedDate: 1689578877756,
            isDeleted: 'False',
          },
        ],
        displayName: 'Project Admin',
      },
      {
        id: '64b4ed7d138ed232323cfb9e',
        roleName: 'ROLE_SUPERADMIN',
        roleDescription: 'access to every resource in the instance',
        createdDate: 1689578877955,
        lastModifiedDate: 1689578877955,
        isDeleted: 'False',
        permissions: [
          {
            id: '64b4ed7daca5f8c8952dcdd7',
            permissionName: 'ViewAll',
            operationName: 'Read',
            resourceName: 'resource5',
            createdDate: 1689578877756,
            lastModifiedDate: 1689578877756,
            isDeleted: 'False',
          },
        ],
        displayName: 'Super Admin',
      },
      {
        id: '64b4ed7d138ed232323cfb9f',
        roleName: 'ROLE_GUEST',
        roleDescription: 'read access for the instance',
        createdDate: 1689578877955,
        lastModifiedDate: 1689578877955,
        isDeleted: 'False',
        permissions: [
          {
            id: '64b4ed7daca5f8c8952dcdd6',
            permissionName: 'View',
            operationName: 'Read',
            resourceName: 'resource4',
            createdDate: 1689578877756,
            lastModifiedDate: 1689578877756,
            isDeleted: 'False',
          },
        ],
        displayName: 'Guest',
      },
    ];
    const errResponse = {
      error: 'Something went wrong',
      success: false,
      message: 'Something went wrong',
    };
    spyOn(httpService, 'submitAutoApproveData').and.returnValue(
      throwError(errResponse),
    );
    const spy = spyOn(messageService, 'add');
    component.onSubmit();
    expect(spy).toHaveBeenCalled();
  });
});
