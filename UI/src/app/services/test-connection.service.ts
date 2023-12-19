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

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TestConnectionService {

  constructor(private http: HttpClient) { }

  /** get: test JIRA connection */

  testJira(baseUrl, apiEndPoint, username, password, vault, bearerToken, patOAuthToken, jaasKrbAuth, jaasConfigFilePath, krb5ConfigFilePath, jaasUser, samlEndPoint): Observable<any> {
    const postData = {
      baseUrl,
      username,
      password: password ? password : '',
      vault,
      patOAuthToken: patOAuthToken ? patOAuthToken : '',
      bearerToken,
      jaasKrbAuth,
      jaasConfigFilePath,
      krb5ConfigFilePath,
      jaasUser,
      samlEndPoint
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/jira', postData
      , { headers }
    );
  }

  testZephyr(baseUrl, username, password, apiEndPoint, accessToken, cloudEnv, vault, bearerToken, patOAuthToken): Observable<any> {
    const postData = {
      baseUrl,
      username,
      password: password ? password : '',
      apiEndPoint,
      accessToken,
      cloudEnv,
      vault,
      bearerToken,
      patOAuthToken
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/zephyr', postData
      , { headers }
    );
  }

  testAzureBoards(baseUrl, username, pat, vault): Observable<any> {
    const postData = {
      baseUrl,
      username,
      password: pat ? pat : '',
      vault
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/azureboard', postData
      , { headers }
    );
  }

    testGitLab(baseUrl, accessToken, vault): Observable<any> {
      const postData = {
        baseUrl,
        accessToken: accessToken ? accessToken : '',
        vault
      };
      let headers: HttpHeaders = new HttpHeaders();
      headers = headers.append('requestArea', 'thirdParty');
      return this.http.post(environment.baseUrl + '/api/testconnection/gitlab', postData
        , { headers }
      );
    }

  testBitbucket(baseUrl, username, password, apiEndPoint, cloudEnv, vault): Observable<any> {
    const postData = {
      baseUrl,
      username,
      password: password ? password : '',
      apiEndPoint,
      cloudEnv,
      vault
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/bitbucket', postData
      , { headers }
    );
  }

  testSonar(baseUrl, username, password, accesstoken, cloudEnv, vault, accessTokenEnabled): Observable<any> {

    let postData = {};

    if (cloudEnv) {

      postData = {
        baseUrl,
        accessToken: accesstoken ? accesstoken : '',
        cloudEnv: true,
        vault,
        accessTokenEnabled
      };
    } else {
      postData = {
        baseUrl,
        cloudEnv: false,
        vault,
        accessTokenEnabled : accessTokenEnabled === undefined ? false : accessTokenEnabled
      };

      if (accessTokenEnabled) {
        postData['accessToken'] = accesstoken ? accesstoken : '';
      } else {
        postData['password'] = password ? password : '';
        postData['username'] =  username;
      }
    }

    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/sonar', postData
      , { headers }
    );
  }

  testJenkins(baseUrl, username, apiKey, vault): Observable<any> {
    const postData = {
      baseUrl,
      username,
      apiKey: apiKey ? apiKey : '',
      vault
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/jenkins', postData
      , { headers }
    );
  }

  testNewRelic(apiEndPoint, apiKey, apiKeyFieldName): Observable<any> {
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'external');
    headers = headers.append(apiKeyFieldName, apiKey);
    return this.http.get(`${apiEndPoint}Select * from Metric`, {
      headers
    });
  }

  testBamboo(baseUrl, username, password, vault): Observable<any> {
    const postData = {
      baseUrl,
      username,
      password: password ? password : '',
      vault
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/bamboo', postData
      , { headers }
    );
  }

  testTeamCity(baseUrl, username, password, vault): Observable<any> {
    const postData = {
      baseUrl,
      username,
      password: password ? password : '',
      vault
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/teamcity', postData
      , { headers }
    );
  }

  testAzurePipeline(baseUrl, username, pat, vault): Observable<any> {
    const postData = {
      baseUrl,
      username,
      password: pat ? pat : '',
      vault
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/azurepipeline', postData
      , { headers }
    );
  }

  testAzureRepository(baseUrl, username, pat, vault): Observable<any> {
    const postData = {
      baseUrl,
      username,
      password: pat ? pat : '',
      vault
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/azurerepo', postData
      , { headers }
    );

  }

  testGithub(baseUrl, username, accessToken, vault): Observable<any> {
    const postData = {
      baseUrl,
      username,
      accessToken: accessToken ? accessToken : '',
      vault
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/github', postData
      , { headers }
    );
  }

  testRepoTool(httpUrl, repoToolProvider, username, accessToken, email): Observable<any> {
    let postData = {};

    postData = {
      httpUrl,
      repoToolProvider,
      username,
      accessToken,
      email
    };
    let headers: HttpHeaders = new HttpHeaders();
    headers = headers.append('requestArea', 'thirdParty');
    return this.http.post(environment.baseUrl + '/api/testconnection/repotool', postData
      , { headers }
    );
  }



}
