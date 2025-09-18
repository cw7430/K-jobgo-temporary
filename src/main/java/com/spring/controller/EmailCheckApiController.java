package com.spring.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.spring.client.service.CmpInfoService;


@RestController
@RequestMapping("/api")
public class EmailCheckApiController {

    private final CmpInfoService cmpInfoService;

    @Autowired
    public EmailCheckApiController(CmpInfoService cmpInfoService) {
        this.cmpInfoService = cmpInfoService;
    }

    public static class EmailCheckRequest {
        private String email;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    @PostMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailDuplicate(@RequestBody EmailCheckRequest req) {
        String bizEmail = req.getEmail();
        Map<String, Object> resp = new HashMap<>();

        if (bizEmail == null || bizEmail.isBlank()) {
            resp.put("error", "이메일이 비어 있습니다.");
            return ResponseEntity.badRequest().body(resp);
        }

        if (!bizEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            resp.put("error", "유효한 이메일 형식이 아닙니다.");
            return ResponseEntity.badRequest().body(resp);
        }

        boolean exists = cmpInfoService.existsByBizEmail(bizEmail);
        resp.put("duplicate", exists);
        return ResponseEntity.ok(resp);
    }
}
