package com.spring.web;

import java.util.*;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServerUrlProvider {

  @Value("${app.reset.base-url}")
  private String fallback;

  @Value("${app.reset.allowed-hosts:k-jobgo.com}")
  private String allowedHostsProp;

  private Set<String> allowedHosts() {
    return Arrays.stream(allowedHostsProp.split(","))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .collect(Collectors.toSet());
  }

  public String currentBaseUrl(HttpServletRequest req) {
    String scheme = Optional.ofNullable(req.getHeader("X-Forwarded-Proto")).orElse(req.getScheme());
    String host   = Optional.ofNullable(req.getHeader("X-Forwarded-Host"))
                    .orElseGet(() -> {
                      String h = req.getHeader("Host");
                      if (h != null && !h.isBlank()) return h;
                      int port = req.getServerPort();
                      String p = (port == 80 || port == 443) ? "" : ":" + port;
                      return req.getServerName() + p;
                    });

    String hostOnly = host.contains(":") ? host.substring(0, host.indexOf(':')) : host;
    if (!allowedHosts().contains(hostOnly)) {
      // 허용되지 않은 호스트면 안전한 폴백 사용
      return fallback;
    }
    return scheme + "://" + host;
  }
}
