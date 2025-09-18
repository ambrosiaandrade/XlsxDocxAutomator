// Elementos
const fileInput = document.getElementById("fileInput");
const sheetUrl = document.getElementById("sheetUrl");
const uploadBtn = document.getElementById("uploadBtn");
const importBtn = document.getElementById("importBtn");
const clearBtn = document.getElementById("clearBtn");
const status = document.getElementById("status");
const progressBar = document.getElementById("progressBar");

// Novos elementos do frontend
const config = document.getElementById("tab-config");
const fieldProvider = document.getElementById("fieldProvider");
const fieldEmail = document.getElementById("fieldEmail");
const fieldPassword = document.getElementById("fieldPassword");
const generateBtn = document.getElementById("generateBtn");
const mailCredentialsBtn = document.getElementById("mailCredentialsBtn");
const downloadAllBtn = document.getElementById("downloadAllBtn");
const sendAllBtn = document.getElementById("sendAllBtn");

// Endpoint único: /upload (pode ser ajustado conforme seu controller)
const UPLOAD_ENDPOINT = "/upload";

// Função utilitária para atualizar UI
function setStatus(text, isError = false) {
  status.style.display = "block";
  status.textContent = text;
  status.style.background = isError ? "#fee2e2" : "#eef2ff";
  status.style.color = isError ? "#7f1d1d" : "#0f172a";
}

function setProgress(percent) {
  progressBar.style.width = (percent || 0) + "%";
}

// Preencher selects com colunas
function populateColumns(columns = []) {
  columName.innerHTML = '<option value="">Selecione uma coluna...</option>';
  columEmail.innerHTML = '<option value="">Selecione uma coluna...</option>';
  columns.forEach((col) => {
    const opt1 = document.createElement("option");
    opt1.value = col;
    opt1.textContent = col;
    columName.appendChild(opt1);

    const opt2 = document.createElement("option");
    opt2.value = col;
    opt2.textContent = col;
    columEmail.appendChild(opt2);
  });
  goToConfigTab();
}

// Upload de arquivo via fetch + FormData
async function uploadFile(file) {
  if (!file) return setStatus("Nenhum arquivo selecionado", true);

  const form = new FormData();
  form.append("file", file, file.name);

  try {
    setStatus("Iniciando upload...");
    setProgress(6);

    const res = await fetch(UPLOAD_ENDPOINT, {
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

    if (data?.columns) {
     // populateColumns(data.columns);
    }
  } catch (err) {
    console.error(err);
    setStatus("Falha no upload: " + err.message, true);
    setProgress(0);
  }
}

// Importar via URL da Google Sheet
async function importSheet(url) {
  if (!url) return setStatus("Coloque a URL da Google Sheet", true);
  try {
    setStatus("Solicitando importação da Google Sheet...");
    setProgress(10);

    const res = await fetch(UPLOAD_ENDPOINT, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ sheetUrl: url }),
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error("Erro no servidor: " + (text || res.status));
    }

    const data = await res.json();

    setProgress(100);
    setStatus("Importação concluída com sucesso.");

    if (data?.columns) {
     // populateColumns(data.columns);
    }
  } catch (err) {
    console.error(err);
    setStatus("Falha ao importar: " + err.message, true);
    setProgress(0);
  }
}

// Listeners
uploadBtn.addEventListener("click", () => uploadFile(fileInput.files[0]));
importBtn.addEventListener("click", () => importSheet(sheetUrl.value.trim()));
clearBtn.addEventListener("click", () => {
  fileInput.value = "";
  sheetUrl.value = "";
  setStatus("", false);
  status.style.display = "none";
  setProgress(0);
  config.style.display = "none";
});

// Drag & drop
["dragenter", "dragover"].forEach((ev) => {
  document.addEventListener(ev, (e) => {
    e.preventDefault();
    e.stopPropagation();
  });
});
document.addEventListener("drop", (e) => {
  e.preventDefault();
  e.stopPropagation();
  const f = e.dataTransfer.files && e.dataTransfer.files[0];
  if (f) fileInput.files = e.dataTransfer.files;
});

// Botões de ação (exemplo)
testCredentialsBtn?.addEventListener("click", async () => {

setStatus(`Testando credenciais...`);
    setProgress(30);

    try {

        const res = await fetch('/validateAndSend', {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json'
          }
        });

        if (!res.ok) {
          const text = await res.text();
          throw new Error(text || res.status);
        }

        const responseData = await res.json();

        setProgress(100);
        setStatus("Credenciais validadas com sucesso!");

        if (responseData?.redirectUrl) {
          window.location.href = responseData.redirectUrl;
        }
      } catch (err) {
        console.error(err);
        setStatus("Erro ao testar credenciais: " + err.message, true);
        setProgress(0);
      }
    });


mailCredentialsBtn?.addEventListener("click", async () => {
  const provider = fieldProvider.value;
  const sender = fieldEmail.value;
  const password = fieldPassword.value;

  setStatus(`Salvando credenciais...`);
    setProgress(30);

    try {
        const data = {
          provider: fieldProvider.value,
          email: fieldEmail.value,
          password: fieldPassword.value
        };

        const res = await fetch('/configEmail', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(data)
        });

        if (!res.ok) {
          const text = await res.text();
          throw new Error(text || res.status);
        }

        const responseData = await res.json();

        setProgress(100);
        setStatus("Credenciais salvas com sucesso!");

        if (responseData?.redirectUrl) {
          window.location.href = responseData.redirectUrl;
        }
      } catch (err) {
        console.error(err);
        setStatus("Erro ao salvar credenciais: " + err.message, true);
        setProgress(0);
      }
    });

generateBtn?.addEventListener("click", async () => {

  setStatus(`Gerando documentos...`);
  setProgress(30);

  try {
    const data = {
      provider: fieldProvider.value,
      sender: fieldEmail.value,
      password: fieldPassword.value
    };

    const res = await fetch('/generate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || res.status);
    }

    const responseData = await res.json();

    setProgress(100);
    setStatus("Documentos gerados com sucesso!");

    if (responseData?.redirectUrl) {
      window.location.href = responseData.redirectUrl;
    }
    if(responseData?.refresh) {
        window.location.reload();
    }
  } catch (err) {
    console.error(err);
    setStatus("Erro ao gerar documentos: " + err.message, true);
    setProgress(0);
  }
});

// Tabs simples
document.querySelectorAll(".tab").forEach((tab) => {
  tab.addEventListener("click", () => {
    document
      .querySelectorAll(".tab")
      .forEach((t) => t.classList.remove("active"));
    document
      .querySelectorAll(".tab-content")
      .forEach((c) => c.classList.remove("active"));
    tab.classList.add("active");
    document.getElementById("tab-" + tab.dataset.tab).classList.add("active");
  });
});

// Quando upload terminar, troca para aba "Configurações"
function goToConfigTab() {
  document.querySelector('.tab[data-tab="config"]').click();
}

// Exemplo: chamar após populateColumns()
// goToConfigTab();
