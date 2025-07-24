// ✅ 전역으로 정의 (HTML inline onclick 에서 사용 가능하게)
// company.js 최상단 또는 어디든 window에 붙이면 전역됨
window.playMedia = function(imgElement) {
  const type = imgElement.getAttribute("data-type");
  const url = imgElement.getAttribute("data-url");
  const container = document.getElementById("mediaDisplay");

  if (!url || !container) return;

  container.innerHTML = "";

  if (type === "image") {
    const img = document.createElement("img");
    img.src = url;
    img.style.width = "100%";
    img.style.maxHeight = "450px";
    img.style.objectFit = "contain";
    img.style.borderRadius = "12px";
    img.style.backgroundColor = "#f5f5f5";
    img.style.boxShadow = "0 4px 16px rgba(0, 0, 0, 0.15)";
    container.appendChild(img);
  } else if (type === "video") {
    const video = document.createElement("video");
    video.src = url;
    video.controls = true;
    video.autoplay = true;
    video.style.width = "100%";
    video.style.maxHeight = "450px";
    video.style.objectFit = "contain";
    video.style.borderRadius = "12px";
    video.style.backgroundColor = "#f5f5f5";
    video.style.boxShadow = "0 4px 16px rgba(0, 0, 0, 0.15)";
    container.appendChild(video);
  }
}
