// Botão de Limpar (exemplo)
document.getElementById("clearBtn").addEventListener("click", () => {
  fileInput.value = ""; // Limpa a seleção do input
  document.getElementById("sheetUrl").value = ""; // Limpa o URL da Google Sheet
  document.getElementById("status").style.display = "none";
  document.getElementById("progressBar").style.width = "0%";
  document.querySelector(".progress").style.display = "none";
});
