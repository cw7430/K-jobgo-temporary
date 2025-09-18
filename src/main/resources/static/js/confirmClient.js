// confirmClient.js

// ===== CSRF =====
function getCsrf() {
  const token  = document.querySelector('meta[name="_csrf"]')?.content;
  const header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
  return { header, token };
}

// ===== 전체선택 체크박스 =====
document.getElementById('checkAll')?.addEventListener('change', (e) => {
  document.querySelectorAll('#client-approval-table .row-check')
    .forEach(chk => chk.checked = e.target.checked);
});

// ===== 유틸: 행에서 현재 상태/값 읽기 =====
function getRowData(row) {
  const cmpId  = row.dataset.cmpId;
  // 회사명은 이제 인풋에서 읽음
  const nameInput = row.querySelector('input[name="cmpName"]');
  const name  = nameInput ? (nameInput.value?.trim() || `ID ${cmpId}`) 
                          : (row.children[2]?.textContent?.trim() || `ID ${cmpId}`);
  const status = row.querySelector('.status-cell .chip')?.dataset.status || 'PENDING';
  const reason = row.querySelector('.reject-reason')?.value?.trim() || '';
  return { cmpId, name, status, reason };
}

// ===== 공통 저장 호출 (승인/반려 처리) =====
async function postDecision({ cmpId, status, reason, sendEmail }) {
  const { header, token } = getCsrf();
  const resp = await fetch('/admin/confirmClient/save', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', [header]: token || '' },
    body: JSON.stringify({
      cmpId: Number(cmpId),
      status,                // "APPROVED" | "REJECTED"
      rejectReason: reason,  // 반려 사유
      sendEmail              // 메일 발송 여부
    })
  });

  if (resp.status === 401) {
    alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
    location.href = '/login';
    return;
  }
  if (resp.status === 403) {
    alert('접근 권한이 없습니다.');
    return;
  }
  if (!resp.ok) {
    let msg = '처리 중 오류가 발생했습니다.';
    try {
      const t = await resp.text();
      if (t) msg = t;
    } catch (_) {}
    throw new Error(msg);
  }
}

// ===== 결정 모달 =====
const dd = document.getElementById('decisionDialog');
const ddTitle = dd?.querySelector('#ddTitle');
const ddMsg   = dd?.querySelector('#ddMsg');
const ddYes   = dd?.querySelector('#ddYes');
const ddNo    = dd?.querySelector('#ddNo');

let ddCtx = null;
let ddBusy = false;

function openDecisionDialog(ctx, msg) {
  ddCtx = ctx;
  ddBusy = false;
  ddYes.disabled = false;
  ddNo.disabled  = false;
  ddTitle.textContent = '메일 발송 안내';
  ddMsg.textContent   = msg;
  dd.showModal();
}
function closeDecisionDialog() {
  ddCtx = null;
  ddBusy = false;
  dd.close();
}
dd?.addEventListener('click', (e) => {
  if (e.target === dd || e.target.classList.contains('pv-close')) closeDecisionDialog();
});

async function finalizeDecision(sendEmail) {
  if (!ddCtx || ddBusy) return;
  ddBusy = true;
  ddYes.disabled = true;
  ddNo.disabled  = true;
  try {
    await postDecision({ ...ddCtx, sendEmail });
    location.reload(); // 필요 시 redirectWithStatus로 교체 가능
  } catch (e) {
    console.error(e);
    alert(e.message || '처리 중 오류가 발생했습니다.');
    ddBusy = false;
    ddYes.disabled = false;
    ddNo.disabled  = false;
  }
}

ddYes?.addEventListener('click', () => finalizeDecision(true));
ddNo?.addEventListener('click', () => finalizeDecision(false));

// ===== 승인 버튼 =====
document.addEventListener('click', (e) => {
  const btn = e.target.closest('.btn-approve');
  if (!btn) return;
  const row = btn.closest('tr[data-cmp-id]');
  const { cmpId, name } = getRowData(row);

  openDecisionDialog(
    { cmpId, status: 'APPROVED', reason: '' },
    `가입신청 회사: ${name}\n승인 처리합니다. 승인 안내 메일을 발송하시겠습니까?`
  );
});

// ===== 반려 버튼 =====
document.addEventListener('click', (e) => {
  const btn = e.target.closest('.btn-reject');
  if (!btn) return;
  const row = btn.closest('tr[data-cmp-id]');
  const input = row.querySelector('.reject-reason');

  if (input.disabled || !input.value.trim()) {
    input.disabled = false;
    input.focus();
    if (!input.value.trim()) {
      alert('반려 사유를 입력한 후 반려 버튼을 다시 클릭해주세요.');
      return;
    }
  }

  const { cmpId, name, reason } = getRowData(row);
  alert('반려 사유가 입력되었습니다.');
  openDecisionDialog(
    { cmpId, status: 'REJECTED', reason },
    `가입신청 회사: ${name}\n반려 처리합니다. 반려 안내 메일을 발송하시겠습니까?`
  );
});

// ===== (선택) 반려 사유만 수정 저장 버튼 =====
document.addEventListener('click', async (e) => {
  const btn = e.target.closest('.btn-save-reason');
  if (!btn) return;
  const row = btn.closest('tr[data-cmp-id]');
  const { cmpId, status, reason } = getRowData(row);

  if (status !== 'REJECTED') {
    alert('반려 상태에서만 사유 저장이 가능합니다.');
    return;
  }
  if (!reason) {
    alert('반려 사유를 입력하세요.');
    return;
  }
  const ok = confirm('반려 사유를 저장할까요? (메일 발송 없음)');
  if (!ok) return;

  try {
    await postDecision({ cmpId, status: 'REJECTED', reason, sendEmail: false });
    location.reload();
  } catch (e2) {
    console.error(e2); alert('저장 중 오류가 발생했습니다.');
  }
});

// ===== 파일 미리보기 =====
document.addEventListener('click', function (e) {
  const btn = e.target.closest('.file-preview');
  if (!btn) return;
  const id   = btn.dataset.id;
  const mime = (btn.dataset.mime || '').toLowerCase();
  const title= btn.dataset.title || '파일 미리보기';

  if (window.innerWidth < 640) {
    window.open('/admin/files/' + encodeURIComponent(id) + '/preview', '_blank', 'noopener');
    return;
  }
  const dlg  = document.getElementById('previewDialog');
  const box  = dlg.querySelector('.pv-body');
  const ttl  = dlg.querySelector('#pvTitle');
  ttl.textContent = title + (mime ? ' (' + mime + ')' : '');
  box.innerHTML = '';

  if (mime.startsWith('image/')) {
    const img = new Image();
    img.alt = title;
    img.src = '/admin/files/' + encodeURIComponent(id) + '/preview';
    box.appendChild(img);
    dlg.showModal();
  } else if (mime === 'application/pdf' || mime.includes('pdf')) {
    const iframe = document.createElement('iframe');
    iframe.src = '/admin/files/' + encodeURIComponent(id) + '/preview';
    iframe.width = '100%';
    iframe.height = '78vh';
    iframe.setAttribute('title', title);
    box.appendChild(iframe);
    dlg.showModal();
  } else {
    window.open('/admin/files/' + encodeURIComponent(id) + '/preview', '_blank', 'noopener');
  }
});

// 미리보기 모달 닫기
(function () {
  const dlg = document.getElementById('previewDialog');
  dlg?.addEventListener('click', (e) => {
    if (e.target === dlg || e.target.classList.contains('pv-close')) dlg.close();
  });
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && dlg?.open) dlg.close();
  });
})();

/* ===== 편집 전용 툴바(수정/저장/취소) ===== */
(function(){
  'use strict';

  const table   = document.getElementById('client-approval-table');
  const tbody   = table?.querySelector('tbody');
  const btnEdit = document.getElementById('btnEdit');
  const btnSave = document.getElementById('btnSave');
  const btnCancel = document.getElementById('btnCancel');

  let editingRow = null;

  // 단일 체크 보장
  function bindRowCheckboxSingleSelect() {
    if (!tbody) return;
    tbody.addEventListener('change', e => {
      if (!e.target.matches('.row-check')) return;
      if (e.target.checked) {
        tbody.querySelectorAll('.row-check').forEach(chk => { if (chk !== e.target) chk.checked = false; });
      }
    });
  }

  // 편집가능 on/off (회사명/담당자/연락처 인풋 + 반려 사유 제어)
  function setRowEditable(tr, enabled) {
    const allow = tr.querySelectorAll('input.ed'); // cmpName, contactName, contactPhone
    allow.forEach(inp => {
      if (enabled) {
        if (!('prev' in inp.dataset)) inp.dataset.prev = inp.value; // 원복용 백업
        inp.disabled = false;
      } else {
        inp.disabled = true;
      }
    });
    // 반려 사유 인풋은 상태가 REJECTED일 때만 활성화
    const rej = tr.querySelector('.reject-reason');
    if (rej) rej.disabled = (tr.querySelector('.status-cell .chip')?.dataset.status !== 'REJECTED');
  }

  // 변경 감지 표시
  document.addEventListener('input', e => {
    const el = e.target;
    if (!el.matches('input.ed')) return;
    el.classList.toggle('is-dirty', el.dataset.prev !== undefined && el.value !== el.dataset.prev);
  });

  // 수정
  function onEditClick() {
    const row = tbody?.querySelector('.row-check:checked')?.closest('tr');
    if (!row) return alert('수정할 행을 선택하세요.');
    editingRow = row;
    setRowEditable(row, true);
    btnEdit.style.display = 'none';
    btnSave.style.display = '';
    btnCancel.style.display = '';
  }

  // 저장 (현재: 반려 사유만 저장)
  async function onSaveClick() {
    if (!editingRow) return;

    const cmpId        = editingRow.dataset.cmpId;
    const cmpName      = editingRow.querySelector('input[name="cmpName"]')?.value?.trim() || null;
    const contactName  = editingRow.querySelector('input[name="contactName"]')?.value?.trim() || null;
    const contactPhone = editingRow.querySelector('input[name="contactPhone"]')?.value?.trim() || null;
    const rejectReason = editingRow.querySelector('.reject-reason')?.value?.trim() || null;

    const { header, token } = getCsrf();
    const resp = await fetch('/admin/confirmClient/inlineEdit', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', [header]: token || '' },
      body: JSON.stringify({ cmpId: Number(cmpId), cmpName, contactName, contactPhone, rejectReason })
    });

    if (!resp.ok) {
      let msg = '저장 실패';
      try { const t = await resp.text(); if (t) msg = t; } catch {}
      alert(msg);
      return;
    }

    // 보기 모드로 복귀
    setRowEditable(editingRow, false);
    editingRow = null;
    btnEdit.style.display = '';
    btnSave.style.display = 'none';
    btnCancel.style.display = 'none';

    // 현재 탭(REJECTED) 유지하며 새로고침
    const url = new URL(location.href);
    url.searchParams.set('status', 'REJECTED');
    location.href = url.toString();
  }


  // 취소 (값 원복)
  function onCancelClick() {
    if (!editingRow) return;
    editingRow.querySelectorAll('input.ed').forEach(inp => {
      if ('prev' in inp.dataset) inp.value = inp.dataset.prev;
      inp.disabled = true;
      inp.classList.remove('is-dirty');
    });
    const input = editingRow.querySelector('.reject-reason');
    if (input && 'prev' in input.dataset) input.value = input.dataset.prev;
    editingRow = null;
    btnEdit.style.display = '';
    btnSave.style.display = 'none';
    btnCancel.style.display = 'none';
  }

  // 초기화
  (function init(){
    bindRowCheckboxSingleSelect();
    btnEdit?.addEventListener('click', onEditClick);
    btnSave?.addEventListener('click', onSaveClick);
    btnCancel?.addEventListener('click', onCancelClick);
  })();
})();
