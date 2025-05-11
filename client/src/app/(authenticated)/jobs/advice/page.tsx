"use client";

import { useState, useEffect, useRef } from "react";
import { Background } from "@/components/ui/background";
import LoadingScreen from "@/components/LoadingScreen";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardFooter,
} from "@/components/ui/card";
import { useToast } from "@/contexts/ToastProvider";
import { Button } from "@/components/ui/button";
import { GeneratedResume } from '@/lib/types';
import GeneratedResumeCard from "@/components/GeneratedResumeCard";

interface JobDescription {
  jobId: string;
  title?: string;
  text: string;
  submittedAt: string;
}

interface AdviceResponse {
  advice: string;
}

export default function JobAdvicePage() {
  const [jobs, setJobs] = useState<JobDescription[]>([]);
  const [resumes, setResumes] = useState<GeneratedResume[]>([]);
  const [selectedJobId, setSelectedJobId] = useState<string>("");
  const [selectedResumeId, setSelectedResumeId] = useState<string>("");
  const [advice, setAdvice] = useState<string>("");
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isGeneratingAdvice, setIsGeneratingAdvice] = useState<boolean>(false);
  const [expandedJobs, setExpandedJobs] = useState<Set<string>>(new Set());
  const [expandedResumes, setExpandedResumes] = useState<Set<string>>(
    new Set()
  );
  const jobRefs = useRef<Record<string, HTMLParagraphElement | null>>({});
  const resumeRefs = useRef<Record<string, HTMLPreElement | null>>({});
  const [overflowingJobs, setOverflowingJobs] = useState<Set<string>>(
    new Set()
  );
  const [overflowingResumes, setOverflowingResumes] = useState<Set<string>>(
    new Set()
  );
  const { showError, showSuccess } = useToast();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [jobsResponse, resumesResponse] = await Promise.all([
          fetch("http://localhost:8080/api/jobs/history", {
            credentials: "include",
          }),
          fetch("http://localhost:8080/api/resumes/generate/history", {
            credentials: "include",
          }),
        ]);

        if (!jobsResponse.ok) throw new Error("Failed to fetch job history");
        if (!resumesResponse.ok)
          throw new Error("Failed to fetch resume history");

        const jobsData = await jobsResponse.json();
        const resumesData = await resumesResponse.json();

        setJobs(jobsData || []);
        setResumes(resumesData || []);
      } catch (err) {
        showError(err instanceof Error ? err.message : "Failed to load data");
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, []);

  useEffect(() => {
    const newOverflowingJobs = new Set<string>();
    jobs.forEach((job) => {
      const el = jobRefs.current[job.jobId];
      if (el && el.scrollHeight > el.clientHeight) {
        newOverflowingJobs.add(job.jobId);
      }
    });
    setOverflowingJobs(newOverflowingJobs);
  }, [jobs]);

  useEffect(() => {
    const newOverflowingResumes = new Set<string>();
    resumes.forEach((resume) => {
      const el = resumeRefs.current[resume.resumeId];
      if (el && el.scrollHeight > el.clientHeight) {
        newOverflowingResumes.add(resume.resumeId);
      }
    });
    setOverflowingResumes(newOverflowingResumes);
  }, [resumes]);

  const handleGenerateAdvice = async () => {
    if (!selectedJobId || !selectedResumeId) {
      showError("Please select both a job description and a resume");
      return;
    }

    setIsGeneratingAdvice(true);
    setAdvice("");

    try {
      const response = await fetch("http://localhost:8080/api/jobs/advice", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          jobId: selectedJobId,
          resumeId: selectedResumeId,
        }),
      });

      if (!response.ok) {
        throw new Error(await response.text());
      }

      const data: AdviceResponse = await response.json();
      setAdvice(data.advice);
      showSuccess("Advice generated successfully!");
    } catch (err) {
      showError(
        err instanceof Error ? err.message : "Failed to generate advice"
      );
    } finally {
      setIsGeneratingAdvice(false);
    }
  };

  if (isLoading) {
    return <LoadingScreen />;
  }

  return (
    <Background className="relative flex flex-col items-center min-h-screen p-8">
      <h1 className="text-4xl font-bold text-foreground mb-8 drop-shadow-md text-center">
        Job-Seeking Advice
      </h1>

      <div
        className="w-full max-w-7xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 mb-8 
      dark:bg-neutral-900 dark:border-neutral-800"
      >
        {/* Advice Display */}
        {advice && (
          <Card className="mb-8 border-2 border-green-500 bg-green-50 dark:bg-green-900/20">
            <CardHeader>
              <CardTitle className="text-xl font-semibold text-foreground">
                Personalized Job-Seeking Advice
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="prose dark:prose-invert max-w-none -mt-4">
                <p className="whitespace-pre-wrap break-words text-gray-700 dark:text-gray-300">
                  {advice}
                </p>
              </div>
            </CardContent>
          </Card>
        )}
        {!advice && (
          <Card className="mb-8 border-2 border-gray-500 bg-gray-50 dark:bg-gray-900/20">
            <CardContent>
              <div className="prose dark:prose-invert max-w-none">
                Please select a job description and generated resume then click the "Get Advice" button
                <p className="mt-4 text-sm text-muted-foreground">
                  The advice will help you understand how well your resume matches the job requirements and provide suggestions for improvement.
                </p>
              </div>
            </CardContent>
          </Card>
        )}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Job Descriptions Section */}
          <div className="space-y-4">
            <h2 className="text-2xl font-semibold text-foreground mb-4 text-center">
              Job Descriptions
            </h2>
            <div className="max-h-[600px] overflow-y-auto space-y-4 pr-2">
              {jobs.length === 0 ? (
                <p className="text-muted-foreground text-center py-4">
                  No job descriptions found. Please submit a job description first.
                </p>
              ) : (
                jobs.map((job) => (
                  <Card
                    key={job.jobId}
                    className={`p-4 shadow-md rounded-xl transition-all duration-200 ${
                      selectedJobId === job.jobId
                        ? "border-2 border-blue-500 bg-blue-50 dark:bg-blue-900/20"
                        : "border border-gray-300 dark:border-neutral-700"
                    }`}
                    onClick={() => setSelectedJobId(job.jobId)}
                  >
                    <CardHeader className="-mb-4">
                      <CardTitle className="line-clamp-1 text-lg">
                        {job.title || `Job ID: ${job.jobId}`}
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <p
                        ref={(el) => {
                          jobRefs.current[job.jobId] = el;
                        }}
                        className={`text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap break-words ${
                          !expandedJobs.has(job.jobId) ? "line-clamp-4" : ""
                        }`}
                      >
                        {job.text}
                      </p>
                      {overflowingJobs.has(job.jobId) && (
                        <Button
                          variant="link"
                          className="p-0 h-auto text-sm mt-2"
                          onClick={(e) => {
                            e.stopPropagation();
                            const newExpanded = new Set(expandedJobs);
                            if (expandedJobs.has(job.jobId)) {
                              newExpanded.delete(job.jobId);
                            } else {
                              newExpanded.add(job.jobId);
                            }
                            setExpandedJobs(newExpanded);
                          }}
                        >
                          {expandedJobs.has(job.jobId)
                            ? "Show less"
                            : "Read more"}
                        </Button>
                      )}
                    </CardContent>
                    <CardFooter className="-mt-2">
                      <p className="text-sm text-muted-foreground">
                        Submitted: {new Date(job.submittedAt).toLocaleString()}
                      </p>
                    </CardFooter>
                  </Card>
                ))
              )}
            </div>
          </div>

          {/* Resumes Section */}
          <div className="space-y-4">
            <h2 className="text-2xl font-semibold text-foreground mb-4 text-center">
              Generated Resumes
            </h2>
            <div className="max-h-[600px] overflow-y-auto space-y-4 pr-2">
              {resumes.length === 0 ? (
                <p className="text-muted-foreground text-center py-4">
                  No generated resumes found. Please generate a resume first.
                </p>
              ) : (
                resumes.map((resume) => (
                  <GeneratedResumeCard
                    key={resume.resumeId}
                    resume={resume}
                    isSelected={selectedResumeId === resume.resumeId}
                    onClick={() => setSelectedResumeId(resume.resumeId)}
                  />
                ))
              )}
            </div>
          </div>
        </div>

        {/* Generate Advice Button */}
        <div className="flex justify-center mt-8">
          <Button
            onClick={handleGenerateAdvice}
            disabled={!selectedJobId || !selectedResumeId || isGeneratingAdvice}
            className={`${
              !selectedJobId || !selectedResumeId || isGeneratingAdvice
                ? "bg-blue-400"
                : "bg-blue-600 hover:bg-blue-700"
            }`}
          >
            {isGeneratingAdvice ? "Generating Advice..." : "Get Advice"}
          </Button>
        </div>
      </div>
    </Background>
  );
}