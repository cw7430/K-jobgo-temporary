/**
 * applyEmp.js (fixed)
 */

// ===== CSRF =====
function getCsrf() {
  const token  = document.querySelector('meta[name="_csrf"]')?.content;
  const header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
  return { header, token };
}

// ===== List redirect =====
function getListRedirect() {
  // 1) 앵커 우선
  const anchor = document.getElementById('listRedirectAnchor');
  if (anchor?.dataset?.url) return anchor.dataset.url;
  // 2) 버튼 href (id는 아래 #3에서 부여)
  const link = document.getElementById('btnList');
  if (link?.href) return link.href;
  // 3) 기본값
  return '/admin/applyEmp/list';
}

function goList() {
  window.location.href = getListRedirect();
}

/** 서버가 돌려준 HTML 프래그먼트로 <tbody id="timelineBody">를 통째로 교체 */
function replaceTimelineBody(html) {
  const current = document.getElementById('timelineBody');
  if (!current) {
    console.warn('[timeline] current <tbody id="timelineBody"> not found on page');
    return;
  }

  const scrollY = window.scrollY;

  // 0) 응답 프리뷰 로그
  console.debug('[timeline] response length:', html?.length ?? 0);
  console.debug('[timeline] response preview:', (html || '').slice(0, 200));

  // 1) 먼저 정규식으로 <tbody id="timelineBody"> 블록을 바로 뽑아보기 (가장 확실)
  const m = html && html.match(/<tbody[^>]*\bid=["']timelineBody["'][^>]*>[\s\S]*<\/tbody>/i);
  if (m) {
    console.debug('[timeline] matched <tbody id="timelineBody"> via regex. Replacing outerHTML.');
    current.outerHTML = m[0];
    window.scrollTo({ top: scrollY });
    return;
  }

  // 2) 문서로 파싱해서 찾기
  const doc = new DOMParser().parseFromString(html || '', 'text/html');

  // 2-1) <tbody id="timelineBody"> 직접 있음?
  let incoming = doc.querySelector('tbody#timelineBody'); // ← let 로!
  console.debug('[timeline] querySelector("tbody#timelineBody") found?', !!incoming);

  // 2-2) 없으면 <tr>만 내려온 케이스 처리
  if (!incoming) {
    const trs = Array.from(doc.querySelectorAll('tr'));
    console.debug('[timeline] fallback <tr> count:', trs.length);

    incoming = document.createElement('tbody');
    incoming.id = 'timelineBody';

    if (trs.length === 0) {
      incoming.innerHTML = '<tr><td colspan="6" class="empty">상담 내역이 없습니다.</td></tr>';
    } else {
      trs.forEach(tr => incoming.appendChild(tr));
    }
  }

  // 3) 통째로 교체
  current.replaceWith(incoming);
  window.scrollTo({ top: scrollY });
}



/** 공통: 프래그먼트 GET 후 타임라인 갱신 */
async function fetchAndReplaceTimeline(url) {
  const u = new URL(url, location.origin);
  u.searchParams.set('_ts', Date.now().toString());
  console.debug('[timeline] fetching URL:', u.toString());

  const res = await fetch(u.toString(), {
    method: 'GET',
    headers: {
      'X-Requested-With': 'XMLHttpRequest',
      'Accept': 'text/html'
    },
    cache: 'no-store',
    credentials: 'same-origin',
  });

  console.debug('[timeline] status:', res.status, 'content-type:', res.headers.get('content-type'));

  if (res.status === 404) {
    // 진짜 빈 달일 때만 빈 메시지로 교체
    replaceTimelineBody('<tbody id="timelineBody"><tr><td colspan="6" class="empty">상담 내역이 없습니다.</td></tr></tbody>');
    return;
  }
  if (!res.ok) throw new Error(`HTTP ${res.status}`);

  const html = await res.text();
  replaceTimelineBody(html);
}


(function () {
  // ---- 상담 로그 저장 (관리자) ----
  const form   = document.getElementById('logForm');
  const toggle = document.getElementById('toggleStatus');
  const select = document.getElementById('statusSelect');

  if (form && toggle && select) {
    const sync = () => {
      const on = !!toggle.checked;
      select.toggleAttribute('disabled', !on);
      select.required = on;
      if (!on) select.value = '';
    };
    toggle.addEventListener('change', sync);
    sync();

    form.addEventListener('submit', async (e) => {
      e.preventDefault();

      if (toggle.checked && !select.value) {
        alert('변경할 상태를 선택하세요.');
        select.focus();
        return;
      }

      const btn = form.querySelector('button[type="submit"]');
      const requestedStatus = (toggle.checked && !select.disabled) ? select.value : null;

      try {
        if (btn) btn.disabled = true;

        const { header, token } = getCsrf();
        const res = await fetch(form.action, {
          method: 'POST',
          body: new FormData(form), // _csrf 포함
          headers: { 'X-Requested-With': 'XMLHttpRequest', 
			'Accept': 'text/html',
			...(token ? { [header]: token } : {}) }
        });
        if (!res.ok) throw new Error('서버 오류');

        const html = await res.text();
        replaceTimelineBody(html);

		// ★ 저장 성공 → dirty 초기화
		isDirty = false;

		// ★ ‘저장 후 목록 이동’이 요청되었으면, 목록 이동 우선
		if (window.__goListAfterSaveOnce) {
		  delete window.__goListAfterSaveOnce;
		  goList();
		  return;  // 아래 로직 실행 안 함
		}

		// 폼 리셋 및 토글 동기화
		form.reset();
		sync();
		
        // 상태가 실제로 바뀌면 상단 배지/헤더 등 전체 동기화, 전체 새로고침
		if (requestedStatus) location.reload();
      } catch (err) {
        alert('저장 중 오류가 발생했습니다.');
        console.error(err);
      } finally {
        if (btn) btn.disabled = false;
      }
    });
  }

  // ---- 목록 행 클릭 이동 (공용) ----
  document.addEventListener('click', (e) => {
    const tr = e.target.closest('.clickable-row');
    if (!tr) return;
    const id = tr.getAttribute('data-job-id');
    const isAdmin = document.body.dataset.isAdmin === 'true';
    const base = isAdmin ? '/admin/applyEmp/detail' : '/client/applyEmp/detail';
    window.location.href = `${base}/${id}`;
  });
})();

// ---- 월 그리드 클릭 → 타임라인 갱신 ----
(function () {
  const anchor = document.getElementById('logFilterAnchor');
  const urlBase = anchor ? anchor.dataset.url : null;
  if (!urlBase) return;

  document.addEventListener('click', async (e) => {
    const btn = e.target.closest('.month-btn');
    if (!btn) return;

    const y = btn.dataset.year;
    const m = btn.dataset.month;
    const url = `${urlBase}?year=${y}&month=${m}`;

    try {
      await fetchAndReplaceTimeline(url);
      // 선택 상태 표시
      document.querySelectorAll('.month-btn').forEach(b => b.classList.remove('is-selected'));
      btn.classList.add('is-selected');
    } catch (err) {
      console.error(err);
      // 필요 시 토스트
    }
  });
})();

// ---- 삭제 버튼 클릭 → 삭제 후 현재 필터 유지하여 갱신 ----
document.addEventListener('click', async (e) => {
  const btn = e.target.closest('button.btn-sm[data-id]');
  if (!btn) return;

  if (btn.disabled) return;                 // 가드1: 비활성
  const tr = btn.closest('tr');             // 가드2: 이미 삭제된 행
  if (tr && tr.classList.contains('is-deleted')) return;

  if (!confirm('해당 상담 로그를 삭제하시겠습니까?')) return;

  // 🔴 사유 필수: 취소(null) 또는 공백 -> 중단
  let reason = prompt('삭제 사유를 입력하세요 (필수).');
  if (reason === null) return;              // 취소눌렀을 때
  reason = reason.trim();
  if (!reason) { alert('삭제 사유는 필수입니다.'); return; }

  const m = location.pathname.match(/\/admin\/applyEmp\/detail\/(\d+)/);
  if (!m) return alert('URL 파싱 실패');
  const jobId = m[1];
  const logId = btn.getAttribute('data-id');

  const fd = new FormData();
  fd.append('reason', reason);              // ✅ 항상 reason 전송

  try {
    const { header, token } = getCsrf();
    const res = await fetch(`/admin/applyEmp/detail/${jobId}/logs/${logId}/delete`, {
      method: 'POST',
      headers: { 'X-Requested-With': 'XMLHttpRequest', ...(token ? { [header]: token } : {}) },
      body: fd
    });

    if (!res.ok) {
      // (선택) 서버에서 에러 메시지 내려주면 노출
      let msg = '삭제 중 오류가 발생했습니다.';
      try { const data = await res.json(); if (data?.error) msg = data.error; } catch {}
      alert(msg);
      return;
    }
  } catch (err) {
    alert('삭제 중 오류가 발생했습니다.');
    console.error(err);
    return;
  }

  // 이후 타임라인 재로딩은 기존 그대로
  const anchor = document.getElementById('logFilterAnchor');
  const base = anchor?.dataset?.url;
  if (!base) { location.reload(); return; }

  const selected = document.querySelector('.month-btn.is-selected');
  const y = selected?.dataset?.year;
  const mth = selected?.dataset?.month;
  const url = (y && mth)
    ? `${base}?year=${y}&month=${mth}&includeDeleted=true`
    : `${base}?includeDeleted=true`;

  try {
    await fetchAndReplaceTimeline(url);
  } catch (err) {
    console.error(err);
    location.reload();
  }
});

// ===== 변경 감지 =====
let isDirty = false;
const logForm = document.getElementById('logForm');
if (logForm) {
  const markDirty = () => { isDirty = true; };
  logForm.addEventListener('input', markDirty, true);
  logForm.addEventListener('change', markDirty, true);
}

// (옵션) 페이지 이탈 경고
window.addEventListener('beforeunload', (e) => {
  if (!isDirty) return;
  e.preventDefault();
  e.returnValue = '';
});

// ---- (옵션) 외부에서 호출할 수 있는 로그 리로드 헬퍼 ----
async function reloadLogs() {
  try {
    const anchor = document.getElementById('logFilterAnchor');
    const base = anchor?.dataset?.url;
    if (!base) { location.reload(); return; }

    const selected = document.querySelector('.month-btn.is-selected');
    const y = selected?.dataset?.year;
    const mth = selected?.dataset?.month;
    const url = (y && mth)
      ? `${base}?year=${y}&month=${mth}&includeDeleted=true`
      : `${base}?includeDeleted=true`;

    await fetchAndReplaceTimeline(url);
  } catch (e) {
    console.error('로그 갱신 실패:', e);
  }
}

// ===== 목록 버튼 클릭 시: 변경사항 있으면 저장 유도 =====
document.addEventListener('click', (e) => {
  const btn = e.target.closest('#btnList');
  if (!btn) return;

  if (!isDirty) {
    // 변경 없음 → 기본 이동
    return;
  }

  // 변경 있음 → 우리가 제어
  e.preventDefault();

  // 1단계: 저장 후 이동?
  if (confirm('작성 중인 상담 기록이 있습니다.\n저장 후 목록으로 이동할까요?')) {
    // submit 핸들러에게 "저장되면 목록으로" 신호
    window.__goListAfterSaveOnce = true;
    // 기존 AJAX submit 로직 실행
    const form = document.getElementById('logForm');
    form?.requestSubmit?.() ?? form?.submit?.();
    return;
  }

  // 2단계: 저장 없이 이동?
  if (confirm('저장하지 않고 목록으로 이동할까요?')) {
    isDirty = false; // 이탈 경고 방지
    goList();
  }
  // 아니오면 그대로 머무름
});
