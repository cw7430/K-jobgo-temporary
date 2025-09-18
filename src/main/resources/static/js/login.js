/* login.js */
document.addEventListener('DOMContentLoaded', function () {
  const modal = document.getElementById('loginModal');
  const closeBtn = document.querySelector('#loginModal .close');
  const loginBtn = document.getElementById('loginBtn'); // (옵션: 헤더에 있을 수 있음)
  const submitLoginBtn = document.getElementById('submitLoginBtn');

  const tabButtons = document.querySelectorAll('.tab-btn');
  const tabContents = document.querySelectorAll('.tab-content');

  function switchTab(targetTabId) {
    tabButtons.forEach(btn => {
      btn.classList.toggle('active', btn.getAttribute('data-tab') === targetTabId);
    });
    tabContents.forEach(content => {
      content.classList.toggle('active', content.id === targetTabId);
    });
  }
  function openLoginModal() {
    if (!modal) return;
    modal.style.display = 'flex';
    const wanted = localStorage.getItem('loginTab');
    if (wanted) {
      const btn = document.querySelector(`.tab-btn[data-tab="${wanted}"]`);
      btn?.click();
    }
  }
  function closeLoginModal() {
    if (modal) modal.style.display = 'none';
    document.getElementById('loginForm')?.reset();
    document.getElementById('clientLoginForm')?.reset();
  }

  if (modal) modal.style.display = 'none';
  if (closeBtn) closeBtn.addEventListener('click', closeLoginModal);
  window.addEventListener('click', (e) => { if (e.target === modal) closeLoginModal(); });

  tabButtons.forEach(button => {
    button.addEventListener('click', () => switchTab(button.getAttribute('data-tab')));
  });

  function doLogout() {
    const isClient = document.body?.dataset?.isClient === 'true';
    const url = isClient ? '/api/client/logout' : '/api/logout';
    fetch(url, { method: 'POST', credentials: 'include' })
      .then(() => { alert('로그아웃 되었습니다'); location.href = '/home'; })
      .catch(() => { location.href = '/home'; });
  }

  if (loginBtn) {
    loginBtn.addEventListener('click', function (e) {
      e.preventDefault();
      const isLogin = this.textContent.replace(/\s/g, '') === '로그인';
      if (isLogin) openLoginModal();
      else doLogout();
    });
  }

  function buildHeadersJson() {
    const headers = { 'Content-Type': 'application/json' };
    const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    if (csrfTokenMeta && csrfHeaderMeta) {
      const token = csrfTokenMeta.getAttribute('content');
      const headerName = csrfHeaderMeta.getAttribute('content');
      if (token && headerName) headers[headerName] = token;
    }
    return headers;
  }

  // ===== 관리자 로그인만 JS로 처리 (/api/login)
  if (submitLoginBtn) {
    const doAdminLogin = async () => {
      const loginId = (document.getElementById('userId')?.value || '').trim();
      const loginPassword = (document.getElementById('loginPassword')?.value || '').trim();
      if (!loginId || !loginPassword) { alert('아이디와 비밀번호를 모두 입력해 주세요.'); return; }

      submitLoginBtn.disabled = true;

      try {
        const resp = await fetch('/api/login', {
          method: 'POST',
          credentials: 'include',
          headers: buildHeadersJson(),
          body: JSON.stringify({ adminLoginId: loginId, adminPassword: loginPassword })
        });

        let payload = {};
        try { payload = await resp.clone().json(); } catch (_) {}

        if (resp.status === 401) throw { code: 'BAD_CREDENTIALS' };
        if (resp.status === 403) throw { code: 'FORBIDDEN', reason: payload?.reason };
        if (!resp.ok) throw { code: 'UNKNOWN' };

        const data  = (Object.keys(payload).length ? payload : await resp.json()) || {};
        const name  = data.adminName || loginId;
        const roles = Array.isArray(data.roles) ? data.roles : [];

        alert(`${name}님 로그인 성공`);
        setInterval(() => { fetch('/api/keep-alive', { method: 'GET', credentials: 'include' }); }, 300000);
        closeLoginModal();

        const next = new URLSearchParams(location.search).get('next');
        if (next) { location.assign(next); return; }
        if (roles.includes('ROLE_AGENT_VISA')) location.assign('/home');
        else location.reload();

      } catch (err) {
        if (err?.code === 'BAD_CREDENTIALS') alert('아이디 또는 비밀번호가 일치하지 않습니다.');
        else if (err?.code === 'FORBIDDEN')
          alert(err.reason === 'RETIRED' ? '퇴사/비활성 계정은 로그인할 수 없습니다.' : '접근 권한이 없습니다.');
        else alert('네트워크 또는 서버 오류로 로그인에 실패했습니다. 잠시 후 다시 시도해 주세요.');
        const pw = document.getElementById('loginPassword');
        if (pw) { pw.value = ''; pw.focus(); }
      } finally {
        submitLoginBtn.disabled = false;
      }
    };

    submitLoginBtn.addEventListener('click', (e) => { e.preventDefault(); doAdminLogin(); });
    document.getElementById('loginForm')?.addEventListener('submit', (e) => { e.preventDefault(); doAdminLogin(); });
  }

  // ===== 기업 로그인 실패 시 모달 자동 오픈 (SecurityConfig에서 referer로 리다이렉트)
  const params = new URLSearchParams(location.search);
  if (params.has('clientLoginError')) {
    localStorage.setItem('loginTab', 'clientLogin');
	if (modal) {
	  modal.style.display = 'flex';
	  document.querySelector('.tab-btn[data-tab="clientLogin"]')?.click();
	  alert('아이디 또는 비밀번호가 일치하지 않거나, 승인되지 않은 계정입니다.');
	}
  }
});
