/* adminMain.js - 관리자 허브 UX 스크립트
   기능:
   1) 카드 전체 클릭 가능(접근성 유지)
   2) 키보드 엔터/스페이스로 진입
   3) 최근 방문 저장 → 다음 접속 시 카드에 '최근' 배지
   4) eval 제거, 링크 기본동작 보존 (Ctrl/Cmd+클릭 등)
*/
(function () {
  // UL 선택: 기존 선택자 + 명시 클래스 둘 다 지원
  const hubList =
    document.querySelector('.admin-menu-grid') ||
    document.querySelector('body.all-page ul[th\\:if]');
  if (!hubList) return;

  function extractHrefFromOnclick(onclickStr) {
    if (!onclickStr) return '';
    // onclick="location.href='/path'" 또는 "location.assign('/path')" 지원
    const m1 = onclickStr.match(/location\.href\s*=\s*['"]([^'"]+)['"]/);
    if (m1 && m1[1]) return m1[1];
    const m2 = onclickStr.match(/location\.(assign|replace)\s*\(\s*['"]([^'"]+)['"]\s*\)/);
    if (m2 && m2[2]) return m2[2];
    return '';
  }

  hubList.querySelectorAll(':scope > li').forEach((li) => {
    const link = li.querySelector('a.menu-link');
    const onclickStr = li.getAttribute('onclick');
    const onclickHref = extractHrefFromOnclick(onclickStr);

    // 최근 방문 배지 (중복 부착 방지)
    try {
      const last = localStorage.getItem('kjobgo.admin.recent');
      const href = link?.getAttribute('href') || onclickHref || '';
      if (last && href && last === href && !li.querySelector('.badge')) {
        const b = document.createElement('span');
        b.className = 'badge';
        b.textContent = '최근';
        li.appendChild(b);
      }
    } catch (_) {}

    // a 태그 자체 클릭 시: 기본 동작 유지 + 최근 저장만
    if (link) {
      link.addEventListener('click', () => {
        try {
          localStorage.setItem('kjobgo.admin.recent', link.getAttribute('href'));
        } catch (_) {}
      });
    }

    // 카드 클릭: a가 없을 때만 보조 네비게이션 수행
    li.addEventListener('click', (e) => {
      // a를 실제로 클릭했으면 브라우저 기본 동작(새탭/히스토리 등)을 그대로
      if (e.target.closest('a')) return;

      // 클릭 수정키(새 탭 등) 존중: 여기선 따로 처리하지 않고 a가 없을 때만 location 이동
      if (link && link.href) {
        // 기본 anchor 동작을 흉내낼 필요 없이 그냥 a.click()
        link.click();
        return;
      }
      if (onclickHref) {
        try {
          localStorage.setItem('kjobgo.admin.recent', onclickHref);
        } catch (_) {}
        window.location.assign(onclickHref);
      }
    });

    // 접근성
    li.setAttribute('tabindex', '0');
    li.setAttribute('role', 'link'); // 네비게이션이므로 link가 의미상 더 적절
    li.setAttribute(
      'aria-label',
      (link?.textContent || li.textContent || '관리자 메뉴').trim()
    );

    // 키보드 진입
    li.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        if (link) {
          link.click();
        } else if (onclickHref) {
          window.location.assign(onclickHref);
        }
      }
    });

    // 시각적 커서 보조 (CSS에 있어도 한 번 더 보강)
    li.style.cursor = 'pointer';
  });
})();

