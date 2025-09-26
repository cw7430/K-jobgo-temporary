/**
 * /js/applyList.js
 */

(function () {
	// 행 전체 클릭 → 상세로 이동
	document.addEventListener('click', (e) => {
	  const tr = e.target.closest('tr.clickable-row');
	  if (!tr) return;

	  // 체크박스/버튼/링크 클릭은 무시
	  if (e.target.closest('input,button,a,label')) return;

	  const id = tr.dataset.jobId;
	  const isAdmin =
	    document.body.dataset.isAdmin === 'true' ||
	    window.location.pathname.startsWith('/admin/');

	  const href = isAdmin
	    ? `/admin/applyEmp/detail/${id}`     // ✅ 실제 매핑과 일치
	    : `/client/applyEmp/detail/${id}`;   // ✅ 클라이언트 상세

	  window.location.assign(href);
	});

	const form = document.getElementById('logForm');
	if (form) {
	  form.addEventListener('submit', async (e) => {
	    e.preventDefault();

	    const token  = document.querySelector('meta[name="_csrf"]')?.content;
	    const header = document.querySelector('meta[name="_csrf_header"]')?.content;

	    const res = await fetch(form.action, {
	      method: 'POST',
	      body: new FormData(form),
	      ...(token && header ? { headers: { [header]: token } } : {})
	    });

	    const html = await res.text();
	    document.getElementById('timelineBody').innerHTML = html;
	    form.reset();
	  });
	}

  // (선택) 전체 체크
  const checkAll = document.getElementById('checkAll');
  if (checkAll) {
    checkAll.addEventListener('change', () => {
      document.querySelectorAll('.row-check').forEach(cb => cb.checked = checkAll.checked);
    });
  }
  
  const sel  = document.getElementById('statusBucket');
  const hint = document.getElementById('bucketHint');
  if (!sel || !hint) return;

  const map = {
    'IN_PROGRESS': '‘진행중’에는 <b>접수됨·검토중·진행중·보류</b>가 포함됩니다.',
    'COMPLETED': '‘완료’에는 <b>완료</b>만 포함됩니다.',
    'CANCELLED_OR_REJECTED': '‘취소·반려’에는 <b>취소·반려</b>가 포함됩니다.',
    '': '상태를 선택하거나 키워드를 입력해 검색하세요.'
  };

  sel.addEventListener('change', () => {
    const key = sel.value || '';
    const html = map[key] || map[''];
    hint.innerHTML = html;
    icon.title = html.replace(/<[^>]*>/g, '');
  });
  
  // 모든 필터 초기화
  const btnReset = document.getElementById('btnReset');
  if (!btnReset) return;

  btnReset.addEventListener('click', () => {
    const base = '/admin/applyEmp/list';
    const url  = new URL(base, location.origin);

    // 🔧 유지할 파라미터만 남기고 모두 초기화
    // 예) 삭제포함은 유지하고 싶다면 keep에 넣고, 완전 초기화면 keep = []
    const keep = []; // ['includeDeleted'] 로 바꾸면 삭제포함만 유지
    const qs = new URLSearchParams(location.search);
    keep.forEach(k => {
      const v = qs.get(k);
      if (v !== null && v !== '') url.searchParams.set(k, v);
    });

    location.assign(url.toString());
  });
})();
