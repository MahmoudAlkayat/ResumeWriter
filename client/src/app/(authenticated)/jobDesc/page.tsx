'use client';

import { useState, useEffect } from 'react';

export default function JobDescriptionForm() {
  const [text, setText] = useState('');
  const [status, setStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
  const [error, setError] = useState<string | null>(null);
  const [jobId, setJobId] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Basic validation
    if (!text.trim()) {
      setError('Job description cannot be empty.');
      return;
    }
    
    // Minimum character validation
    if (text.trim().length < 100) {
      setError('Job description must be at least 100 characters.');
      return;
    }

    setStatus('loading');
    setError(null);

    try {
      const token = localStorage.getItem('JWT_SECRET'); // Get JWT from storage
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
      setText(''); // Clear form on success
    } catch (err: any) {
      setError(err.message);
      setStatus('error');
    }
  };

  return (
    <form onSubmit={handleSubmit} className="max-w-xl mx-auto p-4 bg-white shadow-md rounded">
      <h2 className="text-xl font-semibold mb-4">Submit Job Description</h2>
      
      <textarea
        value={text}
        onChange={(e) => {
          setText(e.target.value);
          setError(null); // Clear error when user modifies input
        }}
        placeholder="Paste or type the job description here (minimum 100 characters)..."
        rows={8}
        className="w-full p-2 border border-gray-300 rounded mb-4"
        minLength={100}
        required
      />
      
      <button
        type="submit"
        disabled={status === 'loading'}
        className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 disabled:opacity-50"
      >
        {status === 'loading' ? 'Submitting...' : 'Submit'}
      </button>

      {status === 'success' && jobId && (
        <div className="mt-4 p-3 bg-green-100 text-green-700 rounded">
          <p>Job description submitted successfully!</p>
          <p className="mt-2 text-sm">Job ID: {jobId}</p>
        </div>
      )}

      {status === 'error' && error && (
        <div className="mt-4 p-3 bg-red-100 text-red-700 rounded">
          <p>Error: {error}</p>
        </div>
      )}
    </form>
  );
}