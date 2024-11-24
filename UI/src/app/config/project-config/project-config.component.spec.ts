import { OnInit } from '@angular/core';
import { ProjectConfigComponent } from './project-config.component';
import { GetAuthorizationService } from '../../services/get-authorization.service';
import { Router } from '@angular/router';
describe('ProjectConfigComponent', () => {
  let component: ProjectConfigComponent;
  let mockGetAuthorizationService: jasmine.SpyObj<GetAuthorizationService>;

  beforeEach(() => {
    mockGetAuthorizationService = jasmine.createSpyObj(
      'GetAuthorizationService',
      ['checkIfSuperUser'],
    );
    let mockRouter: Router;
    component = new ProjectConfigComponent(
      mockGetAuthorizationService,
      mockRouter,
    );
  });

  it('should set isSuperAdmin to true if user is super admin', () => {
    mockGetAuthorizationService.checkIfSuperUser.and.returnValue(true);

    component.ngOnInit();

    expect(component.isSuperAdmin).toBe(true);
  });

  it('should not set isSuperAdmin if user is not super admin', () => {
    mockGetAuthorizationService.checkIfSuperUser.and.returnValue(false);

    component.ngOnInit();

    expect(component.isSuperAdmin).toBeFalse();
  });

  it('should initialize items correctly', () => {
    component.ngOnInit();

    expect(component.items).toEqual([
      {
        label: 'File',
        icon: 'pi pi-pw pi-file',
        items: [
          {
            label: 'New',
            icon: 'pi pi-fw pi-plus',
            items: [
              { label: 'User', icon: 'pi pi-fw pi-user-plus' },
              { label: 'Filter', icon: 'pi pi-fw pi-filter' },
            ],
          },
          { label: 'Open', icon: 'pi pi-fw pi-external-link' },
          { separator: true },
          { label: 'Quit', icon: 'pi pi-fw pi-times' },
        ],
      },
      {
        label: 'Edit',
        icon: 'pi pi-fw pi-pencil',
        items: [
          { label: 'Delete', icon: 'pi pi-fw pi-trash' },
          { label: 'Refresh', icon: 'pi pi-fw pi-refresh' },
        ],
      },
      {
        label: 'Help',
        icon: 'pi pi-fw pi-question',
        items: [
          {
            label: 'Contents',
            icon: 'pi pi-pi pi-bars',
          },
          {
            label: 'Search',
            icon: 'pi pi-pi pi-search',
            items: [
              {
                label: 'Text',
                items: [
                  {
                    label: 'Workspace',
                  },
                ],
              },
              {
                label: 'User',
                icon: 'pi pi-fw pi-file',
              },
            ],
          },
        ],
      },
      {
        label: 'Actions',
        icon: 'pi pi-fw pi-cog',
        items: [
          {
            label: 'Edit',
            icon: 'pi pi-fw pi-pencil',
            items: [
              { label: 'Save', icon: 'pi pi-fw pi-save' },
              { label: 'Update', icon: 'pi pi-fw pi-save' },
            ],
          },
          {
            label: 'Other',
            icon: 'pi pi-fw pi-tags',
            items: [{ label: 'Delete', icon: 'pi pi-fw pi-minus' }],
          },
        ],
      },
    ]);
  });
});
