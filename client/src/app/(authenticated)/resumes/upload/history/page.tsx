"use client";

import { useState, useEffect } from 'react';
import { Background } from '@/components/ui/background';
import LoadingScreen from '@/components/LoadingScreen';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useToast } from '@/contexts/ToastProvider';
import { Button } from '@/components/ui/button';

interface Resume {
    resumeId: string;
    title: string;
    content: string;
    createdAt: string;
}

export default function ResumeHistoryPage() {
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [expandedResumes, setExpandedResumes] = useState<Set<string>>(new Set());
  const { showError } = useToast();

  async function fetchResumeHistory() {
    try {
      const response = await fetch('http://localhost:8080/api/resumes/upload/history', {
        credentials: "include"
      });
      if (!response.ok) throw new Error("Failed to fetch resume history");
      const data = await response.json();
      setResumes(data || []);
    } catch (err) {
      if (err instanceof Error) showError(err.message);
      else showError("An unknown error occurred");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchResumeHistory();
  }, []);

  if (loading) {
    return <LoadingScreen />;
  }

  return (
    <Background className="relative flex flex-col items-center min-h-screen p-8">
        <h1 className="text-4xl font-bold text-foreground mb-8 drop-shadow-md text-center">
          Your Uploaded Resumes
        </h1>

      <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 mb-8 
      dark:bg-neutral-900 dark:border-neutral-800">
        {resumes.length === 0 ? (
          <p className="text-xl text-foreground mb-6 drop-shadow-sm text-center">
            No resumes uploaded yet.
          </p>
        ) : (
          <div className="space-y-8">
            {resumes.map((resume) => (
              <Card
                key={resume.resumeId}
                className="p-4 py-8 shadow-md rounded-xl bg-gray-50 border border-gray-300 dark:bg-neutral-800 dark:border-neutral-700"
              >
                <CardHeader className="-mb-4">
                  <div className="flex">
                  <CardTitle className="text-lg">{new Date(resume.createdAt).toLocaleString('en-US', {
                    dateStyle: 'medium',
                    timeStyle: 'short'
                  })}</CardTitle>
                  <div className="ml-auto">
                    <Button
                        variant="link"
                        className="p-0 h-auto text-sm"
                        onClick={async () => {
                          try {
                            const response = await fetch(`http://localhost:8080/api/resumes/upload/${resume.resumeId}/original`, {
                              credentials: "include"
                            });

                            if (!response.ok) {
                              throw new Error("Failed to download file");
                            }

                            const blob = await response.blob();
                            const url = window.URL.createObjectURL(blob);
                            const a = document.createElement('a');
                            a.href = url;
                            a.download = resume.title;
                            document.body.appendChild(a);
                            a.click();
                            window.URL.revokeObjectURL(url);
                            document.body.removeChild(a);
                          } catch (err) {
                            if (err instanceof Error) showError(err.message);
                            else showError("Failed to download file");
                          }
                        }}
                      >
                        View Original
                      </Button>
                  </div>
                  </div>
                  <CardTitle className="line-clamp-1 text-lg">{resume.title}</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className={`text-md text-gray-700 dark:text-muted-foreground break-words whitespace-pre-wrap ${
                    !expandedResumes.has(resume.resumeId) ? 'line-clamp-4' : ''
                  }`}>
                    {resume.content}
                  </p>
                  {resume.content.length > 400 && (
                    <Button
                      variant="link"
                      className="p-0 h-auto text-sm mt-2"
                      onClick={() => {
                        const newExpanded = new Set(expandedResumes);
                        if (expandedResumes.has(resume.resumeId)) {
                          newExpanded.delete(resume.resumeId);
                        } else {
                          newExpanded.add(resume.resumeId);
                        }
                        setExpandedResumes(newExpanded);
                      }}
                    >
                      {expandedResumes.has(resume.resumeId) ? 'Show Less' : 'Read More'}
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