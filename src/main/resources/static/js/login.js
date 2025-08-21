document.addEventListener('DOMContentLoaded', function () {
  const loginBtn = document.getElementById('loginBtn');
  const closeBtn = document.querySelector('.close');
  const submitLoginBtn = document.getElementById('submitLoginBtn');

  // 모달 열기
  function openLoginModal() {
    const modal = document.getElementById('loginModal');
    if (modal) modal.style.display = 'flex';
  }

  // 로그아웃 실행
  function doLogout() {
    if (!confirm('정말 로그아웃 하시겠습니까?')) {
      return; // 취소 시 아무 것도 안 함
    }
    fetch('/api/logout', { method: 'POST', credentials: 'include' })
      .then(() => {
        alert('로그아웃 되었습니다');
        window.location.href = '/home';
      });
  }

  // 로그인/로그아웃 버튼 클릭
  if (loginBtn) {
    loginBtn.addEventListener('click', function (e) {
      // 링크 이동/폼 제출 같은 기본동작 막기
      e.preventDefault();

      // 매 클릭마다 현재 텍스트로 상태 판단
      const isLogin = this.textContent.replace(/\s/g, '') === '로그인';
      if (isLogin) {
        openLoginModal();
      } else {
        doLogout();
      }
    });
  }

  // 로그인 처리
  if (submitLoginBtn) {
    submitLoginBtn.addEventListener('click', function () {
      const loginId = document.getElementById('userId')?.value ?? '';
      const password = document.getElementById('password')?.value ?? '';

      fetch('/api/login', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ adminLoginId: loginId, adminPassword: password })
      })
      .then(response => {
        if (response.status === 403) throw new Error('퇴사자');
        if (response.ok) return response.json();
        throw new Error('일반실패');
      })
      .then(data => {
        alert(`${data.adminName}님 로그인 성공`);
        setInterval(() => {
          fetch('/api/keep-alive', { method: 'GET', credentials: 'include' });
        }, 300000);
        window.location.href = '/home';
      })
      .catch(err => {
        alert(err.message === '퇴사자'
          ? '퇴사자는 로그인할 수 없습니다.'
          : '아이디 또는 비밀번호가 일치하지 않습니다.');
        const pw = document.getElementById('password');
        if (pw) pw.value = '';
      });
    });
  }

  // 모달 닫기
  if (closeBtn) {
    closeBtn.addEventListener('click', () => {
      const modal = document.getElementById('loginModal');
      if (modal) modal.style.display = 'none';
      const form = document.getElementById('loginForm');
      form?.reset();
    });
  }
});
