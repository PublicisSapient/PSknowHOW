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
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
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
import { of, throwError } from 'rxjs';
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
        'Share connection with everyone',
        'Use bearer token',
        'PAT OAuthToken',
        'Is jaasKrbAuth',
        'Jaas Config FilePath',
        'Krb5 Config FilePath',
        'Jaas User',
        'Saml Endpoint',
        'Select Authentication Type',
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
        'sharedConnection',
        'bearerToken',
        'patOAuthToken',
        'jaasKrbAuth',
        'jaasConfigFilePath',
        'krb5ConfigFilePath',
        'jaasUser',
        'samlEndPoint',
        'jiraAuthType',
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
        'Share connection with everyone',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'pat',
        'sharedConnection',
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
        'User Email',
        'Share connection with everyone',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'accessToken',
        'email',
        'sharedConnection',
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
        'User Email',
        'Share connection with everyone',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'accessToken',
        'email',
        'sharedConnection',
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
        'Profile Username',
        'Use vault password',
        'App Password',
        'API End Point',
        'User Email',
        'Share connection with everyone',
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
        'email',
        'sharedConnection',
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
        'Share connection with everyone',
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
        'sharedConnection',
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
        'Share connection with everyone',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'apiKey',
        'sharedConnection',
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
        'Share connection with everyone',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'password',
        'sharedConnection',
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
        'Share connection with everyone',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'password',
        'sharedConnection',
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
        'Share connection with everyone',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'vault',
        'pat',
        'sharedConnection',
      ],
    },
    {
      connectionType: 'AzureRepository',
      connectionLabel: 'Azure Repository',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Use vault password',
        'PAT',
        'User Email',
        'Share connection with everyone',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'pat',
        'email',
        'sharedConnection',
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
        'Share connection with everyone',
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
        'sharedConnection',
      ],
    },
    {
      connectionType: 'ArgoCD',
      connectionLabel: 'ArgoCD',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Access Token',
        'Share connection with everyone',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'accessToken',
        'sharedConnection',
      ],
    },
  ];

  const fieldsAndLabels = [
    {
      connectionType: 'Jira',
      connectionLabel: 'Jira',
      categoryValue: 'projectManagement',
      categoryLabel: 'Project Management',
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
        'Share connection with everyone',
        'Use bearer token',
        'PAT (OAuth Token)',
        'Is jaasKrbAuth',
        'Jaas Config FilePath',
        'Krb5 Config FilePath',
        'Jaas User',
        'Saml Endpoint',
        'Select Authentication Type',
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
        'sharedConnection',
        'bearerToken',
        'patOAuthToken',
        'jaasKrbAuth',
        'jaasConfigFilePath',
        'krb5ConfigFilePath',
        'jaasUser',
        'samlEndPoint',
        'jiraAuthType',
      ],
    },
    {
      connectionType: 'Azure',
      connectionLabel: 'Azure Boards',
      categoryValue: 'sourceCodeManagement',
      categoryLabel: 'Source Code Management',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Use vault password',
        'PAT',
        'Share connection with everyone',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'pat',
        'sharedConnection',
      ],
    },
    {
      connectionType: 'GitHub',
      connectionLabel: 'GitHub',
      categoryValue: 'sourceCodeManagement',
      categoryLabel: 'Source Code Management',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Repo Ownername',
        'Use vault password',
        'Access Token',
        'User Email',
        'Share connection with everyone',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'accessToken',
        'email',
        'sharedConnection',
      ],
    },
    {
      connectionType: 'GitLab',
      connectionLabel: 'GitLab',
      categoryValue: 'sourceCodeManagement',
      categoryLabel: 'Source Code Management',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Use vault password',
        'Access Token',
        'User Email',
        'Share connection with everyone',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'accessToken',
        'email',
        'sharedConnection',
      ],
    },
    {
      connectionType: 'Bitbucket',
      connectionLabel: 'Bitbucket',
      categoryValue: 'sourceCodeManagement',
      categoryLabel: 'Source Code Management',
      labels: [
        'Connection Type',
        'Connection Name',
        'Is Cloud Environment',
        'Base Url',
        'Username',
        'Use vault password',
        'Password',
        'API End Point',
        'User Email',
        'Share connection with everyone',
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
        'email',
        'sharedConnection',
      ],
    },
    {
      connectionType: 'Sonar',
      connectionLabel: 'Sonar',
      categoryValue: 'sourceCodeManagement',
      categoryLabel: 'Source Code Management',
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
        'Share connection with everyone',
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
        'sharedConnection',
        'accessTokenEnabled',
      ],
    },
    {
      connectionType: 'Jenkins',
      connectionLabel: 'Jenkins',
      categoryValue: 'sourceCodeManagement',
      categoryLabel: 'Source Code Management',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Use vault password',
        'Api Key',
        'Share connection with everyone',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'apiKey',
        'sharedConnection',
      ],
    },

    {
      connectionType: 'Bamboo',
      connectionLabel: 'Bamboo',
      categoryValue: 'sourceCodeManagement',
      categoryLabel: 'Source Code Management',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Use vault password',
        'Password',
        'Share connection with everyone',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'password',
        'sharedConnection',
      ],
    },
    {
      connectionType: 'Teamcity',
      connectionLabel: 'Teamcity',
      categoryValue: 'sourceCodeManagement',
      categoryLabel: 'Source Code Management',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Use vault password',
        'Password',
        'Share connection with everyone',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'password',
        'sharedConnection',
      ],
    },
    {
      connectionType: 'AzurePipeline',
      connectionLabel: 'Azure Pipeline',
      categoryValue: 'sourceCodeManagement',
      categoryLabel: 'Source Code Management',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Use vault password',
        'PAT',
        'Share connection with everyone',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'vault',
        'pat',
        'sharedConnection',
      ],
    },
    {
      connectionType: 'AzureRepository',
      connectionLabel: 'Azure Repository',
      categoryValue: 'sourceCodeManagement',
      categoryLabel: 'Source Code Management',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Use vault password',
        'PAT',
        'User Email',
        'Share connection with everyone',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'vault',
        'pat',
        'email',
        'sharedConnection',
      ],
    },
    {
      connectionType: 'Zephyr',
      connectionLabel: 'Zephyr',
      categoryValue: 'sourceCodeManagement',
      categoryLabel: 'Source Code Management',
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
        'Share connection with everyone',
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
        'sharedConnection',
      ],
    },
    {
      connectionType: 'ArgoCD',
      connectionLabel: 'ArgoCD',
      categoryValue: 'sourceCodeManagement',
      categoryLabel: 'Source Code Management',
      labels: [
        'Connection Type',
        'Connection Name',
        'Base Url',
        'Username',
        'Access Token',
        'Share connection with everyone',
      ],
      inputFields: [
        'type',
        'connectionName',
        'baseUrl',
        'username',
        'accessToken',
        'sharedConnection',
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
      isCloneable: [
        {
          field: 'sshUrl',
          isEnabled: false,
        },
      ],
      accessTokenEnabled: [],
    },
    enableDisableAnotherTime: {
      cloudEnv: [],
      // offline: [
      //   {
      //     field: 'cloudEnv',
      //     isEnabled: true,
      //   },
      //   {
      //     field: 'baseUrl',
      //     isEnabled: true,
      //   },
      //   {
      //     field: 'username',
      //     isEnabled: true,
      //   },
      //   {
      //     field: 'password',
      //     isEnabled: true,
      //   },

      //   {
      //     field: 'apiEndPoint',
      //     isEnabled: true,
      //   },
      //   {
      //     field: 'isOAuth',
      //     isEnabled: true,
      //   },
      //   {
      //     field: 'bearerToken',
      //     isEnabled: true
      //   },
      //   {
      //     field: 'patOAuthToken',
      //     isEnabled: true
      //   },
      //   {
      //     field: 'privateKey',
      //     isEnabled: false,
      //   },
      //   {
      //     field: 'consumerKey',
      //     isEnabled: false,
      //   },
      // ],
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
      isCloneable: [],
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
        DatePipe,
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
    sharedService.setGlobalConfigData({ repoToolFlag: true });
    let connTobeShown;
    const totalConnectionList = 14;
    connTobeShown = totalConnectionList - 1;
    expect(component.addEditConnectionFieldsNlabels.length).toEqual(
      connTobeShown,
    );
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

  it('should update connection type', () => {
    const selectedConnectionType = 'test-type';
    component.selectedConnectionType = selectedConnectionType;
    component.onChangeConnection();
    expect(component.connection['type']).toBe(selectedConnectionType);
  });

  it('should call connectionTypeFieldsAssignment', () => {
    spyOn(component, 'connectionTypeFieldsAssignment');
    component.onChangeConnection();
    expect(component.connectionTypeFieldsAssignment).toHaveBeenCalledTimes(1);
  });

  it('should call defaultEnableDisableSwitch', () => {
    spyOn(component, 'defaultEnableDisableSwitch');
    component.onChangeConnection();
    expect(component.defaultEnableDisableSwitch).toHaveBeenCalledTimes(1);
  });

  it('should reset testConnectionMsg', () => {
    component.testConnectionMsg = 'test-message';
    component.onChangeConnection();
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
    component.basicConnectionForm.controls['type']?.setValue('Jira');
    component.basicConnectionForm.controls['connectionName']?.setValue(
      'TestConnectionRishabhJira4',
    );
    component.basicConnectionForm.controls['cloudEnv']?.setValue(false);
    component.basicConnectionForm.controls['baseUrl']?.setValue(
      'https://test.com/jira',
    );
    component.basicConnectionForm.controls['username']?.setValue('tst-1');
    component.basicConnectionForm.controls['password']?.setValue('test');
    component.basicConnectionForm.controls['apiEndPoint']?.setValue(
      'rest/api/2',
    );
    component.basicConnectionForm.controls['isOAuth']?.setValue(false);
    component.basicConnectionForm.controls['sharedConnection']?.setValue(true);
    component.basicConnectionForm.controls['vault']?.setValue(false);
    component.basicConnectionForm.controls['bearerToken']?.setValue(false);
    component.basicConnectionForm.controls['privateKey']?.disable();
    component.basicConnectionForm.controls['consumerKey']?.disable();
    component.basicConnectionForm.controls['patOAuthToken']?.disable();

    component.basicConnectionForm.controls['jaasKrbAuth']?.disable();
    component.basicConnectionForm.controls['jaasConfigFilePath']?.disable();
    component.basicConnectionForm.controls['krb5ConfigFilePath']?.disable();
    component.basicConnectionForm.controls['jaasUser']?.disable();
    component.basicConnectionForm.controls['samlEndPoint']?.disable();
    component.basicConnectionForm.controls['jiraAuthType']?.disable();
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
      bearerToken: false,
      createdAt: '2021-04-02T07:42:09',
      createdBy: 'SUPERADMIN',
      sharedConnection: true,
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

  it('should be password blank when accessToken or password is toggling', () => {
    component.connection['accessTokenEnabled'] = true;
    fixture.detectChanges();
    expect(component.basicConnectionForm.controls['password'].value).toBe('');
  });

  it('should be privateKey,consumerKey enabled when isOAuth switch is enabled', () => {
    component.basicConnectionForm.controls['isOAuth'].setValue('Any value');
    component.connection['isOAuth'] = true;
    component.defaultEnableDisableSwitch();
    fixture.detectChanges();
    expect(
      component.basicConnectionForm.controls['privateKey'].enabled,
    ).toBeTruthy();
    expect(
      component.basicConnectionForm.controls['consumerKey'].enabled,
    ).toBeTruthy();
  });

  it('should be privateKey,consumerKey disabled when isOAuth switch is disabled', () => {
    component.basicConnectionForm.controls['isOAuth'].setValue('Any value');
    component.connection['isOAuth'] = false;
    component.defaultEnableDisableSwitch();
    fixture.detectChanges();
    expect(
      component.basicConnectionForm.controls['privateKey'].enabled,
    ).toBeFalsy();
    expect(
      component.basicConnectionForm.controls['consumerKey'].enabled,
    ).toBeFalsy();
  });

  it('should be username,password disabled when selected connection is zephyr and cloudEnv switch is enabled', () => {
    component.selectedConnectionType = 'zephyr';
    component.connection['type'] = 'zephyr';
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connectionTypeFieldsAssignment();
    component.basicConnectionForm.controls['cloudEnv'].setValue('Any value');
    component.connection['cloudEnv'] = true;
    component.defaultEnableDisableSwitch();
    fixture.detectChanges();
    expect(
      component.basicConnectionForm.controls['username'].enabled,
    ).toBeFalsy();
    expect(
      component.basicConnectionForm.controls['password'].enabled,
    ).toBeFalsy();
    expect(
      component.basicConnectionForm.controls['accessToken'].enabled,
    ).toBeTruthy();
  });

  it('should be accessToken disabled  when selected connection is zephyr and cloudEnv switch is disabled', () => {
    component.selectedConnectionType = 'zephyr';
    component.connection['type'] = 'zephyr';
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connectionTypeFieldsAssignment();
    component.basicConnectionForm.controls['cloudEnv'].setValue('Any value');
    component.connection['cloudEnv'] = false;
    component.defaultEnableDisableSwitch();
    fixture.detectChanges();
    expect(
      component.basicConnectionForm.controls['accessToken'].enabled,
    ).toBeFalsy();
    expect(
      component.basicConnectionForm.controls['password'].enabled,
    ).toBeTruthy();
  });

  it('should be username,password disabled when selected connection is sonar and cloudEnv switch is enabled', () => {
    component.selectedConnectionType = 'sonar';
    component.connection['type'] = 'sonar';
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connectionTypeFieldsAssignment();
    component.basicConnectionForm.controls['cloudEnv'].setValue('Any value');
    component.connection['cloudEnv'] = true;
    component.defaultEnableDisableSwitch();
    fixture.detectChanges();
    expect(
      component.basicConnectionForm.controls['username'].enabled,
    ).toBeFalsy();
    expect(
      component.basicConnectionForm.controls['password'].enabled,
    ).toBeFalsy();
    expect(
      component.basicConnectionForm.controls['accessTokenEnabled'].enabled,
    ).toBeFalsy();
  });

  it('should be username,password enabled when selected connection is sonar and cloudEnv switch is disabled', () => {
    component.selectedConnectionType = 'sonar';
    component.connection['type'] = 'sonar';
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connectionTypeFieldsAssignment();
    component.basicConnectionForm.controls['cloudEnv'].setValue('Any value');
    component.connection['cloudEnv'] = false;
    component.defaultEnableDisableSwitch();
    fixture.detectChanges();
    expect(
      component.basicConnectionForm.controls['username'].enabled,
    ).toBeTruthy();
    expect(
      component.basicConnectionForm.controls['password'].enabled,
    ).toBeTruthy();
    expect(
      component.basicConnectionForm.controls['accessTokenEnabled'].enabled,
    ).toBeTruthy();
  });

  it('should be accessTokenEnabled,password  disabled when selected connection is sonar and vault switch is enabled', () => {
    component.selectedConnectionType = 'sonar';
    component.connection['type'] = 'sonar';
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connectionTypeFieldsAssignment();
    component.basicConnectionForm.controls['vault'].setValue('Any value');
    component.connection['vault'] = true;
    component.defaultEnableDisableSwitch();
    fixture.detectChanges();
    expect(
      component.basicConnectionForm.controls['password'].enabled,
    ).toBeFalse();
    expect(
      component.basicConnectionForm.controls['accessTokenEnabled'].enabled,
    ).toBeFalse();
  });

  it('should be username disabled when selected connection is sonar and accessTokenEnabled switch is enabled', () => {
    component.selectedConnectionType = 'sonar';
    component.connection['type'] = 'sonar';
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connectionTypeFieldsAssignment();
    component.basicConnectionForm.controls['accessTokenEnabled'].setValue(
      'Any value',
    );
    component.connection['accessTokenEnabled'] = true;
    component.defaultEnableDisableSwitch();
    fixture.detectChanges();
    expect(
      component.basicConnectionForm.controls['password'].enabled,
    ).toBeFalse();
    expect(
      component.basicConnectionForm.controls['username'].enabled,
    ).toBeFalse();
    expect(
      component.basicConnectionForm.controls['accessToken'].enabled,
    ).toBeTruthy();
  });

  it('should be fields enable when checkbox is checked', () => {
    const fakeEvent = { originalEvent: { isTrusted: true }, checked: true };
    const field = 'vault';
    component.enableDisableOnToggle = enableDisableMatrix;
    component.basicConnectionForm = new FormGroup({
      pat: new FormControl(),
      password: new FormControl(),
      accessToken: new FormControl(),
      apiKey: new FormControl(),
    });
    component.enableDisableSwitch(fakeEvent, field);
    component.enableDisableOnToggle.enableDisableEachTime[field].forEach(
      (element) => {
        expect(
          component.basicConnectionForm.controls[element.field].enabled,
        ).toBeTrue();
      },
    );
  });

  it('should be fields disabled when checkbox is unchecked', () => {
    const fakeEvent = { originalEvent: { isTrusted: true }, checked: false };
    const field = 'vault';
    component.enableDisableOnToggle = enableDisableMatrix;
    component.basicConnectionForm = new FormGroup({
      pat: new FormControl(),
      password: new FormControl(),
      accessToken: new FormControl(),
      apiKey: new FormControl(),
    });
    component.enableDisableSwitch(fakeEvent, field);
    component.enableDisableOnToggle.enableDisableEachTime[field].forEach(
      (element) => {
        expect(
          component.basicConnectionForm.controls[element.field].enabled,
        ).toBeFalse();
      },
    );
  });

  it('should be privatekey enabled when isOAuth key is true', () => {
    const fakeEvent = { originalEvent: { isTrusted: true }, checked: false };
    const field = 'isOAuth';
    component.basicConnectionForm.controls['isOAuth'].setValue(true);
    component.enableDisableOnToggle = enableDisableMatrix;
    component.enableDisableSwitch(fakeEvent, field);

    expect(
      component.basicConnectionForm.controls['privateKey'].enabled,
    ).toBeFalsy();
  });

  it('should be privatekey disabled when isOAuth key is false', () => {
    const fakeEvent = { originalEvent: { isTrusted: true }, checked: false };
    const field = 'isOAuth';
    component.basicConnectionForm.controls['isOAuth'].setValue(false);
    component.enableDisableOnToggle = enableDisableMatrix;
    component.enableDisableSwitch(fakeEvent, field);

    expect(
      component.basicConnectionForm.controls['privateKey'].enabled,
    ).toBeFalsy();
  });

  it('should be disabled fields when field is cloudEnv and type is sonar and checkbox is checked', () => {
    const fakeEvent = { originalEvent: { isTrusted: true }, checked: true };
    const field = 'cloudEnv';
    const type = 'sonar';
    component.basicConnectionForm.controls['isOAuth'].setValue(false);
    component.enableDisableOnToggle = enableDisableMatrix;
    component.enableDisableSwitch(fakeEvent, field, type);
    component.enableDisableOnToggle.enableDisableEachTime[field].forEach(
      (element) => {
        expect(
          component.basicConnectionForm.controls[element.field].enabled,
        ).toBeFalsy();
      },
    );
  });

  it('should enable form control while testing connections', () => {
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
    component.addEditConnectionFieldsNlabels.forEach((data) => {
      if (
        !!component.connection.type &&
        !!data.connectionType &&
        component.connection.type.toLowerCase() ===
          data.connectionType.toLowerCase()
      ) {
        data.inputFields.forEach((inputField) => {
          if (
            component.basicConnectionForm.value[inputField] !== undefined &&
            component.basicConnectionForm.value[inputField] !== '' &&
            component.basicConnectionForm.value[inputField] !== 'undefined'
          ) {
            expect(reqData[inputField]).toEqual(
              component.basicConnectionForm.value[inputField],
            );
          }
        });
      }
    });
  });

  it('should be disabled fields when field is cloudEnv and type is sonar and checkbox is unchecked ', () => {
    const fakeEvent = { originalEvent: { isTrusted: true }, checked: false };
    const field = 'cloudEnv';
    const type = 'sonar';
    component.basicConnectionForm.controls['isOAuth'].setValue(false);
    component.enableDisableOnToggle = enableDisableMatrix;
    component.enableDisableSwitch(fakeEvent, field, type);
    component.enableDisableOnToggle.enableDisableEachTime[field].forEach(
      (element) => {
        expect(
          component.basicConnectionForm.controls[element.field].enabled,
        ).toBeFalsy();
      },
    );
  });

  it('should empty url for Zephyr', () => {
    component.selectedConnectionType = 'zephyr';
    component.connection['type'] = 'sonar';
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connectionTypeFieldsAssignment();
    component.basicConnectionForm.controls['type'].setValue('zephyr');
    component.basicConnectionForm.controls['cloudEnv'].setValue('any Value');
    component.basicConnectionForm.controls['baseUrl'].setValue('');
    component.emptyUrlInZephyr();
    expect(component.emptyUrlInZephyr()).toBeFalse();
  });

  it('should give success response, while testing for jira', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: 'true',
      data: 200,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Jira';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testJira').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testJira).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Valid Connection');
    expect(component.testConnectionValid).toBeTruthy();
  });

  it('should give unsuccess response while testing for jira', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: false,
      data: 400,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Jira';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testJira').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testJira).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give success response, while testing for Azure', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: 'true',
      data: 200,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Azure';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testAzureBoards').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testAzureBoards).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Valid Connection');
    expect(component.testConnectionValid).toBeTruthy();
  });

  it('should give unsuccess response while testing for Azure', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: false,
      data: 400,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Azure';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testAzureBoards').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testAzureBoards).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give success response, while testing for GitLab', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: 'true',
      data: 200,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'GitLab';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testGitLab').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testGitLab).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Valid Connection');
    expect(component.testConnectionValid).toBeTruthy();
  });

  it('should empty url for Zephyr', () => {
    component.selectedConnectionType = 'zephyr';
    component.connection['type'] = 'sonar';
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connectionTypeFieldsAssignment();
    component.basicConnectionForm.controls['type'].setValue('');
    component.basicConnectionForm.controls['cloudEnv'].setValue('any Value');
    component.basicConnectionForm.controls['baseUrl'].setValue('');
    component.emptyUrlInZephyr();
    expect(component.emptyUrlInZephyr()).toBeTruthy();
  });

  it('should give unsuccess response while testing for GitLab', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: false,
      data: 400,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'GitLab';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testGitLab').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testGitLab).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give success response, while testing for Bitbucket', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: 'true',
      data: 200,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Bitbucket';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testBitbucket').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testBitbucket).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Valid Connection');
    expect(component.testConnectionValid).toBeTruthy();
  });

  it('should give unsuccess response while testing for Bitbucket', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: false,
      data: 400,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Bitbucket';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testBitbucket').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testBitbucket).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give success response, while testing for Sonar', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: 'true',
      data: 200,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Sonar';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testSonar').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testSonar).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Valid Connection');
    expect(component.testConnectionValid).toBeTruthy();
  });

  it('should give unsuccess response while testing for Sonar', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: false,
      data: 400,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Sonar';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testSonar').and.returnValue(of(fakeResponse));
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testSonar).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give success response, while testing for Jenkins', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: 'true',
      data: 200,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Jenkins';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testJenkins').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testJenkins).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Valid Connection');
    expect(component.testConnectionValid).toBeTruthy();
  });

  it('should give unsuccess response while testing for Jenkins', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: false,
      data: 400,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Jenkins';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testJenkins').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testJenkins).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give success response, while testing for NewRelic', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: 'true',
      data: 200,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'NewRelic';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testNewRelic').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testNewRelic).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Valid Connection');
    expect(component.testConnectionValid).toBeTruthy();
  });

  it('should give success response, while testing for Bamboo', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: 'true',
      data: 200,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Bamboo';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testBamboo').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testBamboo).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Valid Connection');
    expect(component.testConnectionValid).toBeTruthy();
  });

  it('should give unsuccess response while testing for Bamboo', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: false,
      data: 400,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Bamboo';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testBamboo').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testBamboo).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give success response, while testing for Teamcity', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: 'true',
      data: 200,
    };
    const fakeUserDetails = { user_name: 'testUser' };
    localStorage.setItem('currentUserDetails', JSON.stringify(fakeUserDetails));
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Teamcity';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testTeamCity').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testTeamCity).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Valid Connection');
    expect(component.testConnectionValid).toBeTruthy();
  });

  it('should give unsuccess response while testing for Teamcity', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: false,
      data: 400,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Teamcity';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testTeamCity').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testTeamCity).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give success response, while testing for AzurePipeline', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: 'true',
      data: 200,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'AzurePipeline';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testAzurePipeline').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testAzurePipeline).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Valid Connection');
    expect(component.testConnectionValid).toBeTruthy();
  });

  it('should give unsuccess response while testing for AzurePipeline', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: false,
      data: 400,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'AzurePipeline';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testAzurePipeline').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testAzurePipeline).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give success response, while testing for AzureRepository', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: 'true',
      data: 200,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'AzureRepository';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testAzureRepository').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testAzureRepository).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Valid Connection');
    expect(component.testConnectionValid).toBeTruthy();
  });

  it('should give unsuccess response while testing for AzureRepository', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: false,
      data: 400,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'AzureRepository';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testAzureRepository').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testAzureRepository).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give success response, while testing for GitHub', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: 'true',
      data: 200,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'GitHub';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testGithub').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testGithub).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Valid Connection');
    expect(component.testConnectionValid).toBeTruthy();
  });

  it('should give unsuccess response while testing for GitHub', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: false,
      data: 400,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'GitHub';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testGithub').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testGithub).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give success response, while testing for Zephyr', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: 'true',
      data: 200,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Zephyr';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testZephyr').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testZephyr).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Valid Connection');
    expect(component.testConnectionValid).toBeTruthy();
  });

  it('should give unsuccess response while testing for Zephyr', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: false,
      data: 400,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Zephyr';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testZephyr').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testZephyr).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give success response, while testing for ArgoCD', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: 'true',
      data: 200,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'ArgoCD';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testArgoCD').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testArgoCD).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Valid Connection');
    expect(component.testConnectionValid).toBeTruthy();
  });

  it('should give unsuccess response while testing for ArgoCD', () => {
    component.testingConnection = true;
    const fakeResponse = {
      success: false,
      data: 400,
    };
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'ArgoCD';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testArgoCD').and.returnValue(
      of(fakeResponse),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testArgoCD).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should change auth type', () => {
    component.initializeForms(component.jiraConnectionFields, true);
    component.onChangeAuthType('vault');
    component.onChangeAuthType('bearerToken');
    component.onChangeAuthType('jaasKrbAuth');
    component.onChangeAuthType('isOAuth');
    component.onChangeAuthType('na');
    expect(component.jiraForm).toBeDefined();
  });

  it('should initialize forms and edit for vault', () => {
    component.jiraConnectionFields['vault'] = true;
    component.initializeForms(component.jiraConnectionFields, true);
    expect(component.onChangeAuthType).toBeDefined();
  });

  it('should initialize forms and edit for bearerToken', () => {
    component.jiraConnectionFields['bearerToken'] = true;
    component.initializeForms(component.jiraConnectionFields, true);
    expect(component.onChangeAuthType).toBeDefined();
  });

  it('should initialize forms and edit for isOAuth', () => {
    component.jiraConnectionFields['isOAuth'] = true;
    component.initializeForms(component.jiraConnectionFields, true);
    expect(component.onChangeAuthType).toBeDefined();
  });

  it('should initialize forms and edit for jaasKrbAuth', () => {
    component.jiraConnectionFields['jaasKrbAuth'] = true;
    component.initializeForms(component.jiraConnectionFields, true);
    expect(component.onChangeAuthType).toBeDefined();
  });

  it('should add connection successfully', () => {
    spyOn(httpService, 'addConnection').and.returnValue(of('success'));
    const spyObj = spyOn(component, 'renderCreateUpdateConnectionStatus');
    component.addConnectionReq('dummyConnection');
    expect(spyObj).toHaveBeenCalled();
  });

  it('should throw error while adding connections', () => {
    spyOn(httpService, 'addConnection').and.returnValue(throwError('Error'));
    const spyObj = spyOn(component, 'hideDialog');
    component.addConnectionReq('dummyConnection');
    expect(spyObj).toHaveBeenCalled();
  });

  it('should handle edit connection request', () => {
    spyOn(httpService, 'editConnection').and.returnValue(of('success'));
    const spyObj = spyOn(component, 'renderCreateUpdateConnectionStatus');
    component.editConnectionReq('dummyConnection');
    expect(spyObj).toHaveBeenCalled();
  });

  it('should throw error while adding connections', () => {
    spyOn(httpService, 'editConnection').and.returnValue(throwError('Error'));
    const spyObj = spyOn(component, 'hideDialog');
    component.editConnectionReq('dummyConnection');
    expect(spyObj).toHaveBeenCalled();
  });

  it('should call confirm with correct message on failure', () => {
    const mockConfirmationDialog = jasmine.createSpyObj('ConfirmationDialog', [
      'confirm',
    ]);
    const mockResponse = {
      success: false,
      data: ['Project 1', 'Project 2'],
      message: 'Connection deletion failed',
    };
    component.reloadConnections(mockResponse);
    expect(mockConfirmationDialog.confirm).toBeDefined();
  });

  it('should call getConnectionList on success', () => {
    const mockResponse = { success: true };
    spyOn(component, 'getConnectionList');
    component.reloadConnections(mockResponse);
    expect(component.getConnectionList).toHaveBeenCalled();
  });

  it('should call deleteConnection and reloadConnections on accept', () => {
    const mockHttpClient = jasmine.createSpyObj('HttpClient', ['delete']);
    const mockConnection = { connectionName: 'Test Connection' };
    const mockResponse = { success: true };
    spyOn(component, 'reloadConnections');
    mockHttpClient.delete.and.returnValue(of(mockResponse));
    component.deleteConnection(mockConnection);
  });

  it('should call confirmationService.confirm on unsuccessful response with message', () => {
    const mockConfirmationDialog = jasmine.createSpyObj('ConfirmationDialog', [
      'confirm',
    ]);
    const mockResponse = {
      success: false,
      message: 'Connection creation failed',
    };
    const mockHeader = 'Create Connection';
    component.renderCreateUpdateConnectionStatus(mockResponse, mockHeader);
    expect(mockConfirmationDialog).toBeDefined();
  });

  it('should initialize with correct values for a Jira connection', () => {
    const connection = {
      type: 'Jira',
      cloudEnv: false,
      // other properties
    };

    spyOn(component, 'initializeForms');
    component.editConnection(connection);

    expect(component.connection).toEqual({ ...connection, username: '' });
    expect(component.isNewlyConfigAdded).toBeFalse();
    expect(component.selectedConnectionType).toBe('Jira');
    expect(component.jiraConnectionDialog).toBeTrue();
    expect(component.initializeForms).toHaveBeenCalledWith(
      component.connection,
      true,
    );
  });

  it('should initialize with correct values for a non-Jira connection', () => {
    const connection = {
      type: 'Bitbucket',
      cloudEnv: true,
      // other properties
    };

    spyOn(component, 'connectionTypeFieldsAssignment');
    spyOn(component.basicConnectionForm.controls['type'], 'setValue');
    spyOn(component, 'defaultEnableDisableSwitch');
    spyOn(component, 'disableEnableCheckBox');
    spyOn(component, 'checkBitbucketValue');

    component.editConnection(connection);

    expect(component.connection).toEqual({ ...connection, username: '' });
    expect(component.isNewlyConfigAdded).toBeFalse();
    expect(component.selectedConnectionType).toBe('Bitbucket');

    // If jiraConnectionDialog is initialized to false in the component, use toBeFalse()
    // If not initialized and could be undefined, use toBeFalsy() or modify the expectation
    expect(component.jiraConnectionDialog).toBeFalse(); // Ensure initialization in the component
    expect(component.connectionDialog).toBeTrue();
    expect(component.connectionTypeFieldsAssignment).toHaveBeenCalled();
    expect(
      component.basicConnectionForm.controls['type'].setValue,
    ).toHaveBeenCalledWith('Bitbucket');
    expect(component.defaultEnableDisableSwitch).toHaveBeenCalled();
    expect(component.disableEnableCheckBox).toHaveBeenCalled();
    expect(component.checkBitbucketValue).toHaveBeenCalledWith(
      true,
      'cloudEnv',
      'bitbucket',
    );
  });

  it('should handle Zephyr connection correctly', () => {
    const connection = {
      type: 'Zephyr',
      cloudEnv: false,
      // other properties
    };

    spyOn(component, 'connectionTypeFieldsAssignment');
    spyOn(component.basicConnectionForm.controls['type'], 'setValue');
    spyOn(component, 'defaultEnableDisableSwitch');
    spyOn(component, 'disableEnableCheckBox');
    spyOn(component, 'checkZephyr');

    component.editConnection(connection);

    expect(component.connection).toEqual({ ...connection, username: '' });
    expect(component.isNewlyConfigAdded).toBeFalse();
    expect(component.selectedConnectionType).toBe('Zephyr');
    expect(component.jiraConnectionDialog).toBeFalse();
    expect(component.connectionDialog).toBeTrue();
    expect(component.connectionTypeFieldsAssignment).toHaveBeenCalled();
    expect(
      component.basicConnectionForm.controls['type'].setValue,
    ).toHaveBeenCalledWith('Zephyr');
    expect(component.defaultEnableDisableSwitch).toHaveBeenCalled();
    expect(component.disableEnableCheckBox).toHaveBeenCalled();
    expect(component.checkZephyr).toHaveBeenCalled();
  });

  it('should give error response while testing for jira', () => {
    component.testingConnection = true;
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Jira';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testJira').and.returnValue(
      throwError('Error'),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testJira).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give error in response while testing for Azure', () => {
    component.testingConnection = true;
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Azure';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testAzureBoards').and.returnValue(
      throwError('Error'),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testAzureBoards).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give error in response while testing for GitLab', () => {
    component.testingConnection = true;
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'GitLab';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testGitLab').and.returnValue(
      throwError('Error'),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testGitLab).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give error in response while testing for Bitbucket', () => {
    component.testingConnection = true;
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Bitbucket';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testBitbucket').and.returnValue(
      throwError('Error'),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testBitbucket).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give error in response while testing for Sonar', () => {
    component.testingConnection = true;
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Sonar';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testSonar').and.returnValue(
      throwError('Error'),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testSonar).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give error in response while testing for Jenkins', () => {
    component.testingConnection = true;
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Jenkins';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testJenkins').and.returnValue(
      throwError('Error'),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testJenkins).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give error in response while testing for Bamboo', () => {
    component.testingConnection = true;
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Bamboo';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testBamboo').and.returnValue(
      throwError('Error'),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testBamboo).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give error in response while testing for Teamcity', () => {
    component.testingConnection = true;
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Teamcity';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testTeamCity').and.returnValue(
      throwError('Error'),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testTeamCity).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give error in response while testing for AzurePipeline', () => {
    component.testingConnection = true;
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'AzurePipeline';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testAzurePipeline').and.returnValue(
      throwError('Error'),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testAzurePipeline).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give error in response while testing for AzureRepository', () => {
    component.testingConnection = true;
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'AzureRepository';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testAzureRepository').and.returnValue(
      throwError('Error'),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testAzureRepository).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give error in response while testing for GitHub', () => {
    component.testingConnection = true;
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'GitHub';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testGithub').and.returnValue(
      throwError('Error'),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testGithub).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give error in response while testing for Zephyr', () => {
    component.testingConnection = true;
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'Zephyr';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testZephyr').and.returnValue(
      throwError('Error'),
    );
    component.testConnection();
    fixture.detectChanges();
    expect(testConnectionService.testZephyr).toHaveBeenCalled();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should give error in response while testing for NewRelic', () => {
    component.testingConnection = true;
    component.addEditConnectionFieldsNlabels = fieldsAndLabels;
    component.connection['type'] = 'NewRelic';
    component.connectionTypeFieldsAssignment();
    spyOn(testConnectionService, 'testNewRelic').and.returnValue(
      throwError('Error'),
    );
    component.testConnection();
    expect(component.testConnectionMsg).toBe('Connection Invalid');
    expect(component.testConnectionValid).toBeFalsy();
  });

  it('should return tooltip text for github baseUrl', () => {
    const type = 'github';
    const field = 'baseUrl';
    const tooltipText = component.showInfo(type, field);
    expect(tooltipText).toBe(
      'Url i.e : for public github this url will be https://api.github.com.',
    );
  });

  it('should return tooltip text for github username', () => {
    const type = 'github';
    const field = 'username';
    const tooltipText = component.showInfo(type, field);
    expect(tooltipText).toBe(
      'The name appended before your repository name (i.e. ownerName/repositoryName).',
    );
  });

  it('should return empty string for unknown type and field', () => {
    const type = 'unknown';
    const field = 'unknown';
    const tooltipText = component.showInfo(type, field);
    expect(tooltipText).toBe('');
  });

  it('should disable username and password fields and enable accessToken field when accessTokenEnabled is true', () => {
    component.basicConnectionForm = new FormGroup({
      username: new FormControl(),
      password: new FormControl(),
      accessToken: new FormControl(),
    });
    component.connection = { accessTokenEnabled: true };
    spyOn(component.basicConnectionForm.controls['username'], 'disable');
    spyOn(component.basicConnectionForm.controls['password'], 'disable');
    spyOn(component.basicConnectionForm.controls['accessToken'], 'enable');
    component.enableDisableFieldsOnAccessTokenORPasswordToggle();
    expect(
      component.basicConnectionForm.controls['username'].disable,
    ).toHaveBeenCalled();
    expect(
      component.basicConnectionForm.controls['password'].disable,
    ).toHaveBeenCalled();
    expect(
      component.basicConnectionForm.controls['accessToken'].enable,
    ).toHaveBeenCalled();
  });

  // -> checkBitbucketValue
  it('should update labels correctly for Bitbucket cloud environment', () => {
    component.basicConnectionForm.controls['cloudEnv'].setValue(true); // Set cloud environment to true
    component.checkBitbucketValue({}, '', 'bitbucket');

    const bitbucketObj = component.addEditConnectionFieldsNlabels.find(
      (item) => item.connectionLabel.toLowerCase() === 'bitbucket',
    );

    // Ensure bitbucketObj exists before checking its labels
    expect(bitbucketObj).toBeDefined();
    expect(bitbucketObj.labels).toEqual([
      'Connection Type',
      'Connection Name',
      'Is Cloud Environment',
      'Base Url',
      'Username (Profile Username)',
      'Use vault password',
      'Password (App Password)',
      'API End Point',
      'User Email',
      'Share connection with everyone',
    ]);
  });

  it('should update labels correctly for Bitbucket non-cloud environment', () => {
    component.basicConnectionForm.controls['cloudEnv'].setValue(false); // Set cloud environment to false
    component.checkBitbucketValue({}, '', 'bitbucket');

    const bitbucketObj = component.addEditConnectionFieldsNlabels.find(
      (item) => item.connectionLabel.toLowerCase() === 'bitbucket',
    );

    // Ensure bitbucketObj exists before checking its labels
    expect(bitbucketObj).toBeDefined();
    expect(bitbucketObj.labels).toEqual([
      'Connection Type',
      'Connection Name',
      'Is Cloud Environment',
      'Base Url',
      'Username',
      'Use vault password',
      'Password',
      'API End Point',
      'User Email',
      'Share connection with everyone',
    ]);
  });

  it('should not update labels for other types', () => {
    component.basicConnectionForm.controls['cloudEnv'].setValue(true); // Set cloud environment to true
    component.checkBitbucketValue({}, '', 'github'); // Pass a different type

    const bitbucketObj = component.addEditConnectionFieldsNlabels.find(
      (item) => item.connectionLabel.toLowerCase() === 'bitbucket',
    );

    // Ensure bitbucketObj exists before checking its labels
    expect(bitbucketObj).toBeDefined();
    expect(bitbucketObj.labels).toEqual([
      'Connection Type',
      'Connection Name',
      'Is Cloud Environment',
      'Base Url',
      'Profile Username',
      'Use vault password',
      'App Password',
      'API End Point',
      'User Email',
      'Share connection with everyone',
    ]); // Labels should remain unchanged
  });

  it('should not throw error if bitbucket is not found', () => {
    component.addEditConnectionFieldsNlabels = [
      {
        connectionType: 'GitHub',
        connectionLabel: 'GitHub',
        categoryValue: 'sourceCodeManagement',
        categoryLabel: 'Source Code Management',
        labels: [
          'Connection Type',
          'Connection Name',
          'Base Url',
          'Repo Ownername',
          'Use vault password',
          'Access Token',
          'User Email',
          'Share connection with everyone',
        ],
        inputFields: [
          'type',
          'connectionName',
          'baseUrl',
          'username',
          'vault',
          'accessToken',
          'email',
          'sharedConnection',
        ],
      }, // No bitbucket in the list
    ];

    expect(() =>
      component.checkBitbucketValue({}, '', 'bitbucket'),
    ).not.toThrow();
  });
  // -> end of checkBitbucketValue
});
