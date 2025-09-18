// src/main/java/com/spring/interceptor/ApprovedCompanyInterceptor.java
package com.spring.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class ApprovedCompanyInterceptor implements HandlerInterceptor {
  @Override
  public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
    HttpSession session = req.getSession(false);
    // 1) 미로그인(기업)
    if (session == null || session.getAttribute("loggedInClient") == null) {
      String redirectTo = URLEncoder.encode(req.getRequestURI() +
          (req.getQueryString() != null ? ("?" + req.getQueryString()) : ""), StandardCharsets.UTF_8);
      res.sendRedirect("/loginPage?redirectTo=" + redirectTo); // 로그인 후 원위치
      return false;
    }
    // 2) 로그인은 했지만 승인 X
    String st = (String) session.getAttribute("clientApprStatus"); // "APPROVED"/"PENDING"/"REJECTED"
    if (!"APPROVED".equals(st)) {
      res.sendRedirect("/my/join-status"); // 안내 페이지
      return false;
    }
    return true;
  }
}
