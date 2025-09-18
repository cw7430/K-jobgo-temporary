package com.spring.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AdminInsertLoginData {
    public static void main(String[] args) {
        // 행정사 계정 정보 배열: {이름, 로그인ID, 원문비밀번호, 전화번호, 이메일, 직무, 지역, 권한ID}
        String[][] admins = {
           // {"이수환",   "01033352541", "dltnghks6^", "010-3335-2541", "dltnghks27@naver.com", "방문상담", "전북", "3"}
        	/*	{"이준우", "kjobgo0000", "a12b34**", "", "", "행정사", "", "5"},
        		{"허자연", "kjobgo1111", "f01f01##", "", "", "행정사", "", "5"},
        		{"차재욱", "kjobgo2222", "x78x78!!", "", "", "행정사", "", "5"},
        		{"케이잡스", "kjobgo3333", "v56v56^^", "", "", "행정사", "", "5"} */
        		{"이선아", "kjobgo5555", "jj34j56$$", "NULL", "NULL", "행정사", "NULL", "5"} // 빈값으로 하지말고 NULL 로 값 넣어서 출력하기!!!
        };

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        for (String[] admin : admins) {
            // 원문 비밀번호를 bcrypt로 해시
            String encodedPassword = passwordEncoder.encode(admin[2]);

            // INSERT 문 출력
            System.out.println(
                "INSERT INTO admin (admin_name, admin_login_id, admin_password, admin_phone, admin_email, job_duty, job_area, authority_id) VALUES ('"
                + admin[0] + "', '"
                + admin[1] + "', '"
                + encodedPassword + "', '"
                + admin[3] + "', '"
                + admin[4] + "', '"
                + admin[5] + "', '"
                + admin[6] + "', "
                + admin[7] + ");"
            );
        }
    }
}