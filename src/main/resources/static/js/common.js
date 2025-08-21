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
});
