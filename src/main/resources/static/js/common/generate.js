import { setProgress, setStatus } from "./status.js";

async function generateFiles(flow) {
  setStatus(`Gerando documentos do fluxo ${flow}...`);
  setProgress(30);

  try {
    // Se precisar passar parâmetros, use query string:
    // const url = `/generate?param1=${encodeURIComponent(valor1)}&param2=${encodeURIComponent(valor2)}`;
    const res = await fetch("/generate", {
      method: "GET",
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
    if (responseData?.refresh) {
      window.location.reload();
    }
  } catch (err) {
    console.error(err);
    setStatus("Erro ao gerar documentos: " + err.message, true);
    setProgress(0);
  }
}

export { generateFiles };
