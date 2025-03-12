/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC. Further development Copyright 2022 Sapient
 * Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.jira.client;

/*
 * @RunWith(PowerMockRunner.class)
 *
 * @PrepareForTest(JiraOAuthClient.class)
 *
 * @PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*",
 * "javax.management.*" }) public class JiraOAuthClientpowerMockTest {
 *
 * private JiraOAuthClient jiraOAuthClient;
 *
 * @Before public void setUp() { jiraOAuthClient = new JiraOAuthClient(); }
 *
 * @Test public void getOAuthVerifierOtherTest() throws Exception {
 *
 * // Mock the WebClient class WebClient mockWebClient = mock(WebClient.class);
 * whenNew(WebClient.class).withNoArguments().thenReturn(mockWebClient);
 * WebClientOptions webClientOptions = mock(WebClientOptions.class);
 * when(mockWebClient.getOptions()).thenReturn(webClientOptions);
 *
 * // Mock the HtmlPage class HtmlPage mockHtmlPage = mock(HtmlPage.class);
 * when(mockWebClient.getPage(anyString())).thenReturn(mockHtmlPage);
 *
 * // Mock the HtmlForm class HtmlForm mockHtmlForm = mock(HtmlForm.class);
 * when(mockHtmlPage.getHtmlElementById("login-form")).thenReturn(mockHtmlForm);
 * // HtmlTextInput mockTxtUser = mock(HtmlTextInput.class); //
 * when(mockHtmlForm.getInputByName("os_password")).thenReturn(mockTxtUser); //
 * when(mockHtmlForm.getInputByName("login")).thenReturn(mockTxtUser);
 *
 * // Mock other necessary classes and methods as needed
 *
 * // Set up expected values String expectedVerifier = "mockedVerifier";
 * when(mockHtmlPage.getUrl()).thenReturn(new
 * URL("http://mocked-url?oauth_verifier=" + expectedVerifier));
 *
 * // Call the method to test String actualVerifier // =
 * yourObject.getOAuthVerifier("mockedAuthorizationUrl", "mockedUsername", //
 * "mockedPassword"); =
 * jiraOAuthClient.getOAuthVerifier("mockedAuthorizationUrl", "uName",
 * "password");
 *
 * // Verify that the expected methods were called verify(mockHtmlPage,
 * times(1)).getUrl(); verify(mockWebClient, times(2)).getPage(anyString());
 *
 * }
 *
 * }
 */
