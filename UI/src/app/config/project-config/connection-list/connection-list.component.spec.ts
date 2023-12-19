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
import { ConnectionListComponent } from './connection-list.component';
import { AppConfig, APP_CONFIG } from 'src/app/services/app.config';
import { ConfirmationService } from 'primeng/api';
import { environment } from 'src/environments/environment';
import { of } from 'rxjs';
import { TestConnectionService } from 'src/app/services/test-connection.service';
import { SharedService } from 'src/app/services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { DatePipe } from '@angular/common';

describe('ConnectionListComponent', () => {
  let component: ConnectionListComponent;
  let fixture: ComponentFixture<ConnectionListComponent>;
  let httpMock;
  let httpService;
  let sharedService;
  const baseUrl = environment.baseUrl;
  let testConnectionService;
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
      username: 'TESTADMIN',
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
        'Use bearer token',
        'PAT OAuthToken',
        'Is jaasKrbAuth',
        'Jaas Config FilePath',
        'Krb5 Config FilePath',
        'Jaas User',
        'Saml Endpoint',
        'Select Authentication Type'
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
        'bearerToken',
        'patOAuthToken',
        'jaasKrbAuth',
        'jaasConfigFilePath',
        'krb5ConfigFilePath',
        'jaasUser',
        'samlEndPoint',
        'jiraAuthType'
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
        'Use Bearer Token',
        'PatOAuthToken',
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
        'bearerToken',
        'patOAuthToken',
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
        'Use bearer token',
        'PAT (OAuth Token)',
        'Is jaasKrbAuth',
        'Jaas Config FilePath',
        'Krb5 Config FilePath',
        'Jaas User',
        'Saml Endpoint',
        'Select Authentication Type'
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
        'bearerToken',
        'patOAuthToken',
        'jaasKrbAuth',
        'jaasConfigFilePath',
        'krb5ConfigFilePath',
        'jaasUser',
        'samlEndPoint',
        'jiraAuthType'
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
        'Use Bearer Token',
        'PatOAuthToken',
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
        'bearerToken',
        'patOAuthToken',
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
      bearerToken: [
        {
          field: 'patOAuthToken',
          isEnabled: false
        }
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
      isCloneable:[
        {
          field: 'sshUrl',
          isEnabled: false
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
          field: 'bearerToken',
          isEnabled: true
        },
        {
          field: 'patOAuthToken',
          isEnabled: true
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
      bearerToken: [],
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
      isCloneable:[],
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
        SharedService,
        { provide: APP_CONFIG, useValue: AppConfig },
        HelperService,
        DatePipe
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectionListComponent);
    component = fixture.componentInstance;
    httpService = TestBed.inject(HttpService);
    sharedService = TestBed.inject(SharedService);
    httpMock = TestBed.inject(HttpTestingController);
    testConnectionService = TestBed.inject(TestConnectionService);
    fixture.detectChanges();
  });

  it('Should check whether component exist', () => {
    expect(component).toBeTruthy();
  });

  it('Should test 10 connections are loaded', () => {
    sharedService.setGlobalConfigData({repoToolFlag: true});
    let connTobeShown;
    const totalConnectionList = 13;
    if(component.repoToolsEnabled){
      connTobeShown = totalConnectionList - 4;
    }else{
      connTobeShown = totalConnectionList - 1;
    }
    expect(component.addEditConnectionFieldsNlabels.length).toEqual(connTobeShown);
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
      bearerToken:false,
      offline: false,
      password: undefined,
      pat: undefined,
      privateKey: undefined,
      type: 'Jira',
      username: undefined,
    };
    component.onChangeConnection();
    fixture.detectChanges();
    expect(component.selectedConnectionType).toBe(fakeEvent.type);
    expect(component.testConnectionMsg).toBe('');
  });

  it('should allow user to initialize new connection on click of "New Connection" button', () => {
    component.selectedConnectionType = 'Bitbucket';
    component.createConnection();
    fixture.detectChanges();
    expect(component.submitted).toBeFalse();
    expect(component.connectionDialog).toBeTrue();
    expect(component.isNewlyConfigAdded).toBeTrue();
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
    component.basicConnectionForm.controls['vault'].setValue(false);
    component.basicConnectionForm.controls['bearerToken'].setValue(false);
    component.basicConnectionForm.controls['privateKey'].disable();
    component.basicConnectionForm.controls['consumerKey'].disable();
    component.basicConnectionForm.controls['patOAuthToken'].disable();

    component.basicConnectionForm.controls['jaasKrbAuth'].disable();
    component.basicConnectionForm.controls['jaasConfigFilePath'].disable();
    component.basicConnectionForm.controls['krb5ConfigFilePath'].disable();
    component.basicConnectionForm.controls['jaasUser'].disable();
    component.basicConnectionForm.controls['samlEndPoint'].disable();
    component.basicConnectionForm.controls['jiraAuthType'].disable();
    component.isNewlyConfigAdded = true;
    const addConnection = spyOn(component, 'addConnectionReq');
    component.saveConnection();
    // fixture.detectChanges();
    expect(addConnection).toHaveBeenCalled();
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
      username: '',
      apiEndPoint: 'rest/api/2',
      isOAuth: false,
      bearerToken:false,
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
    expect(component.jiraConnectionDialog).toBeTrue();
    expect(component.isNewlyConfigAdded).toBeFalse();
    expect(component.selectedConnectionType).toBe('Jira');
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
    component.connection['patOAuthToken'] = '';
    component.connection['bearerToken'] = false;
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
    component.connection['patOAuthToken'] = '';
    component.connection['bearerToken'] = false;
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
    component.connection['patOAuthToken'] = '';
    component.connection['bearerToken'] = false;
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
    component.connection['patOAuthToken'] = '';
    component.connection['bearerToken'] = false;
    component.selectedConnectionType = 'zephyr';

    component.connectionTypeFieldsAssignment();
    fixture.detectChanges();
    component.checkZephyr();
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      expect(component.basicConnectionForm.get('password').value).toBe('');
    });
  });

  it('should be username,accesstoken blank when Sonar connection selected and cloudEnv switch is enabled', () => {
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

  it('should be password blank when Sonar connection selected and vault switch is enabled', () => {
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

  it('should be password blank when cloudEnv switch is true and sonar connection is selected', () => {
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

  it('should be accesstoken blank when cloudEnv switch is false and sonar connection is selected', () => {
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

  it("should be password blank when accessToken or password is toggling",()=>{
    component.connection['accessTokenEnabled'] = true;
    fixture.detectChanges();
    expect(component.basicConnectionForm.controls['password'].value).toBe("")
  })

  it("should be privateKey,consumerKey enabled when isOAuth switch is enabled",()=>{
    component.basicConnectionForm.controls['isOAuth'].setValue("Any value")
    component.connection['isOAuth'] =true;
    component.defaultEnableDisableSwitch();
    fixture.detectChanges();
    expect(component.basicConnectionForm.controls['privateKey'].enabled).toBeTruthy();
    expect(component.basicConnectionForm.controls['consumerKey'].enabled).toBeTruthy();
  })

  it("should be privateKey,consumerKey disabled when isOAuth switch is disabled",()=>{
    component.basicConnectionForm.controls['isOAuth'].setValue("Any value")
    component.connection['isOAuth'] =false;
    component.defaultEnableDisableSwitch();
    fixture.detectChanges();
    expect(component.basicConnectionForm.controls['privateKey'].enabled).toBeFalsy();
    expect(component.basicConnectionForm.controls['consumerKey'].enabled).toBeFalsy();
  });

  it("should be username,password disabled when selected connection is zephyr and cloudEnv switch is enabled",()=>{
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


  it("should be accessToken disabled  when selected connection is zephyr and cloudEnv switch is disabled",()=>{
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

  it("should be username,password disabled when selected connection is sonar and cloudEnv switch is enabled",()=>{
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

  it("should be username,password enabled when selected connection is sonar and cloudEnv switch is disabled",()=>{
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

  it("should be accessTokenEnabled,password  disabled when selected connection is sonar and vault switch is enabled",()=>{
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

  it("should be username disabled when selected connection is sonar and accessTokenEnabled switch is enabled", () => {
    component.selectedConnectionType = "sonar"
    component.connection['type'] = "sonar"
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connectionTypeFieldsAssignment();
    component.basicConnectionForm.controls['accessTokenEnabled'].setValue("Any value")
    component.connection['accessTokenEnabled'] = true;
    component.defaultEnableDisableSwitch();
    fixture.detectChanges();
    expect(component.basicConnectionForm.controls['password'].enabled).toBeFalse();
    expect(component.basicConnectionForm.controls['username'].enabled).toBeFalse();
    expect(component.basicConnectionForm.controls['accessToken'].enabled).toBeTruthy();
  })

  it("should be fields enable when checkbox is checked", () => {
    const fakeEvent = { originalEvent: { isTrusted: true }, checked: true };
    const field = 'offline';
    component.enableDisableOnToggle = enableDisableMatrix;
    component.enableDisableSwitch(fakeEvent, field);
    component.enableDisableOnToggle.enableDisableEachTime[field].forEach(
      (element) => {
        expect(
          component.basicConnectionForm.controls[element.field].enabled,
        ).toBeTruthy();
      },
    );
  })

  it("should be fields disabled when checkbox is unchecked", () => {
    const fakeEvent = { originalEvent: { isTrusted: true }, checked: false };
    const field = 'offline';
    component.enableDisableOnToggle = enableDisableMatrix;
    component.enableDisableSwitch(fakeEvent, field);
    component.enableDisableOnToggle.enableDisableEachTime[field].forEach(
      (element) => {
        expect(
          component.basicConnectionForm.controls[element.field].enabled,
        ).toBeFalsy();
      },
    );
  })


  it("should be privatekey enabled when isOAuth key is true", () => {
    const fakeEvent = { originalEvent: { isTrusted: true }, checked: false };
    const field = 'offline';
    component.basicConnectionForm.controls['isOAuth'].setValue(true);
    component.enableDisableOnToggle = enableDisableMatrix;
    component.enableDisableSwitch(fakeEvent, field);

    expect(
      component.basicConnectionForm.controls['privateKey'].enabled,
    ).toBeTruthy();
  })

  it("should be privatekey disabled when isOAuth key is false", () => {
    const fakeEvent = { originalEvent: { isTrusted: true }, checked: false };
    const field = 'offline';
    component.basicConnectionForm.controls['isOAuth'].setValue(false);
    component.enableDisableOnToggle = enableDisableMatrix;
    component.enableDisableSwitch(fakeEvent, field);

    expect(
      component.basicConnectionForm.controls['privateKey'].enabled,
    ).toBeFalsy();
  })


  it("should be disabled fields when field is cloudEnv and type is sonar and checkbox is checked", () => {
    const fakeEvent = { originalEvent: { isTrusted: true }, checked: true };
    const field = 'cloudEnv';
    const type = 'sonar'
    component.basicConnectionForm.controls['isOAuth'].setValue(false);
    component.enableDisableOnToggle = enableDisableMatrix;
    component.enableDisableSwitch(fakeEvent, field,type);
    component.enableDisableOnToggle.enableDisableEachTime[field].forEach(
      (element) => {
        expect(
          component.basicConnectionForm.controls[element.field].enabled,
        ).toBeFalsy();
      },
    );
  })

  it("should enable form control while testing connections",()=>{

    component.testingConnection = true;
    const reqData = {};
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Sonar';
    component.connection['vault'] = false;
    component.connection['cloudEnv'] = true;
    component.selectedConnectionType = 'Sonar';
    component.connectionTypeFieldsAssignment();
    component.testConnection();
    fixture.detectChanges();
    component.addEditConnectionFieldsNlabels.forEach(data => {
      if (!!component.connection.type && !!data.connectionType && (component.connection.type.toLowerCase() === data.connectionType.toLowerCase())) {
        data.inputFields.forEach(inputField => {
          if (component.basicConnectionForm.value[inputField] !== undefined && component.basicConnectionForm.value[inputField] !== '' && component.basicConnectionForm.value[inputField] !== 'undefined') {
            expect(reqData[inputField]).toEqual(component.basicConnectionForm.value[inputField])
          }
        });
      }
    });

  })

  it("should be disabled fields when field is cloudEnv and type is sonar and checkbox is unchecked ", () => {
    const fakeEvent = { originalEvent: { isTrusted: true }, checked: false };
    const field = 'cloudEnv';
    const type = 'sonar'
    component.basicConnectionForm.controls['isOAuth'].setValue(false);
    component.enableDisableOnToggle = enableDisableMatrix;
    component.enableDisableSwitch(fakeEvent, field,type);
    component.enableDisableOnToggle.enableDisableEachTime[field].forEach(
      (element) => {
        expect(
          component.basicConnectionForm.controls[element.field].enabled,
        ).toBeFalsy();
      },
    );
  })

  it("should empty url for Zephyr",()=>{
    component.selectedConnectionType = "zephyr"
    component.connection['type'] = "sonar"
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connectionTypeFieldsAssignment();
    component.basicConnectionForm.controls['type'].setValue("zephyr");
    component.basicConnectionForm.controls['cloudEnv'].setValue("any Value");
    component.basicConnectionForm.controls['baseUrl'].setValue("");
    component.emptyUrlInZephyr();
    expect(component.emptyUrlInZephyr()).toBeFalse();
  })



  it("should give success response, while testing for jira",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : "true",
      data : 200
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Jira';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testJira').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testJira).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Valid Connection");
    expect(component.testConnectionValid).toBeTruthy();
  })

  it("should give unsuccess response while testing for jira",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : false,
      data : 400
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Jira';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testJira').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testJira).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Connection Invalid");
    expect(component.testConnectionValid).toBeFalsy();
  })

  it("should give success response, while testing for Azure",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : "true",
      data : 200
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Azure';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testAzureBoards').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testAzureBoards).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Valid Connection");
    expect(component.testConnectionValid).toBeTruthy();
  })

  it("should give unsuccess response while testing for Azure",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : false,
      data : 400
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Azure';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testAzureBoards').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testAzureBoards).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Connection Invalid");
    expect(component.testConnectionValid).toBeFalsy();
  })

  it("should give success response, while testing for GitLab",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : "true",
      data : 200
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'GitLab';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testGitLab').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testGitLab).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Valid Connection");
    expect(component.testConnectionValid).toBeTruthy();
  })

  it("should empty url for Zephyr",()=>{
    component.selectedConnectionType = "zephyr"
    component.connection['type'] = "sonar"
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connectionTypeFieldsAssignment();
    component.basicConnectionForm.controls['type'].setValue("");
    component.basicConnectionForm.controls['cloudEnv'].setValue("any Value");
    component.basicConnectionForm.controls['baseUrl'].setValue("");
    component.emptyUrlInZephyr();
    expect(component.emptyUrlInZephyr()).toBeTruthy();
  })

  it("should give unsuccess response while testing for GitLab",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : false,
      data : 400
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'GitLab';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testGitLab').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testGitLab).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Connection Invalid");
    expect(component.testConnectionValid).toBeFalsy();
  })

  it("should give success response, while testing for Bitbucket",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : "true",
      data : 200
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Bitbucket';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testBitbucket').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testBitbucket).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Valid Connection");
    expect(component.testConnectionValid).toBeTruthy();
  })

  it("should give unsuccess response while testing for Bitbucket",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : false,
      data : 400
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Bitbucket';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testBitbucket').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testBitbucket).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Connection Invalid");
    expect(component.testConnectionValid).toBeFalsy();
  })

  it("should give success response, while testing for Sonar",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : "true",
      data : 200
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Sonar';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testSonar').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testSonar).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Valid Connection");
    expect(component.testConnectionValid).toBeTruthy();
  })

  it("should give unsuccess response while testing for Sonar",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : false,
      data : 400
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Sonar';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testSonar').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testSonar).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Connection Invalid");
    expect(component.testConnectionValid).toBeFalsy();
  })

  it("should give success response, while testing for Jenkins",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : "true",
      data : 200
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Jenkins';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testJenkins').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testJenkins).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Valid Connection");
    expect(component.testConnectionValid).toBeTruthy();
  })

  it("should give unsuccess response while testing for Jenkins",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : false,
      data : 400
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Jenkins';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testJenkins').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testJenkins).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Connection Invalid");
    expect(component.testConnectionValid).toBeFalsy();
  })

  it("should give success response, while testing for NewRelic",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : "true",
      data : 200
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'NewRelic';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testNewRelic').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testNewRelic).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Valid Connection");
    expect(component.testConnectionValid).toBeTruthy();
  })

  it("should give success response, while testing for Bamboo",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : "true",
      data : 200
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Bamboo';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testBamboo').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testBamboo).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Valid Connection");
    expect(component.testConnectionValid).toBeTruthy();
  })

  it("should give unsuccess response while testing for Bamboo",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : false,
      data : 400
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Bamboo';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testBamboo').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testBamboo).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Connection Invalid");
    expect(component.testConnectionValid).toBeFalsy();
  })

  it("should give success response, while testing for Teamcity",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : "true",
      data : 200
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Teamcity';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testTeamCity').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testTeamCity).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Valid Connection");
    expect(component.testConnectionValid).toBeTruthy();
  })

  it("should give unsuccess response while testing for Teamcity",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : false,
      data : 400
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Teamcity';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testTeamCity').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testTeamCity).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Connection Invalid");
    expect(component.testConnectionValid).toBeFalsy();
  })

  it("should give success response, while testing for AzurePipeline",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : "true",
      data : 200
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'AzurePipeline';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testAzurePipeline').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testAzurePipeline).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Valid Connection");
    expect(component.testConnectionValid).toBeTruthy();
  })

  it("should give unsuccess response while testing for AzurePipeline",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : false,
      data : 400
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'AzurePipeline';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testAzurePipeline').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testAzurePipeline).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Connection Invalid");
    expect(component.testConnectionValid).toBeFalsy();
  })

  it("should give success response, while testing for AzureRepository",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : "true",
      data : 200
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'AzureRepository';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testAzureRepository').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testAzureRepository).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Valid Connection");
    expect(component.testConnectionValid).toBeTruthy();
  })

  it("should give unsuccess response while testing for AzureRepository",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : false,
      data : 400
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'AzureRepository';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testAzureRepository').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testAzureRepository).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Connection Invalid");
    expect(component.testConnectionValid).toBeFalsy();
  })

  it("should give success response, while testing for GitHub",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : "true",
      data : 200
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'GitHub';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testGithub').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testGithub).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Valid Connection");
    expect(component.testConnectionValid).toBeTruthy();
  })

  it("should give unsuccess response while testing for GitHub",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : false,
      data : 400
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'GitHub';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testGithub').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testGithub).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Connection Invalid");
    expect(component.testConnectionValid).toBeFalsy();
  })

  it("should give success response, while testing for Zephyr",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : "true",
      data : 200
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Zephyr';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testZephyr').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testZephyr).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Valid Connection");
    expect(component.testConnectionValid).toBeTruthy();
  })

  it("should give unsuccess response while testing for Zephyr",()=>{
    component.testingConnection = true;
    const fakeResponse = {
      success : false,
      data : 400
    }
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Zephyr';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService,'testZephyr').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testZephyr).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe("Connection Invalid");
    expect(component.testConnectionValid).toBeFalsy();
  })

  it('should filter list based on flag',()=>{
    sharedService.setGlobalConfigData({repoToolFlag: true});
    component.ngOnInit();
    component.filterConnections(component.addEditConnectionFieldsNlabels,'connectionLabel')
    expect(component.addEditConnectionFieldsNlabels.length).toEqual(8);
  })

});
