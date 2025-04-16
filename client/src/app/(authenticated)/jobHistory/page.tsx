// app/job-history/page.tsx
"use client";

import { useState, useEffect } from 'react';
import { Background } from '@/components/ui/background';
import LoadingScreen from '@/components/LoadingScreen';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useToast } from '@/contexts/ToastProvider';

interface JobDescription {
  jobId: string;
  title?: string;
  text: string;
  submittedAt: string;
}

const mockJobs: JobDescription[] = [
  {
    jobId: "123",
    title: "Software Engineer",
    text: "We are seeking a software engineer experienced in JavaScript, Node.js, and cloud infrastructure. Responsibilities include developing scalable backend systems and collaborating with cross-functional teams.",
    submittedAt: "2023-01-01"
  },
  {
    jobId: "456",
    text: "We are seeking a software engineer experienced in JavaScript, Node.js, and cloud infrastructure. Responsibilities include developing scalable backend systems and collaborating with cross-functional teams.",
    submittedAt: "2023-01-02"
  }
];

export default function JobHistoryPage() {
  const [jobs, setJobs] = useState<JobDescription[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const { showError } = useToast();

  async function fetchJobHistory() {
    try {
      const response = await fetch('http://localhost:8080/api/jobs/history', {
        credentials: "include"
      });
      if (!response.ok) throw new Error("Failed to fetch job history");
      const data = await response.json();
      setJobs(data.jobs || []);
      setLoading(false);
    } catch (err) {
      if (err instanceof Error) showError(err.message);
      else showError("An unknown error occurred");
    }
  }

  useEffect(() => {
    // fetchJobHistory();
    setJobs(mockJobs);
    setLoading(false);
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
          <p className="text-xl text-muted-foreground mb-6 drop-shadow-sm">
            No job descriptions submitted yet.
          </p>
        ) : (
          <div className="space-y-8">
            {jobs.map((job) => (
              <Card
                key={job.jobId}
                className="p-4 py-8 shadow-md rounded-xl bg-gray-50 border border-gray-300 dark:bg-neutral-800 dark:border-neutral-700"
              >
                <CardHeader className="text-lg">
                  <CardTitle>{job.title}</CardTitle>
                  <CardTitle>{job.submittedAt}</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-md text-gray-700 dark:text-muted-foreground">
                    {job.text}
                  </p>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </Background>
  );
}