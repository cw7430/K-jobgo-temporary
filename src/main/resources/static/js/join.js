// join.js

$(function () {
  // ---------- [공통] CSRF 토큰 세팅 ----------
  const token  = $('meta[name="_csrf"]').attr('content');
  const header = $('meta[name="_csrf_header"]').attr('content');
  if (token && header) {
    $.ajaxSetup({ beforeSend: xhr => xhr.setRequestHeader(header, token) });
  }

  // ---------- 1) 사업자등록번호 조회 ----------
  window.verifyBusinessNumber = function () {
    const p1 = $('#companyNumber1').val().trim();
    const p2 = $('#companyNumber2').val().trim();
    const p3 = $('#companyNumber3').val().trim();
    if (!(p1 && p2 && p3)) {
      alert('⚠️ 사업자등록번호를 모두 입력하세요.');
      $('#companyNumber1').focus();
      return;
    }
    $('#bizNo').val(`${p1}-${p2}-${p3}`);
    $.ajax({
      url: '/api/business/verify',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({ b_no: [p1 + p2 + p3] }),
      dataType: 'json',
      success(result) {
        const $r = $('#bizCheckResult').removeClass('success error').text('');
        if (result.status_code === 'OK' && result.data?.[0]) {
          if (result.data[0].b_stt_cd === '01') {
            $r.addClass('success').text('✅ 사업자등록번호 인증 성공!');
          } else {
            $r.addClass('error').text(`❌ 유효하지 않습니다. 사유: ${result.data[0].b_stt || '없음'}`);
          }
        } else {
          $r.addClass('error').text('❌ API 응답이 비정상입니다.');
        }
      },
      error() {
        $('#bizCheckResult').removeClass('success').addClass('error').text('❌ 인증 중 오류가 발생했습니다.');
      }
    });
  };

  // ---------- 2) 이메일 입력 & 중복 확인 ----------
  window.handleEmailDomainChange = function () {
    const sel = $('#emailDomainSelect').val();
    if (sel === 'custom') {
      $('#email2').show().val('').prop('required', true).focus();
    } else {
      $('#email2').hide().val(sel).prop('required', false);
    }
    updateBizEmail();
  };

  window.checkEmailDuplicate = function () {
    const a = $('#email1').val().trim();
    const b = $('#email2').val().trim();
    if (!a || !b) {
      alert('⚠️ 이메일을 모두 입력해주세요.');
      $('#email1').focus();
      return;
    }
    const email = `${a}@${b}`;
    $('#bizEmail').val(email);
    $.ajax({
      url: '/api/check-email',
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({ email }),
      success(res) {
        $('#emailCheckResult')
          .text(res.duplicate ? '❌ 이미 사용 중인 이메일입니다.' : '✅ 사용 가능한 이메일입니다.')
          .css('color', res.duplicate ? 'red' : 'green');
      },
      error() {
        $('#emailCheckResult').text('❌ 이메일 확인 중 오류 발생').css('color', 'red');
      }
    });
  };

  $('#email1, #email2, #emailDomainSelect').on('input change', updateBizEmail);
  function updateBizEmail() {
    const a = $('#email1').val().trim();
    const b = $('#email2').val().trim();
    if (a && b) $('#bizEmail').val(`${a}@${b}`);
  }

  // ---------- 3) 주소 검색 ----------
  window.openKakaoPostcode = function () {
    new daum.Postcode({
      oncomplete(data) {
        $('#postcode').val(data.zonecode);
        $('#companyAddress').val(data.address);
        $('#detailAddress').val('').focus();
      }
    }).open();
  };

  // ---------- 4) 대리 가입 동의 토글 ----------
  $('input[name="prxJoin"]').on('change', function () {
    const isProxy = $('#fileConfirmProxy').is(':checked');
    $('#proxyConsentSection').toggle(isProxy);

    if (!isProxy) {
      $('#proxyJoinAgree').prop('checked', false);
      $('#proxyExecutorGroup').hide();
      $('#proxyExecutor').prop('required', false).val('');
    } else {
      $('#proxyExecutor').prop('required', $('#proxyJoinAgree').is(':checked'));
    }
  });

  $('#proxyJoinAgree').on('change', function () {
    const checked = $(this).is(':checked');
    $('#proxyExecutorGroup').toggle(checked);
    $('#proxyExecutor').prop('required', checked);
  });

  // ---------- 5) 실시간 비밀번호 검사 ----------
  function norm(v) { return (v || '').normalize('NFKC').replace(/^\s+|\s+$/g, ''); }
  const pwIn = $('#joinPassword');
  const cpwIn = $('#confirmPassword');
  const pwHint = $('#pwHint');
  const cpwHint = $('#confirmHint');

  function isValidPassword(v) {
    v = norm(v);
    return /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{10,}$/.test(v);
  }
  function validatePassword() {
    const pw = pwIn.val();
    const cpw = cpwIn.val();

    if (pw && !isValidPassword(pw)) {
      pwHint.text('❌ 영문자+숫자 포함 최소 10자입니다.').css('color', 'red');
    } else if (pw) {
      pwHint.text('✅ 형식이 유효합니다.').css('color', 'green');
    } else {
      pwHint.text('');
    }

    if (cpw) {
      if (pw !== cpw) cpwHint.text('❌ 일치하지 않습니다.').css('color', 'red');
      else cpwHint.text('✅ 일치합니다.').css('color', 'green');
    } else {
      cpwHint.text('');
    }
  }
  pwIn.on('input', validatePassword);
  cpwIn.on('input', validatePassword);

  // ---------- 6) 기타 항목 토글 ----------
  $('input[name="dormitory"]').on('change', function () {
    const isOther = $('input[name="dormitory"]:checked').val() === '기타';
    $('#dormitoryOtherTxt').toggle(isOther).prop('disabled', !isOther);
    if (!isOther) $('#dormitoryOtherTxt').val('');
  });

  $('input[name="meal"]').on('change', function () {
    const isOther = $('input[name="meal"]:checked').val() === '기타';
    $('#mealOtherTxt').toggle(isOther).prop('disabled', !isOther);
    if (!isOther) $('#mealOtherTxt').val('');
  });

  // ---------- 7) 파일 첨부 힌트 ----------
  ['bizFileLicense', 'bizFileCard'].forEach(id => {
    $(`#${id}`).on('change', function () {
      const file = this.files[0];
      const hint = $(`#${id}Hint`);
      if (!file) {
        hint.text('jpg, png 또는 PDF 파일을 첨부해주세요.').css('color', 'red');
        return;
      }
      const okMime = ['image/jpeg', 'image/png', 'application/pdf'];
      const hasOkMime = okMime.includes(file.type);
      const hasOkExt = /\.(jpe?g|png|pdf)$/i.test(file.name || '');
      if (!(hasOkMime || hasOkExt)) {
        hint.text('❌ 허용되지 않는 파일 형식입니다.').css('color', 'red');
        this.value = '';
        return;
      }
      hint.text(`✅ "${file.name}"이(가) 첨부되었습니다.`).css('color', 'green');
    });
  });

  // ---------- 8) 폼 제출 바인딩 ----------
  $('#joinForm').on('submit', handleJoinSubmit);

  // ---------- 초기 상태 동기화 ----------
  window.handleEmailDomainChange();                 // 이메일 select 초기화
  $('input[name="dormitory"]').trigger('change');   // 기숙사 기타 입력칸 상태 맞추기
  $('input[name="meal"]').trigger('change');        // 식사 기타 입력칸 상태 맞추기
  $('input[name="prxJoin"]').trigger('change');     // 대리가입 라디오 초기 상태 반영
  $('#proxyJoinAgree').trigger('change');           // 대리 동의 체크박스 초기 반영
});

// ---------- [공용] 스크롤 + 포커스 ----------
function focusAndScroll($el) {
  const node = $el.get(0);
  if (!node) return;
  node.scrollIntoView({ behavior: 'smooth', block: 'center' });
  setTimeout(() => $el.trigger('focus'), 250);
}

// ---------- [공용] 복합 히든 필드 세팅 ----------
function fillHiddenCompositeFields() {
  // 사업자번호
  const p1 = $('#companyNumber1').val().trim();
  const p2 = $('#companyNumber2').val().trim();
  const p3 = $('#companyNumber3').val().trim();
  if (p1 || p2 || p3) $('#bizNo').val(`${p1}-${p2}-${p3}`);

  // 이메일
  const a = $('#email1').val().trim();
  const b = $('#email2').val().trim();
  if (a && b) $('#bizEmail').val(`${a}@${b}`);

  // 회사 연락처
  const cp1 = $('input[name="cmpPhone1"]').val().trim();
  const cp2 = $('input[name="cmpPhone2"]').val().trim();
  const cp3 = $('input[name="cmpPhone3"]').val().trim();
  if (cp1 || cp2 || cp3) $('#cmpPhone').val(`${cp1}-${cp2}-${cp3}`);

  // 담당자 연락처
  const ep1 = $('input[name="empPhone1"]').val().trim();
  const ep2 = $('input[name="empPhone2"]').val().trim();
  const ep3 = $('input[name="empPhone3"]').val().trim();
  if (ep1 || ep2 || ep3) $('#empPhone').val(`${ep1}-${ep2}-${ep3}`);
}

// ---------- 폼 제출 처리 (구인조건 포함 정밀 검증) ----------
function handleJoinSubmit(e) {
  e.preventDefault();

  // 1) 회사·대표자
  if (!$('#cmpName').val().trim()) { alert('⚠️ 회사명을 입력해 주세요.'); $('#cmpName').focus(); return; }
  if (!$('#ceoName').val().trim()) { alert('⚠️ 대표자 성명을 입력해 주세요.'); $('#ceoName').focus(); return; }

  // 2) 사업자등록번호 인증
  if (!$('#bizCheckResult').hasClass('success')) {
    alert('⚠️ 사업자등록번호 인증을 먼저 진행해 주세요.');
    $('#companyNumber1').focus(); return;
  }

  // 3) 이메일 중복 확인
  if (!$('#bizEmail').val().trim()) { alert('⚠️ 이메일을 입력해 주세요.'); $('#email1').focus(); return; }
  if (!$('#emailCheckResult').text().includes('✅')) {
    alert('⚠️ 이메일 중복 확인을 진행해 주세요.');
    $('#email1').focus(); return;
  }

  // 4) 비밀번호 형식/일치
  const pw = $('#joinPassword').val();
  const cpw = $('#confirmPassword').val();
  const pwPat = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{10,}$/;
  if (!pwPat.test(pw)) { alert('❌ 비밀번호는 영문자+숫자 포함 최소 10자이어야 합니다.'); $('#joinPassword').focus(); return; }
  if (pw !== cpw) { alert('❌ 비밀번호가 일치하지 않습니다.'); $('#confirmPassword').focus(); return; }

  // 5) 주소
  if (!$('#postcode').val()) { alert('⚠️ 우편번호를 입력해 주세요.'); $('#postcode').focus(); return; }
  if (!$('#detailAddress').val().trim()) { alert('⚠️ 상세 주소를 입력해 주세요.'); $('#detailAddress').focus(); return; }

  // 6) 연락처(회사)
  {
    const cp1 = $('input[name="cmpPhone1"]').val().trim();
    const cp2 = $('input[name="cmpPhone2"]').val().trim();
    const cp3 = $('input[name="cmpPhone3"]').val().trim();
    if (!(cp1 && cp2 && cp3)) {
      alert('⚠️ 회사 연락처를 모두 입력해 주세요.');
      if (!cp1) $('input[name="cmpPhone1"]').focus();
      else if (!cp2) $('input[name="cmpPhone2"]').focus();
      else $('input[name="cmpPhone3"]').focus();
      return;
    }
  }
  // 6-2) 연락처(담당자)
  {
    const ep1 = $('input[name="empPhone1"]').val().trim();
    const ep2 = $('input[name="empPhone2"]').val().trim();
    const ep3 = $('input[name="empPhone3"]').val().trim();
    if (!(ep1 && ep2 && ep3)) {
      alert('⚠️ 담당자 연락처를 모두 입력해 주세요.');
      if (!ep1) $('input[name="empPhone1"]').focus();
      else if (!ep2) $('input[name="empPhone2"]').focus();
      else $('input[name="empPhone3"]').focus();
      return;
    }
  }

  // 7) 파일 첨부
  if (!$('#bizFileLicense')[0].files.length) { alert('⚠️ 사업자등록증 사본을 첨부해 주세요.'); $('#bizFileLicense').focus(); return; }
  if (!$('#bizFileCard')[0].files.length) { alert('⚠️ 담당자 명함을 첨부해 주세요.'); $('#bizFileCard').focus(); return; }

  // 8) 대리/직접 라디오
  const prxVal = $('input[name="prxJoin"]:checked').val();
  if (prxVal === undefined) {
    alert('⚠️ “대리 가입” 또는 “직접 가입” 중 하나를 선택해 주세요.');
    focusAndScroll($('input[name="prxJoin"]').first()); return;
  }
  // 8-1) 대리 가입 시 동의/직원명
  if (prxVal === 'true') {
    if (!$('#proxyJoinAgree').is(':checked')) { alert('⚠️ 대리 회원가입 안내에 동의해 주세요.'); focusAndScroll($('#proxyJoinAgree')); return; }
    if (!$('#proxyExecutor').val().trim()) { alert('⚠️ 대리 회원가입 동의 시 처리 직원명을 입력해 주세요.'); focusAndScroll($('#proxyExecutor')); return; }
  }

  // 9) 첨부파일 확인 동의
  if (!$('#fileConfirm').is(':checked')) { alert('⚠️ 첨부파일 내용을 확인하고 동의해 주세요.'); $('#fileConfirm').focus(); return; }

  // 9.5) 첨부파일 확인 동의 (항상 필수)
  if (!$('#fileConfirm').is(':checked')) {
    alert('⚠️ 첨부파일 내용을 확인하고 동의해 주세요.');
    $('#fileConfirm').focus();
    return;
  }
  
  // 10) 약관 동의
  if (!$('#agreeTerms').is(':checked')) { alert('⚠️ 이용약관에 동의하셔야 합니다.'); $('#agreeTerms').focus(); return; }

  // 11) 채용요청 섹션 검증(체크한 경우에만)
  if ($('#withJobRequest').is(':checked')) {
    const $sec = $('#jobRequestSection');
    let invalid = null;

    // (a) 일반 입력류: enabled + visible + data-jr-required
    $sec.find('[data-jr-required]')
      .filter(':enabled')
      .filter(function () { return this.type !== 'radio' && this.type !== 'checkbox'; })
      .filter(':visible')
      .each(function () {
        const v = (($(this).val() || '') + '').trim();
        if (!v) { invalid = $(this); return false; }
      });

    // (b) 라디오 그룹: name별로 하나 이상 선택
    if (!invalid) {
      const groupNames = new Set();
      $sec.find('input[type="radio"][data-jr-required]:enabled').each(function () { groupNames.add(this.name); });
      for (const name of groupNames) {
        if (!$sec.find(`input[name="${name}"]:checked`).length) {
          invalid = $sec.find(`input[name="${name}"]`).first();
          break;
        }
      }
    }

    // (c) 국적=기타 → 커스텀 국적 필수
    if (!invalid && $('#desiredNationality').val() === '기타') {
      const $c = $('#customNationality');
      if (!($c.val() || '').trim()) invalid = $c;
    }

    if (invalid) {
      alert('⚠️ 채용요청을 함께 제출하려면 구인조건 필드를 모두 입력해 주세요.');
      focusAndScroll(invalid); return;
    }
  }

  // 최종: 히든 합성 + 확인 모달
  fillHiddenCompositeFields();
  $('#customConfirm').removeClass('hidden');
}

// ---------- 모달 확인/취소 ----------
function submitForm() {
  fillHiddenCompositeFields();
  $('#joinForm')[0].submit(); // native submit (submit 이벤트 발생 X)
}
function closeConfirm() {
  $('#customConfirm').addClass('hidden');
}

// ---------- [채용요청] 섹션 토글 ----------
(function setupJobRequestToggle() {
  const $chk = $('#withJobRequest');
  const $sec = $('#jobRequestSection');

  function setJobRequestEnabled(on) {
    // UI 상태
    $sec.css({ display: on ? '' : 'none', opacity: on ? 1 : 0.6, pointerEvents: on ? 'auto' : 'none' });

    // 섹션 요소
    const $all = $sec.find('input, select, textarea');
    const $req = $sec.find('[data-jr-required]');

    // enable/disable + required
    $all.prop('disabled', !on);
    $req.prop('required', !!on);

    if (!on) {
      // 끌 때 값 초기화
      $all.each(function () {
        if (this.type === 'checkbox' || this.type === 'radio') this.checked = false;
        else $(this).val('');
      });
      // 국적 기타/보조칸 초기화
      $('#customNationality').hide().val('');
      $('#dormitoryOtherTxt, #mealOtherTxt').hide().val('');
      $('#dormitoryOtherTxt, #mealOtherTxt').prop('disabled', true);
    } else {
      // 켤 때 현재 상태에 맞춰 보조칸 동기화
      $('input[name="dormitory"]').trigger('change');
      $('input[name="meal"]').trigger('change');
      toggleCustomNationality();

      // 섹션 상단으로 스크롤 + 첫 필드 포커스
      requestAnimationFrame(() => {
        const headerH = document.querySelector('.top-var')?.offsetHeight || 0;
        const y = $('#jobRequestSection').offset().top - headerH - 8;
        window.scrollTo({ top: y, behavior: 'smooth' });

        const first =
          $('#jobRequestSection').find('[data-jr-first]:visible:not([disabled])')[0] ||
          $('#jobRequestSection').find('input, select, textarea').filter(':visible:not([disabled])')[0];
        if (first) first.focus({ preventScroll: true });
      });
    }
  }

  // 체크박스 변화
  $chk.on('change', function () { setJobRequestEnabled(this.checked); });

  // 최초 진입 상태
  setJobRequestEnabled($chk.is(':checked'));

  // 희망 국적 '기타' 처리
  window.toggleCustomNationality = function () {
    const isOther = $('#desiredNationality').val() === '기타';
    $('#customNationality').toggle(isOther).prop('disabled', !isOther);
    if (!isOther) $('#customNationality').val('');
  };
})();
