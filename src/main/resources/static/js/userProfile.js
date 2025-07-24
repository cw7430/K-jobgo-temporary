document.addEventListener('DOMContentLoaded', function () {
  setSelectHasValue();

  // 목록 버튼 클릭 시 검색조건 유지 목록 페이지 이동
  const listBtn = document.getElementById('ListBtn');
  if (listBtn) {
    listBtn.addEventListener('click', function (e) {
      e.preventDefault();
      const params = window.location.search;
      window.location.href = `/profileList${params}`; // 마스킹용 프로필 목록
    });
  }
});

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
