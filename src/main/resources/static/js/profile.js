// /js/profile.js
document.addEventListener('DOMContentLoaded', function () {
  /** 1) 사진 미리보기 */
  const photoInput = document.getElementById('photo');
  const preview = document.getElementById('preview');

  if (photoInput && preview) {
    photoInput.addEventListener('change', function (e) {
      const file = e.target.files && e.target.files[0];
      if (!file) return;

      // 확장자 체크
      const allowed = ['jpg', 'jpeg', 'png', 'gif'];
      const ext = (file.name.split('.').pop() || '').toLowerCase();
      if (!allowed.includes(ext)) {
        alert('jpg, jpeg, png, gif 형식만 업로드 가능합니다.');
        photoInput.value = '';
        preview.removeAttribute('src');
        preview.style.display = 'none';
        return;
      }

      // 미리보기
      const reader = new FileReader();
      reader.onload = function (evt) {
        preview.src = evt.target.result;
        preview.style.display = 'block';
      };
      reader.readAsDataURL(file);
    });
  }

  /** 2) 학력사항 추가/삭제 */
  window.addEducation = function () {
    const section = document.getElementById('education-section');
    if (!section) return;

    const index = section.getElementsByClassName('edu-entry').length;
    const div = document.createElement('div');
    div.className = 'edu-entry';
    div.innerHTML = `
      <input type="checkbox" class="edu-checkbox" />
      <input type="text" name="educationList[${index}].period" placeholder="(예: 2025-00-00 ~ 2025-00-00)" />
      <input type="text" name="educationList[${index}].schoolName" placeholder="학교명" />
      <input type="text" name="educationList[${index}].major" placeholder="학과" />
      <label><input type="radio" name="educationList[${index}].status" value="graduated" /> 졸업</label>
      <label><input type="radio" name="educationList[${index}].status" value="enrolled" /> 재학중</label>
    `;
    section.appendChild(div);
  };

  window.deleteSelectedEdu = function () {
    const section = document.getElementById('education-section');
    if (!section) return;

    Array.from(section.querySelectorAll('.edu-entry')).forEach(entry => {
      const cb = entry.querySelector('.edu-checkbox');
      if (cb && cb.checked) entry.remove();
    });
  };

  /** 3) 경력사항 추가/삭제 */
  window.addWork = function () {
    const section = document.getElementById('work-section');
    if (!section) return;

    const index = section.getElementsByClassName('work-entry').length;
    const div = document.createElement('div');
    div.className = 'work-entry';
    div.innerHTML = `
      <input type="checkbox" class="work-checkbox" />
      <input type="text" name="workList[${index}].period" placeholder="(예: 2025-00-00 ~ 2025-00-00)" />
      <input type="text" name="workList[${index}].companyName" placeholder="회사명" />
      <input type="text" name="workList[${index}].jobResponsibility" placeholder="담당 업무" />
    `;
    section.appendChild(div);
  };

  window.deleteSelectedWork = function () {
    const section = document.getElementById('work-section');
    if (!section) return;

    Array.from(section.querySelectorAll('.work-entry')).forEach(entry => {
      const cb = entry.querySelector('.work-checkbox');
      if (cb && cb.checked) entry.remove();
    });
  };

  /** 4) 폼 제출 검증 */
  const form = document.querySelector('.profileform');
  if (form) {
    form.addEventListener('submit', function (e) {
      const errors = [];

      // 기본정보
      if (!form.querySelector('input[name="profile.nameKor"]')?.value.trim()) {
        errors.push('한국 이름을 입력해주세요.');
      }
      if (!form.querySelector('input[name="profile.nameOrigin"]')?.value.trim()) {
        errors.push('현지 이름을 입력해주세요.');
      }
      if (form.querySelectorAll('input[name="profile.gender"]:checked').length === 0) {
        errors.push('성별을 선택해주세요.');
      }
      if (!form.querySelector('input[name="profile.visaType"]')?.value.trim()) {
        errors.push('비자 종류를 입력해주세요.');
      }
      if (!form.querySelector('input[name="profile.visaExpire"]')?.value) {
        errors.push('비자 만기일을 선택해주세요.');
      }

      // 이메일
      const email1 = form.querySelector('input[name="profile.emailId"]')?.value.trim();
      const email2 = form.querySelector('input[name="profile.emailDomain"]')?.value.trim();
      if (!email1 || !email2) {
        errors.push('이메일 주소를 입력해주세요.');
      }

      // 휴대전화
      const p1 = form.querySelector('input[name="profile.phone1"]')?.value.trim();
      const p2 = form.querySelector('input[name="profile.phone2"]')?.value.trim();
      const p3 = form.querySelector('input[name="profile.phone3"]')?.value.trim();
      if (!p1 || !p2 || !p3) {
        errors.push('핸드폰 번호를 모두 입력해주세요.');
      }

      // 주소
      if (!form.querySelector('input[name="profile.address"]')?.value.trim()) {
        errors.push('주소를 입력해주세요.');
      }

      // 인적사항
      if (!form.querySelector('input[name="personalInfo.nationality"]')?.value.trim()) {
        errors.push('국적을 입력해주세요.');
      }
      if (!form.querySelector('input[name="personalInfo.firstEntry"]')?.value) {
        errors.push('최초 입국일을 선택해주세요.');
      }
      if (!form.querySelector('select[name="personalInfo.desiredLocation"]')?.value) {
        errors.push('희망 근무지역을 선택해주세요.');
      }

      // 학력사항 status 체크 (여기서 진행: 스코프 오류 방지)
      const eduEntries = form.querySelectorAll('.edu-entry');
      eduEntries.forEach((entry, idx) => {
        const radios = entry.querySelectorAll(`input[name="educationList[${idx}].status"]`);
        const isChecked = Array.from(radios).some(r => r.checked);
        if (!isChecked) {
          errors.push(`학력사항 ${idx + 1}번의 졸업/재학중 상태를 선택해주세요.`);
        }
      });

      if (errors.length) {
        e.preventDefault();
        alert(errors.join('\n'));
      }
    });
  }

  /** 5) 수정모드 전환 */
  const updateBtn = document.getElementById('updateBtn');
  const saveBtn = document.getElementById('saveBtn');
  if (updateBtn && saveBtn) {
    const inputs = document.querySelectorAll('input, select, textarea');
    updateBtn.addEventListener('click', () => {
      inputs.forEach(el => {
        el.removeAttribute('readonly');
        el.removeAttribute('disabled');
      });
      saveBtn.style.display = 'inline-block';
      updateBtn.style.display = 'none';
    });
  }

  /** 6) 취소 버튼 */
  const cancelBtn = document.querySelector('.btn-form-group button[type="button"]');
  if (cancelBtn) {
    cancelBtn.addEventListener('click', () => {
      window.location.href = '/admin/profileList';
    });
  }
});
