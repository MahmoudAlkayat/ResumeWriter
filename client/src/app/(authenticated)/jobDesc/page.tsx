'use client';

import { useState } from 'react';

export default function JobDescriptionForm() {
  const [text, setText] = useState('');
  const [status, setStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
  const [error, setError] = useState<string | null>(null);
  const [jobId, setJobId] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!text.trim()) {
      setError('Job description cannot be empty.');
      return;
    }

    if (text.trim().length < 100) {
      setError('Job description must be at least 100 characters.');
      return;
    }

    setStatus('loading');
    setError(null);

    try {
      const token = localStorage.getItem('JWT_SECRET');
      if (!token) throw new Error('Authentication required.');

      const response = await fetch('/api/jobs/submit', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ text }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to submit job description.');
      }

      const data = await response.json();
      setJobId(data.jobId);
      setStatus('success');
      setText('');
    } catch (err: any) {
      setError(err.message);
      setStatus('error');
    }
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="max-w-xl mx-auto p-6 bg-background border border-border rounded-2xl shadow-sm"
    >
      <h2 className="text-2xl font-semibold text-foreground mb-4 drop-shadow-sm">Submit Job Description</h2>

      <textarea
        value={text}
        onChange={(e) => {
          setText(e.target.value);
          setError(null);
        }}
        placeholder="Paste or type the job description here (minimum 100 characters)..."
        rows={8}
        className="w-full p-3 border border-input bg-muted text-foreground rounded-lg mb-4 focus:outline-none focus:ring-2 focus:ring-ring"
        minLength={100}
        required
      />

      <button
        type="submit"
        disabled={status === 'loading'}
        className="w-full bg-primary text-primary-foreground px-4 py-2 rounded-md hover:bg-primary/90 disabled:opacity-50 transition-colors"
      >
        {status === 'loading' ? 'Submitting...' : 'Submit'}
      </button>

      {status === 'success' && jobId && (
        <div className="mt-4 p-4 bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-300 rounded-lg">
          <p className="font-medium">Job description submitted successfully!</p>
          <p className="text-sm mt-1">Job ID: {jobId}</p>
        </div>
      )}

      {status === 'error' && error && (
        <div className="mt-4 p-4 bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-300 rounded-lg">
          <p className="font-medium">Error: {error}</p>
        </div>
      )}
    </form>
  );
}
