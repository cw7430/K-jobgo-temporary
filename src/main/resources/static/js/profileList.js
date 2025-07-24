document.addEventListener('DOMContentLoaded', function () {
	
	const insertBtn = document.getElementById('insertFormBtn');
	if (insertBtn) {
		insertBtn.addEventListener('click', function () {
			console.log("등록 버튼 클릭됨");
			window.location.href = '/admin/profileRegister';
		});
	};

	const isAdmin = document.body.getAttribute('data-is-admin') === 'true';
	    const rows = document.querySelectorAll('.clickable-row');
	    rows.forEach(row => {
	        row.addEventListener('click', function () {
	            const profileId = row.getAttribute('data-id');
	            if (profileId) {
	                const params = new URLSearchParams(window.location.search);
	                let url = '';
	                if (isAdmin) {
	                    // 로그인한 관리자면 관리자 전용 상세페이지로
	                    url = `/admin/profileDetail/${profileId}?${params.toString()}`;
	                } else {
	                    // 비로그인 또는 일반사용자는 공개 상세페이지로
	                    url = `/profileDetail/${profileId}?${params.toString()}`;
	                }
	                window.location.href = url;
	            }
	        });
	    });
	});
