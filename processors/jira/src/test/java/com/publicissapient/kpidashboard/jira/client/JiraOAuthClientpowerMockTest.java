package com.publicissapient.kpidashboard.jira.client;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JiraOAuthClient.class)
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*" })
public class JiraOAuthClientpowerMockTest {

	private JiraOAuthClient jiraOAuthClient;

	@Before
	public void setUp() {
		jiraOAuthClient = new JiraOAuthClient();
	}

	@Test
	public void getOAuthVerifierOtherTest() throws Exception {

		// Mock the WebClient class
		WebClient mockWebClient = mock(WebClient.class);
		whenNew(WebClient.class).withNoArguments().thenReturn(mockWebClient);
		WebClientOptions webClientOptions = mock(WebClientOptions.class);
		when(mockWebClient.getOptions()).thenReturn(webClientOptions);

		// Mock the HtmlPage class
		HtmlPage mockHtmlPage = mock(HtmlPage.class);
		when(mockWebClient.getPage(anyString())).thenReturn(mockHtmlPage);

		// Mock the HtmlForm class
		HtmlForm mockHtmlForm = mock(HtmlForm.class);
		when(mockHtmlPage.getHtmlElementById("login-form")).thenReturn(mockHtmlForm);
		// HtmlTextInput mockTxtUser = mock(HtmlTextInput.class);
		// when(mockHtmlForm.getInputByName("os_password")).thenReturn(mockTxtUser);
		// when(mockHtmlForm.getInputByName("login")).thenReturn(mockTxtUser);

		// Mock other necessary classes and methods as needed

		// Set up expected values
		String expectedVerifier = "mockedVerifier";
		when(mockHtmlPage.getUrl()).thenReturn(new URL("http://mocked-url?oauth_verifier=" + expectedVerifier));

		// Call the method to test
		String actualVerifier // = yourObject.getOAuthVerifier("mockedAuthorizationUrl", "mockedUsername",
							  // "mockedPassword");
				= jiraOAuthClient.getOAuthVerifier("mockedAuthorizationUrl", "uName", "password");

		// Verify that the expected methods were called
		verify(mockHtmlPage, times(1)).getUrl();
		verify(mockWebClient, times(2)).getPage(anyString());

	}

}
