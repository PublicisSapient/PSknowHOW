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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { InputSwitchModule } from 'primeng/inputswitch';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { TableModule } from 'primeng/table';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpService } from 'src/app/services/http.service';
import { RsaEncryptionService } from 'src/app/services/rsa.encryption.service';
import { ConnectionListComponent } from './connection-list.component';
import { AppConfig, APP_CONFIG } from 'src/app/services/app.config';
import { ConfirmationService } from 'primeng/api';
import { environment } from 'src/environments/environment';
import { of } from 'rxjs';

describe('ConnectionListComponent', () => {
  let component: ConnectionListComponent;
  let fixture: ComponentFixture<ConnectionListComponent>;
  let httpMock;
  let httpService;
  const baseUrl = environment.baseUrl;
  const connectionTableData = [
    {
      connectionName: 'jenkinsJenkins',
      type: 'Jenkins',
      username: 'userNameJenkins',
      baseUrl: 'baseUrlJenkins',
    },
    {
      connectionName: 'DojoJira',
      type: 'Jira',
      username: 'fds',
      baseUrl: 'fdsf',
    },
    {
      connectionName: 'dsadsa',
      type: 'Azure Boards',
    },
    {
      connectionName: 'ConnectBit12',
      type: 'Bitbucket',
      username: 'userNameBitbucket12',
      baseUrl: 'baseBitbucket12',
    },
    {
      connectionName: 'Git7',
      type: 'GITLAB',
      username: 'Git7',
      baseUrl: 'Git7',
    },
    {
      connectionName: 'Team1',
      type: 'TEAMCITY',
      username: 'Team1',
      baseUrl: 'Team1',
    },
    {
      connectionName: 'sonar121',
      type: 'SONAR',
      username: 'sonar121',
      baseUrl: 'sonar132',
    },
    {
      connectionName: 'Sonar2',
      type: 'SONAR',
      username: 'Sonar2',
      baseUrl: 'Sonar2',
    },
    {
      connectionName: 'sonar3',
      type: 'SONAR',
      username: 'sonar3',
      baseUrl: 'sonar3',
    },
    {
      connectionName: 'connectNameSonar321',
      type: 'Sonar',
      username: 'userNameSonar321',
      baseUrl: 'baseSonar321',
    },
    {
      connectionName: 'Git1Connect',
      type: 'GitLab',
      username: 'Git1connect',
      baseUrl: 'Git1connect',
    },
    {
      connectionName: 'Git30',
      type: 'GITLAB',
      username: 'Git30',
      baseUrl: 'Git30',
    },
    {
      connectionName: 'Azure pipeline 1',
      type: 'AZURE PIPELINE',
      baseUrl: 'Azure pipeline 1',
    },
    {
      connectionName: 'Azure pipeline 2',
      type: 'Azure Pipeline',
      baseUrl: 'Azure pipeline 2',
    },
    {
      connectionName: 'sonar41',
      type: 'Sonar',
      username: 'sonar41',
      baseUrl: 'sonar41',
    },
    {
      connectionName: 'git6',
      type: 'GitLab',
      username: 'git6',
      baseUrl: 'git6',
    },
    {
      connectionName: 'Sonar 513',
      type: 'Sonar',
      username: 'Sonar 513',
      baseUrl: 'Sonar 513',
    },
    {
      connectionName: 'Sonar Connec1',
      type: 'Sonar',
      username: 'sonarusername1',
      baseUrl: 'baseurl1',
    },
    {
      connectionName: 'bambooConnection1',
      type: 'Bamboo',
      username: 'bambooUsername1',
      baseUrl: 'bambooBaseUrl1',
    },
    {
      connectionName: 'teamCityConnec3',
      type: 'Teamcity',
      username: 'teamCityUsername143',
      baseUrl: 'teamCityBaseUrl223',
    },
    {
      connectionName: 'Azure p4',
      type: 'Azure Pipeline',
      baseUrl: 'azureBase4',
    },
    {
      connectionName: 'azureR4',
      type: 'Azure Repository',
      baseUrl: 'azureBaseR3',
    },
    {
      connectionName: 'Git432',
      type: 'GitLab',
      username: 'Hit3214',
      baseUrl: 'Git42342',
    },
    {
      connectionName: 'DojoJira2',
      type: 'Jira',
      username: 'SUPERADMIN',
      baseUrl: 'https://test.com/jira/',
    },
    {
      connectionName: 'JiraConnection11',
      type: 'Jira',
      username: 'jiraUser11',
      baseUrl: 'JiraBase11',
    },
    {
      connectionName: 'dsf',
      type: 'Jira',
      username: 'rfrsd',
      baseUrl: 'fds',
    },
    {
      connectionName: 'connecJira101',
      type: 'Jira',
      username: 'userConnect101',
      baseUrl: 'baseConnect101',
    },
    {
      connectionName: 'connectJira101',
      type: 'Jira',
      username: 'username102',
      baseUrl: 'baseUrlConnect101',
    },
    {
      connectionName: 'jiraConnect103',
      type: 'Jira',
      username: 'usernameConnect103',
      baseUrl: 'baseUrl103',
    },
    {
      connectionName: '104ConnectName',
      type: 'Jira',
      username: '104Username',
      baseUrl: '104BaseUrl',
    },
    {
      connectionName: 'dfgfvd',
      type: 'Azure',
      username: 'dsvfd',
      baseUrl: 'dsfgvfd',
    },
    {
      connectionName: 'Bit200Connect',
      type: 'Bitbucket',
      username: 'userConnect200',
      baseUrl: 'base200Base',
    },
    {
      connectionName: 'ConnectJira300',
      type: 'Jira',
      username: 'user300',
      baseUrl: 'base300',
    },
    {
      connectionName: 'connect301',
      type: 'Jira',
      username: 'user301',
      baseUrl: 'base301',
    },
    {
      connectionName: 'connect302',
      type: 'Jira',
      username: 'user302',
      baseUrl: 'base302',
    },
    {
      connectionName: 'connect302',
      type: 'Zephyr',
      username: 'user302',
      baseUrl: 'base302',
    },
  ];


  const getConnectionsResponse = require('../../../../test/resource/fakeGetConnectionResponse.json');
  

  const connectionLabelsFields = [
    {
      connectionType: 'Jira',
      connectionLabel: 'Jira',
      labels: [
        'Connection Type',
        'Connection Name',
        'Is Cloud Environment',
        'Base Url',
        'Username',
        'Use vault password',
        'Password',
        'Api End Point',
        'IsOAuth',
        'Private Key',
        'Consumer Key',
        'Is Offline',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'cloudEnv',
        'baseUrl',
        'username',
        'vault',
        'password',
        'apiEndPoint',
        'isOAuth',
        'privateKey',
        'consumerKey',
        'offline',
        'connPrivate',
      ],
    },
    {
      connectionType: 'Azure',
      connectionLabel: 'Azure Boards',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Use vault password',
        'PAT',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'pat',
        'connPrivate',
      ],
    },
    {
      connectionType: 'GitHub',
      connectionLabel: 'GitHub',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Repo Ownername',
        'Use vault password',
        'Access Token',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'accessToken',
        'connPrivate',
      ],
    },
    {
      connectionType: 'GitLab',
      connectionLabel: 'GitLab',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Use vault password',
        'Access Token',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'accessToken',
        'connPrivate',
      ],
    },
    {
      connectionType: 'Bitbucket',
      connectionLabel: 'Bitbucket',
      labels: [
        'Connection Type',
        'Connection Name',
        'Is Cloud Environment',
        'Base Url',
        'Username',
        'Use vault password',
        'Password',
        'API End Point',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'cloudEnv',
        'baseUrl',
        'username',
        'vault',
        'password',
        'apiEndPoint',
        'connPrivate',
      ],
    },
    {
      connectionType: 'Sonar',
      connectionLabel: 'Sonar',
      labels: [
        'Connection Type',
        'Connection Name',
        'Is Cloud Environment',
        'Base Url',
        'Username',
        'Use vault password',
        ['Use Password', 'Use Token'],
        'Password',
        'Access Token',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'cloudEnv',
        'baseUrl',
        'username',
        'vault',
        'accessTokenEnabled',
        'password',
        'accessToken',
        'connPrivate',
      ],
    },
    {
      connectionType: 'Jenkins',
      connectionLabel: 'Jenkins',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Use vault password',
        'Api Key',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'apiKey',
        'connPrivate',
      ],
    },
    {
      connectionType: 'Bamboo',
      connectionLabel: 'Bamboo',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Use vault password',
        'Password',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'password',
        'connPrivate',
      ],
    },
    {
      connectionType: 'Teamcity',
      connectionLabel: 'Teamcity',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Use vault password',
        'Password',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'password',
        'connPrivate',
      ],
    },
    {
      connectionType: 'AzurePipeline',
      connectionLabel: 'Azure Pipeline',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Use vault password',
        'PAT',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'vault',
        'pat',
        'connPrivate',
      ],
    },
    {
      connectionType: 'AzureRepository',
      connectionLabel: 'Azure Repository',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Use vault password',
        'PAT',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'vault',
        'pat',
        'connPrivate',
      ],
    },
    {
      connectionType: 'Zephyr',
      connectionLabel: 'Zephyr',
      labels: [
        'Connection Type',
        'Connection Name',
        'Is Cloud Environment',
        'Base Url',
        'Username',
        'Use vault password',
        'Password',
        'Api End Point',
        'Access Token',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'cloudEnv',
        'baseUrl',
        'username',
        'vault',
        'password',
        'apiEndPoint',
        'accessToken',
        'connPrivate',
      ],
    },
  ];

  const fieldsAndLabels = [
    {
      connectionType: 'Jira',
      connectionLabel: 'Jira',
      labels: [
        'Connection Type',
        'Connection Name',
        'Is Cloud Environment',
        'Base Url',
        'Username',
        'Use vault password',
        'Password',
        'Api End Point',
        'IsOAuth',
        'Private Key',
        'Consumer Key',
        'Is Offline',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'cloudEnv',
        'baseUrl',
        'username',
        'vault',
        'password',
        'apiEndPoint',
        'isOAuth',
        'privateKey',
        'consumerKey',
        'offline',
        'connPrivate',
      ],
    },
    {
      connectionType: 'Azure',
      connectionLabel: 'Azure Boards',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Use vault password',
        'PAT',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'pat',
        'connPrivate',
      ],
    },
    {
      connectionType: 'GitHub',
      connectionLabel: 'GitHub',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Repo Ownername',
        'Use vault password',
        'Access Token',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'accessToken',
        'connPrivate',
      ],
    },
    {
      connectionType: 'GitLab',
      connectionLabel: 'GitLab',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Use vault password',
        'Access Token',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'accessToken',
        'connPrivate',
      ],
    },
    {
      connectionType: 'Bitbucket',
      connectionLabel: 'Bitbucket',
      labels: [
        'Connection Type',
        'Connection Name',
        'Is Cloud Environment',
        'Base Url',
        'Username',
        'Use vault password',
        'Password',
        'API End Point',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'cloudEnv',
        'baseUrl',
        'username',
        'vault',
        'password',
        'apiEndPoint',
        'connPrivate',
      ],
    },
    {
      connectionType: 'Sonar',
      connectionLabel: 'Sonar',
      labels: [
        'Connection Type',
        'Connection Name',
        'Is Cloud Environment',
        'Base Url',
        'Username',
        'Use vault password',
        'Password',
        'Access Token',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'cloudEnv',
        'baseUrl',
        'username',
        'vault',
        'password',
        'accessToken',
        'connPrivate',
        "accessTokenEnabled",
      ],
    },
    {
      connectionType: 'Jenkins',
      connectionLabel: 'Jenkins',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Use vault password',
        'Api Key',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'apiKey',
        'connPrivate',
      ],
    },
    {
      connectionType: 'Bamboo',
      connectionLabel: 'Bamboo',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Use vault password',
        'Password',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'password',
        'connPrivate',
      ],
    },
    {
      connectionType: 'Teamcity',
      connectionLabel: 'Teamcity',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Use vault password',
        'Password',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'password',
        'connPrivate',
      ],
    },
    {
      connectionType: 'AzurePipeline',
      connectionLabel: 'Azure Pipeline',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Use vault password',
        'PAT',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'vault',
        'pat',
        'connPrivate',
      ],
    },
    {
      connectionType: 'AzureRepository',
      connectionLabel: 'Azure Repository',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Use vault password',
        'PAT',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'vault',
        'pat',
        'connPrivate',
      ],
    },
    {
      connectionType: 'Zephyr',
      connectionLabel: 'Zephyr',
      labels: [
        'Connection Type',
        'Connection Name',
        'Is Cloud Environment',
        'Base Url',
        'Username',
        'Use vault password',
        'Password',
        'Api End Point',
        'Access Token',
        'Is Connection Private',
      ],
      inputFields: [
        'type',
        'connectionName',
        'cloudEnv',
        'baseUrl',
        'username',
        'vault',
        'password',
        'apiEndPoint',
        'accessToken',
        'connPrivate',
      ],
    },
  ];

  const connectionList = [
    'Jira',
    'Azure',
    'GitHub',
    'GitLab',
    'Bitbucket',
    'Sonar',
    'Jenkins',
    'Bamboo',
    'Teamcity',
    'AzurePipeline',
    'AzureRepository',
    'Zephyr',
  ];
  const enableDisableMatrix = {
    enableDisableEachTime: {
      cloudEnv: [],
      offline: [],
      isOAuth: [
        {
          field: 'privateKey',
          isEnabled: false,
        },
        {
          field: 'consumerKey',
          isEnabled: false,
        },
      ],
      vault: [
        {
          field: 'password',
          isEnabled: false,
        },
        {
          field: 'accessToken',
          isEnabled: false,
        },
        {
          field: 'pat',
          isEnabled: false,
        },
        {
          field: 'apiKey',
          isEnabled: false,
        },
      ],
      accessTokenEnabled: [],
    },
    enableDisableAnotherTime: {
      cloudEnv: [],
      offline: [
        {
          field: 'cloudEnv',
          isEnabled: true,
        },
        {
          field: 'baseUrl',
          isEnabled: true,
        },
        {
          field: 'username',
          isEnabled: true,
        },
        {
          field: 'password',
          isEnabled: true,
        },
        {
          field: 'apiEndPoint',
          isEnabled: true,
        },
        {
          field: 'isOAuth',
          isEnabled: true,
        },
        {
          field: 'privateKey',
          isEnabled: false,
        },
        {
          field: 'consumerKey',
          isEnabled: false,
        },
      ],
      isOAuth: [],
      vault: [
        {
          field: 'password',
          isEnabled: false,
        },
        {
          field: 'accessToken',
          isEnabled: false,
        },
        {
          field: 'pat',
          isEnabled: false,
        },
        {
          field: 'apiKey',
          isEnabled: false,
        },
      ],
      accessTokenEnabled: [],
    },
  };
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        InputSwitchModule,
        ReactiveFormsModule,
        CommonModule,
        RouterTestingModule.withRoutes([]),
        HttpClientTestingModule,
        TableModule,
        ConfirmDialogModule,
        BrowserAnimationsModule,
      ],
      declarations: [ConnectionListComponent],
      providers: [
        HttpService,
        ConfirmationService,
        RsaEncryptionService,
        { provide: APP_CONFIG, useValue: AppConfig },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectionListComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  it('Should check whether component exist', () => {
    expect(component).toBeTruthy();
  });

  it('Should test 10 connections are loaded', () => {
    expect(component.addEditConnectionFieldsNlabels.length).toEqual(12);
  });

  it('Should test all connections are present', () => {
    connectionList.forEach((connection, index) => {
      expect(
        component.addEditConnectionFieldsNlabels[index].connectionType,
      ).toEqual(connection);
    });
  });

  it('Should test all the fields exist in the Add/Edit connection popup', () => {
    connectionLabelsFields.forEach((connectionData, index) => {
      expect(
        component.addEditConnectionFieldsNlabels[index].connectionType,
      ).toEqual(connectionData.connectionType);
      connectionData.labels.forEach((label, innerIndex) => {
        expect(
          component.addEditConnectionFieldsNlabels[index].labels[innerIndex],
        ).toEqual(label);
      });
      connectionData.inputFields.forEach((field, innerIndex) => {
        expect(
          component.addEditConnectionFieldsNlabels[index].inputFields[
            innerIndex
          ],
        ).toEqual(field);
      });
    });
  });

  it('Should test connection list table is rendering', () => {
    connectionTableData.forEach((connectionRecord) => {
      expect(connectionRecord.connectionName.length).toBeGreaterThan(0);
      expect(connectionRecord.type.length).toBeGreaterThan(0);
    });
  });

  it('should allow user to change connection type from the dropdown', () => {
    const fakeEvent = {
      apiEndPoint: undefined,
      baseUrl: undefined,
      cloudEnv: false,
      connPrivate: true,
      connectionName: undefined,
      consumerKey: undefined,
      isOAuth: false,
      offline: false,
      password: undefined,
      pat: undefined,
      privateKey: undefined,
      type: 'Azure',
      username: undefined,
    };
    component.onChangeConnection(fakeEvent);
    fixture.detectChanges();
    expect(component.selectedConnectionType).toBe(fakeEvent.type);
    expect(component.testConnectionMsg).toBe('');
  });

  it('should allow user to initialize new connection on click of "New Connection" button', () => {
    component.selectedConnectionType = 'Jira';
    component.createConnection();
    fixture.detectChanges();
    expect(component.submitted).toBeFalse();
    expect(component.connectionDialog).toBeTrue();
    expect(component.isNewlyConfigAdded).toBeTrue();
    expect(component.disableConnectionTypeDropDown).toBeFalse();
  });

  it('should enable fields depending on inputs', () => {
    const fakeEvent = { originalEvent: { isTrusted: true }, checked: true };
    const field = 'isOAuth';
    component.enableDisableOnToggle = enableDisableMatrix;
    component.enableDisableSwitch(fakeEvent, field);
    fixture.detectChanges();
    expect(
      component.basicConnectionForm.controls['privateKey'].enabled,
    ).toBeTrue();
    expect(
      component.basicConnectionForm.controls['consumerKey'].enabled,
    ).toBeTrue();
  });

  it('should disable fields depending on inputs', () => {
    const fakeEvent = { originalEvent: { isTrusted: true }, checked: false };
    const field = 'isOAuth';
    component.enableDisableOnToggle = enableDisableMatrix;
    component.enableDisableSwitch(fakeEvent, field);
    fixture.detectChanges();
    expect(
      component.basicConnectionForm.controls['privateKey'].enabled,
    ).toBeFalse();
    expect(
      component.basicConnectionForm.controls['consumerKey'].enabled,
    ).toBeFalse();
  });

  it('should fetch and render connections list', () => {
    component.getConnectionList();
    fixture.detectChanges();
    httpMock
      .match(`${baseUrl}/api/connections`)[0]
      .flush(getConnectionsResponse);
  });

  it('should save connection', () => {
    component.connection['type'] = 'Jira';
    component.selectedConnectionType = 'Jira';
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.ngOnInit();
    component.createConnection();
    component.connectionTypeFieldsAssignment();
    fixture.detectChanges();
    component.basicConnectionForm.controls['type'].setValue('Jira');
    component.basicConnectionForm.controls['connectionName'].setValue(
      'TestConnectionRishabhJira4',
    );
    component.basicConnectionForm.controls['cloudEnv'].setValue(false);
    component.basicConnectionForm.controls['baseUrl'].setValue(
      'https://test.com/jira',
    );
    component.basicConnectionForm.controls['username'].setValue('tst-1');
    component.basicConnectionForm.controls['password'].setValue('test');
    component.basicConnectionForm.controls['apiEndPoint'].setValue(
      'rest/api/2',
    );
    component.basicConnectionForm.controls['isOAuth'].setValue(false);
    component.basicConnectionForm.controls['offline'].setValue(false);
    component.basicConnectionForm.controls['connPrivate'].setValue(true);
    component.basicConnectionForm.controls['privateKey'].setValue('test');
    component.basicConnectionForm.controls['consumerKey'].setValue('test');
    component.basicConnectionForm.controls['vault'].setValue(false);
    component.isNewlyConfigAdded = true;
    component.saveConnection();
    fixture.detectChanges();
    expect(component.basicConnectionForm.valid).toBeTruthy();
    httpMock.match(`${baseUrl}/api/connections`)[0].flush({
      message: 'created and saved new connection',
      success: true,
      data: {
        id: '6066c07569515b0001df160f',
        type: 'Jira',
        connectionName: 'TestConnectionRishabh4',
        cloudEnv: true,
        baseUrl: ' https://test.com/jira',
        username: 'tst-1',
        password: '',
        apiEndPoint: 'rest/api/2',
        isOAuth: false,
        offline: false,
        createdBy: 'SUPERADMIN',
        connPrivate: true,
        updatedBy: 'SUPERADMIN',
        connectionUser: ['SUPERADMIN'],
      },
    });
  });

  it('should hide dialog', () => {
    component.hideDialog();
    fixture.detectChanges();
    expect(component.connectionDialog).toBeFalse();
    expect(component.submitted).toBeFalse();
    expect(component.isNewlyConfigAdded).toBeFalse();
    expect(component.testConnectionMsg).toBe('');
    expect(component.testConnectionValid).toBeTrue();
  });

  it('should allow user to edit connection', () => {
    const connection = {
      id: '6066cad069515b0001df1809',
      type: 'Jira',
      connectionName: 'TestConnectionRishabh4',
      cloudEnv: false,
      baseUrl: 'https://test.com/jira',
      username: 'tst-1',
      apiEndPoint: 'rest/api/2',
      isOAuth: false,
      offline: false,
      createdAt: '2021-04-02T07:42:09',
      createdBy: 'SUPERADMIN',
      connPrivate: true,
      updatedBy: 'SUPERADMIN',
      connectionUsers: ['SUPERADMIN'],
      vault: false,
    };
    component.editConnection(connection);
    fixture.detectChanges();
    expect(component.connection).toEqual({ ...connection });
    expect(component.connectionDialog).toBeTrue();
    expect(component.isNewlyConfigAdded).toBeFalse();
    expect(component.selectedConnectionType).toBe('Jira');
    expect(component.disableConnectionTypeDropDown).toBeTrue();
  });

  it('should get zypherURL', () => {
    const response = {
      data: 'https://api.zephyrscale.smartbear.com/v2/',
      message: 'Fetched Zephyr Cloud Base Url successfully',
      success: true,
    };
    spyOn(httpService, 'getZephyrUrl').and.returnValue(of(response));
    component.getZephyrUrl();
    fixture.detectChanges();
    expect(component.zephyrUrl).toBe(
      'https://api.zephyrscale.smartbear.com/v2/',
    );
  });

  it('should validate fields when zypher connection selected', () => {
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.zephyrUrl = 'https://api.zephyrscale.smartbear.com/v2/';
    component.connection['type'] = 'zephyr';
    component.connection['vault'] = true;
    component.connection['cloudEnv'] = true;
    component.selectedConnectionType = 'zephyr';

    component.connectionTypeFieldsAssignment();
    fixture.detectChanges();
    component.checkZephyr();
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      expect(component.basicConnectionForm.get('baseUrl').value).toBe(
        'https://api.zephyrscale.smartbear.com/v2/',
      );
    });
  });

  it('should validate fields when zypher connection selected', () => {
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.zephyrUrl = 'https://api.zephyrscale.smartbear.com/v2/';
    component.connection['type'] = 'zephyr';
    component.connection['vault'] = true;
    component.connection['cloudEnv'] = false;
    component.selectedConnectionType = 'zephyr';

    component.connectionTypeFieldsAssignment();
    fixture.detectChanges();
    component.checkZephyr();
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      expect(component.basicConnectionForm.get('password').value).toBe('');
    });
  });

  it('should validate fields when zypher connection selected', () => {
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.zephyrUrl = 'https://api.zephyrscale.smartbear.com/v2/';
    component.connection['type'] = 'zephyr';
    component.connection['vault'] = false;
    component.connection['cloudEnv'] = true;
    component.selectedConnectionType = 'zephyr';

    component.connectionTypeFieldsAssignment();
    fixture.detectChanges();
    component.checkZephyr();
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      expect(component.basicConnectionForm.get('password').value).toBe('');
    });
  });

  it('should validate fields when zypher connection selected', () => {
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.zephyrUrl = 'https://api.zephyrscale.smartbear.com/v2/';
    component.connection['type'] = 'zephyr';
    component.connection['vault'] = false;
    component.connection['cloudEnv'] = false;
    component.selectedConnectionType = 'zephyr';

    component.connectionTypeFieldsAssignment();
    fixture.detectChanges();
    component.checkZephyr();
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      expect(component.basicConnectionForm.get('password').value).toBe('');
    });
  });

  it('should validate fields when Sonar connection selected', () => {
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Sonar';
    component.connection['vault'] = true;
    component.connection['cloudEnv'] = true;
    component.selectedConnectionType = 'Sonar';

    component.connectionTypeFieldsAssignment();
    fixture.detectChanges();
    component.enableDisableFieldsOnIsCloudSwithChange();
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      expect(component.basicConnectionForm.get('username').value).toBe('');
      expect(component.basicConnectionForm.get('password').value).toBe('');
      expect(component.basicConnectionForm.get('accessToken').value).toBe('');
    });
  });

  it('should validate fields when Sonar connection selected', () => {
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Sonar';
    component.connection['vault'] = true;
    component.connection['cloudEnv'] = false;
    component.selectedConnectionType = 'Sonar';

    component.connectionTypeFieldsAssignment();
    fixture.detectChanges();
    component.enableDisableFieldsOnIsCloudSwithChange();
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      expect(component.basicConnectionForm.get('password').value).toBe('');
    });
  });

  it('should validate fields when Sonar connection selected', () => {
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Sonar';
    component.connection['vault'] = false;
    component.connection['cloudEnv'] = true;
    component.selectedConnectionType = 'Sonar';

    component.connectionTypeFieldsAssignment();
    fixture.detectChanges();
    component.enableDisableFieldsOnIsCloudSwithChange();
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      expect(component.basicConnectionForm.get('password').value).toBe('');
    });
  });

  it('should validate fields when Sonar connection selected', () => {
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Sonar';
    component.connection['vault'] = false;
    component.connection['cloudEnv'] = false;
    component.selectedConnectionType = 'Sonar';

    component.connectionTypeFieldsAssignment();
    fixture.detectChanges();
    component.enableDisableFieldsOnIsCloudSwithChange();
    fixture.detectChanges();
    fixture.whenStable().then(() => {
     expect(component.basicConnectionForm.get('accessToken').value).toBe('');
     });
  });

  it("Should enable field on accesstoken enabled",()=>{
    component.connection['accessTokenEnabled'] = true;
    component.enableDisableFieldsOnAccessTokenORPasswordToggle();
    fixture.detectChanges();
    expect(component.basicConnectionForm.controls['password'].value).toBe("")
  })

  it("Should enable/disable fields based on connection and selected connection ",()=>{
    component.basicConnectionForm.controls['isOAuth'].setValue("Any value")
    component.connection['isOAuth'] =true;
    component.defaultEnableDisableSwitch();
    fixture.detectChanges();
    expect(component.basicConnectionForm.controls['privateKey'].enabled).toBeTruthy();
    expect(component.basicConnectionForm.controls['consumerKey'].enabled).toBeTruthy();
  })

  it("Should enable/disable fields based on connection and selected connection ",()=>{
    component.basicConnectionForm.controls['isOAuth'].setValue("Any value")
    component.connection['isOAuth'] =false;
    component.defaultEnableDisableSwitch();
    fixture.detectChanges();
    expect(component.basicConnectionForm.controls['privateKey'].enabled).toBeFalsy();
    expect(component.basicConnectionForm.controls['consumerKey'].enabled).toBeFalsy();
  })

  it("Should enable/disable fields based on connection and selected connection",()=>{
    component.selectedConnectionType = "zephyr"
    component.connection['type'] = "zephyr"
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connectionTypeFieldsAssignment();
    component.basicConnectionForm.controls['cloudEnv'].setValue("Any value")
    component.connection['cloudEnv'] = true;
    component.defaultEnableDisableSwitch();
    fixture.detectChanges();
    expect(component.basicConnectionForm.controls['username'].enabled).toBeFalsy();
    expect(component.basicConnectionForm.controls['password'].enabled).toBeFalsy();
    expect(component.basicConnectionForm.controls['accessToken'].enabled).toBeTruthy();
  })


  it("Should enable/disable fields based on connection and selected connection",()=>{
    component.selectedConnectionType = "zephyr"
    component.connection['type'] = "zephyr"
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connectionTypeFieldsAssignment();
    component.basicConnectionForm.controls['cloudEnv'].setValue("Any value")
    component.connection['cloudEnv'] = false;
    component.defaultEnableDisableSwitch();
    fixture.detectChanges();
    expect(component.basicConnectionForm.controls['accessToken'].enabled).toBeFalsy();
    expect(component.basicConnectionForm.controls['password'].enabled).toBeTruthy();
  })

  it("Should enable/disable fields based on connection and selected connection",()=>{
    component.selectedConnectionType = "sonar"
    component.connection['type'] = "sonar"
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connectionTypeFieldsAssignment();
    component.basicConnectionForm.controls['cloudEnv'].setValue("Any value")
    component.connection['cloudEnv'] = true;
    component.defaultEnableDisableSwitch();
    fixture.detectChanges();
    expect(component.basicConnectionForm.controls['username'].enabled).toBeFalsy();
    expect(component.basicConnectionForm.controls['password'].enabled).toBeFalsy();
    expect(component.basicConnectionForm.controls['accessTokenEnabled'].enabled).toBeFalsy();

  })

  it("Should enable/disable fields based on connection and selected connection",()=>{
    component.selectedConnectionType = "sonar"
    component.connection['type'] = "sonar"
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connectionTypeFieldsAssignment();
    component.basicConnectionForm.controls['cloudEnv'].setValue("Any value")
    component.connection['cloudEnv'] = false;
    component.defaultEnableDisableSwitch();
    fixture.detectChanges();
    expect(component.basicConnectionForm.controls['username'].enabled).toBeTruthy();
    expect(component.basicConnectionForm.controls['password'].enabled).toBeTruthy();
    expect(component.basicConnectionForm.controls['accessTokenEnabled'].enabled).toBeTruthy();
  })

  it("Should enable/disable fields based on connection and selected connection defaultEnableDisableSwitch",()=>{
    component.selectedConnectionType = "sonar"
    component.connection['type'] = "sonar"
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connectionTypeFieldsAssignment();
    component.basicConnectionForm.controls['vault'].setValue("Any value")
    component.connection['vault'] = true;
    component.defaultEnableDisableSwitch();
    fixture.detectChanges();
    expect(component.basicConnectionForm.controls['password'].enabled).toBeFalse();
    expect(component.basicConnectionForm.controls['accessTokenEnabled'].enabled).toBeFalse();
  })


  

});
