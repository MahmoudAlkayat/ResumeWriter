'use client';

import { Background } from '@/components/ui/background';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { useToast } from '@/contexts/ToastProvider';
import { useState } from 'react';

export default function JobDescriptionForm() {
  const [text, setText] = useState('');
  const [title, setTitle] = useState('');
  const [loading, setLoading] = useState(false);
  const [jobId, setJobId] = useState<string | null>(null);
  const { showSuccess, showError } = useToast();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!text.trim()) {
      showError('Job description cannot be empty.');
      return;
    }

    if (text.trim().length < 100) {
      showError('Job description must be at least 100 characters.');
      return;
    }

    if (title.length > 255) {
      showError('Job title must not exceed 255 characters.');
      return;
    }

    try {
      setLoading(true);
      const response = await fetch('http://localhost:8080/api/jobs/submit', {
        method: 'POST',
        credentials: "include",
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
          jobTitle: title,
          jobDescription: text 
        }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to submit job description.');
      }

      const data = await response.json();
      showSuccess('Job description submitted successfully!');
      setJobId(data.jobId);
      setText('');
      setTitle('');
    } catch (err: any) {
      showError('Error submitting job description');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Background className="relative flex flex-col items-center min-h-screen text-center p-8">
      <h1 className="text-4xl font-bold text-foreground mb-8">Submit a Job Description</h1>
      <div className="w-full max-w-5xl bg-white shadow-xl rounded-2xl p-8 border border-gray-200 mb-8 
      dark:bg-neutral-900 dark:border-neutral-800">
        <Input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Job Title (optional)"
          className="w-full p-3 border border-input bg-muted text-foreground rounded-lg mb-4 focus:outline-none focus:ring-2 focus:ring-ring"
          maxLength={255}
        />
        <Textarea
          value={text}
          onChange={(e) => {
            setText(e.target.value);
          }}
          placeholder="Paste or type the job description here (minimum 100 characters)..."
          className="w-full p-3 border border-input bg-muted text-foreground rounded-lg mb-4 focus:outline-none focus:ring-2 focus:ring-ring h-[300px] overflow-y-auto"
          minLength={100}
        />
      </div>

      <Button
        type="submit"
        disabled={loading || !text.trim()}
        className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2"
        onClick={handleSubmit}
      >
          {loading ? 'Submitting...' : 'Submit'}
        </Button>
    </Background>
  );
}
