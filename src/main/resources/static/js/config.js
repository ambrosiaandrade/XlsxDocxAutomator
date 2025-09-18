const fieldProvider = document.getElementById("fieldProvider");
const fieldEmail = document.getElementById("fieldEmail");
const fieldPassword = document.getElementById("fieldPassword");

const mailCredentialsBtn = document.getElementById("mailCredentialsBtn");
const testCredentialsBtn = document.getElementById("testCredentialsBtn");

testCredentialsBtn?.addEventListener("click", async () => {
  setStatus(`Testando credenciais...`);
  setProgress(30);

  try {
    const res = await fetch("/validateAndSend", {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
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
  setStatus(`Salvando credenciais...`);
  setProgress(30);

  try {
    const data = {
      provider: fieldProvider.value,
      email: fieldEmail.value,
      password: fieldPassword.value,
    };

    const res = await fetch("/configEmail", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
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
