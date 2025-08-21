// /js/visaRecord.js
(function () {
  'use strict';

  /** DOM */
  const form   = document.getElementById('visaForm');
  if (!form) return;

  const table  = document.querySelector('.table-scroll table');
  const tbody  = table?.querySelector('#rows');           // 목록 tbody
  const tmpl   = document.getElementById('visaRowTemplate');

  // 툴바 버튼
  const btnPrimary = document.getElementById('btnPrimary'); // 등록 ⇄ 저장
  const btnEdit    = document.getElementById('btnEdit');    // 수정
  const btnDelete  = document.getElementById('btnDelete');  // 삭제
  const btnCancel  = document.getElementById('btnCancel');  // 취소

  // 삭제 포함 보기 토글(슈퍼만 존재)
  const toggleIncludeDeleted = document.getElementById('toggleIncludeDeleted');
  // 검색 폼의 hidden (삭제포함 유지용)
  const hiddenIncludeDeleted = document.getElementById('includeDeletedHidden');

  /** 로그인한 행정사 이름 (HEAD 메타/바디 data에서 주입) */
  const currentAdminName =
    document.querySelector('meta[name="current-admin-name"]')?.content?.trim()
    || document.body?.dataset?.adminName?.trim()
    || '';

  /** 권한: 오직 authorityId === 5 만 CRUD 허용 (최고권한 포함 나머지는 조회 전용) */
  const authorityIdRaw =
    document.body?.dataset?.authority ||
    document.querySelector('meta[name="authority-id"]')?.content;
  const authorityId = authorityIdRaw ? Number(authorityIdRaw) : null;
  const CAN_WRITE = authorityId === 5;

  console.log('[VISA] authorityIdRaw=', authorityIdRaw, '→ Number=', authorityId, 'CAN_WRITE=', CAN_WRITE);

  /** CSRF (있으면 자동 첨부) */
  const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
  function withCsrf(init = {}) {
    init.headers = init.headers || {};
    if (csrfToken && csrfHeader) init.headers[csrfHeader] = csrfToken;
    return init;
  }

  /** ===== 상태 ===== */
  let editingRow = null;   // 현재 편집중인 <tr> (신규/기존 변환 모두)
  let creating   = false;  // 등록 버튼 연타 방지

  /** ===== 유틸 ===== */

  /** 기본 동작(네비게이션/submit) 막는 안전 바인더 */
  function bindClick(btn, handler) {
    if (!btn) return;
    if (btn.tagName === 'BUTTON' && !btn.getAttribute('type')) {
      btn.setAttribute('type', 'button');
    }
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      e.stopPropagation();
      handler(e);
    });
  }

  /** 클라이언트 측 빠른 필터 (토글만으로 즉시 표시/숨김) */
  function applyClientFilter() {
    if (!tbody) return;

    // 토글 없으면(=일반 관리자) 필터 불필요 → 항상 전부 보이기
    if (!toggleIncludeDeleted) {
      tbody.querySelectorAll('tr').forEach(tr => { tr.style.display = ''; });
      return;
    }

    const showDeleted = !!toggleIncludeDeleted.checked;
    const rows = Array.from(tbody.querySelectorAll('tr'));

    rows.forEach(tr => {
      const isDeleted = (tr.dataset.deleted === 'true') || tr.classList.contains('is-deleted');
      tr.style.display = (!showDeleted && isDeleted) ? 'none' : '';
    });
  }

  /** 응답 html에서 tbody#rows 추출 (서버 규약: 반드시 tbody#rows) */
  function extractRowsTbody(html) {
	  const src = (html || '').trim();

	  // <template> 안에서 강제로 <table>로 감싸 파싱 (tbody 단독/ tr만 오는 경우 모두 처리)
	  const t = document.createElement('template');

	  if (src.startsWith('<tbody')) {
	    t.innerHTML = `<table>${src}</table>`;
	  } else if (src.startsWith('<tr')) {
	    t.innerHTML = `<table><tbody id="rows">${src}</tbody></table>`;
	  } else {
	    // 혹시 전체 페이지가 내려온 경우에도 안전
	    t.innerHTML = `<table>${src}</table>`;
	  }

	  // id가 있으면 우선, 없으면 첫 번째 tbody
	  const tb = t.content.querySelector('tbody#rows') || t.content.querySelector('tbody');
	  return tb || null;
	}

  /** 부분 갱신 (취소/삭제/토글 후 목록 복구) — includeDeleted 유지 */
  async function reloadTableBodyPartial() {
    if (!tbody) return;

    // 요청 URL 구성
    const requestUrl = new URL(window.location.href);
    requestUrl.searchParams.set('partial', 'table');
    if (toggleIncludeDeleted?.checked) requestUrl.searchParams.set('includeDeleted', 'true');
    else requestUrl.searchParams.delete('includeDeleted');
    requestUrl.searchParams.set('_', Date.now()); // 캐시 바스터

    const hadAnyBefore = !!tbody.querySelector('tr');

    let res;
    try {
      res = await fetch(requestUrl.toString(), withCsrf({
        headers: { 'X-Requested-With': 'XMLHttpRequest', 'Accept': 'text/html' },
        credentials: 'same-origin'
      }));
    } catch (err) {
      console.error('[VISA] fetch 실패:', err);
      return;
    }

    const ct   = res.headers.get('content-type') || '';
    const html = await res.text();

    if (!ct.includes('text/html')) {
      console.warn('[VISA] unexpected content-type:', ct, 'url=', requestUrl.toString());
      return; // 기존 tbody 유지
    }

    const fresh = extractRowsTbody(html);
    if (!fresh) {
      if (res.redirected && new URL(res.url, location.origin).pathname.includes('/login')) {
        location.href = res.url;
        return;
      }
      console.warn('[VISA] partial(#rows) 미검출. url=', requestUrl.toString(), 'head=', html.slice(0, 200));
      return; // 기존 tbody 유지
    }

    // 정상 fragment면 교체
    tbody.innerHTML = fresh.innerHTML;
	applyClientFilter(); 
	
    // 현재 페이지가 비면 이전 페이지로 보정
    if (!tbody.querySelector('tr') && hadAnyBefore) {
      const u = new URL(location.href);
      const cur = parseInt(u.searchParams.get('page') || '0', 10);
      if (cur > 0) {
        u.searchParams.set('page', String(cur - 1));
        history.replaceState(null, '', u.toString());
        await reloadTableBodyPartial();
        return;
      }
    }

    bindRowCheckboxSingleSelect();
    applyClientFilter();
    syncIncludeDeletedToPagination(); // 부분갱신 후 페이지네이션도 상태 반영

    // 주소창 정리
    const historyUrl = new URL(window.location.href);
    historyUrl.searchParams.delete('partial');
    if (toggleIncludeDeleted?.checked) historyUrl.searchParams.set('includeDeleted', 'true');
    else historyUrl.searchParams.delete('includeDeleted');
    window.history.replaceState(null, '', historyUrl.toString());
  }

  /** 단일 체크 보장 */
  function bindRowCheckboxSingleSelect() {
    if (!tbody) return;
    if (tbody.__singleBind) tbody.removeEventListener('change', tbody.__singleBind);

    const handler = (e) => {
      const cb = e.target;
      if (!cb || !cb.matches('input.row-check[type="checkbox"]')) return;
      if (!cb.checked) return;
      tbody.querySelectorAll('input.row-check[type="checkbox"]').forEach(other => {
        if (other !== cb) other.checked = false;
      });
    };
    tbody.addEventListener('change', handler);
    tbody.__singleBind = handler;
  }

  /** 선택된 행 얻기 */
  function getSelectedRow() {
    const cb = tbody?.querySelector('input.row-check[type="checkbox"]:checked');
    return cb ? cb.closest('tr') : null;
  }

  /** 행 editable 전환 시 비활성/활성 */
  function setRowEditable(tr, enabled) {
    const fields = tr.querySelectorAll('input, select, textarea, button');
    fields.forEach(el => {
      const type = (el.getAttribute('type') || '').toLowerCase();

      // 행 선택 체크박스는 항상 활성화
      const isRowCheck = type === 'checkbox' && el.classList.contains('row-check');
      if (isRowCheck) {
        el.disabled = false;
        return;
      }

      // 읽기전용/숨김/submit/행삭제버튼 제외
      if (el.classList.contains('readonly') || type === 'hidden' || type === 'submit') return;
      if (el.classList.contains('row-delete')) return;

      el.disabled = !enabled;
    });
  }

  /** 툴바 상태 전환: 보기모드 / 편집모드 */
  function toggleToolbarForEdit(isEditing) {
    if (btnPrimary) btnPrimary.textContent = isEditing ? '저장' : '등록';
    if (btnEdit)    btnEdit.hidden   = !!isEditing;
    if (btnDelete)  btnDelete.hidden = !!isEditing;
    if (btnCancel)  btnCancel.hidden = !isEditing;
  }

  /** 셀 텍스트 얻기 (빈칸 방지 트림) */
  function getCellText(tr, index) {
    const td = tr.children[index];
    return td ? (td.textContent || '').trim() : '';
  }

  /** 표시행 → 템플릿 기반 편집행으로 치환(기존행 편집 진입용) */
  function buildEditableRowFromDisplayRow(displayTr) {
    if (!tmpl?.content) throw new Error('템플릿(#visaRowTemplate)을 찾을 수 없습니다.');
    const editTr = tmpl.content.firstElementChild.cloneNode(true);

    // 기존 행의 id 유지
    const visaId = displayTr.dataset.id || displayTr.querySelector('input[type="checkbox"]')?.value;
    if (visaId) editTr.dataset.id = visaId;
    editTr.dataset.new = 'false';

    // 컬럼 순서에 맞춰 매핑 (0~14)
    editTr.querySelector('input[name="companyName"]').value       = getCellText(displayTr, 2);
    editTr.querySelector('input[name="companyAddress"]').value    = getCellText(displayTr, 3);
    editTr.querySelector('input[name="companyContact"]').value    = getCellText(displayTr, 4);
    editTr.querySelector('input[name="headcount"]').value         = getCellText(displayTr, 5);
    editTr.querySelector('input[name="nationality"]').value       = getCellText(displayTr, 6);
    editTr.querySelector('input[name="jobCode"]').value           = getCellText(displayTr, 7);
    editTr.querySelector('input[name="workerName"]').value        = getCellText(displayTr, 8);
    editTr.querySelector('input[name="immigrationOffice"]').value = getCellText(displayTr, 9);
    editTr.querySelector('input[name="applicationDate"]').value   = getCellText(displayTr, 10);

    // 승인여부: label 텍스트 ↔ select 옵션 text 매칭 (index 11)
    const approvalLabel = getCellText(displayTr, 11);
    const sel = editTr.querySelector('select[name="approvalStatus"]');
    if (sel) {
      const opt = Array.from(sel.options).find(o => (o.text || '').trim() === approvalLabel);
      if (opt) sel.value = opt.value;
    }

    // 행정사 이름 (index 12) — 로그인 이름으로 교체(읽기전용)
    const adminInput = editTr.querySelector('input[name="agentName"]') ||
                       editTr.querySelector('input[placeholder="행정사 이름"]');
    if (adminInput) {
      adminInput.value    = currentAdminName || getCellText(displayTr, 12);
      adminInput.readOnly = true;
      adminInput.required = true;
    }

    // 비고 (index 13)
    const remarks = editTr.querySelector('textarea[name="remarks"]');
    if (remarks) remarks.value = getCellText(displayTr, 13);

    // 등록/수정일 표시용 (서버 반영 X) index 14,15
    const createdAt = getCellText(displayTr, 14);
    const updatedAt = getCellText(displayTr, 15);
    const ro = editTr.querySelectorAll('input.readonly');
    if (ro[0]) ro[0].value = createdAt || '등록 시 자동 생성';
    if (ro[1]) ro[1].value = updatedAt || '수정 시 자동 갱신';

    return editTr;
  }

  /** 템플릿 행 추가(맨 위) — 신규 등록 */
  function addEmptyRowFromTemplate() {
    if (!tmpl?.content) throw new Error('템플릿(#visaRowTemplate)을 찾을 수 없습니다.');
    const tr = tmpl.content.firstElementChild.cloneNode(true);
    tr.dataset.new = 'true';

    // 신규행도 로그인 이름 자동 세팅 + readonly
    const adminInput = tr.querySelector('input[name="agentName"]') ||
                       tr.querySelector('input[placeholder="행정사 이름"]');
    if (adminInput) {
      adminInput.value    = currentAdminName;
      adminInput.readOnly = true;
      adminInput.required = true;
    }

    if (tbody.firstElementChild) tbody.insertBefore(tr, tbody.firstElementChild);
    else tbody.appendChild(tr);
    return tr;
  }

  /** 클라이언트 유효성 검사 */
  function validateRow(tr) {
    const fields = tr.querySelectorAll('input, select, textarea');
    for (const el of fields) {
      const name = el.name || el.getAttribute('name');
      const type = (el.getAttribute('type') || '').toLowerCase();

      // remarks는 선택 입력
      if (name === 'remarks') continue;

      if (el.classList.contains('readonly') || type === 'hidden' || type === 'submit' || type === 'checkbox') continue;

      if (el.required && (el.value == null || el.value.trim() === '')) {
        return { ok: false, el, msg: '필수 항목을 입력해 주세요.' };
      }

      if (type === 'number') {
        const min = el.min !== '' ? Number(el.min) : null;
        if (min != null && Number(el.value) < min) {
          return { ok: false, el, msg: `최소 ${min} 이상 입력해 주세요.` };
        }
      }
    }
    return { ok: true };
  }

  /** 행 직렬화 */
  function serializeRow(tr) {
    const fd = new FormData();
    tr.querySelectorAll('input, select, textarea').forEach(el => {
      const name = el.name || el.getAttribute('name');
      if (!name) return;
      if (el.type === 'checkbox') return;
      fd.append(name, el.value ?? '');
    });
    return fd;
  }

  /** 서버 프래그먼트(<tr>)로 교체 */
  function replaceRowWithFragment(currentTr, html) {
    const tpl = document.createElement('template');
    tpl.innerHTML = html.trim();
    const newTr = tpl.content.querySelector('tr[data-id]') || tpl.content.querySelector('tr');
    if (!newTr) {
      console.error('서버 응답 원문:', html.slice(0, 1000));
      throw new Error('프래그먼트에 <tr>가 없습니다.');
    }
    currentTr.replaceWith(newTr);
    return newTr;
  }

  /** 서버 삭제 호출 */
  async function deleteOnServer(id) {
    const res = await fetch(`/admin/visa/${encodeURIComponent(id)}`, withCsrf({
      method: 'DELETE',
      headers: { 'X-Requested-With': 'XMLHttpRequest' }
    }));
    if (!res.ok) {
      if (res.status === 403) throw new Error('권한이 없습니다. (권한 5만 가능)');
      throw new Error(`삭제 실패 [${res.status}]`);
    }
  }

  /** 변경사항 감지(간단): 비어있지 않은 필드가 있으면 true */
  function hasUserInput(tr) {
    const inputs = tr.querySelectorAll('input, select, textarea');
    for (const el of inputs) {
      const type = (el.getAttribute('type') || '').toLowerCase();
      if (el.classList.contains('readonly') || type === 'hidden' || type === 'checkbox' || type === 'submit') continue;
      if ((el.value ?? '').trim() !== '') return true;
    }
    return false;
  }

  /** 저장 (신규=POST /admin/visa/ajax, 기존=POST /admin/visa/{id}/ajax) */
  async function saveEditingRow() {
    if (!editingRow) return;

    if (!CAN_WRITE) { alert('권한이 없습니다. (권한 5만 등록/수정/삭제 가능)'); return; }

    const v = validateRow(editingRow);
    if (!v.ok) { alert(v.msg); v.el?.focus(); v.el?.reportValidity?.(); return; }

    btnPrimary.disabled = true;
    btnPrimary.textContent = '저장 중...';

    try {
      const fd = serializeRow(editingRow);
      const isNew = editingRow.dataset.new === 'true';

      let url = '/admin/visa/ajax';
      let method = 'POST';

      if (!isNew) {
        const id = editingRow.dataset.id;
        if (!id) throw new Error('수정할 항목 ID를 찾을 수 없습니다.');
        url = `/admin/visa/${encodeURIComponent(id)}/ajax`;
        method = 'POST';
      }

      const res = await fetch(url, withCsrf({
        method,
        headers: { 'X-Requested-With': 'XMLHttpRequest', 'Accept': 'text/html' },
        body: fd,
        credentials: 'same-origin'
      }));

      if (res.redirected) {
        alert('세션이 만료됐거나 권한이 없습니다. 다시 로그인해 주세요.');
        location.href = res.url;
        return;
      }

      if (!res.ok) {
        if (res.status === 400) throw new Error('입력값을 확인해 주세요.');
        if (res.status === 403) throw new Error('권한이 없습니다. (권한 5만 등록/수정/삭제 가능)');
        if (res.status === 409) throw new Error('중복된 정보가 있습니다.');
        throw new Error(`요청 처리 중 문제가 발생했습니다. [${res.status}] ${url}`);
      }

      const ct = res.headers.get('content-type') || '';
      const html = await res.text();

      if (!ct.includes('text/html') || !html.includes('<tr')) {
        console.error('서버 응답:', html.slice(0, 800));
        throw new Error('서버 응답이 목록 행(<tr>)이 아닙니다. (권한/세션/서버 오류 가능)');
      }

      const newTr = replaceRowWithFragment(editingRow, html);

      // 저장 완료 후: 체크박스 재활성 + 단일선택 이벤트 재바인딩
      newTr.querySelector('input[type="checkbox"].row-check')?.removeAttribute('disabled');
      bindRowCheckboxSingleSelect();
      setRowEditable(newTr, false);

      editingRow = null;
      toggleToolbarForEdit(false);
      newTr.scrollIntoView({ block: 'nearest' });

      // 저장 후 현재 토글 상태 유지하며 클라 필터 재적용
      applyClientFilter();
    } catch (e) {
      console.error(e);
      alert(e.message || '저장 중 문제가 발생했습니다.');
    } finally {
      btnPrimary.disabled = false;
      btnPrimary.textContent = '등록';
    }
  }

  /** 등록(⇄ 저장) 클릭 */
  async function onPrimaryClick() {
    if (!CAN_WRITE) { alert('권한이 없습니다. (권한 5만 등록/수정/삭제 가능)'); return; }

    if (editingRow) {
      return saveEditingRow();
    }

    if (creating) return;
    creating = true;
    try {
      editingRow = addEmptyRowFromTemplate();
      setRowEditable(editingRow, true);
      toggleToolbarForEdit(true);
      editingRow.querySelector('input,select,textarea')?.focus();
    } catch (e) {
      console.error(e);
      alert('새 항목을 추가할 수 없습니다.');
    } finally {
      creating = false;
    }
  }

  /** 수정 클릭 */
  function onEditClick() {
    if (!CAN_WRITE) { alert('권한이 없습니다. (권한 5만 등록/수정/삭제 가능)'); return; }

    if (editingRow) {
      alert('작성 중인 항목이 있습니다. 저장하거나 취소한 뒤 계속해 주세요.');
      return;
    }
    const checked = tbody?.querySelectorAll('input[type="checkbox"]:checked') || [];
    if (!checked.length) return alert('수정할 항목을 선택해 주세요.');
    if (checked.length > 1) return alert('한 번에 하나씩만 수정할 수 있습니다.');

    const displayTr = checked[0].closest('tr');
    if (!displayTr) return alert('선택한 항목을 찾을 수 없습니다.');

    const editTr = buildEditableRowFromDisplayRow(displayTr);
    displayTr.replaceWith(editTr);
    editingRow = editTr;
    setRowEditable(editTr, true);
    toggleToolbarForEdit(true);
    editTr.querySelector('input,select,textarea')?.focus();
  }

  /** 취소 클릭 */
  async function onCancelClick() {
    if (!editingRow) return;

    if (hasUserInput(editingRow)) {
      const ok = confirm('작성/수정 중인 내용이 사라집니다. 취소할까요?');
      if (!ok) return;
    }

    const isNew = editingRow.dataset.new === 'true';
    if (isNew) {
      editingRow.remove();
      editingRow = null;
      toggleToolbarForEdit(false);
      return;
    }

    try {
      await reloadTableBodyPartial();
    } catch {
      location.reload();
    } finally {
      editingRow = null;
      toggleToolbarForEdit(false);
    }
  }

  /** 삭제 클릭 */
  async function onDeleteClick() {
    if (!CAN_WRITE) { alert('권한이 없습니다.'); return; }

    // 편집중일 때
    if (editingRow) {
      const isNew = editingRow.dataset.new === 'true';

      if (isNew) {
        const needConfirm = hasUserInput(editingRow);
        if (!needConfirm || confirm('작성 중인 내용이 사라집니다. 삭제할까요?')) {
          editingRow.remove();
          editingRow = null;
          toggleToolbarForEdit(false);
        }
        return;
      }

      // 기존 편집행 삭제
      const id = editingRow.dataset.id;
      if (!id) return alert('삭제할 항목의 ID를 찾을 수 없습니다.');
      if (!confirm('이 항목을 삭제할까요? 되돌릴 수 없습니다.')) return;

      try {
        await deleteOnServer(id);
        await reloadTableBodyPartial(); // includeDeleted 유지

        // 삭제 후 현재 페이지에 행이 하나도 없으면 이전 페이지로 이동
        let hasAny = !!tbody.querySelector('tr');
        if (!hasAny) {
          const u = new URL(location.href);
          const cur = parseInt(u.searchParams.get('page') || '0', 10);
          if (cur > 0) {
            u.searchParams.set('page', String(cur - 1));
            history.replaceState(null, '', u.toString());
            await reloadTableBodyPartial(); // 이전 페이지 기준으로 다시 부분 갱신
          }
        }
        alert('삭제되었습니다.');
      } catch (e) {
        console.error(e);
        alert(e.message || '삭제 중 문제가 발생했습니다.');
      } finally {
        if (editingRow) {
          editingRow = null;
          toggleToolbarForEdit(false);
        }
      }
      return;
    }

    // 보기모드
    const tr = getSelectedRow();
    if (!tr) return alert('삭제할 항목을 선택해 주세요.');
    const id = tr.dataset.id || tr.querySelector('input.row-check')?.value;
    if (!id) return alert('삭제할 항목의 ID를 찾을 수 없습니다.');

    const label = tr.children[1]?.textContent?.trim() || '';
    if (!confirm(label ? `"${label}" 항목을 삭제할까요?` : '선택한 항목을 삭제할까요?')) return;

    try {
      await deleteOnServer(id);

      // 1) 낙관적 제거
      tr.remove();

      // 2) 페이지가 비면 이전 페이지로 보정
      if (!tbody.querySelector('tr')) {
        const u = new URL(location.href);
        const cur = parseInt(u.searchParams.get('page') || '0', 10);
        if (cur > 0) {
          u.searchParams.set('page', String(cur - 1));
          history.replaceState(null, '', u.toString());
        }
      }

      // 3) 부분 갱신(서버 기준으로 싱크)
      await reloadTableBodyPartial();

      alert('삭제되었습니다.');
    } catch (e) {
      console.error(e);
      alert(e.message || '삭제 중 문제가 발생했습니다.');
    }
  }

  /** 조회 전용 모드 툴바 비활성화 */
  function applyReadOnlyToolbar() {
    [btnPrimary, btnEdit, btnDelete, btnCancel].forEach(b => {
      if (!b) return;
      b.disabled = true;
      b.classList.add('is-disabled');
      b.setAttribute('aria-disabled', 'true');
      b.title = '권한이 없습니다. (권한 5만 등록/수정/삭제 가능)';
    });
    btnPrimary?.addEventListener('click', e => { e.preventDefault(); alert('권한이 없습니다. (권한 5만 등록/수정/삭제 가능)'); });
    btnEdit?.addEventListener('click',    e => { e.preventDefault(); alert('권한이 없습니다. (권한 5만 등록/수정/삭제 가능)'); });
    btnDelete?.addEventListener('click',  e => { e.preventDefault(); alert('권한이 없습니다. (권한 5만 등록/수정/삭제 가능)'); });
    btnCancel?.addEventListener('click',  e => { e.preventDefault(); });
  }

  function forceEnableButtonsIfWritable() {
    if (!CAN_WRITE) return;
    [btnPrimary, btnEdit, btnDelete, btnCancel].forEach(b => {
      if (!b) return;
      b.removeAttribute('disabled');
      b.classList.remove('is-disabled');
      b.removeAttribute('aria-disabled');
      b.style.pointerEvents = '';
      b.title = '';
    });
  }

  // ✅ 삭제포함 상태 ↔ 검색폼(hidden)/페이지네이션 링크 동기화
  function syncIncludeDeletedToForm() {
    if (hiddenIncludeDeleted) {
      hiddenIncludeDeleted.value = (toggleIncludeDeleted?.checked ? 'true' : 'false');
    }
  }

  function syncIncludeDeletedToPagination() {
    const checked = !!(toggleIncludeDeleted && toggleIncludeDeleted.checked);
    document.querySelectorAll('.pagination a').forEach(a => {
      try {
        const u = new URL(a.getAttribute('href'), location.origin);
        if (checked) u.searchParams.set('includeDeleted', 'true');
        else u.searchParams.delete('includeDeleted');
        a.setAttribute('href', u.pathname + u.search);
      } catch (_) { /* ignore */ }
    });
  }

  /** 초기화 */
  function init() {
    // (1) visaForm은 AJAX 전용이라 submit 막기
    form.addEventListener('submit', e => e.preventDefault());

    // (2) 버튼 타입 보정
    [btnPrimary, btnEdit, btnDelete, btnCancel].forEach(b => {
      if (!b) return;
      if (b.tagName === 'BUTTON' && !b.getAttribute('type')) {
        b.setAttribute('type', 'button');
      }
    });

    // (3) 권한에 따른 툴바 바인딩
    forceEnableButtonsIfWritable();
    if (CAN_WRITE) {
      bindClick(btnPrimary, onPrimaryClick);
      bindClick(btnEdit,    onEditClick);
      bindClick(btnDelete,  onDeleteClick);
      bindClick(btnCancel,  onCancelClick);
    } else {
      applyReadOnlyToolbar();
    }

    // ✅ (4) 여기! 검색폼의 “전체보기” 체크박스 바인딩
    const searchForm   = document.getElementById('visaSearchForm');
    const keywordInput = searchForm?.querySelector('input[name="keyword"]');
    const toggleAll    = document.getElementById('toggleAllResults');

    if (toggleAll && searchForm && keywordInput) {
      toggleAll.addEventListener('change', () => {
        const u = new URL(location.href);
        if (toggleAll.checked) {
          keywordInput.value = '';
          u.searchParams.delete('keyword'); // 주소도 정리
        }
        u.searchParams.set('page', '0');
        history.replaceState(null, '', u.toString());

        // 네이티브 제출 → 페이지네이션/상태 전부 서버에서 일관 렌더
        searchForm.submit();
      });
    }

    // (5) 삭제포함 토글
    if (toggleIncludeDeleted) {
      toggleIncludeDeleted.addEventListener('change', async () => {
        const u = new URL(location.href);
        if (toggleIncludeDeleted.checked) u.searchParams.set('includeDeleted', 'true');
        else u.searchParams.delete('includeDeleted');
        u.searchParams.set('page', '0');
        history.replaceState(null, '', u.toString());

        syncIncludeDeletedToForm();
        syncIncludeDeletedToPagination();
        applyClientFilter();

        try {
          await reloadTableBodyPartial();
        } catch (e) {
          console.error(e);
          alert(e.message || '삭제 포함 보기를 적용하는 중 문제가 발생했습니다.');
        }
      });
    }

    // (6) 나머지 초기화
    toggleToolbarForEdit(false);
    bindRowCheckboxSingleSelect();
    applyClientFilter();
    syncIncludeDeletedToForm();
    syncIncludeDeletedToPagination();
  }

  init();
})();
