// /js/agencyRegister.js (모달 유무 자동 감지 + 퍼블릭 등록 대응)
(function () {
  'use strict';

  // 공통 DOM
  const $form     = document.getElementById('agencyForm');
  const $saveBtn  = document.getElementById('agencySave');
  const $cancel   = document.getElementById('agencyCancel');
  const $redirect = $form?.querySelector('input[name="redirectAfter"]');

  // 모달 관련(있을 때만 동작)
  const $modal    = document.getElementById('agencyModal');
  const $openBtn  = document.getElementById('openAgencyRegister');
  const $closeBtn = document.getElementById('agencyClose');

  // ----- CSRF -----
  function csrfHeaders() {
    const token  = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    return token ? { [header]: token } : {};
  }

  // ----- 파일 검증 -----
  function validateFiles() {
    const files = document.getElementById('files')?.files || [];
    // ✅ Word, Excel, PDF, Image 허용
    const okExt = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'jpg', 'jpeg', 'png'];
    for (const f of files) {
      const ext = f.name.split('.').pop().toLowerCase();
      if (!okExt.includes(ext)) {
        alert('허용되지 않는 파일 형식이 있습니다. (pdf, doc, docx, xls, xlsx, jpg, jpeg, png)');
        return false;
      }
      if (f.size > 10 * 1024 * 1024) { // 10MB
        alert('파일은 10MB 이하만 업로드 가능합니다.');
        return false;
      }
    }
    return true;
  }

  // ----- 모달 열고/닫기(모달이 있을 때만) -----
  function openModal() {
    if (!$modal) return;
    $modal.hidden = false;
    document.body.style.overflow = 'hidden';
  }
  function closeModal() {
    if (!$modal) return;
    $modal.hidden = true;
    document.body.style.overflow = '';
    $form?.reset();
  }

  // ----- 제출 -----
  async function handleSubmit(e) {
    e.preventDefault();
    if (!$form) return;

    // 기본 필수값은 브라우저 required가 처리. 파일만 커스텀 검증
    if (!validateFiles()) return;

    // 중복 제출 방지
    $saveBtn?.setAttribute('disabled', 'true');

    try {
      const fd = new FormData($form);
      const res = await fetch($form.getAttribute('action') || '/agency/register', {
        method: 'POST',
        headers: { ...csrfHeaders() },
        body: fd,
        credentials: 'same-origin',
      });

      if (res.ok) {
        const dest = $redirect?.value?.trim() || '/agency/List';
        alert('등록이 완료되었습니다.');
        window.location.href = dest;
        return;
      } else {
        const msg = await res.text().catch(() => '');
        alert('등록 실패: ' + (msg || res.status));
      }
    } catch (err) {
      console.error(err);
      alert('네트워크 오류가 발생했습니다.');
    } finally {
      $saveBtn?.removeAttribute('disabled');
    }
  }

  // ----- 취소 -----
  function handleCancel(e) {
    e.preventDefault();
    if ($modal) {
      closeModal();
    } else {
      $form?.reset();
      history.length > 1 ? history.back() : (window.location.href = '/');
    }
  }

  // ===== 초기 바인딩 =====
  function init() {
    if ($form) $form.addEventListener('submit', handleSubmit);
    if ($cancel) $cancel.addEventListener('click', handleCancel);

    // 모달 관련(있을 때만)
    if ($openBtn && $modal) $openBtn.addEventListener('click', openModal);
    if ($closeBtn && $modal) $closeBtn.addEventListener('click', closeModal);
    $modal?.addEventListener('click', (e) => {
      if (e.target === $modal) closeModal();
    });
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && !$modal?.hidden) closeModal();
    });
  }
  init();
})();
