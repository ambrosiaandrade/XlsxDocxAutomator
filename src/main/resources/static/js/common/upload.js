import { setProgress, setStatus } from "./status.js";

const fileInput = document.getElementById("fileInput");

// Lida com a seleção de arquivo através do input
fileInput.addEventListener("change", (e) => {
  displayFileInfo(e.target.files);
});

function displayFileInfo(files) {
  const statusEl = document.getElementById("status");
  if (files.length > 0) {
    statusEl.style.display = "block";
    statusEl.innerHTML = `<p class="font-bold">Arquivo selecionado:</p><p>${files[0].name}</p>`;
  }
}

// Upload de arquivo via fetch + FormData
async function uploadFile(file, path) {
  if (!file) return setStatus("Nenhum arquivo selecionado", true);

  const form = new FormData();
  form.append("file", file, file.name);

  try {
    setStatus("Iniciando upload... " + file.name);
    setProgress(6);

    const res = await fetch(path, {
      method: "POST",
      body: form,
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error("Erro no servidor: " + (text || res.status));
    }

    const data = await res.json();

    setProgress(100);
    setStatus("Upload concluído com sucesso.");
  } catch (err) {
    console.error(err);
    setStatus("Falha no upload: " + err.message, true);
    setProgress(0);
  }
}

async function uploadGenericFiles(excelFile, docFile) {
  console.log("Uploading files...");
  if (!excelFile || !docFile) {
    setStatus("Selecione a planilha e o modelo Word.", true);
    return;
  }
  const form = new FormData();
  form.append("excel", excelFile, excelFile.name);
  form.append("word", docFile, docFile.name);

  try {
    setStatus("Enviando arquivos...");
    setProgress(10);

    const res = await fetch("/upload-generic", {
      method: "POST",
      body: form,
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error("Erro no servidor: " + (text || res.status));
    }

    const data = await res.json();

    setProgress(100);
    setStatus("Documentos importados com sucesso!");
    if (data?.redirectUrl) {
      window.location.href = data.redirectUrl;
    }
    if (data?.refresh) {
      window.location.reload();
    }
  } catch (err) {
    console.error(err);
    setStatus("Falha no upload: " + err.message, true);
    setProgress(0);
  }
}

export { uploadFile, uploadGenericFiles };
