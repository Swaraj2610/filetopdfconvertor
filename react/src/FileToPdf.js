import React, { useState } from "react";
import axios from "axios";

function FileToPdf() {
  const [file, setFile] = useState(null);
  const [fileType, setFileType] = useState("image"); 

  const handleFileChange = (event) => {
    setFile(event.target.files[0]);
  };

  const handleTypeChange = (event) => {
    setFileType(event.target.value);
    setFile(null); 
  };

  const handleUpload = async () => {
    if (!file) {
      alert(`Please select a ${fileType === "image" ? "image" : "Word"} file`);
      return;
    }

    const formData = new FormData();
    formData.append(fileType === "image" ? "image" : "file", file);

    try {
      const url =
        fileType === "image"
          ? "http://localhost:8080/api/pdf/convert"
          : "http://localhost:8080/api/pdf/word-to-pdf";

      const response = await axios.post(url, formData, {
        responseType: "blob",
        headers: { "Content-Type": "multipart/form-data" },
      });

      const blob = new Blob([response.data], { type: "application/pdf" });
      const link = document.createElement("a");
      link.href = URL.createObjectURL(blob);
      link.download = `${file.name.split(".")[0]}.pdf`;
      link.click();
    } catch (error) {
      console.error("Error uploading file", error);
    }
  };

  const containerStyle = {
    maxWidth: "400px",
    margin: "50px auto",
    padding: "20px",
    backgroundColor: "#fff",
    borderRadius: "10px",
    boxShadow: "0 4px 10px rgba(0, 0, 0, 0.1)",
    textAlign: "center",
    fontFamily: "Arial, sans-serif",
  };

  const headingStyle = {
    marginBottom: "20px",
    color: "#333",
  };

  const fileInputContainer = {
    marginBottom: "15px",
  };

  const hiddenFileInput = {
    display: "none",
  };

  const customFileButton = {
    backgroundColor: "#2196F3",
    color: "#fff",
    padding: "10px 15px",
    borderRadius: "5px",
    cursor: "pointer",
    fontSize: "14px",
    display: "inline-block",
    transition: "background 0.3s",
  };

  const buttonStyle = {
    backgroundColor: "#4CAF50",
    color: "#fff",
    padding: "10px 20px",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
    fontSize: "16px",
    transition: "background 0.3s",
  };

  const buttonHoverStyle = {
    backgroundColor: "#45a049",
  };

  return (
    <div style={containerStyle}>
      <h2 style={headingStyle}>File to PDF Converter</h2>

      {/* File type selector */}
      <div style={{ marginBottom: "15px" }}>
        <label>
          File Type:{" "}
          <select value={fileType} onChange={handleTypeChange}>
            <option value="image">Image</option>
            <option value="word">Word (.docx)</option>
          </select>
        </label>
      </div>

      {/* File input */}
      <div style={fileInputContainer}>
        <label style={customFileButton} htmlFor="fileUpload">
          {file ? `Selected: ${file.name}` : "📂 Choose a File"}
        </label>
        <input
          id="fileUpload"
          type="file"
          accept={fileType === "image" ? "image/*" : ".docx"}
          onChange={handleFileChange}
          style={hiddenFileInput}
        />
      </div>

      <button
        onClick={handleUpload}
        style={buttonStyle}
        onMouseOver={(e) =>
          (e.target.style.backgroundColor = buttonHoverStyle.backgroundColor)
        }
        onMouseOut={(e) =>
          (e.target.style.backgroundColor = buttonStyle.backgroundColor)
        }
      >
        Convert
      </button>
    </div>
  );
}

export default FileToPdf;

