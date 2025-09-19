import { setupDropArea } from "./common/drop-area.js";
import { generateFiles } from "./common/generate.js";
import { importSheet } from "./common/import-google-sheet.js";
import { uploadFile } from "./common/upload.js";

setupDropArea("drop-area-upe", "fileInput");

document.getElementById("importBtn").addEventListener("click", () => {
  const sheetUrl = document.getElementById("sheetUrl").value;
  const fileInput = document.getElementById("fileInput");
  if (sheetUrl.length > 0) {
    console.log("Sheet URL: ", sheetUrl);
    importSheet(sheetUrl.trim());
  } else if (fileInput.files.length > 0) {
    console.log("File Input: ", fileInput.files[0]);
    uploadFile(fileInput.files[0], "/upload-upe");
  } else {
    alert("Por favor, insira uma URL ou selecione um arquivo de planilha.");
  }
});

document
  .getElementById("generateBtn")
  .addEventListener("click", async () => {
    await generateFiles("UPE");
  });
