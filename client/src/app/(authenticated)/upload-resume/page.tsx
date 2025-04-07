"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Background } from "@/components/ui/background";
import { UploadCloud } from "lucide-react";
import { useToast } from "@/contexts/ToastProvider";
import { useAuth } from "@/hooks/auth";
import React from "react";

export default function ResumeUploadPage() {
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [status, setStatus] = useState("");
  const { showSuccess, showError } = useToast();
  const { user } = useAuth();

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = event.target.files?.[0];
    if (
      selectedFile &&
      ["application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"].includes(selectedFile.type)
    ) {
      setFile(selectedFile);
    } else {
      showError("Only PDF and DOCX files are supported");
    }
  };

  const handleUpload = async () => {
    if (!file) {
      showError("Please select a resume file to upload");
      return;
    }

    setUploading(true);
    setStatus("Uploading...");

    const formData = new FormData();
    const id = user?.id;
    if (!id) {
      showError("User authentication failed, try again.");
      return;
    }
    formData.append("file", file);
    formData.append("title", file.name);
    formData.append("userId", id.toString());

    try {
      const response = await fetch("http://localhost:8080/api/resumes/upload", {
        method: "POST",
        credentials: "include",
        body: formData,
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || "Upload failed");
      }

      await response.json();
      setStatus("Upload successful: Processing resume...");
      showSuccess("Resume uploaded successfully");
    } catch (error) {
      setStatus("Upload failed");
      showError("Error uploading resume");
    } finally {
      setUploading(false);
    }
  };

  const LoadingSpinner = () => {
    return (
      <div className="flex justify-center items-center">
        <svg
          className="animate-spin h-8 w-8 text-blue-500"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
        >
          <circle
            className="opacity-25"
            cx="12"
            cy="12"
            r="10"
            stroke="currentColor"
            strokeWidth="4"
          />
          <path
            className="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"
          />
        </svg>
      </div>
    );
  };

  return (
    <Background className="relative flex flex-col items-center justify-center h-screen text-center p-4">
      <h1 className="text-4xl font-bold text-black mb-4">Upload Your Resume</h1>
      <p className="text-lg text-gray-700 mb-6">Supported formats: PDF, DOCX</p>

      {uploading ? <LoadingSpinner /> :
        <div className="flex flex-col items-center border-2 border-dashed border-gray-400 rounded-lg p-6 w-80 bg-white shadow-md">
          <label className="cursor-pointer flex flex-col items-center">
            <UploadCloud size={40} className="text-gray-600 mb-2" />
            <span className="text-gray-700">Click to upload or drag & drop</span>
            <input type="file" accept=".pdf,.docx" className="hidden" onChange={handleFileChange} />
          </label>
        </div>
      }

      {file && <p className="text-gray-800 mt-4">Selected: {file.name}</p>}
      {status && <p className="text-blue-600 mt-2">{status}</p>}

      <Button className="mt-6 bg-blue-600 text-white px-6 py-2" onClick={handleUpload} disabled={uploading}>
        {uploading ? "Uploading..." : "Upload Resume"}
      </Button>
    </Background>
  );
}
