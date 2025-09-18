// src/main/java/com/spring/client/service/ClientAuthService.java
package com.spring.client.service;

import com.spring.client.entity.CmpInfo;

public interface ClientAuthService {
    CmpInfo authenticate(String email, String rawPassword);
}