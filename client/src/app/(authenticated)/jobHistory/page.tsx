// app/job-history/page.tsx
"use client";

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Background } from '@/components/ui/background';

interface JobDescription {
  jobId: string;
  text: string;
  submittedAt: string;
}

// Inline auth function - no separate file needed
const getToken = async (): Promise<string | null> => {
  try {
    // Using NextAuth.js default session (adjust if using different auth)
    const response = await fetch('/api/auth/session');
    if (!response.ok) return null;
    const session = await response.json();
    return session?.user?.accessToken || session?.accessToken || null;
  } catch (error) {
    console.error('Error fetching session:', error);
    return null;
  }
};

export default function JobHistoryPage() {
  const [jobs, setJobs] = useState<JobDescription[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  useEffect(() => {
    const fetchJobHistory = async () => {
      try {
        const token = await getToken();
        if (!token) {
          router.push('/login');
          return;
        }

        const response = await fetch('/api/jobs/history', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          throw new Error(response.status === 401 
            ? 'Please login to view job history' 
            : 'Failed to fetch job history');
        }

        const data = await response.json();
        setJobs(data.jobs || []);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'An unknown error occurred');
      } finally {
        setLoading(false);
      }
    };

    fetchJobHistory();
  }, [router]);

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const truncateText = (text: string, maxLength: number = 100) => {
    if (text.length <= maxLength) return text;
    return `${text.substring(0, maxLength)}...`;
  };

  const handleUseJob = (jobId: string) => {
    router.push(`/generate-resume?jobId=${jobId}`);
  };

  return (
    <Background className="relative flex flex-col items-center min-h-screen p-8">
      <div className="w-full max-w-4xl">
        <h1 className="text-4xl font-bold text-foreground mb-6 drop-shadow-md text-center">
          Your Job Description History
        </h1>
        
        {loading ? (
          <div className="text-center py-12">
            <p className="text-xl text-muted-foreground drop-shadow-sm">
              Loading your job history...
            </p>
          </div>
        ) : error ? (
          <div className="bg-destructive/10 p-6 rounded-lg text-center">
            <p className="text-destructive text-lg mb-4">Error: {error}</p>
            <button 
              onClick={() => window.location.reload()}
              className="px-4 py-2 bg-primary text-primary-foreground rounded-md hover:bg-primary/90 transition-colors"
            >
              Try Again
            </button>
          </div>
        ) : jobs.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-xl text-muted-foreground mb-6 drop-shadow-sm">
              No job descriptions submitted yet.
            </p>
            <button 
              onClick={() => router.push('/submit-job')}
              className="px-6 py-3 bg-primary text-primary-foreground rounded-md hover:bg-primary/90 transition-colors text-lg"
            >
              Submit Your First Job Description
            </button>
          </div>
        ) : (
          <div className="space-y-6">
            {jobs.map((job) => (
              <div 
                key={job.jobId} 
                className="bg-background/80 backdrop-blur-sm border border-border rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow"
              >
                <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-2 mb-4">
                  <h3 className="text-lg font-medium text-foreground">
                    Job ID: <span className="text-primary">{job.jobId}</span>
                  </h3>
                  <span className="text-sm text-muted-foreground">
                    {formatDate(job.submittedAt)}
                  </span>
                </div>
                <div className="mb-6">
                  <p className="text-muted-foreground">
                    {truncateText(job.text)}
                  </p>
                </div>
                <div className="flex flex-col sm:flex-row gap-3">
                  <button 
                    onClick={() => handleUseJob(job.jobId)}
                    className="px-4 py-2 bg-primary text-primary-foreground rounded-md hover:bg-primary/90 transition-colors flex-1"
                  >
                    Use for Resume
                  </button>
                  <button 
                    onClick={() => {
                      navigator.clipboard.writeText(job.text);
                      alert('Job description copied to clipboard!');
                    }}
                    className="px-4 py-2 border border-border bg-background text-foreground rounded-md hover:bg-accent transition-colors flex-1"
                  >
                    Copy Text
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </Background>
  );
}