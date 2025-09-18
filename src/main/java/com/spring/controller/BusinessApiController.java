package com.spring.controller;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/business")
public class BusinessApiController {

    @Autowired
    private RestTemplate restTemplate;

    // ✅ 요청 DTO 클래스
    public static class BusinessRequest {
        private String[] b_no;

        public String[] getB_no() {
            return b_no;
        }

        public void setB_no(String[] b_no) {
            this.b_no = b_no;
        }
    }

    // ✅ POST 요청 처리
    @PostMapping("/verify")
    public ResponseEntity<String> verifyBusinessNumber(@RequestBody BusinessRequest request) {
        System.out.println("✅ 받은 b_no 배열: " + Arrays.toString(request.getB_no()));

     // 2. 아래처럼 인코딩된 인증키로 수정 ✅
        String encodedServiceKey = "LJLYhKcoMLo0kTHeqqKGx1uH8invRLw3pEOhM527uo3R7xc2HA2aMsforKIeCT%2BW0m2w7179bzAb8p55iZtm9Q%3D%3D";

        URI uri = UriComponentsBuilder
                .fromUriString("https://api.odcloud.kr/api/nts-businessman/v1/status")
                .queryParam("serviceKey", encodedServiceKey) // 🔑 반드시 인코딩된 키 사용
                .queryParam("returnType", "JSON")
                .build(true) // 반드시 true로 설정해줘야 이중 인코딩 없이 전달됨
                .toUri();

        // ✅ 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // ✅ 요청 본문
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("b_no", request.getB_no());

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 디버깅 로그
            System.out.println("✅ 요청 URI: " + uri);
            System.out.println("✅ 요청 바디: " + new ObjectMapper().writeValueAsString(requestBody));

            // ✅ POST 요청 전송
            ResponseEntity<String> response = restTemplate.postForEntity(uri, httpEntity, String.class);

            // ✅ 응답 성공 처리
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ API 응답: " + response.getBody());
                return ResponseEntity.ok(response.getBody());
            } else {
                System.err.println("❌ API 호출 실패 상태 코드: " + response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode()).body("API 호출 실패");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"서버 오류 발생: " + e.getMessage() + "\"}");
        }
    }
}
