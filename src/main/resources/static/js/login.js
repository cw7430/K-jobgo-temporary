document.addEventListener('DOMContentLoaded', function () {
  const loginBtn = document.getElementById('loginBtn');
  const closeBtn = document.querySelector('.close');
  const submitLoginBtn = document.getElementById('submitLoginBtn');

  // 🔹 로그인 모달 열기
  function openLoginModal() {
    document.getElementById('loginModal').style.display = 'flex';
  }

  if (loginBtn && loginBtn.textContent === '로그인') {
    loginBtn.addEventListener('click', openLoginModal);
  }

  // 🔹 로그인 실행
  if (submitLoginBtn) {
    submitLoginBtn.addEventListener('click', function () {
      const loginId = document.getElementById('userId').value;
      const password = document.getElementById('password').value;

      fetch('/api/login', {
        method: 'POST',
		credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          adminLoginId: loginId,
          adminPassword: password
        })
      })
        .then(response => {
          if (response.status === 403) {
            throw new Error('퇴사자');
          } else if (response.ok) {
            return response.json();
          } else {
            throw new Error('일반실패');
          }
        })
        .then(data => {
          alert(`${data.adminName}님 로그인 성공`);
		  // ✅ 로그인 성공 후 세션 자동 연장 시작 (5분마다)
		  setInterval(() => {
		    fetch('/api/keep-alive', { method: 'GET', credentials: 'include' });
		  }, 300000); // 300,000ms = 5분
		  
          window.location.href = "/home";
        })
        .catch(err => {
          if (err.message === '퇴사자') {
            alert('퇴사자는 로그인할 수 없습니다.');
          } else {
            alert('아이디 또는 비밀번호가 일치하지 않습니다.');
          }
		  // 실패 시 비밀번호 input 초기화 (암호화된 값 남지 않도록!)
		  document.getElementById('password').value = '';
        });
    });
  }

  // 🔹 모달 닫기
  if (closeBtn) {
    closeBtn.addEventListener('click', () => {
      document.getElementById('loginModal').style.display = 'none';
    });
  }

  // 🔹 로그아웃
  if (loginBtn && loginBtn.textContent === '로그아웃') {
    loginBtn.addEventListener('click', () => {
      fetch('/api/logout')
        .then(() => {
          alert('로그아웃 되었습니다');
          window.location.href = "/home";
        });
    });
  }
});
