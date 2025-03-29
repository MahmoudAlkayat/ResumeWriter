"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Background } from "@/components/ui/background";
import { UploadCloud } from "lucide-react";
import { toast } from "react-hot-toast";
import React from "react";

export default function ResumeUploadPage() {
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [status, setStatus] = useState("");

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = event.target.files?.[0];
    if (
      selectedFile &&
      ["application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"].includes(selectedFile.type)
    ) {
      setFile(selectedFile);
    } else {
      toast.error("Only PDF and DOCX files are supported");
    }
  };

  const handleUpload = async () => {
    if (!file) {
      toast.error("Please select a resume file to upload");
      return;
    }

    setUploading(true);
    setStatus("Uploading...");

    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await fetch("/api/resumes/upload", {
        method: "POST",
        body: formData,
      });

      if (!response.ok) {
        throw new Error("Upload failed");
      }

      await response.json();
      setStatus("Upload successful: Processing resume...");
      toast.success("Resume uploaded successfully");
    } catch (error) {
      setStatus("Upload failed");
      toast.error("Error uploading resume");
    } finally {
      setUploading(false);
    }
  };

  return (
    <Background className="relative flex flex-col items-center justify-center h-screen text-center p-4">
      <h1 className="text-4xl font-bold text-black mb-4">Upload Your Resume</h1>
      <p className="text-lg text-gray-700 mb-6">Supported formats: PDF, DOCX</p>

      <div className="flex flex-col items-center border-2 border-dashed border-gray-400 rounded-lg p-6 w-80 bg-white shadow-md">
        <label className="cursor-pointer flex flex-col items-center">
          <UploadCloud size={40} className="text-gray-600 mb-2" />
          <span className="text-gray-700">Click to upload or drag & drop</span>
          <input type="file" accept=".pdf,.docx" className="hidden" onChange={handleFileChange} />
        </label>
      </div>

      {file && <p className="text-gray-800 mt-4">Selected: {file.name}</p>}
      {status && <p className="text-blue-600 mt-2">{status}</p>}

      <Button className="mt-6 bg-blue-600 text-white px-6 py-2" onClick={handleUpload} disabled={uploading}>
        {uploading ? "Uploading..." : "Upload Resume"}
      </Button>
    </Background>
  );
}
