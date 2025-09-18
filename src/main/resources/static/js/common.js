// === 전역 노출: 승인대기 다이얼로그 ===
window.openPendingDialog = function () {
  document.getElementById('pendingDialog')?.showModal();
};

// === 전역 노출: 로그인 유도 게이트 ===
window.openLoginGate = function (e, node) {
  if (e) e.preventDefault();

  alert('로그인 후 이용 가능합니다.');

  // 이동 목적지 (없으면 구직신청으로)
  const redirectTo = node?.dataset?.redirectTo || '/client/applyEmp';

  // 'client' | 'admin' 등 짧은 키를 받아 'clientLogin' | 'adminLogin' 으로 정규화
  const raw  = node?.dataset?.loginTab || 'client';
  const full = raw.endsWith('Login') ? raw : (raw + 'Login'); // ★ 핵심 포인트

  try {
    localStorage.setItem('postLoginRedirect', redirectTo);
    localStorage.setItem('loginTab', full); // 'clientLogin' 형식으로 저장
  } catch (_) {}

  // 열려 있는 메뉴 닫기
  document.getElementById('menu')?.classList.add('hidden-menu');

  // 로그인 버튼 클릭(모달 오픈 트리거) → 해당 탭으로 즉시 전환
  const loginBtn = document.getElementById('clientLoginBtn') || document.getElementById('loginBtn');
  if (loginBtn) {
    loginBtn.click();
    setTimeout(() => {
      document.querySelector(`.tab-btn[data-tab="${full}"]`)?.click();
    }, 0);
  } else {
    // 로그인 버튼이 없는 화면이라면 로그인 페이지로 이동
    window.location.href = '/loginPage';
  }
  return false;
};


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
    prod: { keep: ["error"] }             // 운영: 전부 차단(원하면 ["error"] 등으로 조절)
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


/* ========= 공통 메뉴 토글 & 접근성 ========= */
window.addEventListener("DOMContentLoaded", function () {
  console.log("공통 JS 실행");

  const toggleBtn = document.querySelector(".menu-toggle");
  const menu = document.getElementById("menu");

  // 접근성: ARIA 상태 동기화
  if (toggleBtn) {
    toggleBtn.setAttribute("aria-haspopup", "true");
    toggleBtn.setAttribute("aria-expanded", "false");
    toggleBtn.setAttribute("aria-controls", "menu");
  }

  function openMenu() {
    if (!menu) return;
    menu.classList.remove("hidden-menu");
    toggleBtn?.setAttribute("aria-expanded", "true");
  }
  function closeMenu() {
    if (!menu) return;
    menu.classList.add("hidden-menu");
    toggleBtn?.setAttribute("aria-expanded", "false");
  }
  function isOpen() {
    return !!(menu && !menu.classList.contains("hidden-menu"));
  }

  // 1) 토글 버튼으로 열고/닫기
  if (toggleBtn && menu) {
    toggleBtn.addEventListener("click", function (e) {
      e.stopPropagation(); // 바깥 클릭 핸들러와 충돌 방지(버블 단계만)
      isOpen() ? closeMenu() : openMenu();
    });
  }

  // 2) 메뉴 내부 클릭 처리
  if (menu) {
    // ⚠️ 캡처 단계 전파 차단 제거(충돌 원인) —> 기본 버블 단계 리스너만 사용
    menu.addEventListener("click", function (e) {
      // a[href] 기본 이동은 브라우저에 맡김. 우리는 메뉴만 닫기.
      const a = e.target.closest("a[href]");
      if (a) {
        const isNewTab =
          e.metaKey || e.ctrlKey || e.shiftKey || e.button === 1 || a.target === "_blank";
        if (!isNewTab) {
          // 기본 이동 전에 메뉴만 닫기 (preventDefault 사용하지 않음)
          closeMenu();
        }
        return; // 브라우저 기본 동작 수행
      }

      // (옵션) 레거시: li[onclick] 에 location.href='...'
      const li = e.target.closest('li[onclick]');
      if (li) {
        const m = li.getAttribute('onclick')?.match(/location\.href\s*=\s*['"]([^'"]+)['"]/);
        if (m && m[1]) {
          closeMenu();
          // 여기서는 수동 이동 필요
          window.location.href = m[1];
        }
      }
    }, false);
  }

  // 3) 외부 클릭 시 닫기
  document.addEventListener("click", function (e) {
    if (!menu || !toggleBtn) return;
    if (isOpen() && !menu.contains(e.target) && !toggleBtn.contains(e.target)) {
      closeMenu();
    }
  });

  // 4) ESC로 닫기
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape" && isOpen()) {
      closeMenu();
      toggleBtn?.focus();
    }
  });
 });