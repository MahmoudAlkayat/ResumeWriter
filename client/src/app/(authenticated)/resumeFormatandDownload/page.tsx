'use client';

import { useEffect, useState } from 'react';
import { Button } from "@/components/ui/button";
import { Background } from "@/components/ui/background";
import { Card, CardContent } from "@/components/ui/card";
import { useToast } from "@/contexts/ToastProvider";
import { Input } from "@/components/ui/input";
import LoadingScreen from "@/components/LoadingScreen";
import { useAuth } from "@/hooks/auth";

interface Resume {
  id: string;
  title: string;
}

export default function ResumeFormatPage() {
  const { user } = useAuth();
  const { showError, showSuccess } = useToast();
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [selectedResume, setSelectedResume] = useState('');
  const [formatType, setFormatType] = useState('markdown');
  const [isLoading, setIsLoading] = useState(false);
  const [isFetchingResumes, setIsFetchingResumes] = useState(true);
  const [formattedResumeId, setFormattedResumeId] = useState<string | null>(null);

  const formatOptions = [
    { value: 'markdown', label: 'Markdown (.md)' },
    { value: 'html', label: 'HTML (.html)' },
    { value: 'text', label: 'Plain Text (.txt)' },
  ];

  // Fetch user's resumes
  useEffect(() => {
    const fetchResumes = async () => {
      try {
        const response = await fetch('/api/resumes', {
          credentials: "include"
        });

        if (!response.ok) {
          throw new Error('Failed to fetch resumes');
        }

        const data = await response.json();
        setResumes(data);
        setIsFetchingResumes(false);
      } catch (err) {
        showError(err instanceof Error ? err.message : 'Failed to load resumes');
        setIsFetchingResumes(false);
      }
    };

    if (user?.id) {
      fetchResumes();
    }
  }, [user]);

  const handleFormatResume = async () => {
    if (!selectedResume) {
      showError('Please select a resume');
      return;
    }

    setIsLoading(true);
    setFormattedResumeId(null);

    try {
      const response = await fetch('/api/resumes/format', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
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

      const { formattedResumeId } = await response.json();
      setFormattedResumeId(formattedResumeId);
      showSuccess('Resume formatted successfully!');
    } catch (err) {
      showError(err instanceof Error ? err.message : 'Failed to format resume');
    } finally {
      setIsLoading(false);
    }
  };

  const handleDownload = async () => {
    if (!formattedResumeId) return;

    try {
      const response = await fetch(`/api/resumes/download/${formattedResumeId}`, {
        credentials: "include"
      });
      if (!response.ok) {
        throw new Error('Failed to download resume');
      }
      
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `resume.${formatType}`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      a.remove();
      showSuccess('Download started!');
    } catch (error) {
      showError('Failed to download resume');
    }
  };

  if (isFetchingResumes) {
    return <LoadingScreen />;
  }

  return (
    <Background className="relative flex flex-col items-center justify-start min-h-screen p-8 text-center">
      <h2 className="text-4xl font-bold text-primary mb-8 drop-shadow-md">
        Format and Download Resume
      </h2>

      <Card className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-10 border border-gray-200 dark:bg-neutral-900 dark:border-neutral-800">
        <CardContent className="space-y-6">
          <div className="space-y-2 text-left">
            <label htmlFor="resume-select" className="block text-sm font-medium text-gray-700 dark:text-gray-300">
              Select Resume
            </label>
            <select
              id="resume-select"
              value={selectedResume}
              onChange={(e) => setSelectedResume(e.target.value)}
              className="w-full p-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 dark:bg-neutral-800 dark:border-neutral-700 dark:text-white"
              disabled={isLoading}
            >
              <option value="">Choose a resume</option>
              {resumes.map((resume) => (
                <option key={resume.id} value={resume.id}>
                  {resume.title}
                </option>
              ))}
            </select>
          </div>

          <div className="space-y-2 text-left">
            <label htmlFor="format-select" className="block text-sm font-medium text-gray-700 dark:text-gray-300">
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

          <div className="flex justify-center gap-4 pt-4">
            <Button
              onClick={handleFormatResume}
              disabled={isLoading || !selectedResume}
              className={`${isLoading || !selectedResume ? 'bg-blue-400' : 'bg-blue-600 hover:bg-blue-700'}`}
            >
              {isLoading ? 'Formatting...' : 'Format Resume'}
            </Button>

            {formattedResumeId && (
              <Button
                onClick={handleDownload}
                className="bg-green-600 hover:bg-green-700"
              >
                Download Resume
              </Button>
            )}
          </div>
        </CardContent>
      </Card>
    </Background>
  );
}