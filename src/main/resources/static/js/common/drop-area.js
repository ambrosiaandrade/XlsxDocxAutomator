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
    // Remove apenas o texto, mantendo o input
    let info = area.querySelector(".file-info");
    if (!info) {
      info = document.createElement("p");
      info.className = "file-info";
      area.insertBefore(info, area.firstChild);
    }
    if (files.length > 0) {
      info.textContent = files[0].name;
      info.className = "file-info text-gray-900 font-bold";
    } else {
      info.textContent =
        "Arraste e solte o arquivo aqui, ou clique para selecionar.";
      info.className = "file-info text-gray-500";
    }
  }
}

export { setupDropArea };