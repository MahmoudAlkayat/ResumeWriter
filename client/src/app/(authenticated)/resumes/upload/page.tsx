"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Background } from "@/components/ui/background";
import { UploadCloud } from "lucide-react";
import { useToast } from "@/contexts/ToastProvider";
import { useAuth } from "@/hooks/auth";
import { useResumeProcessing } from "@/contexts/ResumeProcessingProvider";
import React from "react";
import { Card, CardContent } from "@/components/ui/card";

export default function ResumeUploadPage() {
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const { showError, showInfo } = useToast();
  const { user } = useAuth();
  const { addActiveProcess } = useResumeProcessing();

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

      const data = await response.json();
      addActiveProcess(data.statusId, 'upload');
      showInfo("Resume uploaded. Please wait while we process your resume.");
    } catch (error) {
      showError("Error uploading resume");
    } finally {
      setUploading(false);
      setFile(null);
    }
  };

  return (
    <Background className="relative flex flex-col items-center min-h-screen p-8">
      <h1 className="text-4xl font-bold text-foreground mb-4">Upload Your Resume</h1>

      <div className="w-full max-w-7xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 mb-8 
      dark:bg-neutral-900 dark:border-neutral-800">
        <Card className="mb-8 border-2 border-gray-500 bg-gray-50 dark:bg-gray-900/20">
          <CardContent>
            <div className="prose dark:prose-invert max-w-none">
              <p className="text-sm text-foreground">
                Upload your current resume to analyze and improve it. Our system will process your resume and extract key information
                that can be used to generate optimized versions for different job applications.
              </p>
              <ul className="mt-4 text-sm text-muted-foreground list-disc list-inside space-y-1">
                <li>Upload PDF or DOCX format resumes</li>
                <li>Get your resume analyzed automatically</li>
                <li>Extract key information for future use</li>
              </ul>
            </div>
          </CardContent>
        </Card>

        <div className="flex flex-col items-center justify-center w-full">
          <div className="flex flex-col items-center border-2 border-dashed border-gray-400 rounded-lg p-6 w-80 bg-white dark:bg-neutral-900 shadow-md">
            <label className="cursor-pointer flex flex-col items-center">
              <UploadCloud size={40} className="text-primary mb-2" />
              <span className="text-primary">Click to upload or drag & drop</span>
              <input type="file" accept=".pdf,.docx" className="hidden" onChange={handleFileChange} />
            </label>
          </div>

          <p className="text-sm text-muted-foreground mt-4">Supported formats: PDF, DOCX</p>

          {file && <p className="text-foreground mt-4">Selected: <span className="text-blue-600">{file.name}</span></p>}

          <Button 
            className="mt-6 bg-blue-600 hover:bg-blue-700 text-white px-6 py-2" 
            onClick={handleUpload} 
            disabled={uploading || !file}
          >
            {uploading ? "Uploading..." : "Upload Resume"}
          </Button>
        </div>
      </div>
    </Background>
  );
}
