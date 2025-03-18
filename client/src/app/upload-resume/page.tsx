"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";

export default function FileUploadPage() {
  const [file, setFile] = useState<File | null>(null);
  const [userId, setUserId] = useState<string | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"success" | "error" | "">("");
  const router = useRouter();

  // Check authentication on mount by calling /auth/me.
  useEffect(() => {
    async function checkAuth() {
      try {
        const response = await fetch("http://localhost:8080/auth/me", {
          method: "GET",
          credentials: "include",
        });
        if (!response.ok) {
          router.push("/login");
          return;
        }
        // Expecting JSON with an "id" field.
        const data = await response.json();
        if (data && data.id) {
          const idStr = data.id.toString();
          setUserId(idStr);
          // Optionally store the user ID in local storage.
          localStorage.setItem("userId", idStr);
        } else {
          router.push("/login");
        }
      } catch (error) {
        console.error("Error checking authentication:", error);
        router.push("/login");
      }
    }
    checkAuth();
  }, [router]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      setFile(e.target.files[0]);
      // Clear any previous messages.
      setMessage("");
      setMessageType("");
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!file) {
      setMessage("Please select a file to upload.");
      setMessageType("error");
      return;
    }

    if (!userId) {
      setMessage("User not authenticated. Please log in first.");
      setMessageType("error");
      router.push("/login");
      return;
    }

    setIsUploading(true);
    setMessage("Uploading file...");
    setMessageType("");

    const formData = new FormData();
    formData.append("file", file);
    formData.append("userId", userId);

    try {
      console.log("Cookie present:", document.cookie);
      const response = await fetch("http://localhost:8080/api/resumes/upload", {
        method: "POST",
        credentials: "include",
        body: formData,
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `Upload failed with status: ${response.status}`);
      }

      const data = await response.text();
      setMessage(`File uploaded successfully! ${data}`);
      setMessageType("success");
      setFile(null);
      const fileInput = document.getElementById("file-upload") as HTMLInputElement;
      if (fileInput) fileInput.value = "";
    } catch (error: any) {
      console.error("Upload error:", error);
      setMessage(error.message || "An unexpected error occurred during upload.");
      setMessageType("error");
    } finally {
      setIsUploading(false);
    }
  };

  const navigateToHome = () => {
    router.push("/");
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-4">
      <div className="w-full max-w-md bg-white rounded-lg shadow-md overflow-hidden">
        <div className="bg-blue-600 p-4 flex justify-between items-center">
          <h1 className="text-white text-xl font-bold">File Upload</h1>
          <button
            onClick={navigateToHome}
            className="text-white bg-blue-700 hover:bg-blue-800 px-3 py-1 rounded-md text-sm font-medium focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            Back to Home
          </button>
        </div>
        <form onSubmit={handleSubmit} className="p-6">
          <div className="mb-6">
            <label htmlFor="file-upload" className="block text-sm font-medium text-gray-700 mb-2">
              Select File
            </label>
            <div className="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-md">
              <div className="space-y-1 text-center">
                <svg 
                  className="mx-auto h-12 w-12 text-gray-400" 
                  stroke="currentColor" 
                  fill="none" 
                  viewBox="0 0 48 48" 
                  aria-hidden="true"
                >
                  <path 
                    d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" 
                    strokeWidth="2" 
                    strokeLinecap="round" 
                    strokeLinejoin="round" 
                  />
                </svg>
                <div className="flex text-sm text-gray-600">
                  <label 
                    htmlFor="file-upload" 
                    className="relative cursor-pointer bg-white rounded-md font-medium text-blue-600 hover:text-blue-500 focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-blue-500"
                  >
                    <span>Upload a file</span>
                    <input 
                      id="file-upload" 
                      name="file-upload" 
                      type="file" 
                      className="sr-only" 
                      onChange={handleFileChange}
                      accept=".pdf,.doc,.docx,.txt,.rtf"
                    />
                  </label>
                  <p className="pl-1">or drag and drop</p>
                </div>
                <p className="text-xs text-gray-500">
                  PDF, DOC, DOCX, TXT, RTF up to 10MB
                </p>
              </div>
            </div>
          </div>
          {file && (
            <div className="mb-4 p-3 bg-gray-50 rounded-md">
              <p className="text-sm font-medium">Selected file: {file.name}</p>
              <p className="text-xs text-gray-500">
                Size: {(file.size / 1024 / 1024).toFixed(2)} MB
              </p>
            </div>
          )}
          {message && (
            <div 
              className={`mb-4 p-3 rounded-md ${
                messageType === "success" 
                  ? "bg-green-50 text-green-800" 
                  : messageType === "error"
                  ? "bg-red-50 text-red-800"
                  : "bg-blue-50 text-blue-800"
              }`}
            >
              <p className="text-sm">{message}</p>
            </div>
          )}
          <div className="flex items-center justify-between">
            <button
              type="button"
              onClick={navigateToHome}
              className="px-4 py-2 rounded-md text-gray-700 bg-gray-200 hover:bg-gray-300 font-medium focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isUploading || !file}
              className={`px-4 py-2 rounded-md text-white font-medium ${
                isUploading || !file
                  ? "bg-gray-400 cursor-not-allowed"
                  : "bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              }`}
            >
              {isUploading ? (
                <span className="flex items-center">
                  <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Uploading...
                </span>
              ) : (
                "Upload File"
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}