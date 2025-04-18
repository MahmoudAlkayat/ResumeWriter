"use client";

import { useState, useEffect, useRef } from 'react';
import { Background } from '@/components/ui/background';
import LoadingScreen from '@/components/LoadingScreen';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useToast } from '@/contexts/ToastProvider';
import { Button } from '@/components/ui/button';
import { useResumeProcessing } from '@/contexts/ResumeProcessingProvider';
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
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
        body: JSON.stringify({ jobId: selectedJobId }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || "Generation failed");
      }

      const data = await response.json();
      addActiveProcess(data.statusId, 'generate');
      showInfo("Resume generation started. You can track the progress in the status dialog.");
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

      <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 mb-8 
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
            <h2 className="text-2xl font-semibold mb-6">Select a Job Description</h2>
            <RadioGroup
              value={selectedJobId || ""}
              onValueChange={(value) => setSelectedJobId(value)}
              className="flex flex-col gap-4"
            >
              {jobs.map((job) => (
                <div key={job.jobId} className="flex items-center space-x-3 w-full">
                  <RadioGroupItem value={job.jobId} id={job.jobId} className="shrink-0" />
                  <Label htmlFor={job.jobId} className="w-full cursor-pointer">
                    <Card className={`p-4 transition-colors w-full ${selectedJobId === job.jobId ? 'bg-accent' : ''} hover:bg-accent`}>
                      <CardHeader className="p-0">
                        <CardTitle className="text-lg break-words">
                          {job.title || "Untitled Job"}
                        </CardTitle>
                        <p className="text-sm text-muted-foreground">
                          {new Date(job.submittedAt).toLocaleString('en-US', {
                            dateStyle: 'medium',
                            timeStyle: 'short'
                          })}
                        </p>
                      </CardHeader>
                      <CardContent className="p-0">
                        <p
                          ref={(el) => { paragraphRefs.current[job.jobId] = el; }}
                          className={`text-sm text-muted-foreground whitespace-pre-wrap break-all ${
                            !expandedJobs.has(job.jobId) ? 'line-clamp-3' : ''
                          }`}
                        >
                          {job.text}
                        </p>
                        {overflowingJobs.has(job.jobId) && (
                          <Button
                            variant="link"
                            className="p-0 h-auto text-sm mt-2"
                            onClick={(e) => {
                              e.preventDefault();
                              const newExpanded = new Set(expandedJobs);
                              if (expandedJobs.has(job.jobId)) {
                                newExpanded.delete(job.jobId);
                              } else {
                                newExpanded.add(job.jobId);
                              }
                              setExpandedJobs(newExpanded);
                            }}
                          >
                            {expandedJobs.has(job.jobId) ? 'Show less' : 'Show more'}
                          </Button>
                        )}
                      </CardContent>
                    </Card>
                  </Label>
                </div>
              ))}
            </RadioGroup>

            <div className="mt-8 flex justify-center">
              <Button
                onClick={handleGenerate}
                disabled={!selectedJobId || submitting}
                className="bg-blue-600 hover:bg-blue-700 text-white px-8"
              >
                {submitting ? "Submitting..." : "Generate Resume"}
              </Button>
            </div>
          </>
        )}
      </div>
    </Background>
  );
}