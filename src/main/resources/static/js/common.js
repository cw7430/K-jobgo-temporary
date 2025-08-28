/* ========= Console Manager (dev/test 허용, prod 차단) ========= */
(function () {
  // 1) 환경값 주입(택1): <body data-env="dev|test|prod"> 또는 window.APP_ENV
  var ENV =
    (document.body && document.body.dataset && document.body.dataset.env) ||
    (typeof window !== "undefined" && window.APP_ENV) ||
    "dev"; // 기본 dev

  // 2) 환경별 정책
  var POLICY = {
    dev:  { keep: "all" },         // 개발: 전체 허용
    test: { keep: ["error","warn","log"] }, // 테스트: error/warn/log만
    prod: { keep: [] }             // 운영: 전부 차단(원하면 ["error"] 등으로 조절)
  };

  var p = POLICY[ENV] || POLICY.dev;
  if (p.keep === "all") return; // 전체 허용이면 아무것도 안 함

  var keep = Array.isArray(p.keep) ? p.keep : [];
  var noop = function () {};
  var methods = [
    "log","debug","info","trace","table",
    "group","groupCollapsed","groupEnd",
    "time","timeLog","timeEnd",
    "dir","dirxml","count","countReset",
    "assert","clear","profile","profileEnd"
  ];

  // console 객체 보정
  if (typeof window !== "undefined" && !window.console) window.console = {};
  methods.forEach(function (m) {
    if (!keep.includes(m) && typeof console[m] === "function") {
      console[m] = noop;
    }
  });
})();


// 👉 기존 코드
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
    if (
      menu && toggleBtn &&
      !menu.classList.contains("hidden-menu") &&
      !menu.contains(e.target) &&
      !toggleBtn.contains(e.target)
    ) {
      menu.classList.add("hidden-menu");
    }
  });
});
