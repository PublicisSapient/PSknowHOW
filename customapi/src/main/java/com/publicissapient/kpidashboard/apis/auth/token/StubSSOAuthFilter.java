package com.publicissapient.kpidashboard.apis.auth.token;

import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.rbac.UserTokenData;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

@Component
public class StubSSOAuthFilter implements Filter {

    private static final String AUTH_RESPONSE_HEADER = "X-Authentication-Token";
    private static final String ROLES_CLAIM = "roles";
    private static final String DETAILS_CLAIM = "details";

    public static final String USERNAME = "testssouser";


    @Autowired
    private AuthProperties tokenAuthProperties;

    @Autowired
    private CookieUtil cookieUtil;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        String jwt = Jwts.builder().setSubject(USERNAME)
                .claim(DETAILS_CLAIM, AuthType.SSO)
                .claim(ROLES_CLAIM, Arrays.asList(Constant.ROLE_SUPERADMIN))
                .setExpiration(new Date(System.currentTimeMillis() + tokenAuthProperties.getExpirationTime()))
                .signWith(SignatureAlgorithm.HS512, tokenAuthProperties.getSecret()).compact();

        httpServletResponse.addHeader(AUTH_RESPONSE_HEADER, jwt);
        httpServletResponse.addHeader("username", USERNAME);
        Cookie cookie = cookieUtil.createAccessTokenCookie(jwt);
        httpServletResponse.addCookie(cookie);
        cookieUtil.addSameSiteCookieAttribute(httpServletResponse);
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
