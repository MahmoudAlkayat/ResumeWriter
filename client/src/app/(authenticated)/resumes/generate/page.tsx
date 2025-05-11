"use client";

import { useState, useEffect, useRef } from 'react';
import { Background } from '@/components/ui/background';
import LoadingScreen from '@/components/LoadingScreen';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { useToast } from '@/contexts/ToastProvider';
import { Button } from '@/components/ui/button';
import { useResumeProcessing } from '@/contexts/ResumeProcessingProvider';
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

interface JobDescription {
  jobId: string;
  title?: string;
  text: string;
  submittedAt: string;
}

export default function GenerateResumePage() {
  const [jobs, setJobs] = useState<JobDescription[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [selectedJobId, setSelectedJobId] = useState<string | null>(null);
  const [resumeTitle, setResumeTitle] = useState<string>("");
  const [submitting, setSubmitting] = useState(false);
  const [expandedJobs, setExpandedJobs] = useState<Set<string>>(new Set());
  const paragraphRefs = useRef<Record<string, HTMLParagraphElement | null>>({});
  const [overflowingJobs, setOverflowingJobs] = useState<Set<string>>(new Set());
  const { showError, showInfo } = useToast();
  const { addActiveProcess } = useResumeProcessing();

  async function fetchJobHistory() {
    try {
      const response = await fetch('http://localhost:8080/api/jobs/history', {
        credentials: "include"
      });
      if (!response.ok) throw new Error("Failed to fetch job history");
      const data = await response.json();
      setJobs(data || []);
    } catch (err) {
      if (err instanceof Error) showError(err.message);
      else showError("An unknown error occurred");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchJobHistory();
  }, []);

  useEffect(() => {
    const newOverflowing = new Set<string>();
    jobs.forEach(job => {
      const el = paragraphRefs.current[job.jobId];
      if (el && el.scrollHeight > el.clientHeight) {
        newOverflowing.add(job.jobId);
      }
    });
    setOverflowingJobs(newOverflowing);
  }, [jobs]);

  const handleGenerate = async () => {
    if (!selectedJobId) {
      showError("Please select a job description first");
      return;
    }

    setSubmitting(true);
    try {
      const response = await fetch('http://localhost:8080/api/resumes/generate', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
          jobId: selectedJobId,
          title: resumeTitle.trim() || undefined
        }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || "Generation failed");
      }

      const data = await response.json();
      addActiveProcess(data.statusId, 'generate');
      showInfo("Resume generation started. You can track the progress in the status dialog.");
      
      // Reset form
      setSelectedJobId(null);
      setResumeTitle("");
    } catch (error) {
      if (error instanceof Error) showError(error.message);
      else showError("Failed to generate resume");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <LoadingScreen />;
  }

  return (
    <Background className="relative flex flex-col items-center min-h-screen p-8">
      <h1 className="text-4xl font-bold text-foreground mb-8 drop-shadow-md text-center">
        Generate a Resume
      </h1>

      <div className="w-full max-w-7xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 mb-8 
      dark:bg-neutral-900 dark:border-neutral-800">
        {jobs.length === 0 ? (
          <div className="text-center">
            <p className="text-xl text-foreground mb-6 drop-shadow-sm">
              No job descriptions found.
            </p>
            <p className="text-muted-foreground">
              Submit a job description first to generate a resume.
            </p>
          </div>
        ) : (
          <>
            <div className="">
              {!selectedJobId && (
                <h2 className="text-2xl font-semibold text-foreground mb-6 text-center">
                Select a Job Description
              </h2>
              )}
              {selectedJobId && (
              <div className="space-y-6 mb-6">
                <div className="space-y-2">
                  <Label htmlFor="resumeTitle" className="text-sm font-medium text-gray-700 dark:text-gray-300">
                    Resume Title
                  </Label>
                  <Input
                    id="resumeTitle"
                    value={resumeTitle}
                    onChange={(e) => setResumeTitle(e.target.value)}
                    placeholder="Enter a title for your resume (optional)"
                    className="w-full"
                  />
                </div>

                <div className="flex justify-center">
                  <Button
                    onClick={handleGenerate}
                    disabled={submitting}
                    className={`${
                      submitting
                        ? "bg-blue-400"
                        : "bg-blue-600 hover:bg-blue-700"
                    }`}
                  >
                    {submitting ? "Generating..." : "Generate Resume"}
                  </Button>
                </div>
              </div>
            )}
              <div className="max-h-[600px] overflow-y-auto space-y-4 pr-2">
                {jobs.map((job) => (
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
                        {job.title || `JobID: ${job.jobId}`}
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <p
                        ref={(el) => {
                          paragraphRefs.current[job.jobId] = el;
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
                          {expandedJobs.has(job.jobId) ? "Show less" : "Read more"}
                        </Button>
                      )}
                    </CardContent>
                    <CardFooter className="-mt-2">
                      <p className="text-sm text-muted-foreground">
                        Submitted: {new Date(job.submittedAt).toLocaleString()}
                      </p>
                    </CardFooter>
                  </Card>
                ))}
              </div>
            </div>
          </>
        )}
      </div>
    </Background>
  );
}