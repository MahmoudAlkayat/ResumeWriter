"use client";

import { useState, useEffect, useRef } from 'react';
import { Background } from '@/components/ui/background';
import LoadingScreen from '@/components/LoadingScreen';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { useToast } from '@/contexts/ToastProvider';
import { Button } from '@/components/ui/button';
import { GeneratedResume } from '@/lib/types';
import GeneratedResumeCard from "@/components/GeneratedResumeCard";

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
              <GeneratedResumeCard
                key={resume.resumeId}
                resume={resume}
              />
            ))}
          </div>
        )}
      </div>
    </Background>
  );
}