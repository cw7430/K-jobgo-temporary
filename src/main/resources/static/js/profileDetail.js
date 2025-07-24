function setSelectHasValue() {
  document.querySelectorAll('select[disabled]').forEach(sel => {
    if (
      sel.value &&
      sel.value !== "" &&
      sel.value !== "등급" &&
      sel.value !== "급여 선택" &&
      sel.value !== "지역"
    ) {
      sel.classList.add('has-value');
    } else {
      sel.classList.remove('has-value');
    }
  });
}

document.addEventListener('DOMContentLoaded', function () {
	// 핵심 DOM 변수
	const updateForm = document.getElementById('updateForm');
	const updateBtn = document.getElementById('updateBtn');
	const saveBtn = document.getElementById('saveBtn');
	const cancelBtn = document.getElementById('cancelBtn');
	const deleteBtn = document.getElementById('deleteBtn');
	const changePhotoBtn = document.getElementById('changePhotoBtn');
	const photoInput = document.getElementById('photoInput');
	const profilePhoto = document.getElementById('profilePhoto');
	const profileId = document.getElementById('profileId')?.value;
	const allInputs = updateForm.querySelectorAll('input, select, textarea');
	const strengthsTextarea = updateForm.querySelector('textarea[name="strengths"]');
	const strengthsPlaceholder = document.getElementById('strengthsPlaceholder');
	let originalValues = Array.from(allInputs).map(input => input.value); // 취소용 백업
	let originalStrengthsValue = strengthsTextarea ? strengthsTextarea.value : "";

	// ✅ 목록 버튼 처리
	const listBtn = document.getElementById('ListBtn');
	if (listBtn) {
	  listBtn.addEventListener('click', function (e) {
	    e.preventDefault();
	    // 현재 쿼리스트링을 가져온다
	    const params = window.location.search;
	    // 목록으로 이동 (검색 조건 유지)
	    window.location.href = `/admin/profileList${params}`;
	  });
	}
	
	// strengths 섹션: 값 없으면 안내문구
	 if (strengthsTextarea) {
	   if (!originalStrengthsValue.trim()) {
	     strengthsTextarea.style.display = 'none';
	     if (strengthsPlaceholder) strengthsPlaceholder.style.display = 'block';
	   } else {
	     strengthsTextarea.style.display = 'block';
	     if (strengthsPlaceholder) strengthsPlaceholder.style.display = 'none';
	   }
	 }

	 setSelectHasValue(); // select 스타일 보정

	 // --- 사진 이벤트: 1회만 등록! ---
	 if (changePhotoBtn && photoInput) {
	   changePhotoBtn.addEventListener('click', function () {
	     photoInput.click();
	   });
	 }
	 if (photoInput && profilePhoto) {
	   profilePhoto.dataset.originalSrc = profilePhoto.src;
	   photoInput.addEventListener('change', function (e) {
	     const file = e.target.files[0];
	     if (file) {
	       const reader = new FileReader();
	       reader.onload = function (evt) {
	         profilePhoto.src = evt.target.result;
	       };
	       reader.readAsDataURL(file);
	     }
	   });
	 }

	 // ====== 수정 버튼 클릭 ======
	 updateBtn?.addEventListener('click', function () {
	   if (!confirm("수정하시겠습니까?")) return;
	   enterEditMode();
	 });
	 
	 // ====== 취소 버튼 클릭 ======
	 cancelBtn?.addEventListener('click', function () {
	   if (!confirm("정말 취소하시겠습니까?")) return;
	   exitEditMode();
	 });
	 

	 // ====== 저장(완료) 버튼 클릭 ======
	 saveBtn?.addEventListener('click', saveProfile);

	 // ====== 삭제 버튼 클릭 ======
	 deleteBtn?.addEventListener('click', deleteProfile);
	 
	 // --------- 내부 함수: 수정모드 진입 ----------
	 function enterEditMode() {
	   // input, select, textarea 활성화
	   allInputs.forEach(input => {
	     input.disabled = false;
	     input.readOnly = false;
	     input.classList.add('editable-field');
	     input.style.cursor = 'text';
	   });
    document.querySelectorAll('input, select, textarea').forEach(el => {
      el.disabled = false;
      el.removeAttribute('disabled');
      el.readOnly = false;
      el.removeAttribute('readonly');
      el.style.cursor = 'text';
    });

	  // 버튼/체크박스/안내 등
	  document.getElementById('edu-button-group').style.display = 'flex';
	  document.getElementById('work-button-group').style.display = 'flex';
	  document.querySelectorAll('.info-msg').forEach(p => p.style.display = 'block');
	  document.querySelectorAll('.edu-checkbox, .work-checkbox').forEach(cb => {
	    cb.style.display = 'inline-block';
	    cb.disabled = false;
	  });
	  if (changePhotoBtn) changePhotoBtn.style.display = 'inline-block';
	  if (photoInput) photoInput.style.display = 'none';
	  // 특이사항
	  if (strengthsTextarea) {
	    strengthsTextarea.removeAttribute('readonly');
	    strengthsTextarea.style.display = 'block';
	    strengthsTextarea.placeholder = "자격증, 기술능력, 경험 등을 입력";
	    strengthsTextarea.classList.add('editable-field');
	    strengthsTextarea.style.cursor = 'text';
	  }
	  if (strengthsPlaceholder) strengthsPlaceholder.style.display = 'none';

	  updateBtn.style.display = 'none';
	  saveBtn.style.display = 'inline-block';
	  cancelBtn.style.display = 'inline-block';

	  // 백업
	  originalValues = Array.from(allInputs).map(input => input.value);
	  originalStrengthsValue = strengthsTextarea ? strengthsTextarea.value : "";
	}
	

	// --------- 내부 함수: 수정모드 취소 ----------
	function exitEditMode() {
	  allInputs.forEach((input, i) => {
	    input.readOnly = true;
	    input.disabled = true;
	    input.classList.remove('editable-field');
	    input.style.cursor = 'not-allowed';
	    input.value = originalValues[i];
	  });
	  document.getElementById('edu-button-group').style.display = 'none';
	  document.getElementById('work-button-group').style.display = 'none';
	  document.querySelectorAll('.info-msg').forEach(p => p.style.display = 'none');
	  document.querySelectorAll('.edu-checkbox, .work-checkbox').forEach(cb => {
	    cb.style.display = 'none';
	    cb.disabled = true;
	  });
	  if (changePhotoBtn) changePhotoBtn.style.display = 'none';
	  if (photoInput) photoInput.style.display = 'none';
	  if (profilePhoto && profilePhoto.dataset.originalSrc)
	    profilePhoto.src = profilePhoto.dataset.originalSrc;
	  // 특이사항
	  if (strengthsTextarea) {
	    strengthsTextarea.setAttribute('readonly', true);
	    strengthsTextarea.classList.remove('editable-field');
	    strengthsTextarea.style.cursor = 'not-allowed';
	    strengthsTextarea.placeholder = "";
	    strengthsTextarea.value = originalStrengthsValue;
	    if (strengthsTextarea.value.trim() === '') {
	      strengthsTextarea.style.display = 'none';
	      if (strengthsPlaceholder) strengthsPlaceholder.style.display = 'block';
	    } else {
	      strengthsTextarea.style.display = 'block';
	      if (strengthsPlaceholder) strengthsPlaceholder.style.display = 'none';
	    }
	  }
	  saveBtn.style.display = 'none';
	  cancelBtn.style.display = 'none';
	  updateBtn.style.display = 'inline-block';

	  setSelectHasValue(); // select 스타일 보정
	}
	// --------- 내부 함수: 저장(완료) ---------
    async function saveProfile() {
      allInputs.forEach(el => { el.disabled = false; });
      const formData = new FormData(updateForm);
      try {
        const response = await fetch("/admin/profileUpdate", {
          method: "POST",
          body: formData
        });

        const result = await response.json(); // ✅ 변경됨

        if (response.ok && result.success) {
          alert("수정이 완료되었습니다");
          window.location.href = result.redirectUrl; // ✅ 서버가 보내준 경로로 이동
        } else {
          alert("수정 실패: " + (result.message || "오류 발생"));
        }
      } catch (err) {
        alert("오류 발생: " + err.message);
      }
    }

	  // --------- 내부 함수: 삭제 ---------
	  async function deleteProfile() {
	    if (!confirm('정말 삭제하시겠습니까?')) return;
	    try {
	      const response = await fetch(`/admin/profileDelete/${profileId}`, {
	        method: 'POST'
	      });
	      if (response.ok) {
	        alert('삭제 완료');
	        window.location.href = '/admin/profileList';
	      } else {
	        alert('삭제 실패');
	      }
	    } catch (err) {
	      alert('오류: ' + err.message);
	    }
	  }
	}); // DOMContentLoaded 끝
	
	// ------------------- 학력/경력 동적 추가/삭제/정렬 -------------------

	function deleteSelectedEdu() {
	  document.querySelectorAll('.edu-checkbox:checked').forEach(cb => {
	    cb.closest('.edu-entry')?.remove();
	  });
	  reindexEducation();
	  setSelectHasValue();
	}
	function deleteSelectedWork() {
	  document.querySelectorAll('.work-checkbox:checked').forEach(cb => {
	    cb.closest('.work-entry')?.remove();
	  });
	  reindexWork();
	  setSelectHasValue();
	}
	function addEducation() {
	  const section = document.getElementById('education-section');
	  const entries = section.getElementsByClassName('edu-entry');
	  const index = entries.length;
	  const div = document.createElement('div');
	  div.className = 'edu-entry';
	  div.innerHTML = `
	    <input type="checkbox" class="edu-checkbox" />
	    <input type="text" name="educationList[${index}].period" placeholder="(예: 2025-00-00 ~ 2025-00-00)" class="editable-field"/>
	    <input type="text" name="educationList[${index}].schoolName" placeholder="학교명" class="editable-field"/>
	    <input type="text" name="educationList[${index}].major" placeholder="학과" class="editable-field"/>
	    <label><input type="radio" name="educationList[${index}].status" value="graduated" class="editable-field"/> 졸업</label>
	    <label><input type="radio" name="educationList[${index}].status" value="enrolled" class="editable-field"/> 재학중</label>
	  `;
	  section.appendChild(div);
	  reindexEducation();
	  setSelectHasValue();
	}
	function addWork() {
	  const section = document.getElementById('work-section');
	  const entries = section.getElementsByClassName('work-entry');
	  const index = entries.length;
	  const div = document.createElement('div');
	  div.className = 'work-entry';
	  div.innerHTML = `
	    <input type="checkbox" class="work-checkbox" />
	    <input type="text" name="workList[${index}].period" placeholder="(예: 2025-00-00 ~ 2025-00-00)"  class="editable-field"/>
	    <input type="text" name="workList[${index}].companyName" placeholder="회사명" class="editable-field"/>
	    <input type="text" name="workList[${index}].jobResponsibility" placeholder="담당 업무" class="editable-field"/>
	  `;
	  section.appendChild(div);
	  reindexWork();
	  setSelectHasValue();
	}
	function reindexEducation() {
	  const entries = document.querySelectorAll('#education-section .edu-entry');
	  entries.forEach((entry, idx) => {
	    entry.querySelectorAll('input, select').forEach(input => {
	      if (input.name) {
	        input.name = input.name.replace(/educationList\[\d+\]/g, `educationList[${idx}]`);
	      }
	    });
	  });
	}
	function reindexWork() {
	  const entries = document.querySelectorAll('#work-section .work-entry');
	  entries.forEach((entry, idx) => {
	    entry.querySelectorAll('input, select').forEach(input => {
	      if (input.name) {
	        input.name = input.name.replace(/workList\[\d+\]/g, `workList[${idx}]`);
	      }
	    });
	  });
	}