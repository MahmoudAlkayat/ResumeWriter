"use client";

import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Background } from "@/components/ui/background";
import { useToast } from "@/contexts/ToastProvider";
import LoadingScreen from "@/components/LoadingScreen";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { GeneratedResume } from '@/lib/types';
import GeneratedResumeCard from "@/components/GeneratedResumeCard";
import { Card, CardContent } from "@/components/ui/card";

interface FormattedResume {
  formattedResumeId: string;
  formatType: string;
  fileExtension: string;
}

interface ResumeTemplate {
  id: string;
  name: string;
  description: string;
  previewUrl: string;
  templateId: string;
}

const RESUMES_PER_PAGE = 5;

export default function ResumeFormatPage() {
  const { showError, showSuccess } = useToast();
  const [resumes, setResumes] = useState<GeneratedResume[]>([]);
  const [selectedResume, setSelectedResume] = useState<string>("");
  const [formatType, setFormatType] = useState("markdown");
  const [isLoading, setIsLoading] = useState(false);
  const [isFetchingResumes, setIsFetchingResumes] = useState(true);
  const [templates, setTemplates] = useState<ResumeTemplate[]>([]);
  const [selectedTemplate, setSelectedTemplate] = useState<string>("");
  const [formattedResume, setFormattedResume] = useState<FormattedResume | null>(null);
  const [currentPage, setCurrentPage] = useState(1);

  const formatOptions = [
    { value: "markdown", label: "Markdown (.md)" },
    { value: "html", label: "HTML (.html)" },
    { value: "text", label: "Plain Text (.txt)" },
    { value: "pdf", label: "LaTeX as PDF (.pdf)"}
  ];

  const totalPages = Math.ceil(resumes.length / RESUMES_PER_PAGE);
  const startIndex = (currentPage - 1) * RESUMES_PER_PAGE;
  const endIndex = startIndex + RESUMES_PER_PAGE;
  const currentResumes = resumes.slice(startIndex, endIndex);

  useEffect(() => {
    const fetchResumes = async () => {
      try {
        const response = await fetch(
          "http://localhost:8080/api/resumes/generate/history",
          {
            credentials: "include",
          }
        );

        if (!response.ok) {
          throw new Error("Failed to fetch resumes");
        }

        const data = await response.json();
        setResumes(data || []);
        setIsFetchingResumes(false);
      } catch (err) {
        showError(
          err instanceof Error ? err.message : "Failed to load resumes"
        );
        setIsFetchingResumes(false);
      }
    };

    fetchResumes();
  }, []);

  useEffect(() => {
    const fetchTemplates = async () => {
      try {
        const response = await fetch(
          "http://localhost:8080/api/resumes/templates",
          {
            credentials: "include",
          }
        );

        if (!response.ok) {
          throw new Error("Failed to fetch templates");
        }

        const data = await response.json();
        setTemplates(data || []);
        console.log(templates);
        console.log(data)
      } catch (err) {
        showError(
          err instanceof Error ? err.message : "Failed to load templates"
        );
      }
    };

    fetchTemplates();
  }, []);

  const handleFormatResume = async () => {
    if (!selectedResume) {
      showError("Please select a resume");
      return;
    }

    if (formatType === "pdf" && !selectedTemplate) {
      showError("Please select a template for PDF format");
      return;
    }

    setIsLoading(true);
    setFormattedResume(null);

    try {
      console.log(formatType, selectedTemplate)
      const response = await fetch("http://localhost:8080/api/resumes/format", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          resumeId: selectedResume,
          formatType,
          templateId: formatType === "pdf" ? selectedTemplate : undefined,
        }),
      });

      if (!response.ok) {
        throw new Error(await response.text());
      }

      const data = await response.json();
      setFormattedResume(data);
      showSuccess("Resume formatted successfully!");
    } catch (err) {
      showError(err instanceof Error ? err.message : "Failed to format resume");
    } finally {
      setIsLoading(false);
    }
  };

  const handleDownload = async () => {
    if (!formattedResume) return;

    try {
      const response = await fetch(
        `http://localhost:8080/api/resumes/download/${formattedResume.formattedResumeId}`,
        {
          credentials: "include",
        }
      );

      if (!response.ok) {
        throw new Error("Failed to download resume");
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `resume.${formattedResume.fileExtension}`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      a.remove();
      showSuccess("Download started!");
    } catch (error) {
      showError("Failed to download resume");
    }
  };

  const handlePageChange = (newPage: number) => {
    setCurrentPage(newPage);
    // Clear selection when changing pages
    setSelectedResume("");
    setFormattedResume(null);
  };

  if (isFetchingResumes) {
    return <LoadingScreen />;
  }

  return (
    <Background className="relative flex flex-col items-center min-h-screen p-8">
      <h1 className="text-4xl font-bold text-foreground drop-shadow-md text-center mb-4">
        Format and Download Resume
      </h1>

      <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 mb-8 
      dark:bg-neutral-900 dark:border-neutral-800">
        {!selectedResume && (
          <Card className="mb-8 border-2 border-gray-500 bg-gray-50 dark:bg-gray-900/20">
            <CardContent>
              <div className="prose dark:prose-invert max-w-none">
                <p className="text-sm text-foreground">
                  Choose from multiple formats and templates to create the perfect version of your resume.
                  Download your resume in various formats to suit different application requirements.
                </p>
                <ul className="mt-4 text-sm text-muted-foreground list-disc list-inside space-y-1">
                  <li>Select from multiple format options (PDF, HTML, Markdown, Text)</li>
                  <li>Choose from professional LaTeX templates for PDF format</li>
                  <li>Preview and download your formatted resume</li>
                  <li>Get a polished, professional-looking document</li>
                </ul>
              </div>
            </CardContent>
          </Card>
        )}
        {resumes.length === 0 ? (
          <p className="text-xl text-foreground mb-6 drop-shadow-sm text-center">
            No resumes generated yet.
          </p>
        ) : (
          <>
            {!selectedResume && (
              <h2 className="text-2xl font-semibold text-foreground mb-6 text-center">
                Select a Generated Resume to Format
              </h2>
            )}
            {/* Format Controls - Only show when a resume is selected */}
            {selectedResume && (
              <div className="mb-6 space-y-6 dark:border-neutral-700">
                <div className="space-y-2">
                  <label
                    htmlFor="format-select"
                    className="block text-sm font-medium text-gray-700 dark:text-gray-300"
                  >
                    Format Type
                  </label>
                  <select
                    id="format-select"
                    value={formatType}
                    onChange={(e) => setFormatType(e.target.value)}
                    className="w-full p-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 dark:bg-neutral-800 dark:border-neutral-700 dark:text-white"
                    disabled={isLoading}
                  >
                    {formatOptions.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                  {formatType === 'pdf' && (
                    <div className="space-y-4">
                      <label
                        htmlFor="template-select"
                        className="block text-sm font-medium text-gray-700 dark:text-gray-300"
                      >
                        Select a Template
                      </label>
                      <div className="grid grid-cols-3 gap-4">
                        {templates.map((template) => (
                          <div
                            key={template.id}
                            className={`relative cursor-pointer rounded-lg border-2 p-2 transition-all hover:border-blue-500 ${
                              selectedTemplate === template.templateId
                                ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                                : 'border-gray-200 dark:border-gray-700'
                            }`}
                            onClick={() => setSelectedTemplate(template.templateId)}
                          >
                            <div className="aspect-[3/4] w-full overflow-hidden rounded-md">
                              <img
                                src={template.previewUrl}
                                alt={`${template.name} preview`}
                                className="h-full w-full object-cover"
                              />
                            </div>
                            <div className="mt-2 space-y-1">
                              <h3 className="font-medium text-gray-900 dark:text-gray-100">
                                {template.name}
                              </h3>
                              <p className="text-sm text-gray-500 dark:text-gray-400">
                                {template.description}
                              </p>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>

                <div className="flex justify-center gap-4">
                  <Button
                    onClick={handleFormatResume}
                    disabled={isLoading}
                    className={`${
                      isLoading
                        ? "bg-blue-400"
                        : "bg-blue-600 hover:bg-blue-700"
                    }`}
                  >
                    {isLoading ? "Formatting..." : "Format Resume"}
                  </Button>

                  {formattedResume && (
                    <Button
                      onClick={handleDownload}
                      className="bg-green-600 hover:bg-green-700"
                    >
                      Download Resume
                    </Button>
                  )}
                </div>
              </div>
            )}

            <div className="space-y-8">
              {currentResumes.map((resume) => (
                <GeneratedResumeCard
                  key={resume.resumeId}
                  resume={resume}
                  isSelected={selectedResume === resume.resumeId}
                  onClick={() => setSelectedResume(resume.resumeId)}
                />
              ))}
            </div>
          </>
        )}
      </div>
    </Background>
  );
}