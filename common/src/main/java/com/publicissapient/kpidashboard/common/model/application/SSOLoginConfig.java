package com.publicissapient.kpidashboard.common.model.application;

import lombok.Data;

@Data
public class SSOLoginConfig {
    private String clientID;
    private String secretRef;
    private String environment;
    private String discoveryURI;
    private String callbackUri;
    private String cookieDomain;
    private String jwksUrl;
    private String issuer;
    private String cookiePassRef;
}