import { setupDropArea } from "./common/drop-area.js";
import { importSheet } from "./common/import-google-sheet.js";
import { setProgress, setStatus } from "./common/status.js";
import { uploadGenericFiles } from "./common/upload.js";
import { generateFiles } from "./common/generate.js";

setupDropArea("drop-area-generic", "fileInput");
setupDropArea("drop-area-doc", "docInput");

document.getElementById("importBtn").addEventListener("click", async () => {
  const sheetUrl = document.getElementById("sheetUrl").value;

  if (sheetUrl && sheetUrl.trim().length > 0) {
    console.log("Sheet URL:", sheetUrl);
    importSheet(sheetUrl.trim(), setStatus, setProgress);
  }

  const excelFile = document.getElementById("fileInput").files[0];
  const docFile = document.getElementById("docInput").files[0];

  if (excelFile && docFile) {
    console.log("Excel File:", excelFile.name);
    console.log("Doc File:", docFile.name);
    await uploadGenericFiles(excelFile, docFile);
  } else {
    alert("Por favor, selecione tanto a planilha quanto o modelo Word.");
  }
});

document.getElementById("generateBtn").addEventListener("click", async () => {
  await generateFiles("genérico");
});
