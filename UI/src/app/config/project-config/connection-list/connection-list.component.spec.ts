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
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TableModule } from 'primeng/table';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpService } from 'src/app/services/http.service';
import { RsaEncryptionService } from 'src/app/services/rsa.encryption.service';
import { ConnectionListComponent } from './connection-list.component';
import { AppConfig, APP_CONFIG } from 'src/app/services/app.config';
import { ConfirmationService } from 'primeng/api';
import { environment } from 'src/environments/environment';


describe('ConnectionListComponent', () => {
  let component: ConnectionListComponent;
  let fixture: ComponentFixture<ConnectionListComponent>;
  let httpMock;
  const baseUrl = environment.baseUrl;
  const connectionTableData = [
    {
      connectionName: 'jenkinsJenkins',
      type: 'Jenkins',
      username: 'userNameJenkins',
      baseUrl: 'baseUrlJenkins'
    },
    {
      connectionName: 'DojoJira',
      type: 'Jira',
      username: 'fds',
      baseUrl: 'fdsf'
    },
    {
      connectionName: 'dsadsa',
      type: 'Azure Boards'
    },
    {
      connectionName: 'ConnectBit12',
      type: 'Bitbucket',
      username: 'userNameBitbucket12',
      baseUrl: 'baseBitbucket12'
    },
    {
      connectionName: 'Git7',
      type: 'GITLAB',
      username: 'Git7',
      baseUrl: 'Git7'
    },
    {
      connectionName: 'Team1',
      type: 'TEAMCITY',
      username: 'Team1',
      baseUrl: 'Team1'
    },
    {
      connectionName: 'sonar121',
      type: 'SONAR',
      username: 'sonar121',
      baseUrl: 'sonar132'
    },
    {
      connectionName: 'Sonar2',
      type: 'SONAR',
      username: 'Sonar2',
      baseUrl: 'Sonar2'
    },
    {
      connectionName: 'sonar3',
      type: 'SONAR',
      username: 'sonar3',
      baseUrl: 'sonar3'
    },
    {
      connectionName: 'connectNameSonar321',
      type: 'Sonar',
      username: 'userNameSonar321',
      baseUrl: 'baseSonar321'
    },
    {
      connectionName: 'Git1Connect',
      type: 'GitLab',
      username: 'Git1connect',
      baseUrl: 'Git1connect'
    },
    {
      connectionName: 'Git30',
      type: 'GITLAB',
      username: 'Git30',
      baseUrl: 'Git30'
    },
    {
      connectionName: 'Azure pipeline 1',
      type: 'AZURE PIPELINE',
      baseUrl: 'Azure pipeline 1'
    },
    {
      connectionName: 'Azure pipeline 2',
      type: 'Azure Pipeline',
      baseUrl: 'Azure pipeline 2'
    },
    {
      connectionName: 'sonar41',
      type: 'Sonar',
      username: 'sonar41',
      baseUrl: 'sonar41'
    },
    {
      connectionName: 'git6',
      type: 'GitLab',
      username: 'git6',
      baseUrl: 'git6'
    },
    {
      connectionName: 'Sonar 513',
      type: 'Sonar',
      username: 'Sonar 513',
      baseUrl: 'Sonar 513'
    },
    {
      connectionName: 'Sonar Connec1',
      type: 'Sonar',
      username: 'sonarusername1',
      baseUrl: 'baseurl1'
    },
    {
      connectionName: 'bambooConnection1',
      type: 'Bamboo',
      username: 'bambooUsername1',
      baseUrl: 'bambooBaseUrl1'
    },
    {
      connectionName: 'teamCityConnec3',
      type: 'Teamcity',
      username: 'teamCityUsername143',
      baseUrl: 'teamCityBaseUrl223'
    },
    {
      connectionName: 'Azure p4',
      type: 'Azure Pipeline',
      baseUrl: 'azureBase4'
    },
    {
      connectionName: 'azureR4',
      type: 'Azure Repository',
      baseUrl: 'azureBaseR3'
    },
    {
      connectionName: 'Git432',
      type: 'GitLab',
      username: 'Hit3214',
      baseUrl: 'Git42342'
    },
    {
      connectionName: 'DojoJira2',
      type: 'Jira',
      username: 'SUPERADMIN',
      baseUrl: 'https://test.com/jira/'
    },
    {
      connectionName: 'JiraConnection11',
      type: 'Jira',
      username: 'jiraUser11',
      baseUrl: 'JiraBase11'
    },
    {
      connectionName: 'dsf',
      type: 'Jira',
      username: 'rfrsd',
      baseUrl: 'fds'
    },
    {
      connectionName: 'connecJira101',
      type: 'Jira',
      username: 'userConnect101',
      baseUrl: 'baseConnect101'
    },
    {
      connectionName: 'connectJira101',
      type: 'Jira',
      username: 'username102',
      baseUrl: 'baseUrlConnect101'
    },
    {
      connectionName: 'jiraConnect103',
      type: 'Jira',
      username: 'usernameConnect103',
      baseUrl: 'baseUrl103'
    },
    {
      connectionName: '104ConnectName',
      type: 'Jira',
      username: '104Username',
      baseUrl: '104BaseUrl'
    },
    {
      connectionName: 'dfgfvd',
      type: 'Azure',
      username: 'dsvfd',
      baseUrl: 'dsfgvfd'
    },
    {
      connectionName: 'Bit200Connect',
      type: 'Bitbucket',
      username: 'userConnect200',
      baseUrl: 'base200Base'
    },
    {
      connectionName: 'ConnectJira300',
      type: 'Jira',
      username: 'user300',
      baseUrl: 'base300'
    },
    {
      connectionName: 'connect301',
      type: 'Jira',
      username: 'user301',
      baseUrl: 'base301'
    },
    {
      connectionName: 'connect302',
      type: 'Jira',
      username: 'user302',
      baseUrl: 'base302'
    },
    {
      connectionName: 'connect302',
      type: 'Zephyr',
      username: 'user302',
      baseUrl: 'base302'
    }
  ];

  const getConnectionsResponse = { message: 'Found all connectionData', success: true, data: [{ id: '601a3fd369515b0001fe072e', type: 'Jira', connectionName: 'jira connection', cloudEnv: false, baseUrl: 'https://test.com/jira', username: 'tst-11', apiEndPoint: 'rest/api/1.0', isOAuth: false, offline: false, createdAt: '2021-02-03T06:16:51', updatedAt: '2021-03-25T03:37:42', createdBy: 'SUPERADMIN', connPrivate: false, updatedBy: 'SUPERADMIN', connectionUsers: ['SUPERADMIN'] }, { id: '601baea569515b0001d6816c', type: 'Jenkins', connectionName: 'testjenkins', cloudEnv: false, baseUrl: 'http://10.148.241.13:8080/', username: 'user- speedy_test', isOAuth: false, offline: false, createdAt: '2021-02-04T08:21:57', updatedAt: '2021-03-25T09:40:03', createdBy: 'SUPERADMIN', connPrivate: false, updatedBy: 'SUPERADMIN', connectionUsers: ['SUPERADMIN'] }, { id: '601bd7f669515b0001d68187', type: 'Jira', connectionName: 'DOJO', cloudEnv: false, baseUrl: 'https://test.com/jira', username: 'ritsharm0', apiEndPoint: 'rest/api/2', isOAuth: false, offline: false, createdAt: '2021-02-04T11:18:14', createdBy: 'SUPERADMIN', connPrivate: false, updatedBy: 'SUPERADMIN', connectionUsers: ['SUPERADMIN'] }, { id: '6020f77ee077d700018adc4a', type: 'Sonar', connectionName: 'Sonar DOJO', cloudEnv: false, baseUrl: 'https://test.com/sonar', username: 'tst-1', isOAuth: false, offline: false, createdAt: '2021-02-08T08:34:06', updatedAt: '2021-03-25T09:39:41', createdBy: 'SUPERADMIN', connPrivate: false, updatedBy: 'SUPERADMIN', connectionUsers: ['SUPERADMIN'] }, { id: '602117b2e077d700018adc59', type: 'Sonar', connectionName: 'DOJO Sonar', cloudEnv: false, baseUrl: 'https://test.com/sonar', username: 'ritsharm0', isOAuth: false, offline: false, createdAt: '2021-02-08T10:51:30', createdBy: 'SUPERADMIN', connPrivate: false, updatedBy: 'SUPERADMIN', connectionUsers: ['SUPERADMIN'] }, { id: '602267059c0b1500015c011e', type: 'GitLab', connectionName: 'ndsb fan', cloudEnv: false, baseUrl: 'dsbfn/fgdsg', username: 'fdsfdsg', isOAuth: false, offline: false, createdAt: '2021-02-09T10:42:13', updatedAt: '2021-02-09T10:42:26', createdBy: 'SUPERADMIN', connPrivate: false, updatedBy: 'SUPERADMIN', connectionUsers: ['SUPERADMIN'] }, { id: '6023b045b86e73000106d5fc', type: 'Jenkins', connectionName: 'Jenkins', cloudEnv: false, baseUrl: 'http://10.148.241.13:8080/', username: 'svc-apac-ps-fcadevop', isOAuth: false, offline: false, createdAt: '2021-02-10T10:07:01', createdBy: 'SUPERADMIN', connPrivate: false, updatedBy: 'SUPERADMIN', connectionUsers: ['SUPERADMIN'] }, { id: '60263c56387a4900015537ef', type: 'NewRelic', connectionName: 'TestConnectionRishabhNewRelic', cloudEnv: false, apiEndPoint: 'https://insights-api.newrelic.com/v1/accounts/729003/query?nrql=', isOAuth: false, apiKeyFieldName: 'X-Query-Key', offline: false, createdAt: '2021-02-12T08:29:10', createdBy: 'SUPERADMIN', connPrivate: false, updatedBy: 'SUPERADMIN', connectionUsers: ['SUPERADMIN'] }, { id: '60263d17387a4900015537f0', type: 'GitLab', connectionName: 'TestConnectionRishabhGitLab', cloudEnv: false, baseUrl: 'https://pscode.lioncloud.net', username: 'risshukl0', isOAuth: false, offline: false, createdAt: '2021-02-12T08:32:23', updatedAt: '2021-02-12T08:32:52', createdBy: 'SUPERADMIN', connPrivate: false, updatedBy: 'SUPERADMIN', connectionUsers: ['SUPERADMIN'] }, { id: '60263d95387a4900015537f1', type: 'Azure', connectionName: 'TestConnectionRishabhAzureBoards', cloudEnv: false, baseUrl: 'https://dev.azure.com/sundeepm/AzureSpeedy', username: 'risshukl0', isOAuth: false, offline: false, createdAt: '2021-02-12T08:34:29', createdBy: 'SUPERADMIN', connPrivate: false, updatedBy: 'SUPERADMIN', connectionUsers: ['SUPERADMIN'] }, { id: '603f67f5ec57dc0001405ebf', type: 'AzureRepository', connectionName: 'AzureRepo1', cloudEnv: false, baseUrl: 'https://dev.azure.com/ankbhard/KnowHOW', isOAuth: false, offline: false, createdAt: '2021-03-03T10:41:57', updatedAt: '2021-03-25T09:37:06', createdBy: 'SUPERADMIN', connPrivate: false, updatedBy: 'SUPERADMIN', connectionUsers: ['SUPERADMIN'] }, { id: '603f6a8bec57dc0001405ec3', type: 'Bamboo', connectionName: 'BambooTool1', cloudEnv: false, baseUrl: 'https://test.com/bamboo/browse/BAMUP', username: 'tst-1', isOAuth: false, offline: false, createdAt: '2021-03-03T10:52:59', updatedAt: '2021-03-25T09:37:59', createdBy: 'SUPERADMIN', connPrivate: false, updatedBy: 'SUPERADMIN', connectionUsers: ['SUPERADMIN'] }, { id: '603f6ab1ec57dc0001405ec4', type: 'GitLab', connectionName: 'Gitlab', cloudEnv: false, baseUrl: 'https://pscode.lioncloud.net', username: 'risshukl0', isOAuth: false, offline: false, createdAt: '2021-03-03T10:53:37', createdBy: 'SUPERADMIN', connPrivate: false, updatedBy: 'SUPERADMIN', connectionUsers: ['SUPERADMIN'] }, { id: '6040a1683b3af5000151b2b6', type: 'Sonar', connectionName: 'sonartest123', cloudEnv: false, baseUrl: 'https://test.com/sonar/', username: 'sansharm13', isOAuth: false, offline: false, createdAt: '2021-03-04T08:59:21', createdBy: 'SUPERADMIN', connPrivate: false, updatedBy: 'SUPERADMIN', connectionUsers: ['SUPERADMIN'] }, { id: '6040d76b3b3af5000151b3be', type: 'AzurePipeline', connectionName: 'Azure-pipline-conn1', cloudEnv: false, baseUrl: 'https://dev.azure.com/sundeepm/AzureSpeedy', isOAuth: false, offline: false, createdAt: '2021-03-04T12:49:47', updatedAt: '2021-03-25T09:37:23', createdBy: 'superadmin2', connPrivate: false, updatedBy: 'SUPERADMIN', connectionUsers: ['superadmin2'] }, { id: '6041ac7f3b3af5000121a690', type: 'Bitbucket', connectionName: 'bitbucket test connection', cloudEnv: false, baseUrl: 'https://test.com/KNOW/KnowHOWRepo', username: 'tst-1', apiEndPoint: '/bitbucket/rest/api/1.0/', isOAuth: false, offline: false, createdAt: '2021-03-05T03:58:55', updatedAt: '2021-04-01T11:58:50', createdBy: 'SUPERADMIN', connPrivate: false, updatedBy: 'SUPERADMIN', connectionUsers: ['SUPERADMIN'] }, { id: '605c5bfbf0fb3000015e96d1', type: 'Teamcity', connectionName: 'Teamcity', cloudEnv: false, baseUrl: 'https://budp-tc.bunnings.com.au/', username: 'speedysvc@publicissapient.com', isOAuth: false, offline: false, createdAt: '2021-03-25T09:46:35', createdBy: 'SUPERADMIN', connPrivate: false, updatedBy: 'SUPERADMIN', connectionUsers: ['SUPERADMIN'] }] };

  const connectionLabelsFields = [
    {
      connectionType: 'Jira',
      connectionLabel: 'Jira',
       labels: ['Connection Type', 'Connection Name', 'Is Cloud Environment', 'Base Url', 'Username', 'Use vault password', 'Password', 'Api End Point', 'IsOAuth', 'Private Key', 'Consumer Key', 'Is Offline', 'Is Connection Private'],
       inputFields: ['type', 'connectionName', 'cloudEnv', 'baseUrl', 'username', 'vault', 'password', 'apiEndPoint', 'isOAuth', 'privateKey', 'consumerKey', 'offline', 'connPrivate']
    },
    {
      connectionType: 'Azure',
      connectionLabel: 'Azure Boards',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Username', 'Use vault password', 'PAT', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault', 'pat', 'connPrivate']
    },
    {
      connectionType: 'GitHub',
      connectionLabel: 'GitHub',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Repo Ownername', 'Use vault password', 'Access Token', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault', 'accessToken', 'connPrivate']
    },
    {
      connectionType: 'GitLab',
      connectionLabel: 'GitLab',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Username', 'Use vault password', 'Access Token', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault', 'accessToken', 'connPrivate']
    },
    {
      connectionType: 'Bitbucket',
      connectionLabel: 'Bitbucket',
      labels: ['Connection Type', 'Connection Name', 'Is Cloud Environment', 'Base Url', 'Username', 'Use vault password','Password', 'API End Point', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'cloudEnv', 'baseUrl', 'username', 'vault','password', 'apiEndPoint', 'connPrivate']
    },
    {
      connectionType: 'Sonar',
      connectionLabel: 'Sonar',
      labels: ['Connection Type', 'Connection Name', 'Is Cloud Environment', 'Base Url', 'Username','Use vault password',['Use Password', 'Use Token'], 'Password', 'Access Token', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'cloudEnv', 'baseUrl', 'username','vault','accessTokenEnabled', 'password', 'accessToken', 'connPrivate']
    },
    {
      connectionType: 'Jenkins',
      connectionLabel: 'Jenkins',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Username', 'Use vault password', 'Api Key', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault', 'apiKey', 'connPrivate']
    },
    {
      connectionType: 'Bamboo',
      connectionLabel: 'Bamboo',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Username', 'Use vault password','Password', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault','password', 'connPrivate']
    },
    {
      connectionType: 'Teamcity',
      connectionLabel: 'Teamcity',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Username', 'Use vault password', 'Password', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault', 'password', 'connPrivate']
    },
    {
      connectionType: 'AzurePipeline',
      connectionLabel: 'Azure Pipeline',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Use vault password', 'PAT', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'vault', 'pat', 'connPrivate']
    },
    {
      connectionType: 'AzureRepository',
      connectionLabel: 'Azure Repository',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Use vault password', 'PAT', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'vault', 'pat', 'connPrivate']
    },
    {
      connectionType: 'Zephyr',
      connectionLabel: 'Zephyr',
      labels: ['Connection Type', 'Connection Name', 'Is Cloud Environment', 'Base Url', 'Username', 'Use vault password', 'Password', 'Api End Point', 'Access Token', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'cloudEnv', 'baseUrl', 'username', 'vault', 'password', 'apiEndPoint', 'accessToken', 'connPrivate']
    }
  ];

  const fieldsAndLabels = [
    {
      connectionType: 'Jira',
      connectionLabel: 'Jira',
       labels: ['Connection Type', 'Connection Name', 'Is Cloud Environment', 'Base Url', 'Username', 'Use vault password', 'Password', 'Api End Point', 'IsOAuth', 'Private Key', 'Consumer Key', 'Is Offline', 'Is Connection Private'],
       inputFields: ['type', 'connectionName', 'cloudEnv', 'baseUrl', 'username', 'vault', 'password', 'apiEndPoint', 'isOAuth', 'privateKey', 'consumerKey', 'offline', 'connPrivate']
    },
    {
      connectionType: 'Azure',
      connectionLabel: 'Azure Boards',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Username', 'Use vault password', 'PAT', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault', 'pat', 'connPrivate']
    },
    {
      connectionType: 'GitHub',
      connectionLabel: 'GitHub',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Repo Ownername', 'Use vault password', 'Access Token', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault', 'accessToken', 'connPrivate']
    },
    {
      connectionType: 'GitLab',
      connectionLabel: 'GitLab',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Username', 'Use vault password', 'Access Token', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault', 'accessToken', 'connPrivate']
    },
    {
      connectionType: 'Bitbucket',
      connectionLabel: 'Bitbucket',
      labels: ['Connection Type', 'Connection Name', 'Is Cloud Environment', 'Base Url', 'Username', 'Use vault password','Password', 'API End Point', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'cloudEnv', 'baseUrl', 'username', 'vault','password', 'apiEndPoint', 'connPrivate']
    },
    {
      connectionType: 'Sonar',
      connectionLabel: 'Sonar',
      labels: ['Connection Type', 'Connection Name', 'Is Cloud Environment', 'Base Url', 'Username','Use vault password', 'Password', 'Access Token', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'cloudEnv', 'baseUrl', 'username','vault', 'password', 'accessToken', 'connPrivate']
    },
    {
      connectionType: 'Jenkins',
      connectionLabel: 'Jenkins',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Username', 'Use vault password', 'Api Key', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault', 'apiKey', 'connPrivate']
    },
    {
      connectionType: 'Bamboo',
      connectionLabel: 'Bamboo',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Username', 'Use vault password','Password', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault','password', 'connPrivate']
    },
    {
      connectionType: 'Teamcity',
      connectionLabel: 'Teamcity',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Username', 'Use vault password', 'Password', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault', 'password', 'connPrivate']
    },
    {
      connectionType: 'AzurePipeline',
      connectionLabel: 'Azure Pipeline',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Use vault password', 'PAT', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'vault', 'pat', 'connPrivate']
    },
    {
      connectionType: 'AzureRepository',
      connectionLabel: 'Azure Repository',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Use vault password', 'PAT', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'vault', 'pat', 'connPrivate']
    },
    {
      connectionType: 'Zephyr',
      connectionLabel: 'Zephyr',
      labels: ['Connection Type', 'Connection Name', 'Is Cloud Environment', 'Base Url', 'Username', 'Use vault password', 'Password', 'Api End Point', 'Access Token', 'Is Connection Private'],
      inputFields: ['type', 'connectionName', 'cloudEnv', 'baseUrl', 'username', 'vault', 'password', 'apiEndPoint', 'accessToken', 'connPrivate']
    }
  ];

  const connectionList = ['Jira', 'Azure', 'GitHub', 'GitLab', 'Bitbucket', 'Sonar', 'Jenkins', 'Bamboo', 'Teamcity', 'AzurePipeline', 'AzureRepository','Zephyr'];
  const enableDisableMatrix = {
    enableDisableEachTime: {
      cloudEnv: [],
      offline: [
      ],
      isOAuth: [
        {
          field: 'privateKey',
          isEnabled: false
        },
        {
          field: 'consumerKey',
          isEnabled: false
        }
      ],
      vault: [
        {
          field: 'password',
          isEnabled: false
        },
        {
          field: 'accessToken',
          isEnabled: false
        },
        {
          field: 'pat',
          isEnabled: false
        },
        {
          field:'apiKey',
          isEnabled: false
        }
      ],
      accessTokenEnabled:[]
    },
    enableDisableAnotherTime: {
      cloudEnv: [],
      offline: [
        {
          field: 'cloudEnv',
          isEnabled: true
        },
        {
          field: 'baseUrl',
          isEnabled: true
        },
        {
          field: 'username',
          isEnabled: true
        },
        {
          field: 'password',
          isEnabled: true
        },
        {
          field: 'apiEndPoint',
          isEnabled: true
        },
        {
          field: 'isOAuth',
          isEnabled: true
        },
        {
          field: 'privateKey',
          isEnabled: false
        },
        {
          field: 'consumerKey',
          isEnabled: false
        }
      ],
      isOAuth: [],
      vault: [
        {
          field: 'password',
          isEnabled: false
        },
        {
          field: 'accessToken',
          isEnabled: false
        },
        {
          field: 'pat',
          isEnabled: false
        },
        {
          field:'apiKey',
          isEnabled: false
        }
      ],
      accessTokenEnabled:[]
    }
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
        BrowserAnimationsModule
      ],
      declarations: [
        ConnectionListComponent
      ],
      providers: [
        HttpService,
        ConfirmationService,
        RsaEncryptionService,
        { provide: APP_CONFIG, useValue: AppConfig }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectionListComponent);
    component = fixture.componentInstance;
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
      expect(component.addEditConnectionFieldsNlabels[index].connectionType).toEqual(connection);
    });
  });

  it('Should test all the fields exist in the Add/Edit connection popup', () => {
    connectionLabelsFields.forEach((connectionData, index) => {
      expect(component.addEditConnectionFieldsNlabels[index].connectionType).toEqual(connectionData.connectionType);
      connectionData.labels.forEach((label, innerIndex) => {
        expect(component.addEditConnectionFieldsNlabels[index].labels[innerIndex]).toEqual(label);
      });
      connectionData.inputFields.forEach((field, innerIndex) => {
        expect(component.addEditConnectionFieldsNlabels[index].inputFields[innerIndex]).toEqual(field);
      });
    });

  });

  it('Should test connection list table is rendering', () => {
    connectionTableData.forEach(connectionRecord => {
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
    expect(component.basicConnectionForm.controls['privateKey'].enabled).toBeTrue();
    expect(component.basicConnectionForm.controls['consumerKey'].enabled).toBeTrue();
  });

  it('should disable fields depending on inputs', () => {
    const fakeEvent = { originalEvent: { isTrusted: true }, checked: false };
    const field = 'isOAuth';
    component.enableDisableOnToggle = enableDisableMatrix;
    component.enableDisableSwitch(fakeEvent, field);
    fixture.detectChanges();
    expect(component.basicConnectionForm.controls['privateKey'].enabled).toBeFalse();
    expect(component.basicConnectionForm.controls['consumerKey'].enabled).toBeFalse();
  });

  it('should fetch and render connections list', () => {
    component.getConnectionList();
    fixture.detectChanges();
    httpMock.match(`${baseUrl}/api/connections`)[0].flush(getConnectionsResponse);
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
    component.basicConnectionForm.controls['connectionName'].setValue('TestConnectionRishabhJira4');
    component.basicConnectionForm.controls['cloudEnv'].setValue(false);
    component.basicConnectionForm.controls['baseUrl'].setValue('https://test.com/jira');
    component.basicConnectionForm.controls['username'].setValue('tst-1');
    component.basicConnectionForm.controls['password'].setValue('test');
    component.basicConnectionForm.controls['apiEndPoint'].setValue('rest/api/2');
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
    httpMock.match(`${baseUrl}/api/connections`)[0].flush({ message: 'created and saved new connection', success: true, data: { id: '6066c07569515b0001df160f', type: 'Jira', connectionName: 'TestConnectionRishabh4', cloudEnv: true, baseUrl: ' https://test.com/jira', username: 'tst-1', password: '', apiEndPoint: 'rest/api/2', isOAuth: false, offline: false, createdBy: 'SUPERADMIN', connPrivate: true, updatedBy: 'SUPERADMIN', connectionUser: ['SUPERADMIN'] } });
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
      connectionUsers: [
        'SUPERADMIN'
      ],
      vault: false
    };
    component.editConnection(connection);
    fixture.detectChanges();
    expect(component.connection).toEqual({ ...connection });
    expect(component.connectionDialog).toBeTrue();
    expect(component.isNewlyConfigAdded).toBeFalse();
    expect(component.selectedConnectionType).toBe('Jira');
    expect(component.disableConnectionTypeDropDown).toBeTrue();
  });
});
