package com.publicissapient.kpidashboard.apis.config;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.context.ExecutionLogContext;

@Slf4j
@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private AuthenticationService authenticationService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
		ExecutionLogContext context = ExecutionLogContext.getContext();
		try {
			Optional<Authentication> authentication = Optional
					.ofNullable(SecurityContextHolder.getContext().getAuthentication());
			if (authentication.isPresent() && authentication.get().isAuthenticated()) {
				context.setUserName(authentication.get().getPrincipal().toString());
				ExecutionLogContext.set(context);
				log.info("Setting the ExecutionContext with username {}",
						authentication.get().getPrincipal().toString());
			}
		} catch (Exception ex) {
			log.error("Exception while setting username in context");
		}
		return true;
	}

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response, Object object, ModelAndView model)
            throws Exception {
        MDC.remove(CommonConstant.USER_NAME);
    }
}
