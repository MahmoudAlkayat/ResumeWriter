"use client";

import { useState, useEffect } from 'react';
import { Background } from '@/components/ui/background';
import LoadingScreen from '@/components/LoadingScreen';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useToast } from '@/contexts/ToastProvider';
import { Button } from '@/components/ui/button';

interface JobDescription {
  jobId: string;
  title?: string;
  text: string;
  submittedAt: string;
}

export default function JobHistoryPage() {
  const [jobs, setJobs] = useState<JobDescription[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [expandedJobs, setExpandedJobs] = useState<Set<string>>(new Set());
  const { showError } = useToast();

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

  if (loading) {
    return <LoadingScreen />;
  }

  return (
    <Background className="relative flex flex-col items-center min-h-screen p-8">
        <h1 className="text-4xl font-bold text-foreground mb-8 drop-shadow-md text-center">
          Your Job Description History
        </h1>

      <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 mb-8 
      dark:bg-neutral-900 dark:border-neutral-800">
        {jobs.length === 0 ? (
          <p className="text-xl text-foreground mb-6 drop-shadow-sm text-center">
            No job descriptions submitted yet.
          </p>
        ) : (
          <div className="space-y-8">
            {jobs.map((job) => (
              <Card
                key={job.jobId}
                className="p-4 py-8 shadow-md rounded-xl bg-gray-50 border border-gray-300 dark:bg-neutral-800 dark:border-neutral-700"
              >
                <CardHeader className="-mb-4">
                  <CardTitle className="text-lg">{new Date(job.submittedAt).toLocaleString('en-US', {
                    dateStyle: 'medium',
                    timeStyle: 'short'
                  })}</CardTitle>
                  <CardTitle className="line-clamp-1 text-lg">{job.title}</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className={`text-md text-gray-700 dark:text-muted-foreground break-words whitespace-pre-wrap ${
                    !expandedJobs.has(job.jobId) ? 'line-clamp-4' : ''
                  }`}>
                    {job.text}
                  </p>
                  {job.text.length > 400 && (
                    <Button
                      variant="link"
                      className="p-0 h-auto text-sm mt-2"
                      onClick={() => {
                        const newExpanded = new Set(expandedJobs);
                        if (expandedJobs.has(job.jobId)) {
                          newExpanded.delete(job.jobId);
                        } else {
                          newExpanded.add(job.jobId);
                        }
                        setExpandedJobs(newExpanded);
                      }}
                    >
                      {expandedJobs.has(job.jobId) ? 'Show less' : 'Read more'}
                    </Button>
                  )}
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </Background>
  );
}