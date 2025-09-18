// /js/agencyList.js
(function () {
  'use strict';

  /** ───────── 검색폼 “전체보기” ───────── */
  // ✅ HTML에서 form id="AgencySearchForm" 를 바라보도록 수정
  const searchForm   = document.getElementById('AgencySearchForm');
  const keywordInput = searchForm?.querySelector('input[name="keyword"]');
  const toggleAll    = document.getElementById('toggleAllResults');

  // 폼 제출 시 항상 page=0으로 리셋 (검색어 바꾸고도 2페이지 등 유지되는 문제 방지)
  if (searchForm) {
    searchForm.addEventListener('submit', () => {
      const u = new URL(location.href);
      u.searchParams.set('page', '0');
      history.replaceState(null, '', u.toString());
    });
  }

  // “전체보기” 체크 시: keyword 비우고 page=0 → 서버 렌더 제출
  if (toggleAll && searchForm && keywordInput) {
    toggleAll.addEventListener('change', () => {
      const u = new URL(location.href);

      if (toggleAll.checked) {
        keywordInput.value = '';
        u.searchParams.delete('keyword'); // 주소에서 keyword 제거
      }
      u.searchParams.set('page', '0');     // 첫 페이지로
      history.replaceState(null, '', u.toString());
      searchForm.submit();
    });
  }

  /** ───────── 테이블 핸들/권한 ───────── */
  const rowsSuper  = document.getElementById('rows-super');   // 권한 1,2
  const rowsAgent  = document.getElementById('rows-agent');   // 권한 5

  const btnDelete   = document.getElementById('btnDelete');    // 1,2만
  const btnDownload = document.getElementById('btnDownload');  // 1,2,5

  // 권한 파악
  const authorityIdRaw =
    document.body?.dataset?.authority ||
    document.querySelector('meta[name="authority-id"]')?.content;
  const authorityId = authorityIdRaw ? Number(authorityIdRaw) : null;

  const IS_SUPER_ADMIN = authorityId === 1 || authorityId === 2;
  const IS_AGENT_VISA  = authorityId === 5;

  // 단일 선택만 허용
  function bindSingleSelect(tbody) {
    if (!tbody) return;
    tbody.addEventListener('change', (e) => {
      const cb = e.target;
      if (!cb.matches('input.row-check[type="checkbox"]')) return;
      if (!cb.checked) return;
      tbody.querySelectorAll('input.row-check[type="checkbox"]').forEach(other => {
        if (other !== cb) other.checked = false;
      });
    });
  }

  // ✅ 권한 5 전용: 배정완료 행 체크박스 비활성화
   function getRowStatus(tr) {
     const cell = tr?.querySelector('.status-cell');
     return (cell?.dataset?.status || cell?.textContent || '').trim();
   }
   

   // ✅ 관리자(1,2)는 다중 체크 허용 → 바인딩 하지 않음
   // bindSingleSelect(rowsSuper);   // ❌ 제거/주석
   // ✅ 에이전트(5)는 단일 체크 강제
   bindSingleSelect(rowsAgent);

   // 권한 5 전용: 배정완료 행 체크박스 비활성화
   function disableAssignedRowsForAgent(tbody) {
     if (!tbody || !IS_AGENT_VISA) return;
     tbody.querySelectorAll('tr').forEach(tr => {
       if (getRowStatus(tr) === '배정완료') {
         const cb = tr.querySelector('input.row-check[type="checkbox"]');
         if (cb) {
           cb.checked = false;
           cb.disabled = true;
           cb.classList.add('is-disabled');
           cb.title = '배정완료 항목은 선택할 수 없습니다.';
         }
         tr.classList.add('row-assigned'); // 시각 표시(옵션)
       }
     });
   }
   disableAssignedRowsForAgent(rowsAgent);
   
   // 선택 도우미
   function getSelectedId(tbody) {
     if (!tbody) return null;
     const selected = tbody.querySelector('input.row-check:checked');
     if (!selected) return null;
     const tr = selected.closest('tr');
     return selected.value || tr?.dataset?.id || null;
   }
   function getSelectedRow(tbody) {
     const selected = tbody?.querySelector('input.row-check:checked');
     return selected ? selected.closest('tr') : null;
   }
   function getSelectedCount(tbody) {
     if (!tbody) return 0;
     return tbody.querySelectorAll('input.row-check:checked').length;
   }

   // 상태 UI 업데이트
   function setAssignedUI(tbody) {
     if (!tbody) return;
     const tr = getSelectedRow(tbody);
     if (!tr) return;
     const statusCell = tr.querySelector('.status-cell');
     if (statusCell) {
       statusCell.textContent = '배정완료';
       statusCell.setAttribute('data-status', '배정완료');
     }
     // 에이전트라면 방금 항목도 바로 비활성화(옵션)
     if (IS_AGENT_VISA) {
       const cb = tr.querySelector('input.row-check[type="checkbox"]');
       if (cb) {
         cb.checked = false;
         cb.disabled = true;
         cb.classList.add('is-disabled');
         cb.title = '배정완료 항목은 선택할 수 없습니다.';
       }
       tr.classList.add('row-assigned');
     }
   }
   
   // ✅ 다운로드 (단일행만 허용 + 배정완료 방어)
   function handleDownload() {
     const tbody = rowsSuper || rowsAgent;
     if (!tbody) return;

     const count = getSelectedCount(tbody);
     if (count === 0) { alert('다운로드할 항목을 한 개 선택하세요.'); return; }
     if (count > 1)   { alert('다운로드는 한 개만 가능합니다. 하나만 선택해 주세요.'); return; }

     const tr = getSelectedRow(tbody);
     const status = getRowStatus(tr);
     if (status === '배정완료') {
       alert('배정완료 항목은 다운로드할 수 없습니다.');
       return;
     }

     const id = getSelectedId(tbody);

     // 낙관적 UI (원치 않으면 이 줄을 주석)
     setAssignedUI(tbody);

     const a = document.createElement('a');
     a.href = `/agency/files/${id}`;
     a.style.display = 'none';
     document.body.appendChild(a);
     a.click();
     document.body.removeChild(a);

     if (IS_AGENT_VISA && btnDownload) {
       btnDownload.disabled = true;
       btnDownload.classList.add('is-disabled');
       btnDownload.setAttribute('aria-disabled', 'true');
     }
  }

  // 공통: CSRF 헤더 읽기
  function getCsrfHeaders() {
    const token  = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    return token ? { [header]: token } : {};
  }
  
  // 삭제 (1,2만)
  async function handleDelete() {
    if (!IS_SUPER_ADMIN) {
      alert('삭제 권한이 없습니다.');
      return;
    }
    const tbody = rowsSuper;
    if (!tbody) return;

    const checked = Array.from(tbody.querySelectorAll('input.row-check:checked'));
    if (!checked.length) { alert('삭제할 항목을 선택하세요.'); return; }

    if (!confirm(`선택한 ${checked.length}개 항목을 삭제할까요? 되돌릴 수 없습니다.`)) return;

    try {
      // 선택한 id 수집
      const ids = checked.map(cb => {
        const tr = cb.closest('tr');
        return Number(cb.value || tr?.dataset?.id);
      }).filter(Boolean);

      // 1) 배치 삭제 API가 있는 경우: POST /agency/delete
      const res = await fetch('/agency/delete', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...getCsrfHeaders(),
        },
        credentials: 'same-origin',
        body: JSON.stringify(ids)
      });

      // 2) 배치 API가 없다면, 위 블록 대신 아래 주석을 해제해 단건 DELETE 반복 호출
      /*
      const headers = { ...getCsrfHeaders() };
      for (const id of ids) {
        const r = await fetch(`/agency/${id}`, {
          method: 'DELETE',
          headers,
          credentials: 'same-origin'
        });
        if (!r.ok) throw new Error(`삭제 실패: ${id} (status ${r.status})`);
      }
      */

      if (!res.ok) {
        const msg = await res.text().catch(() => '');
        throw new Error(`삭제 실패: ${res.status} ${msg}`);
      }

      // 서버 반영 확인 후 재조회
      location.reload();

    } catch (e) {
      console.error(e);
      alert(e.message || '삭제 중 오류가 발생했습니다.');
    }
  }


  // 이벤트 바인딩
  if (btnDownload) btnDownload.addEventListener('click', handleDownload);
  if (btnDelete)   btnDelete.addEventListener('click', handleDelete);
})();
