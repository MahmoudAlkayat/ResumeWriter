"use client";

import { useState, useEffect, useRef } from "react";
import { Button } from "@/components/ui/button";
import { Background } from "@/components/ui/background";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { useToast } from "@/contexts/ToastProvider";
import LoadingScreen from "@/components/LoadingScreen";
import { ChevronLeft, ChevronRight } from "lucide-react";

interface GeneratedResume {
  resumeId: string;
  content: string;
  createdAt: string;
  updatedAt: string;
  jobId: string;
  jobDescriptionTitle: string | null;
}

interface FormattedResume {
  formattedResumeId: string;
  formatType: string;
  fileExtension: string;
}

const RESUMES_PER_PAGE = 5;

export default function ResumeFormatPage() {
  const { showError, showSuccess } = useToast();
  const [resumes, setResumes] = useState<GeneratedResume[]>([]);
  const [selectedResume, setSelectedResume] = useState<string>("");
  const [formatType, setFormatType] = useState("markdown");
  const [isLoading, setIsLoading] = useState(false);
  const [isFetchingResumes, setIsFetchingResumes] = useState(true);
  const [formattedResume, setFormattedResume] =
    useState<FormattedResume | null>(null);
  const [expandedResumes, setExpandedResumes] = useState<Set<string>>(
    new Set()
  );
  const paragraphRefs = useRef<Record<string, HTMLPreElement | null>>({});
  const [overflowingResumes, setOverflowingResumes] = useState<Set<string>>(
    new Set()
  );
  const [currentPage, setCurrentPage] = useState(1);

  const formatOptions = [
    { value: "markdown", label: "Markdown (.md)" },
    { value: "html", label: "HTML (.html)" },
    { value: "text", label: "Plain Text (.txt)" },
  ];

  // Calculate pagination values
  const totalPages = Math.ceil(resumes.length / RESUMES_PER_PAGE);
  const startIndex = (currentPage - 1) * RESUMES_PER_PAGE;
  const endIndex = startIndex + RESUMES_PER_PAGE;
  const currentResumes = resumes.slice(startIndex, endIndex);

  // Fetch user's generated resumes
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
    const startIndex = (currentPage - 1) * RESUMES_PER_PAGE;
    const endIndex = startIndex + RESUMES_PER_PAGE;
    const visibleResumes = resumes.slice(startIndex, endIndex);
  
    const newOverflowing = new Set<string>();
    visibleResumes.forEach((resume) => {
      const el = paragraphRefs.current[resume.resumeId];
      if (el && el.scrollHeight > el.clientHeight) {
        newOverflowing.add(resume.resumeId);
      }
    });
    setOverflowingResumes(newOverflowing);
  }, [resumes, currentPage]);  

  const handleFormatResume = async () => {
    if (!selectedResume) {
      showError("Please select a resume");
      return;
    }

    setIsLoading(true);
    setFormattedResume(null);

    try {
      const response = await fetch("http://localhost:8080/api/resumes/format", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          resumeId: selectedResume,
          formatType,
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
      {/* Pagination Controls */}
      {totalPages > 1 && (
        <div className="flex justify-center items-center gap-4 mb-4">
          <Button
            variant="outline"
            size="icon"
            onClick={() => handlePageChange(currentPage - 1)}
            disabled={currentPage === 1}
            className="h-8 w-8"
          >
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <div className="text-sm text-gray-600 dark:text-gray-400">
            Page {currentPage} of {totalPages}
          </div>
          <Button
            variant="outline"
            size="icon"
            onClick={() => handlePageChange(currentPage + 1)}
            disabled={currentPage === totalPages}
            className="h-8 w-8"
          >
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>
      )}
      <div
        className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 mb-8 
      dark:bg-neutral-900 dark:border-neutral-800"
      >
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
                <Card
                  key={resume.resumeId}
                  className={`p-4 py-8 shadow-md rounded-xl bg-gray-50 border border-gray-300 dark:bg-neutral-800 dark:border-neutral-700 ${
                    selectedResume === resume.resumeId
                      ? "ring-2 ring-blue-500"
                      : ""
                  }`}
                  onClick={() => setSelectedResume(resume.resumeId)}
                >
                  <CardHeader className="-mb-4">
                    <CardTitle className="line-clamp-1 text-lg">
                      For:{" "}
                      <span className="italic">
                        {resume.jobDescriptionTitle
                          ? resume.jobDescriptionTitle
                          : `JobID ${resume.jobId}`}
                      </span>
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    {!resume.content && (
                      <p className="text-red-500">Failed to generate</p>
                    )}
                    <pre
                      ref={(el) => {
                        paragraphRefs.current[resume.resumeId] = el;
                      }}
                      className={`text-sm text-gray-800 dark:text-gray-200 whitespace-pre-wrap break-words ${
                        !expandedResumes.has(resume.resumeId)
                          ? "line-clamp-4"
                          : ""
                      }`}
                    >
                      {(() => {
                        try {
                          return JSON.stringify(
                            JSON.parse(resume.content),
                            null,
                            2
                          );
                        } catch {
                          return resume.content;
                        }
                      })()}
                    </pre>
                    {overflowingResumes.has(resume.resumeId) && (
                      <Button
                        variant="link"
                        className="p-0 h-auto text-sm mt-2"
                        onClick={(e) => {
                          e.stopPropagation();
                          const newExpanded = new Set(expandedResumes);
                          if (expandedResumes.has(resume.resumeId)) {
                            newExpanded.delete(resume.resumeId);
                          } else {
                            newExpanded.add(resume.resumeId);
                          }
                          setExpandedResumes(newExpanded);
                        }}
                      >
                        {expandedResumes.has(resume.resumeId)
                          ? "Show less"
                          : "Read more"}
                      </Button>
                    )}
                  </CardContent>
                  <CardFooter className="-mt-2">
                    <div className="flex justify-between w-full items-center italic">
                      <p className="ml-auto text-muted-foreground text-sm">
                        Last Updated:{" "}
                        {new Date(resume.updatedAt).toLocaleString("en-US", {
                          dateStyle: "medium",
                          timeStyle: "short",
                        })}
                      </p>
                    </div>
                  </CardFooter>
                </Card>
              ))}
            </div>
          </>
        )}
      </div>
    </Background>
  );
}