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

import { Component, Input, OnInit } from '@angular/core';
import { FormControl, FormGroup, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ConfirmationService } from 'primeng/api';
import { HttpService } from '../../../services/http.service';
import { TestConnectionService } from '../../../services/test-connection.service';
import { GetAuthorizationService } from '../../../services/get-authorization.service';
import { SharedService } from 'src/app/services/shared.service';
import { HelperService } from 'src/app/services/helper.service';
import { ActivatedRoute, Router } from '@angular/router';
import { FeatureFlagsService } from 'src/app/services/feature-toggle.service';

interface JiraConnectionField {
  'type': string,
  'connectionName': string,
  'cloudEnv': boolean,
  'baseUrl': string,
  'username': string,
  'vault': boolean,
  'password': string,
  'bearerToken': boolean,
  'patOAuthToken': string,
  'apiEndPoint': string,
  'isOAuth': boolean,
  'privateKey': string,
  'consumerKey': string,
  'sharedConnection': boolean,
  'jaasKrbAuth': boolean,
  'jaasConfigFilePath': string,
  'krb5ConfigFilePath': string,
  'jaasUser': string,
  'samlEndPoint': string,
  'jiraAuthType': string
}
@Component({
  selector: 'app-connection-list',
  templateUrl: './connection-list.component.html',
  styleUrls: ['./connection-list.component.css']
})
export class ConnectionListComponent implements OnInit {
  basicConnectionForm: UntypedFormGroup;
  rallyEnabled: boolean = false;
  addEditConnectionFieldsNlabels = [
    {
      connectionType: 'Jira',
      connectionLabel: 'Jira',
      categoryValue: 'projectManagement',
      categoryLabel: 'Project Management',
      labels: ['Connection Type', 'Connection Name', 'Is Cloud Environment', 'Base Url', 'Username', 'Use vault password', 'Password', 'Api End Point', 'IsOAuth', 'Private Key', 'Consumer Key', 'Share connection with everyone', 'Use bearer token', 'PAT OAuthToken', 'Is jaasKrbAuth', 'Jaas Config FilePath', 'Krb5 Config FilePath', 'Jaas User', 'Saml Endpoint', 'Select Authentication Type'],
      inputFields: ['type', 'connectionName', 'cloudEnv', 'baseUrl', 'username', 'vault', 'password', 'apiEndPoint', 'isOAuth', 'privateKey', 'consumerKey', 'sharedConnection', 'bearerToken', 'patOAuthToken', 'jaasKrbAuth', 'jaasConfigFilePath', 'krb5ConfigFilePath', 'jaasUser', 'samlEndPoint', 'jiraAuthType']
    },
    {
      connectionType: 'Azure',
      connectionLabel: 'Azure Boards',
      categoryValue: 'projectManagement',
      categoryLabel: 'Project Management',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Username', 'Use vault password', 'PAT', 'Share connection with everyone'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault', 'pat', 'sharedConnection']
    },
    {
      connectionType: 'GitHub',
      connectionLabel: 'GitHub',
      categoryValue: 'sourceCodeManagement',
      categoryLabel: 'Source Code Management',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Repo Ownername', 'Use vault password', 'Access Token', 'User Email', 'Share connection with everyone'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault', 'accessToken', 'email', 'sharedConnection']
    },
    {
      connectionType: 'GitLab',
      connectionLabel: 'GitLab',
      categoryValue: 'sourceCodeManagement',
      categoryLabel: 'Source Code Management',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Username', 'Use vault password', 'Access Token', 'User Email', 'Share connection with everyone'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault', 'accessToken', 'email', 'sharedConnection']
    },
    {
      connectionType: 'Bitbucket',
      connectionLabel: 'Bitbucket',
      categoryValue: 'sourceCodeManagement',
      categoryLabel: 'Source Code Management',
      labels: ['Connection Type', 'Connection Name', 'Is Cloud Environment', 'Base Url', 'Profile Username', 'Use vault password', 'App Password', 'API End Point', 'User Email', 'Share connection with everyone'],
      inputFields: ['type', 'connectionName', 'cloudEnv', 'baseUrl', 'username', 'vault', 'password', 'apiEndPoint', 'email', 'sharedConnection']
    },
    {
      connectionType: 'Sonar',
      connectionLabel: 'Sonar',
      categoryValue: 'security',
      categoryLabel: 'Security',
      labels: ['Connection Type', 'Connection Name', 'Is Cloud Environment', 'Base Url', 'Username', 'Use vault password', ['Use Password', 'Use Token'], 'Password', 'Access Token', 'Share connection with everyone'],
      inputFields: ['type', 'connectionName', 'cloudEnv', 'baseUrl', 'username', 'vault', 'accessTokenEnabled', 'password', 'accessToken', 'sharedConnection']
    },
    {
      connectionType: 'Jenkins',
      connectionLabel: 'Jenkins',
      categoryValue: 'build',
      categoryLabel: 'Build',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Username', 'Use vault password', 'Api Key', 'Share connection with everyone'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault', 'apiKey', 'sharedConnection'],
      placeholder: ['', '', 'Enter a public URL', '', '', '', '']
    },
    {
      connectionType: 'Bamboo',
      connectionLabel: 'Bamboo',
      categoryValue: 'build',
      categoryLabel: 'Build',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Username', 'Use vault password', 'Password', 'Share connection with everyone'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault', 'password', 'sharedConnection']
    },
    {
      connectionType: 'Teamcity',
      connectionLabel: 'Teamcity',
      categoryValue: 'build',
      categoryLabel: 'Build',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Username', 'Use vault password', 'Password', 'Share connection with everyone'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault', 'password', 'sharedConnection']
    },
    {
      connectionType: 'AzurePipeline',
      connectionLabel: 'Azure Pipeline',
      categoryValue: 'build',
      categoryLabel: 'Build',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Use vault password', 'PAT', 'Share connection with everyone'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'vault', 'pat', 'sharedConnection']
    },
    {
      connectionType: 'AzureRepository',
      connectionLabel: 'Azure Repository',
      categoryValue: 'sourceCodeManagement',
      categoryLabel: 'Source Code Management',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Username', 'Use vault password', 'PAT', 'User Email', 'Share connection with everyone'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'vault', 'pat', 'email', 'sharedConnection']
    },
    {
      connectionType: 'Zephyr',
      connectionLabel: 'Zephyr',
      categoryValue: 'testManagement',
      categoryLabel: 'Test Management',
      labels: ['Connection Type', 'Connection Name', 'Is Cloud Environment', 'Use Bearer Token', 'PatOAuthToken', 'Base Url', 'Username', 'Use vault password', 'Password', 'Api End Point', 'Access Token', 'Share connection with everyone'],
      inputFields: ['type', 'connectionName', 'cloudEnv', 'bearerToken', 'patOAuthToken', 'baseUrl', 'username', 'vault', 'password', 'apiEndPoint', 'accessToken', 'sharedConnection']
    },
    {
      connectionType: 'ArgoCD',
      connectionLabel: 'ArgoCD',
      categoryValue: 'build',
      categoryLabel: 'Build',
      labels: ['Connection Type', 'Connection Name', 'Base Url', 'Username', 'Access Token','Share connection with everyone'],
      inputFields: ['type', 'connectionName', 'baseUrl', 'username', 'accessToken', 'sharedConnection']
    }

  ];

  enableDisableOnToggle = {
    enableDisableEachTime: {
      cloudEnv: [],
      // offline: [
      // ],
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
          field: 'apiKey',
          isEnabled: false
        }
      ],
      isCloneable: [
        {
          field: 'sshUrl',
          isEnabled: false
        },
      ],
      accessTokenEnabled: [],
      bearerToken: [
        {
          field: 'patOAuthToken',
          isEnabled: true
        },
      ]
    },
    enableDisableAnotherTime: {
      cloudEnv: [],
      // offline: [
      //   {
      //     field: 'cloudEnv',
      //     isEnabled: true
      //   },
      //   {
      //     field: 'baseUrl',
      //     isEnabled: true
      //   },
      //   {
      //     field: 'username',
      //     isEnabled: true
      //   },
      //   {
      //     field: 'password',
      //     isEnabled: true
      //   },
      //   {
      //     field: 'apiEndPoint',
      //     isEnabled: true
      //   },
      //   {
      //     field: 'isOAuth',
      //     isEnabled: true
      //   },
      //   {
      //     field: 'privateKey',
      //     isEnabled: false
      //   },
      //   {
      //     field: 'consumerKey',
      //     isEnabled: false
      //   }
      // ],
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
          field: 'apiKey',
          isEnabled: false
        }
      ],
      isCloneable: [],
      accessTokenEnabled: []
    }
  };

  connectionListAllType = {};

  connectionTypeCompleteList = [
    {
      label: 'Jira',
      value: 'Jira',
      connectionTableCols: [
        { field: 'connectionName', header: 'Connection Name', class: 'long-text' },
        { field: 'username', header: 'User Name', class: 'normal' },
        { field: 'apiEndPoint', header: 'API Endpoint', class: 'long-text' },
        // { field: 'apiKey', header: 'API Key', class: 'normal' },
        { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
        { field: 'cloudEnv', header: 'Cloud Env.?', class: 'small-text' },
        { field: 'isOAuth', header: 'OAuth', class: 'small-text' },
      ]
    },
    {
      label: 'Azure Boards',
      value: 'Azure',
      connectionTableCols: [
        { field: 'connectionName', header: 'Connection Name', class: 'long-text' },
        { field: 'username', header: 'User Name', class: 'normal' },
        // { field: 'apiKey', header: 'API Key', class: 'normal' },
        { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
        { field: 'isOAuth', header: 'OAuth', class: 'small-text' },
      ]
    },
    {
      label: 'GitHub',
      value: 'GitHub',
      connectionTableCols: [
        { field: 'connectionName', header: 'Connection Name', class: 'long-text' },
        { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
        { field: 'username', header: 'Repo Ownername', class: 'normal' },
      ]
    },
    {
      label: 'GitLab',
      value: 'GitLab',
      connectionTableCols: [
        { field: 'connectionName', header: 'Connection Name', class: 'long-text' },
        { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
        { field: 'username', header: 'User Name', class: 'long-text' },
        // { field: 'accessToken', header: 'Access Token', class: 'long-text' },
      ]
    },
    {
      label: 'Bitbucket',
      value: 'Bitbucket',
      connectionTableCols: [
        { field: 'connectionName', header: 'Connection Name', class: 'long-text' },
        { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
        { field: 'cloudEnv', header: 'Cloud Env.?', class: 'small-text' },
        { field: 'username', header: 'User Name', class: 'normal' },
        // { field: 'apiKey', header: 'API Key', class: 'normal' },
      ]
    },
    {
      label: 'Sonar',
      value: 'Sonar',
      connectionTableCols: [
        { field: 'connectionName', header: 'Connection Name', class: 'long-text' },
        { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
        { field: 'username', header: 'User Name', class: 'long-text' },
        { field: 'cloudEnv', header: 'Cloud Env.?', class: 'small-text' }
      ]
    },
    {
      label: 'Jenkins',
      value: 'Jenkins',
      connectionTableCols: [
        { field: 'connectionName', header: 'Connection Name', class: 'long-text' },
        { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
        { field: 'username', header: 'User Name', class: 'long-text' },
        // { field: 'apiKey', header: 'API Key', class: 'small-text' },
      ]
    },
    {
      label: 'Bamboo',
      value: 'Bamboo',
      connectionTableCols: [
        { field: 'connectionName', header: 'Connection Name', class: 'long-text' },
        { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
        { field: 'username', header: 'User Name', class: 'long-text' },
      ]
    },
    {
      label: 'Teamcity',
      value: 'Teamcity',
      connectionTableCols: [
        { field: 'connectionName', header: 'Connection Name', class: 'long-text' },
        { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
        { field: 'username', header: 'User Name', class: 'long-text' },
      ]
    },
    {
      label: 'Azure Pipeline',
      value: 'AzurePipeline',
      connectionTableCols: [
        { field: 'connectionName', header: 'Connection Name', class: 'long-text' },
        { field: 'username', header: 'User Name', class: 'normal' },
        { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
        { field: 'isOAuth', header: 'OAuth', class: 'small-text' },
      ]
    },
    {
      label: 'Azure Repository',
      value: 'AzureRepository',
      connectionTableCols: [
        { field: 'connectionName', header: 'Connection Name', class: 'long-text' },
        { field: 'username', header: 'User Name', class: 'normal' },
        { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
        { field: 'isOAuth', header: 'OAuth', class: 'small-text' },
      ]
    },
    {
      label: 'Zephyr',
      value: 'Zephyr',
      connectionTableCols: [
        { field: 'connectionName', header: 'Connection Name', class: 'long-text' },
        { field: 'cloudEnv', header: 'Cloud Env.?', class: 'small-text' },
        { field: 'username', header: 'User Name', class: 'normal' },
        { field: 'apiEndPoint', header: 'API Endpoint', class: 'long-text' },
        // { field: 'apiKey', header: 'API Key', class: 'normal' },
        { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
      ]
    },
    {
      label: 'ArgoCD',
      value: 'ArgoCD',
      connectionTableCols: [
        { field: 'connectionName', header: 'Connection Name', class: 'long-text' },
        { field: 'baseUrl', header: 'Base URL', class: 'long-text' },
        { field: 'username', header: 'User Name', class: 'long-text' },
      ]
    }
  ];

  nonMendatoryFieldsOnEditConnection = ['accessToken', 'password', 'pat'];

  selectedConnectionType = !!this.addEditConnectionFieldsNlabels[0] && !!this.addEditConnectionFieldsNlabels[0].connectionType ? this.addEditConnectionFieldsNlabels[0].connectionType : '';

  connection: any = { type: this.selectedConnectionType };

  connectionList = [];

  connectionDialog: boolean;

  submitted: boolean;

  testingConnection: boolean;

  isNewlyConfigAdded = false;

  fieldsObj = {};

  roleAccess: any = {};

  isConnectionAddEditAccess = false;

  testConnectionMsg = '';
  testConnectionValid = true;
  isRoleViewer = false;
  currentUser = '';
  zephyrUrl = '';
  jiraForm: FormGroup<any> = new FormGroup({});
  jiraAuthDdwn = [{
    'label': 'Basic Authentication',
    'key': 'basic'
  }, {
    'label': 'Vault Credentials',
    'key': 'vault'
  }, {
    'label': 'Bearer Token Authentication',
    'key': 'bearerToken',
  },
  {
    'label': "Oauth Authentication",
    'key': 'isOAuth'
  },
  {
    'label': "SPNEGO Authentication",
    'key': 'jaasKrbAuth'
  }];
  jiraVaultDdwn = [{
    'label': 'Cred Vault',
    'key': 'credVault'
  }];

  platformDdwn = [{
    'label': 'GitHub',
    'key': 'github'
  }, {
    'label': 'GitLab',
    'key': 'gitlab'
  }, {
    'label': 'Bitbucket',
    'key': 'bitbucket',
  }, {
    'label': 'Azure Repository',
    'key': 'AzureRepository',
  }];

  jiraConnectionFields: JiraConnectionField = {
    'type': 'Jira',
    'cloudEnv': false,
    'connectionName': '',
    'baseUrl': '',
    'username': '',
    'apiEndPoint': '',
    'vault': false,
    'bearerToken': false,
    'isOAuth': false,
    'jaasKrbAuth': false,
    'password': '',
    'patOAuthToken': '',
    'privateKey': '',
    'consumerKey': '',
    'jaasConfigFilePath': '',
    'krb5ConfigFilePath': '',
    'jaasUser': '',
    'samlEndPoint': '',
    'sharedConnection': false,
    'jiraAuthType': ''
  }
  jiraConnectionDialog: boolean = false;
  @Input() selectedToolName: string;
  groupedToolsGroup: any;

  constructor(private httpService: HttpService,
    private formBuilder: UntypedFormBuilder,
    private confirmationService: ConfirmationService,
    private testConnectionService: TestConnectionService,
    private authorization: GetAuthorizationService,
    private sharedService: SharedService,
    private helper: HelperService,
    private route: ActivatedRoute,
    public router: Router,private featureFlagService: FeatureFlagsService) {
  }

  ngOnInit(): void {

    this.roleAccessAssign();
    this.getConnectionList();
    this.connectionTypeFieldsAssignment();
    this.isRoleViewer = this.authorization.getRole() === 'roleViewer' ? true : false;

    this.currentUser = this.sharedService.getCurrentUserDetails('user_name') ? this.sharedService.getCurrentUserDetails('user_name') : '';
    this.getZephyrUrl();
    this.initializeForms(this.jiraConnectionFields);

    /** formating data for connection dropdown */
    this.groupedToolsGroup = this.createFormatCategoryWise(this.addEditConnectionFieldsNlabels);

    /* this.httpService.getAllToolConfigs().subscribe(res => {
      console.log(res)
    }) */
    this.selectedToolName = this.selectedToolName !== undefined ? this.selectedToolName : this.addEditConnectionFieldsNlabels[0].connectionType;
    this.selectedConnectionType = this.addEditConnectionFieldsNlabels.filter(el => el.connectionLabel === this.selectedToolName)[0]?.connectionType;
    this.enableRally();
  }

  initializeForms(connection, isEdit?) {
    for (let key in this.jiraConnectionFields) {
      this.jiraForm.controls[key] = new FormControl({ value: connection[key], disabled: false }, [Validators.required])
    }
    if (isEdit) {
      if (connection['vault']) {
        this.jiraForm.controls['jiraAuthType'].setValue('vault');
        this.onChangeAuthType('vault')
      } else if (connection['bearerToken']) {
        this.jiraForm.controls['jiraAuthType'].setValue('bearerToken');
        this.onChangeAuthType('bearerToken')
      } else if (connection['isOAuth']) {
        this.jiraForm.controls['jiraAuthType'].setValue('isOAuth');
        this.onChangeAuthType('isOAuth')
      } else if (connection['jaasKrbAuth']) {
        this.jiraForm.controls['jiraAuthType'].setValue('jaasKrbAuth');
        this.onChangeAuthType('jaasKrbAuth')
      } else {
        this.jiraForm.controls['jiraAuthType'].setValue('basic');
        this.onChangeAuthType('basic')
      }
    } else {
      this.jiraForm.controls['jiraAuthType'].setValue('basic');
      this.onChangeAuthType('basic')
    }
  }

  onChangeAuthType(event) {
    switch (event) {
      case 'vault':
        this.jiraForm.controls['vault'].setValue(true);
        this.jiraForm.controls['bearerToken'].setValue(false);
        this.jiraForm.controls['bearerToken'].disable();
        this.jiraForm.controls['isOAuth'].setValue(false);
        this.jiraForm.controls['isOAuth'].disable();
        this.jiraForm.controls['jaasKrbAuth'].setValue(false);
        this.jiraForm.controls['jaasKrbAuth'].disable();
        this.jiraForm.controls['password'].setValue('');
        this.jiraForm.controls['password'].disable();
        this.jiraForm.controls['patOAuthToken'].setValue('');
        this.jiraForm.controls['patOAuthToken'].disable();
        this.jiraForm.controls['privateKey'].setValue('');
        this.jiraForm.controls['privateKey'].disable();
        this.jiraForm.controls['consumerKey'].setValue('');
        this.jiraForm.controls['consumerKey'].disable();
        this.jiraForm.controls['jaasConfigFilePath'].setValue('');
        this.jiraForm.controls['jaasConfigFilePath'].disable();
        this.jiraForm.controls['krb5ConfigFilePath'].setValue('');
        this.jiraForm.controls['krb5ConfigFilePath'].disable();
        this.jiraForm.controls['jaasUser'].setValue('');
        this.jiraForm.controls['jaasUser'].disable();
        this.jiraForm.controls['samlEndPoint'].setValue('');
        this.jiraForm.controls['samlEndPoint'].disable();
        break;
      case 'bearerToken':
        this.jiraForm.controls['bearerToken'].setValue(true);
        this.jiraForm.controls['patOAuthToken'].enable();
        this.jiraForm.controls['vault'].setValue(false);
        this.jiraForm.controls['vault'].disable();
        this.jiraForm.controls['isOAuth'].setValue(false);
        this.jiraForm.controls['isOAuth'].disable();
        this.jiraForm.controls['jaasKrbAuth'].setValue(false);
        this.jiraForm.controls['jaasKrbAuth'].disable();
        this.jiraForm.controls['password'].setValue('');
        this.jiraForm.controls['password'].disable();
        this.jiraForm.controls['privateKey'].setValue('');
        this.jiraForm.controls['privateKey'].disable();
        this.jiraForm.controls['consumerKey'].setValue('');
        this.jiraForm.controls['consumerKey'].disable();
        this.jiraForm.controls['jaasConfigFilePath'].setValue('');
        this.jiraForm.controls['jaasConfigFilePath'].disable();
        this.jiraForm.controls['krb5ConfigFilePath'].setValue('');
        this.jiraForm.controls['krb5ConfigFilePath'].disable();
        this.jiraForm.controls['jaasUser'].setValue('');
        this.jiraForm.controls['jaasUser'].disable();
        this.jiraForm.controls['samlEndPoint'].setValue('');
        this.jiraForm.controls['samlEndPoint'].disable();
        break;
      case 'isOAuth':
        this.jiraForm.controls['isOAuth'].setValue(true);
        this.jiraForm.controls['password'].enable();
        this.jiraForm.controls['privateKey'].enable();
        this.jiraForm.controls['consumerKey'].enable();
        this.jiraForm.controls['vault'].setValue(false);
        this.jiraForm.controls['vault'].disable();
        this.jiraForm.controls['bearerToken'].setValue(false);
        this.jiraForm.controls['bearerToken'].disable();
        this.jiraForm.controls['jaasKrbAuth'].setValue(false);
        this.jiraForm.controls['jaasKrbAuth'].disable();
        this.jiraForm.controls['patOAuthToken'].setValue('');
        this.jiraForm.controls['patOAuthToken'].disable();
        this.jiraForm.controls['jaasConfigFilePath'].setValue('');
        this.jiraForm.controls['jaasConfigFilePath'].disable();
        this.jiraForm.controls['krb5ConfigFilePath'].setValue('');
        this.jiraForm.controls['krb5ConfigFilePath'].disable();
        this.jiraForm.controls['jaasUser'].setValue('');
        this.jiraForm.controls['jaasUser'].disable();
        this.jiraForm.controls['samlEndPoint'].setValue('');
        this.jiraForm.controls['samlEndPoint'].disable();
        break;
      case 'jaasKrbAuth':
        this.jiraForm.controls['jaasKrbAuth'].setValue(true);
        this.jiraForm.controls['jaasConfigFilePath'].enable();
        this.jiraForm.controls['krb5ConfigFilePath'].enable();
        this.jiraForm.controls['jaasUser'].enable();
        this.jiraForm.controls['samlEndPoint'].enable();
        this.jiraForm.controls['vault'].setValue(false);
        this.jiraForm.controls['vault'].disable();
        this.jiraForm.controls['bearerToken'].setValue(false);
        this.jiraForm.controls['bearerToken'].disable();
        this.jiraForm.controls['isOAuth'].setValue(false);
        this.jiraForm.controls['isOAuth'].disable();
        this.jiraForm.controls['password'].setValue('');
        this.jiraForm.controls['password'].disable();
        this.jiraForm.controls['patOAuthToken'].setValue('');
        this.jiraForm.controls['patOAuthToken'].disable();
        this.jiraForm.controls['privateKey'].setValue('');
        this.jiraForm.controls['privateKey'].disable();
        this.jiraForm.controls['consumerKey'].setValue('');
        this.jiraForm.controls['consumerKey'].disable();
        break;
      default:
        this.jiraForm.controls['vault'].setValue(false);
        this.jiraForm.controls['vault'].disable();
        this.jiraForm.controls['bearerToken'].setValue(false);
        this.jiraForm.controls['bearerToken'].disable();
        this.jiraForm.controls['isOAuth'].setValue(false);
        this.jiraForm.controls['isOAuth'].disable();
        this.jiraForm.controls['jaasKrbAuth'].setValue(false);
        this.jiraForm.controls['jaasKrbAuth'].disable();
        this.jiraForm.controls['patOAuthToken'].setValue('');
        this.jiraForm.controls['patOAuthToken'].disable();
        this.jiraForm.controls['privateKey'].setValue('');
        this.jiraForm.controls['privateKey'].disable();
        this.jiraForm.controls['consumerKey'].setValue('');
        this.jiraForm.controls['consumerKey'].disable();
        this.jiraForm.controls['jaasConfigFilePath'].setValue('');
        this.jiraForm.controls['jaasConfigFilePath'].disable();
        this.jiraForm.controls['krb5ConfigFilePath'].setValue('');
        this.jiraForm.controls['krb5ConfigFilePath'].disable();
        this.jiraForm.controls['jaasUser'].setValue('');
        this.jiraForm.controls['jaasUser'].disable();
        this.jiraForm.controls['samlEndPoint'].setValue('');
        this.jiraForm.controls['samlEndPoint'].disable();
        break;
    }
    this.jiraForm.updateValueAndValidity();
  }

  getZephyrUrl() {
    this.httpService.getZephyrUrl().subscribe(response => {
      if (response.data) {
        this.zephyrUrl = response.data;
      }

    });
  }

  /* Assign role along with project Id */
  roleAccessAssign() {
    /* Temporary commented because as per the requirement all users needs access */
    this.isConnectionAddEditAccess = true;
  }

  connectionTypeFieldsAssignment() {
    this.fieldsObj = {};
    this.addEditConnectionFieldsNlabels.forEach(connectionObj => {
      if (!!this.selectedConnectionType && !!connectionObj.connectionType && this.selectedConnectionType.toLowerCase() === connectionObj.connectionType.toLowerCase()) {
        connectionObj.inputFields.forEach(field => {
          if (!this.isNewlyConfigAdded && this.nonMendatoryFieldsOnEditConnection.indexOf(field) > -1) {
            this.fieldsObj[field] = [{ value: '', disabled: false }];
          } else {
            this.fieldsObj[field] = [{ value: '', disabled: false }, Validators.required];
          }
        });
      }
    });
    this.basicConnectionForm = this.formBuilder.group(this.fieldsObj);
  }

  onChangeConnection() {
    this.connection['type'] = this.selectedConnectionType;
    this.connectionTypeFieldsAssignment();
    this.defaultEnableDisableSwitch();
    this.testConnectionMsg = '';
  }

  createConnection() {
    this.connection = { type: this.selectedConnectionType, sharedConnection: true };
    this.isNewlyConfigAdded = true;
    if (this.selectedConnectionType?.toLowerCase() === 'jira') {
      this.jiraConnectionDialog = true;
      this.jiraForm.controls['type'].setValue(this.connection.type);
      this.initializeForms(this.jiraConnectionFields);
    } else {
      this.submitted = false;
      this.connectionDialog = true;
      this.connectionTypeFieldsAssignment();
      this.basicConnectionForm.controls['type']?.setValue(this.connection.type);
      this.defaultEnableDisableSwitch();
      this.disableEnableCheckBox();
    }

  }

  deleteConnection(connection) {
    this.confirmationService.confirm({
      message: `Do you want to delete ${connection.connectionName}?`,
      header: 'Delete this connection',
      icon: 'pi pi-info-circle',
      key: 'confirmToDeleteDialog',
      accept: () => {
        this.httpService.deleteConnection(connection).subscribe(response => {
          this.reloadConnections(response);
          this.getConnectionList();
        });
      },
      reject: () => {
        console.log('Do not delete it');
      }
    });
  }

  reloadConnections(response) {
    if (response.success) {
      this.getConnectionList();
    } else {
      let projectListHTML = '<div class="project-list-container"><ul class="project-list">';
      response.data.forEach(element => {
        projectListHTML += `<li>${element}</li>`;
      });
      projectListHTML += '</ul></div>';
      this.confirmationService.confirm({
        message: response.message + ' in <br/>' + projectListHTML,
        header: 'Connection Deletion Status',
        key: 'cannotDeleteMessageDialog',
      });
    }
  }

  getConnectionList() {
    this.httpService.getAllConnections().subscribe(response => {
      this.renderConnectionList(response);
    });
  }

  renderConnectionList(response) {
    // Initialize connection list for all types
    this.connectionTypeCompleteList.forEach(eachConnection => {
      this.connectionListAllType[eachConnection.value.toString()] = [];
    });

    if (!response.data.length) return;

    response.data.forEach(eachConnection => {
      const connectionType = eachConnection.type;

      // Clear sensitive fields
      ['password', 'pat', 'accessToken', 'privateKey', 'consumerKey', 'apiKey'].forEach(field => {
        if (eachConnection[field]) {
          eachConnection[field] = '';
        }
      });

      // Add connection to the appropriate type list
      if (this.connectionListAllType[connectionType]) {
        this.connectionListAllType[connectionType].push(eachConnection);
      }
    });
  }

  saveConnection() {
    const reqData = {};
    this.submitted = true;
    if (this.jiraForm.invalid && this.basicConnectionForm.invalid) {
      return;
    }
    if (this.connection?.type?.toLowerCase() == 'jira') {
      for (let key in this.jiraForm.controls) {
        if (this.jiraForm.controls[key]?.value) {
          reqData[key] = this.jiraForm.controls[key]?.value;
        }
      }
    } else {
      this.addEditConnectionFieldsNlabels.forEach(data => {
        if (!!this.connection.type && !!data.connectionType && (this.connection.type.toLowerCase() === data.connectionType.toLowerCase())) {
          data.inputFields.forEach(inputField => {
            if (this.basicConnectionForm.value[inputField] !== undefined && this.basicConnectionForm.value[inputField] !== '' && this.basicConnectionForm.value[inputField] !== 'undefined') {
              reqData[inputField] = this.basicConnectionForm.value[inputField];
            }
          });
        }
      });
    }

    if (!!this.connection['id']) {
      reqData['id'] = this.connection['id'];
    }

    if (!!this.connection['password']) {
      reqData['password'] = this.connection['password'];
    }

    if (!!this.connection['patOAuthToken']) {
      reqData['patOAuthToken'] = this.connection['patOAuthToken'];
    }

    if (!!this.connection['pat']) {
      reqData['pat'] = this.connection['pat'];
    }

    if (!!this.connection['accessToken'] && this.connection['type'].toLowerCase() !== 'zephyr') {
      reqData['accessToken'] = this.connection['accessToken'];
    }

    if (!!this.connection['apiKey']) {
      reqData['apiKey'] = this.connection['apiKey'];
    }

    if (this.connection['type']?.toLowerCase() === 'zephyr' && this.connection['cloudEnv']) {
      reqData['baseUrl'] = this.basicConnectionForm.controls['baseUrl']['value'];
    }

    if (this.connection['type']?.toLowerCase() === 'sonar' && this.connection['cloudEnv'] === true) {
      reqData['accessTokenEnabled'] = true;
    }

    if (this.isNewlyConfigAdded) {
      this.addConnectionReq(reqData);
    } else {
      this.editConnectionReq(reqData);
    }

  }

  updateForm() {
    this.jiraForm.updateValueAndValidity();
  }

  addConnectionReq(reqData) {
    this.httpService.addConnection(reqData).subscribe(response => {
      this.renderCreateUpdateConnectionStatus(response, 'Connection Creation Status');
    }, error => {
      this.hideDialog();
    });
  }

  editConnectionReq(reqData) {
    this.httpService.editConnection(reqData).subscribe(response => {
      this.renderCreateUpdateConnectionStatus(response, 'Connection Updation Status');
    }, error => {
      this.hideDialog();
    });
  }

  renderCreateUpdateConnectionStatus(response, header) {
    if (!response.success && !!response.message) {
      this.confirmationService.confirm({
        message: response.message,
        header,
        icon: 'fa fa-times-circle text-danger',
        key: 'connectionStatus',
        accept: () => {
        },
        reject: () => {
        }
      });
    }
    this.hideDialog();
    this.getConnectionList();

  }

  editConnection(connection) {
    this.connection = { ...connection };
    this.connection['username'] = '';
    this.isNewlyConfigAdded = false;
    this.selectedConnectionType = this.connection.type;
    if (connection.type?.toLowerCase() == 'jira') {
      this.jiraConnectionDialog = true;
      this.initializeForms(this.connection, true)
    } else {
      this.connectionDialog = true;
      this.connectionTypeFieldsAssignment();
      this.basicConnectionForm.controls['type']?.setValue(this.selectedConnectionType);
      this.defaultEnableDisableSwitch();
      this.disableEnableCheckBox();
      if (connection.type.toLowerCase() == 'bitbucket' && connection.cloudEnv == true) {
        this.checkBitbucketValue(true, 'cloudEnv', connection.type.toLowerCase());
      } else if (connection.type.toLowerCase() == 'zephyr') {
        this.checkZephyr();
      }
    }
  }

  disableEnableCheckBox() {
    if (!this.connection.sharedConnection) {
      this.basicConnectionForm.controls['sharedConnection']?.disable();
    } else {
      this.basicConnectionForm.controls['sharedConnection']?.enable();
    }
  }

  hideDialog() {
    this.connectionDialog = false;
    this.jiraConnectionDialog = false;
    this.submitted = false;
    this.isNewlyConfigAdded = false;
    this.testConnectionMsg = '';
    this.testConnectionValid = true;
  }

  defaultEnableDisableSwitch() {
    /* Default Enable/Disable Fields on the basis of default flags at one time */
    Object.keys(this.enableDisableOnToggle.enableDisableEachTime).forEach(field => {
      if (Object.keys(this.basicConnectionForm.value).indexOf(field) > -1) {
        if (this.enableDisableOnToggle.enableDisableEachTime[field].length) {
          this.enableDisableOnToggle.enableDisableEachTime[field].forEach(element => {
            if (this.connection[field] === true) {
              this.basicConnectionForm.value[field] = true;
              this.connection[field] = true;
              if (!!this.basicConnectionForm.controls[element.field]) {
                this.basicConnectionForm.controls[element.field].enable();
              }
            } else {
              this.basicConnectionForm.value[field] = false;
              this.connection[field] = false;
              if (!!this.basicConnectionForm.controls[element.field]) {
                this.basicConnectionForm.controls[element.field].disable();
              }
            }
          });
        } else {
          if (this.connection[field] !== undefined && this.connection[field] !== 'undefined' && this.connection[field] !== '') {
            this.basicConnectionForm.value[field] = this.connection[field];
          } else {
            this.basicConnectionForm.value[field] = false;
            this.connection[field] = false;
          }
        }
      }
    });

    /* Default Enable/Disable Fields on the basis of default flags at another time */
    Object.keys(this.enableDisableOnToggle.enableDisableAnotherTime).forEach(field => {
      if (Object.keys(this.basicConnectionForm.value).indexOf(field) > -1) {
        if (this.enableDisableOnToggle.enableDisableAnotherTime[field].length) {
          this.enableDisableOnToggle.enableDisableAnotherTime[field].forEach(element => {
            if (this.connection[field] === true && !!this.basicConnectionForm.controls[element.field]) {
              this.basicConnectionForm.controls[element.field].disable();
            } else if (this.connection[field] === false && !!this.basicConnectionForm.controls[element.field]) {
              this.basicConnectionForm.controls[element.field].enable();
            }
          });
        }
      }
    });

    if (!!this.basicConnectionForm.controls['isOAuth'] && this.connection['isOAuth'] === true) {
      this.basicConnectionForm.controls['privateKey'].enable();
      this.basicConnectionForm.controls['consumerKey'].enable();
    } else if (!!this.basicConnectionForm.controls['isOAuth'] && this.connection['isOAuth'] === false) {
      this.basicConnectionForm.controls['privateKey'].disable();
      this.basicConnectionForm.controls['consumerKey'].disable();
    } else if (this.selectedConnectionType?.toLowerCase() === 'zephyr' && !!this.basicConnectionForm.controls['cloudEnv'] && this.connection['cloudEnv'] === true) {
      this.basicConnectionForm.controls['username'].disable();
      this.basicConnectionForm.controls['password'].disable();
      this.basicConnectionForm.controls['baseUrl'].disable();
      this.basicConnectionForm.controls['accessToken'].enable();
    } else if (this.selectedConnectionType?.toLowerCase() === 'zephyr' && !!this.basicConnectionForm.controls['cloudEnv'] && this.connection['cloudEnv'] === false) {
      this.basicConnectionForm.controls['accessToken'].disable();
      this.basicConnectionForm.controls['username'].enable();
      this.basicConnectionForm.controls['password'].enable();
    } else if (this.selectedConnectionType?.toLowerCase() === 'sonar' && !!this.basicConnectionForm.controls['cloudEnv'] && this.connection['cloudEnv'] === true) {
      this.basicConnectionForm.controls['username'].disable();
      this.basicConnectionForm.controls['password'].disable();
      this.basicConnectionForm.controls['accessTokenEnabled'].disable();
      this.basicConnectionForm.controls['accessToken'].enable();
    } else if (this.selectedConnectionType?.toLowerCase() === 'sonar' && !!this.basicConnectionForm.controls['cloudEnv'] && this.connection['cloudEnv'] === false) {
      this.basicConnectionForm.controls['username'].enable();
      this.basicConnectionForm.controls['password'].enable();
      this.basicConnectionForm.controls['accessTokenEnabled'].enable();
      this.basicConnectionForm.controls['accessToken'].disable();
    }

    if (this.selectedConnectionType?.toLowerCase() === 'sonar' && !!this.basicConnectionForm.controls['vault'] && this.connection['vault'] === true) {
      this.basicConnectionForm.controls['password'].disable();
      this.basicConnectionForm.controls['accessToken'].disable();
      this.basicConnectionForm.controls['accessTokenEnabled'].disable();
    }
    if (this.selectedConnectionType?.toLowerCase() === 'sonar' && !!this.basicConnectionForm.controls['accessTokenEnabled'] && !!this.connection['accessTokenEnabled'] === true) {
      this.basicConnectionForm.controls['username'].disable();
      this.basicConnectionForm.controls['password'].disable();
      this.basicConnectionForm.controls['accessToken'].enable();
    }
  }

  enableDisableSwitch(event, field, type?) {
    /* Enable/Disable fields on the basis of flag selection at one time */
    if (this.enableDisableOnToggle.enableDisableEachTime[field]?.length) {
      this.enableDisableOnToggle.enableDisableEachTime[field].forEach(element => {
        this.setEnabledOrDisabled(element, event);
      });
    }
    /* Enable/Disable fields on the basis of flag selection at second time */
    if (this.enableDisableOnToggle.enableDisableAnotherTime[field]?.length) {
      this.enableDisableOnToggle.enableDisableAnotherTime[field].forEach(element => {
        this.setEnabledOrDisabled(element, event);
      });
    }

    if (field === 'cloudEnv' && type.toLowerCase() === 'sonar') {
      if (event.checked) {
        this.basicConnectionForm.controls['accessTokenEnabled']?.setValue(true);
      } else {
        this.basicConnectionForm.controls['accessTokenEnabled']?.setValue(false);
      }
    }

    this.checkBitbucketValue(event.checked, field, type);
    if (type?.toLowerCase() == 'zephyr') {
      this.checkZephyr();
    }
    this.enableDisableFieldsOnIsCloudSwithChange();
  }

  setEnabledOrDisabled(element, event) {
    if (event.checked) {
      this.basicConnectionForm.controls[element.field]?.enable();
    } else {
      this.basicConnectionForm.controls[element.field]?.disable();
    }
  }

  testConnection() {
    this.testingConnection = true;
    const reqData = {};
    if (this.connection?.type?.toLowerCase() == 'jira') {
      for (let key in this.jiraForm.controls) {
        reqData[key] = this.jiraForm.controls[key]?.value;
      }
    } else {
      this.addEditConnectionFieldsNlabels.forEach(data => {
        if (!!this.connection.type && !!data.connectionType && (this.connection.type.toLowerCase() === data.connectionType.toLowerCase())) {
          data.inputFields.forEach(inputField => {
            if (this.basicConnectionForm.value[inputField] !== undefined && this.basicConnectionForm.value[inputField] !== '' && this.basicConnectionForm.value[inputField] !== 'undefined') {
              reqData[inputField] = this.basicConnectionForm.value[inputField];
            }
          });
        }
      });
    }

    if (this.connection['type'].toLowerCase() === 'zephyr' && this.connection['cloudEnv']) {
      reqData['baseUrl'] = this.basicConnectionForm.controls['baseUrl']['value'];
    }
    if (reqData['vault'] == true) {
      reqData['password'] = '';
      reqData['pat'] = '';
      reqData['accessToken'] = '';
      reqData['apiKey'] = '';
    }

    if (this.connection['type'].toLowerCase() === 'sonar' && this.connection['cloudEnv'] === true) {
      reqData['accessTokenEnabled'] = true;
    }

    this.testConnectionMsg = '';
    this.testConnectionValid = true;
    switch (this.connection.type) {
      case 'Jira':
        this.testConnectionService.testJira(reqData['baseUrl'], reqData['apiEndPoint'], reqData['username'], reqData['password'], reqData['vault'], reqData['bearerToken'], reqData['patOAuthToken'], reqData['jaasKrbAuth'], reqData['jaasConfigFilePath'], reqData['krb5ConfigFilePath'], reqData['jaasUser'], reqData['samlEndPoint']).subscribe(next => {
          if (next.success && next.data === 200) {
            this.testConnectionMsg = 'Valid Connection';
            this.testConnectionValid = true;
          } else {
            this.testConnectionMsg = 'Connection Invalid';
            this.testConnectionValid = false;
          }
          this.testingConnection = false;
        }, error => {
          this.testConnectionMsg = 'Connection Invalid';
          this.testConnectionValid = false;
          this.testingConnection = false;
        });

        break;
      case 'Azure':
        this.testConnectionService.testAzureBoards(reqData['baseUrl'], reqData['username'], reqData['pat'], reqData['vault'])
          .subscribe(next => {
            if (next.success && next.data === 200) {
              this.testConnectionMsg = 'Valid Connection';
              this.testConnectionValid = true;
            } else {
              this.testConnectionMsg = 'Connection Invalid';
              this.testConnectionValid = false;
            }
            this.testingConnection = false;
          }, error => {
            this.testConnectionMsg = 'Connection Invalid';
            this.testConnectionValid = false;
            this.testingConnection = false;
          });
        break;
      case 'GitLab': this.testConnectionService.testGitLab(reqData['baseUrl'], reqData['accessToken'], reqData['vault']).subscribe(next => {
        if (next.success && next.data === 200) {
          this.testConnectionMsg = 'Valid Connection';
          this.testConnectionValid = true;
        } else {
          this.testConnectionMsg = 'Connection Invalid';
          this.testConnectionValid = false;
        }
        this.testingConnection = false;
      }, error => {
        this.testConnectionMsg = 'Connection Invalid';
        this.testConnectionValid = false;
        this.testingConnection = false;
      });
        break;
      case 'Bitbucket': this.testConnectionService.testBitbucket(reqData['baseUrl'], reqData['username'], reqData['password'], reqData['apiEndPoint'], reqData['cloudEnv'], reqData['vault']).subscribe(next => {
        if (next.success && next.data === 200) {
          this.testConnectionMsg = 'Valid Connection';
          this.testConnectionValid = true;
        } else {
          this.testConnectionMsg = 'Connection Invalid';
          this.testConnectionValid = false;
        }
        this.testingConnection = false;
      }, error => {
        this.testConnectionMsg = 'Connection Invalid';
        this.testConnectionValid = false;
        this.testingConnection = false;
      });
        break;
      case 'Sonar':
        this.testConnectionService.testSonar(reqData['baseUrl'], reqData['username'], reqData['password'], reqData['accessToken'], reqData['cloudEnv'], reqData['vault'], reqData['accessTokenEnabled']).subscribe(next => {
          if (next.success && next.data === 200) {
            this.testConnectionMsg = 'Valid Connection';
            this.testConnectionValid = true;
          } else {
            this.testConnectionMsg = 'Connection Invalid';
            this.testConnectionValid = false;
          }
          this.testingConnection = false;
        }, error => {
          this.testConnectionMsg = 'Connection Invalid';
          this.testConnectionValid = false;
          this.testingConnection = false;
        });
        break;
      case 'Jenkins': this.testConnectionService.testJenkins(reqData['baseUrl'], reqData['username'], reqData['apiKey'], reqData['vault']).subscribe(next => {
        if (next.success && next.data === 200) {
          this.testConnectionMsg = 'Valid Connection';
          this.testConnectionValid = true;
        } else {
          this.testConnectionMsg = 'Connection Invalid';
          this.testConnectionValid = false;
        }
        this.testingConnection = false;
      }, error => {
        this.testConnectionMsg = 'Connection Invalid';
        this.testConnectionValid = false;
        this.testingConnection = false;
      });
        break;
      case 'NewRelic': this.testConnectionService.testNewRelic(reqData['apiEndPoint'], reqData['apiKey'], reqData['apiKeyFieldName']).subscribe(next => {
        this.testConnectionMsg = 'Valid Connection';
        this.testConnectionValid = true;
        this.testingConnection = false;
      }, error => {
        this.testConnectionMsg = 'Connection Invalid';
        this.testConnectionValid = false;
        this.testingConnection = false;
      });

        break;
      case 'Bamboo': this.testConnectionService.testBamboo(reqData['baseUrl'], reqData['username'], reqData['password'], reqData['vault']).subscribe(next => {
        if (next.success && next.data === 200) {
          this.testConnectionMsg = 'Valid Connection';
          this.testConnectionValid = true;
        } else {
          this.testConnectionMsg = 'Connection Invalid';
          this.testConnectionValid = false;
        }
        this.testingConnection = false;
      }, error => {
        this.testConnectionMsg = 'Connection Invalid';
        this.testConnectionValid = false;
        this.testingConnection = false;
      });
        break;
      case 'Teamcity': this.testConnectionService.testTeamCity(reqData['baseUrl'], reqData['username'], reqData['password'], reqData['vault']).subscribe(next => {
        if (next.success && next.data === 200) {
          this.testConnectionMsg = 'Valid Connection';
          this.testConnectionValid = true;
        } else {
          this.testConnectionMsg = 'Connection Invalid';
          this.testConnectionValid = false;
        }
        this.testingConnection = false;
      }, error => {
        this.testConnectionMsg = 'Connection Invalid';
        this.testConnectionValid = false;
        this.testingConnection = false;
      });
        break;
      case 'AzurePipeline':
        this.testConnectionService.testAzurePipeline(reqData['baseUrl'], 'dummyUserName', reqData['pat'], reqData['vault'])
          .subscribe(next => {
            if (next.success && next.data === 200) {
              this.testConnectionMsg = 'Valid Connection';
              this.testConnectionValid = true;
            } else {
              this.testConnectionMsg = 'Connection Invalid';
              this.testConnectionValid = false;
            }
            this.testingConnection = false;
          }, error => {
            this.testConnectionMsg = 'Connection Invalid';
            this.testConnectionValid = false;
            this.testingConnection = false;
          });
        break;
      case 'AzureRepository':
        this.testConnectionService.testAzureRepository(reqData['baseUrl'], 'dummyUserName', reqData['pat'], reqData['vault'])
          .subscribe(next => {
            if (next.success && next.data === 200) {
              this.testConnectionMsg = 'Valid Connection';
              this.testConnectionValid = true;
            } else {
              this.testConnectionMsg = 'Connection Invalid';
              this.testConnectionValid = false;
            }
            this.testingConnection = false;
          }, error => {
            this.testConnectionMsg = 'Connection Invalid';
            this.testConnectionValid = false;
            this.testingConnection = false;
          });
        break;
      case 'GitHub': this.testConnectionService.testGithub(reqData['baseUrl'], reqData['username'], reqData['accessToken'], reqData['vault']).subscribe(next => {
        if (next.success && next.data === 200) {
          this.testConnectionMsg = 'Valid Connection';
          this.testConnectionValid = true;
        } else {
          this.testConnectionMsg = 'Connection Invalid';
          this.testConnectionValid = false;
        }
        this.testingConnection = false;
      }, error => {
        this.testConnectionMsg = 'Connection Invalid';
        this.testConnectionValid = false;
        this.testingConnection = false;
      });
        break;

      case 'Zephyr': this.testConnectionService.testZephyr(reqData['baseUrl'], reqData['username'], reqData['password'], reqData['apiEndPoint'], reqData['accessToken'], reqData['cloudEnv'], reqData['vault'], reqData['bearerToken'], reqData['patOAuthToken']).subscribe(next => {
        if (next.success && next.data === 200) {
          this.testConnectionMsg = 'Valid Connection';
          this.testConnectionValid = true;
        } else {
          this.testConnectionMsg = 'Connection Invalid';
          this.testConnectionValid = false;
        }
        this.testingConnection = false;
      }, error => {
        this.testConnectionMsg = 'Connection Invalid';
        this.testConnectionValid = false;
        this.testingConnection = false;
      });
        break;
      case 'ArgoCD': this.testConnectionService.testArgoCD(reqData['baseUrl'], reqData['username'], reqData['accessToken']).subscribe(next => {
        if (next.success && next.data === 200) {
          this.testConnectionMsg = 'Valid Connection';
          this.testConnectionValid = true;
        } else {
          this.testConnectionMsg = 'Connection Invalid';
          this.testConnectionValid = false;
        }
        this.testingConnection = false;
      }, error => {
        this.testConnectionMsg = 'Connection Invalid';
        this.testConnectionValid = false;
        this.testingConnection = false;
      });
        break;
      case 'Rally': this.testConnectionService.testRally(reqData['baseUrl'], reqData['accessToken']).subscribe(next => {
        if (next.success && next.data === 200) {
          this.testConnectionMsg = 'Valid Connection';
          this.testConnectionValid = true;
        } else {
          this.testConnectionMsg = 'Connection Invalid';
          this.testConnectionValid = false;
        }
        this.testingConnection = false;
      }, error => {
        this.testConnectionMsg = 'Connection Invalid';
        this.testConnectionValid = false;
        this.testingConnection = false;
      });
        break;
    }
  }

  showInfo(type, field) {
    let tooltipText = '';
    if (type == 'github' && field == 'baseUrl') {
      tooltipText = 'Url i.e : for public github this url will be https://api.github.com.';
    }
    if (type == 'github' && field == 'username') {
      tooltipText = 'The name appended before your repository name (i.e. ownerName/repositoryName).';
    }
    return tooltipText;
  }

  checkBitbucketValue(event, field, type) {
    /** to add information besides username and password labels for bitbucket when isCloudEnv = true */
    if (type == 'bitbucket') {
      const tempArr = [...this.addEditConnectionFieldsNlabels];
      const bitbucketObj = tempArr.filter((item) => item.connectionLabel.toLowerCase() == 'bitbucket')[0];
      if (!bitbucketObj) {
        // Exit if the bitbucketObj is not found
        return;
      }
      if (this.basicConnectionForm.controls['cloudEnv'].value) {
        bitbucketObj.labels = ['Connection Type', 'Connection Name', 'Is Cloud Environment', 'Base Url', 'Username (Profile Username)', 'Use vault password', 'Password (App Password)', 'API End Point', 'User Email', 'Share connection with everyone'];
      } else {
        bitbucketObj.labels = ['Connection Type', 'Connection Name', 'Is Cloud Environment', 'Base Url', 'Username', 'Use vault password', 'Password', 'API End Point', 'User Email', 'Share connection with everyone'];
      }
      const index = tempArr.findIndex((item) => item.connectionLabel.toLowerCase() == 'bitbucket');
      if (index !== -1) {
        tempArr[index] = bitbucketObj;
        this.addEditConnectionFieldsNlabels = [...tempArr];
      }
    }
  }

  checkZephyr() {
    /** to add information besides username and password labels for bitbucket when isCloudEnv = true */
    if (this.connection['type']?.toLowerCase() == 'zephyr') {
      if (this.connection['vault'] == true && this.connection['cloudEnv'] == true) {
        this.basicConnectionForm.controls['baseUrl'].setValue(this.zephyrUrl);
        this.basicConnectionForm.controls['baseUrl'].disable();
        this.basicConnectionForm.controls['apiEndPoint'].setValue('');
        this.basicConnectionForm.controls['apiEndPoint'].disable();
        this.basicConnectionForm.controls['username'].setValue('');
        this.basicConnectionForm.controls['username'].disable();
        this.basicConnectionForm.controls['password'].setValue('');
        this.basicConnectionForm.controls['password'].disable();
        this.basicConnectionForm.controls['accessToken'].setValue('');
        this.basicConnectionForm.controls['accessToken'].disable();
        this.basicConnectionForm.controls['patOAuthToken'].setValue('');
        this.basicConnectionForm.controls['patOAuthToken'].disable();
        this.basicConnectionForm.controls['bearerToken'].disable();
      } else if (this.connection['vault'] == true && this.connection['cloudEnv'] == false) {
        this.basicConnectionForm.controls['baseUrl'].enable();
        this.basicConnectionForm.controls['apiEndPoint'].enable();
        this.basicConnectionForm.controls['username'].enable();
        this.basicConnectionForm.controls['password'].setValue('');
        this.basicConnectionForm.controls['password'].disable();
        this.basicConnectionForm.controls['accessToken'].setValue('');
        this.basicConnectionForm.controls['accessToken'].disable();
        this.basicConnectionForm.controls['patOAuthToken'].setValue('');
        this.basicConnectionForm.controls['patOAuthToken'].disable();
        this.basicConnectionForm.controls['bearerToken'].disable();
      } else if (this.connection['vault'] == false && this.connection['cloudEnv'] == true) {
        this.basicConnectionForm.controls['baseUrl'].setValue(this.zephyrUrl);
        this.basicConnectionForm.controls['baseUrl'].disable();
        this.basicConnectionForm.controls['apiEndPoint'].setValue('');
        this.basicConnectionForm.controls['apiEndPoint'].disable();
        this.basicConnectionForm.controls['username'].setValue('');
        this.basicConnectionForm.controls['username'].disable();
        this.basicConnectionForm.controls['password'].setValue('');
        this.basicConnectionForm.controls['password'].disable();
        this.basicConnectionForm.controls['accessToken']?.enable();
        this.basicConnectionForm.controls['patOAuthToken'].setValue('');
        this.basicConnectionForm.controls['patOAuthToken'].disable();
        this.basicConnectionForm.controls['bearerToken'].disable();
      } else if (this.connection['bearerToken'] == true) {
        this.basicConnectionForm.controls['patOAuthToken'].enable();
        this.basicConnectionForm.controls['password'].setValue('');
        this.basicConnectionForm.controls['password'].disable();
        this.basicConnectionForm.controls['accessToken'].setValue('');
        this.basicConnectionForm.controls['accessToken'].disable();
        this.basicConnectionForm.controls['vault'].disable();
        this.basicConnectionForm.controls['cloudEnv'].disable();
      } else {
        this.basicConnectionForm.controls['baseUrl'].enable();
        this.basicConnectionForm.controls['apiEndPoint'].enable();
        this.basicConnectionForm.controls['username'].enable();
        this.basicConnectionForm.controls['password'].enable();
        this.basicConnectionForm.controls['accessToken'].setValue('');
        this.basicConnectionForm.controls['accessToken'].disable();
        this.basicConnectionForm.controls['bearerToken'].setValue('false');
        this.basicConnectionForm.controls['patOAuthToken'].disable();
        this.basicConnectionForm.controls['bearerToken'].enable();
        this.basicConnectionForm.controls['vault'].enable();
        this.basicConnectionForm.controls['cloudEnv'].enable();
      }
    }
  }
  enableDisableFieldsOnIsCloudSwithChange() {
    if (this.connection['type']?.toLowerCase() == 'sonar') {
      if (this.connection['vault'] == true && this.connection['cloudEnv'] == true) {
        this.basicConnectionForm.controls['username'].setValue('');
        this.basicConnectionForm.controls['username'].disable();
        this.basicConnectionForm.controls['password'].setValue('');
        this.basicConnectionForm.controls['password'].disable();
        this.basicConnectionForm.controls['accessToken'].setValue('');
        this.basicConnectionForm.controls['accessToken'].disable();
      } else if (this.connection['vault'] == true && this.connection['cloudEnv'] == false) {
        this.basicConnectionForm.controls['username'].enable();
        this.basicConnectionForm.controls['password'].setValue('');
        this.basicConnectionForm.controls['password'].disable();
        this.basicConnectionForm.controls['accessToken'].setValue('');
        this.basicConnectionForm.controls['accessToken'].disable();
      } else if (this.connection['vault'] == false && this.connection['cloudEnv'] == true) {
        this.basicConnectionForm.controls['username'].setValue('');
        this.basicConnectionForm.controls['username'].disable();
        this.basicConnectionForm.controls['password'].setValue('');
        this.basicConnectionForm.controls['password'].disable();
        this.basicConnectionForm.controls['accessToken']?.enable();
      } else {
        this.basicConnectionForm.controls['username'].enable();
        this.basicConnectionForm.controls['password'].enable();
        this.basicConnectionForm.controls['accessToken'].setValue('');
        this.basicConnectionForm.controls['accessToken'].disable();
      }

      if (this.connection['cloudEnv'] === true || this.connection['vault'] === true) {
        this.basicConnectionForm.controls['accessTokenEnabled'].disable();
      } else {
        this.basicConnectionForm.controls['accessTokenEnabled'].enable();
        this.enableDisableFieldsOnAccessTokenORPasswordToggle();
      }
    }
  }

  enableDisableFieldsOnAccessTokenORPasswordToggle() {
    if (this.connection['accessTokenEnabled'] === true) {
      this.basicConnectionForm.controls['username'].setValue('');
      this.basicConnectionForm.controls['username'].disable();
      this.basicConnectionForm.controls['password'].setValue('');
      this.basicConnectionForm.controls['password'].disable();
      this.basicConnectionForm.controls['accessToken']?.enable();
    } else {
      this.basicConnectionForm.controls['password'].enable();
      this.basicConnectionForm.controls['accessToken']?.setValue('');
      this.basicConnectionForm.controls['accessToken']?.disable();
    }
  }

  emptyUrlInZephyr() {
    if (this.basicConnectionForm.controls['type'].value.toLowerCase() == 'zephyr'
      && this.basicConnectionForm.controls['cloudEnv'].value
      && this.basicConnectionForm.controls['baseUrl'].value == '') {
      return false;
    } else {
      return true;
    }
  }

  createFormatCategoryWise(addEditConnectionFieldsNlabels) {
    const formatedData = addEditConnectionFieldsNlabels.reduce((acc, curr) => {
      // Find the category in the accumulator
      let category = acc.find(item => item.label === curr.categoryLabel);

      // If the category doesn't exist, create a new one
      if (!category) {
        category = {
          label: curr.categoryLabel,
          value: curr.categoryValue,
          items: []
        };
        acc.push(category);
      }
      // Add the connection label and type to the category's items
      category.items.push({ label: curr.connectionLabel, value: curr.connectionType });
      return acc;
    }, []);
    return formatedData;
  }
  async enableRally(){
    this.rallyEnabled = await this.featureFlagService.isFeatureEnabled('Rally');
    if(this.rallyEnabled) {
      this.addEditConnectionFieldsNlabels.push({
        connectionType: 'Rally',
        connectionLabel: 'Rally',
        categoryValue: 'projectManagement',
        categoryLabel: 'Project Management',
        labels: ['Connection Type', 'Connection Name', 'Base Url', 'Access Token', 'Share connection with everyone'],
        inputFields: ['type', 'connectionName', 'baseUrl', 'accessToken', 'sharedConnection']
      });
      this.connectionTypeCompleteList.push({
        label: 'Rally',
          value: 'Rally',
        connectionTableCols: [
        { field: 'connectionName', header: 'Connection Name', class: 'long-text' },
        { field: 'baseUrl', header: 'Base URL', class: 'long-text' }
      ]
      });
    }
  }

}
