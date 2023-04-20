package com.publicissapient.kpidashboard.jira.spnego;

import com.atlassian.httpclient.api.Request;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;

public class SpnegoAuthenticationHandler implements AuthenticationHandler {

    private static final String COOKIE_HEADER = "Cookie";

    private final String authCookies;

    public SpnegoAuthenticationHandler(final String authCookies){
        this.authCookies = authCookies;
    }

    @Override
    public void configure(Request.Builder builder) {
        builder.setHeader(COOKIE_HEADER, authCookies);
    }
}
