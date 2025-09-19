// Botão de Importar Google Sheet (exemplo)
document.getElementById("importBtn").addEventListener("click", () => {
  const sheetUrl = document.getElementById("sheetUrl").value;
  const statusEl = document.getElementById("status");
  if (sheetUrl) {
    statusEl.style.display = "block";
    statusEl.innerHTML = `<p>Importando dados do Google Sheets...</p>`;
    // Adicione aqui a lógica real para processar o URL
  }
});

// Importar via URL da Google Sheet
async function importSheet(url, path) {
  if (!url) return setStatus("Coloque a URL da Google Sheet", true);
  try {
    setStatus("Solicitando importação da Google Sheet...");
    setProgress(10);

    const res = await fetch(path, {
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
  } catch (err) {
    console.error(err);
    setStatus("Falha ao importar: " + err.message, true);
    setProgress(0);
  }
}

export { importSheet };
