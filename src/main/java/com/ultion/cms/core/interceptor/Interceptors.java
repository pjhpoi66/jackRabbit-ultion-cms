package com.ultion.cms.core.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.models.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;

/**
 * <pre>
 * Controller에 대한 Exception 처리하는 Advice
 * </pre>
 */
@Slf4j
@Component
public class Interceptors extends HandlerInterceptorAdapter {

    /**
     * <pre>
     * client 의 요청을 controller 에 전달하기 전 호출
     * return false 일 경우 controller 응답을 전달하지 않음
     * </pre>
     */
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object obj) throws Exception {

        if (HttpMethod.OPTIONS.toString().equals(req.getMethod())) {
            return true;
        }
        RereadableRequestWrapper request = new RereadableRequestWrapper(req);
        log.info("request uri : [{}] {}", req.getMethod(), request.getRequestURI());
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        log.info("request ip : {} ", ip);

        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headers = req.getHeaders(headerName);
            while (headers.hasMoreElements()) {
                String headerValue = headers.nextElement();
                log.info(headerName + " : " + headerValue);
            }
        }

        if (!request.getRequestURI().equals("/sw/upload")) {
            if (req.getMethod().equals("GET")) {
                log.info("request params : " + request.getParameter());
            } else {
                String body = request.getBody();
                body = body.replaceAll(" ", "");
                body = body.replaceAll("\n", "");
                body = body.replaceAll("\t", "");
                log.info("request params : " + body);
            }
        }

        return true;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws JsonProcessingException, UnsupportedEncodingException {
        if (modelAndView != null) {
            Cookie[] cookies = request.getCookies();
            String labName = "";
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("CAGOS_CLINIC_PORTAL_NAME")) {
                        labName = URLDecoder.decode(cookie.getValue(),"UTF-8");
                    }
                    if (cookie.getName().equals("CAGOS_CLINIC_PORTAL_CSS_NAME")) {
                        modelAndView.addObject("CAGOS_CLINIC_PORTAL_CSS_NAME", cookie.getValue());
                    }
                    if (cookie.getName().equals("CAGOS_CLINIC_PORTAL_LOGO_URL")) {
                        modelAndView.addObject("CAGOS_CLINIC_PORTAL_LOGO_URL", URLDecoder.decode(cookie.getValue(),"UTF-8"));
                    }
                }
            }

            if(modelAndView.getModel().get("lnbTitle") != null && !labName.equals("")){
                modelAndView.addObject("lnbTitle", modelAndView.getModel().get("lnbTitle") + " (<a href='/profile'>"+labName+"</a>)");
            }
            if(modelAndView.getModel().get("deptTitle") != null && !labName.equals("")
                && modelAndView.getModel().get("deptTitle").toString().startsWith("<span>")){

                modelAndView.addObject("deptTitle", labName + modelAndView.getModel().get("deptTitle"));

            }
        }
    }
}
