package com.nex.common;

import com.nex.user.entity.SessionInfoDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class HttpInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
//        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
//        response.setHeader("Expires", "0"); // Proxies.

//        HttpSession session = request.getSession(false);
//        HttpSession session = getTempSession(request);      ////////////////////////////////////////////////////// commit 반영안할것
        SessionInfoDto session = getSession(request);

//        log.info(request.getRequestURI());

        if(session == null) {
            response.sendRedirect("/user/login");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
//        log.debug("[postHandle]");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
//        log.debug("[afterCompletion]");
    }

    private SessionInfoDto getSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
//        SessionInfoDto sessionInfoDto = SessionInfoDto.builder().userId("admin").userNm("홍길동").isAdmin(true).build();
//        session.setAttribute("LOGIN_SESSION", sessionInfoDto);
        SessionInfoDto sessionInfo = (SessionInfoDto) session.getAttribute(Consts.LOGIN_SESSION);
        return sessionInfo;
    }
}
