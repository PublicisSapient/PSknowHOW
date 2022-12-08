package com.publicissapient.kpidashboard.apis.auth.token;

import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.rbac.UserTokenData;
import com.sun.deploy.net.HttpResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;

@Component
public class StubSSOUserService {

    private static final String AUTH_RESPONSE_HEADER = "X-Authentication-Token";
    public static final String USERNAME = "testssouser";
    private static final String ROLES_CLAIM = "roles";
    private static final String DETAILS_CLAIM = "details";


    @Autowired
    private AuthProperties tokenAuthProperties;

    @Autowired
    private CookieUtil cookieUtil;

    public String createJwtToken(){
        return Jwts.builder().setSubject(USERNAME)
                .claim(DETAILS_CLAIM, AuthType.SSO)
                .claim(ROLES_CLAIM, Arrays.asList(Constant.ROLE_SUPERADMIN))
                .setExpiration(new Date(System.currentTimeMillis() + tokenAuthProperties.getExpirationTime()))
                .signWith(SignatureAlgorithm.HS512, tokenAuthProperties.getSecret()).compact();

    }

    public String getUsername(){
        return USERNAME;
    }

}
