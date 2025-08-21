package com.spring.repository;

import com.spring.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    // 전체 관리자 조회 (사실 JpaRepository 기본 제공 메서드라 생략 가능)
    List<Admin> findAll();

    // 로그인 ID로 관리자 조회 (공백 제거 후 비교)
    @Query("SELECT a FROM Admin a WHERE TRIM(a.adminLoginId) = TRIM(:adminLoginId)")
    Admin findByAdminLoginId(@Param("adminLoginId") String adminLoginId);
    
}
