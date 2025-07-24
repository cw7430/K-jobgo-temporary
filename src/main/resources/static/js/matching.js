// matching.js

document.addEventListener('DOMContentLoaded', function () {
  // ✅ QR 이미지 매핑
  const qrMap = {
    'tab-en': {
      targetId: 'qr-en',
      url: 'https://open.kakao.com/o/smfDD40f'
    },
	/*
    'tab-mn': {
      targetId: 'qr-mn',
      url: ''
    }, */
    'tab-vi': {
      targetId: ['qr-vi-1', 'qr-vi-2'],
      url: [
        ' https://open.kakao.com/o/sW0ARODh',
        'https://open.kakao.com/o/stxqjlWf'  // 예시 URL
      ]
    },
    'tab-mm': {
      targetId: 'qr-mm',
      url: 'https://open.kakao.com/o/sVOnrODh'
    },
    'tab-hi': {
      targetId: 'qr-hi',
      url: 'https://open.kakao.com/o/s3Wk5oBh'
    },
	'tab-id': {
	  targetId: 'qr-id',
	  url: 'https://open.kakao.com/o/sOcLTODh'
	}
  };

  const tabs = document.querySelectorAll('.tab-nav li');
  const tabContents = document.querySelectorAll('.tab-content');

  function generateQr(tabId) {
    const info = qrMap[tabId];
    if (!info) {
      console.warn(`❌ QR 정보 없음: ${tabId}`);
      return;
    }

    console.log(`🟢 QR 생성 시작: ${tabId}`);

    if (Array.isArray(info.targetId)) {
      // 다중 QR 처리
      info.targetId.forEach((id, idx) => {
        const qrContainer = document.getElementById(id);
        const url = info.url[idx];
        if (!qrContainer) {
          console.warn(`❌ 요소를 찾을 수 없음: ${id}`);
          return;
        }
        qrContainer.innerHTML = ''; // 기존 QR 제거

        console.log(`🔷 QR 생성: ID = ${id}, URL = ${url}`);
        QRCode.toCanvas(url, function (err, canvas) {
          if (err) {
            console.error(`❌ QR 생성 오류 (${id}):`, err);
            return;
          }
          qrContainer.appendChild(canvas);
        });
      });
    } else {
      // 단일 QR 처리
      const qrContainer = document.getElementById(info.targetId);
      if (!qrContainer) {
        console.warn(`❌ 요소를 찾을 수 없음: ${info.targetId}`);
        return;
      }
      qrContainer.innerHTML = '';
      console.log(`🔷 QR 생성: ID = ${info.targetId}, URL = ${info.url}`);
      QRCode.toCanvas(info.url, function (err, canvas) {
        if (err) {
          console.error(`❌ QR 생성 오류:`, err);
          return;
        }
        qrContainer.appendChild(canvas);
      });
    }
  }

  // 🔸 탭 클릭 이벤트
  tabs.forEach(tab => {
    tab.addEventListener('click', function () {
      const tabId = this.getAttribute('data-tab');

      tabs.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');

      tabContents.forEach(content => {
        content.classList.remove('active');
        if (content.id === tabId) {
          content.classList.add('active');
        }
      });

      generateQr(tabId); // ✅ QR 생성 호출
    });
  });

  // 🔸 초기 로딩 시 영어 탭 활성화
  generateQr('tab-en');
});
