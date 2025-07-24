// js/common.js
window.addEventListener("DOMContentLoaded", function () {
  console.log("공통 JS 실행");

  const toggleBtn = document.querySelector(".menu-toggle");
  const menu = document.getElementById("menu");

  // 1. 토글 버튼 클릭 시 메뉴 열고/닫기
  if (toggleBtn && menu) {
    toggleBtn.addEventListener("click", function () {
      menu.classList.toggle("hidden-menu");
    });
  }

  // 2. 외부 클릭 시 메뉴 닫기
  document.addEventListener("click", function (e) {
    if (menu && toggleBtn && 
        !menu.classList.contains("hidden-menu") && // 열려있을 때만 처리
        !menu.contains(e.target) &&
        !toggleBtn.contains(e.target)) {
      menu.classList.add("hidden-menu");
    }
  });
  
  const isAdmin = document.body.getAttribute('data-is-admin') === 'true';

  // 로그인 버튼 동작 정의
  const loginBtn = document.getElementById("loginBtn");
  if (loginBtn) {
    loginBtn.addEventListener("click", function () {
      if (isAdmin) {
        // 로그아웃 요청
        location.href = "/logout";  // 로그아웃 경로 설정 필요
      } else {
        document.getElementById("loginModal").style.display = 'flex';
      }
    });
  }
});
