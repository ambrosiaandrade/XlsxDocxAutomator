const statusSection = document.getElementById("status-section");
const progressBar = document.getElementById("progressBar");
const statusEl = document.getElementById("status");

// Função utilitária para atualizar UI
function setStatus(text, isError = false) {
  statusSection.style.display = "block";
  statusEl.style.display = "block";
  statusEl.textContent = text;
  statusEl.style.background = isError ? "#fee2e2" : "#eef2ff";
  statusEl.style.color = isError ? "#7f1d1d" : "#0f172a";
}

function setProgress(percent) {
  progressBar.style.width = (percent || 0) + "%";
}

function simulateProgress(callback) {
  statusSection.style.display = "flex";
  progressBar.style.width = "0%";
  statusEl.style.display = "block";
  statusEl.innerHTML = `<p>Processando...</p>`;
  let progress = 0;
  const interval = setInterval(() => {
    progress += 10;
    progressBar.style.width = `${progress}%`;
    if (progress >= 100) {
      clearInterval(interval);
      setTimeout(() => {
        statusSection.style.display = "none";
        if (callback) callback();
      }, 500);
    }
  }, 100);
}

export { setStatus, setProgress, simulateProgress };