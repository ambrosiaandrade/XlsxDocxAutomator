const statusSection = document.getElementById("status-section");
const progressBar = document.getElementById("progressBar");
const statusEl = document.getElementById("status");

function setupDropArea(areaId, inputId) {
  const dropArea = document.getElementById(areaId);
  const fileInput = document.getElementById(inputId);

  ["dragenter", "dragover", "dragleave", "drop"].forEach((eventName) => {
    dropArea.addEventListener(eventName, preventDefaults, false);
  });

  function preventDefaults(e) {
    e.preventDefault();
    e.stopPropagation();
  }

  ["dragenter", "dragover"].forEach((eventName) => {
    dropArea.addEventListener(eventName, () => highlight(dropArea), false);
  });

  ["dragleave", "drop"].forEach((eventName) => {
    dropArea.addEventListener(eventName, () => unhighlight(dropArea), false);
  });

  function highlight(el) {
    el.classList.add("is-dragover");
  }
  function unhighlight(el) {
    el.classList.remove("is-dragover");
  }

  dropArea.addEventListener("drop", handleDrop, false);
  function handleDrop(e) {
    let dt = e.dataTransfer;
    let files = dt.files;
    fileInput.files = files;
    displayFileInfo(files, dropArea);
  }

  dropArea.addEventListener("click", () => fileInput.click());
  fileInput.addEventListener("change", (e) =>
    displayFileInfo(e.target.files, dropArea)
  );

  function displayFileInfo(files, area) {
    if (files.length > 0) {
      const fileName = files[0].name;
      area.innerHTML = `<p class="text-gray-900 font-bold">${fileName}</p>`;
    } else {
      area.innerHTML = `<p>Arraste e solte o arquivo aqui, ou <label class="text-blue-500 underline cursor-pointer">clique para selecionar</label>.</p>`;
    }
  }
}

setupDropArea("drop-area-generic", "fileInputGeneric");
setupDropArea("drop-area-doc", "docInputGeneric");

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

document.getElementById("importBtnGeneric").addEventListener("click", () => {
  const sheetUrl = document.getElementById("sheetUrlGeneric").value;
  if (sheetUrl) {
    simulateProgress(() => {
      statusEl.innerHTML = `<p class="text-green-600 font-bold">Importação do Google Sheets concluída!</p>`;
    });
  } else {
    alert("Por favor, insira o link da Google Sheet.");
  }
});

document.getElementById("generateBtnGeneric").addEventListener("click", () => {
  const excelFile = document.getElementById("fileInputGeneric").files;
  const docFile = document.getElementById("docInputGeneric").files;

  if (excelFile.length > 0 && docFile.length > 0) {
    simulateProgress(() => {
      statusEl.innerHTML = `<p class="text-green-600 font-bold">Documentos genéricos gerados com sucesso!</p>`;
    });
  } else {
    alert("Por favor, selecione tanto a planilha quanto o modelo Word.");
  }
});
