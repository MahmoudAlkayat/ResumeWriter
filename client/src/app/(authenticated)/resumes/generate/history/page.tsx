"use client";

import { useState, useEffect, useRef } from 'react';
import { Background } from '@/components/ui/background';
import LoadingScreen from '@/components/LoadingScreen';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { useToast } from '@/contexts/ToastProvider';
import { Button } from '@/components/ui/button';
import { GeneratedResume } from '@/lib/types';

export default function GeneratedResumeHistoryPage() {
  const [resumes, setResumes] = useState<GeneratedResume[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [expandedResumes, setExpandedResumes] = useState<Set<string>>(new Set());
  const paragraphRefs = useRef<Record<string, HTMLPreElement | null>>({});
  const [overflowingResumes, setOverflowingResumes] = useState<Set<string>>(new Set());
  const { showError } = useToast();

  async function fetchGeneratedResumeHistory() {
    try {
      const response = await fetch('http://localhost:8080/api/resumes/generate/history', {
        credentials: "include"
      });
      if (!response.ok) throw new Error("Failed to fetch generated resume history");
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
    fetchGeneratedResumeHistory();
  }, []);

  useEffect(() => {
    const newOverflowing = new Set<string>();
    resumes.forEach(resume => {
      const el = paragraphRefs.current[resume.resumeId];
      if (el && el.scrollHeight > el.clientHeight) {
        newOverflowing.add(resume.resumeId);
      }
    });
    setOverflowingResumes(newOverflowing);
  }, [resumes]);

  if (loading) {
    return <LoadingScreen />;
  }

  return (
    <Background className="relative flex flex-col items-center min-h-screen p-8">
        <h1 className="text-4xl font-bold text-foreground mb-8 drop-shadow-md text-center">
          Your Generated Resumes
        </h1>

      <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 mb-8 
      dark:bg-neutral-900 dark:border-neutral-800">
        {resumes.length === 0 ? (
          <p className="text-xl text-foreground mb-6 drop-shadow-sm text-center">
            No resumes generated yet.
          </p>
        ) : (
          <div className="space-y-8">
            {resumes.map((resume) => (
              <Card
                key={resume.resumeId}
                className="p-4 py-8 shadow-md rounded-xl bg-gray-50 border border-gray-300 dark:bg-neutral-800 dark:border-neutral-700"
              >
                <CardHeader className="-mb-4">
                <CardTitle className="line-clamp-1 text-lg">
                  {resume.resumeTitle ? (
                    resume.resumeTitle
                  ) : (
                    <>
                    For: {" "}
                    <span className="italic">
                    {resume.jobDescriptionTitle ? (
                      resume.jobDescriptionTitle
                    ) : (
                      `JobID ${resume.jobId}`
                    )}
                    </span>
                    </>
                  )}
                  </CardTitle>
                </CardHeader>
                <CardContent>
                {!resume.content && <p className="text-red-500">Failed to generate</p>}
                <pre
                    ref={(el) => { paragraphRefs.current[resume.resumeId] = el; }}
                    className={`text-sm text-gray-800 dark:text-gray-200 whitespace-pre-wrap break-words ${
                      !expandedResumes.has(resume.resumeId) ? 'line-clamp-4' : ''
                    }`}
                  >
                    {(() => {
                      try {
                        return JSON.stringify(JSON.parse(resume.content), null, 2);
                      } catch {
                        return resume.content;
                      }
                    })()}
                  </pre>
                  {overflowingResumes.has(resume.resumeId) && (
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
                      {expandedResumes.has(resume.resumeId) ? 'Show less' : 'Read more'}
                    </Button>
                  )}
                </CardContent>
                <CardFooter className="-mt-2">
                    <div className="flex justify-between w-full items-center italic">
                        <p className="ml-auto text-muted-foreground text-sm">
                        Last Updated: {new Date(resume.updatedAt).toLocaleString('en-US', {
                            dateStyle: 'medium',
                            timeStyle: 'short'
                        })}
                        </p>
                        {/* {resume.resumeId && (
                            <p className="text-muted-foreground text-xs">
                                ID: {resume.resumeId}
                            </p>
                        )} */}
                    </div>
                </CardFooter>
              </Card>
            ))}
          </div>
        )}
      </div>
    </Background>
  );
}