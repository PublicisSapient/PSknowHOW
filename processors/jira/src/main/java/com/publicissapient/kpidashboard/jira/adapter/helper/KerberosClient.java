package com.publicissapient.kpidashboard.jira.adapter.helper;



import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.web.client.RestClientException;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import java.io.IOException;
import java.security.Principal;
import java.security.PrivilegedAction;

public class KerberosClient {
    private static final Credentials credentials = new NullCredentials();
    private String jaasConfigFilePath;
    private String krb5ConfigFilePath;
    private HttpClient loginHttpClient;
    private HttpClient httpClient;
    private BasicCookieStore cookieStore;
    private String JaasUser;
    private String samlEndPoint;
    private String jiraHost;

    public KerberosClient(){}

    public KerberosClient(String JaasConfigFilePath, String krb5ConfigFilePath, String JaasUser,
                          String samlEndPoint,String jiraHost) {
        this.jaasConfigFilePath = JaasConfigFilePath;
        this.krb5ConfigFilePath = krb5ConfigFilePath;
        this.loginHttpClient = buildLoginHttpClient();
        this.cookieStore = new BasicCookieStore();
        this.httpClient = buildHttpClient();
        this.JaasUser = JaasUser;
        this.samlEndPoint = samlEndPoint;
        this.jiraHost = jiraHost;
    }

    public BasicCookieStore getCookieStore() {
        return cookieStore;
    }


    public String getJiraHost() {
        return jiraHost;
    }

    private HttpClient buildLoginHttpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        Lookup<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create().register("negotiate", new SPNegoSchemeFactory(true)).build();
        builder.setDefaultAuthSchemeRegistry(authSchemeRegistry);
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope((String)null, -1, (String)null), credentials);
        builder.setDefaultCredentialsProvider(credentialsProvider);
        CloseableHttpClient httpClient = builder.setDefaultCookieStore(cookieStore).build();
        return httpClient;
    }

    private HttpClient buildHttpClient(){
        return HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
    }

    public String login(){
        try {
            String loginURL = this.samlEndPoint + this.jiraHost;
            System.setProperty("java.security.auth.login.config",this.jaasConfigFilePath);
            System.setProperty("java.security.krb5.conf",this.krb5ConfigFilePath);
            System.setProperty("javax.security.auth.useSubjectCredsOnly","false");
            System.setProperty("http.auth.preference","SPNEGO");
            LoginContext lc = new LoginContext(this.JaasUser);
            lc.login();
            Subject serviceSubject = lc.getSubject();
            PrivilegedAction<String> action = ()-> {
                try {
                    return loginCall(loginURL);
                } catch (IOException e) {
                    throw new RestClientException("error while logging in"+e.getMessage());
                }
            };
            return Subject.doAs(serviceSubject, action);
        } catch (Exception ex) {
            throw new RestClientException("Error running rest call "+ ex.getMessage());
        }
    }

    private String loginCall(String loginURL) throws IOException {
        HttpUriRequest getRequest = RequestBuilder.get().setUri(loginURL).build();
        HttpResponse response =  this.loginHttpClient.execute(getRequest);
        HttpEntity entity = response.getEntity();
        String loginResponse =  EntityUtils.toString(entity, "UTF-8");
        if(null != loginResponse && !loginResponse.equalsIgnoreCase("")){
            System.out.println(loginResponse);
            generateSamlCookies(loginResponse);
        }else {
            loginResponse = null;
        }
        return loginResponse;
    }

    public void generateSamlCookies(String loginResponse) throws IOException {
        String samlToken = extractString(loginResponse,"<input type=\"hidden\" name=\"SAMLResponse\" value=\"","\"/>");
        String samlURL = extractString(loginResponse, "<form method=\"post\" action=\"","\">");
        System.out.println(samlURL+">>>"+samlToken);
        HttpUriRequest postRequest = RequestBuilder.post().
                setUri(samlURL)
                .setHeader(HttpHeaders.ACCEPT,"application/json")
                .setHeader(HttpHeaders.CONTENT_TYPE,"application/x-www-form-urlencoded")
                .addParameter("SAMLResponse",samlToken)
                .build();

        this.httpClient.execute(postRequest);
    }

    public String getResponse(HttpUriRequest httpUriRequest) throws IOException {
        httpUriRequest.addHeader("Cookie",getCookies());
        HttpResponse response =  this.httpClient.execute(httpUriRequest);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, "UTF-8");
    }

    private String extractString(String input,String start, String end){
        String[] strArray = input.split(start);
        if(strArray.length > 1){
            String[] value = strArray[1].split(end);
            if(value.length > 1){
                return value[0];
            }
        }
        return null;
    }


    public String getCookies(){
        StringBuilder cookieHeaderBuilder = new StringBuilder();
        this.getCookieStore().getCookies().forEach(cookie -> cookieHeaderBuilder.append(cookie).append(";"));
        return cookieHeaderBuilder.toString();
    }

    private static class NullCredentials implements Credentials {
        private NullCredentials() {
        }

        public Principal getUserPrincipal() {
            return null;
        }

        public String getPassword() {
            return null;
        }
    }
}
