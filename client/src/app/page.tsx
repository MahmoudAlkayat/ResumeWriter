'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';

export default function LandingPage() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null); 

  const router = useRouter();

  useEffect(() => {
    setLoading(true);
    try {
      // Simulate authentication check (replace with actual auth logic)
      const user = localStorage.getItem('user');

      setTimeout(() => {
        if (user) {
          setIsAuthenticated(true);
          router.push('/home'); // Redirect to home if authenticated
        } else {
          setLoading(false);
        }
      }, 1500); // Simulate loading delay
    } catch (err) {
      console.error('Error checking authentication:', err);
      setError('Failed to check authentication. Please try again.');
      setLoading(false);
    }
  }, [router]);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen bg-gray-100">
        <p className="text-5xl font-extrabold text-black mb-4 drop-shadow-md">Loading...</p>
      </div>
    ); // Show a loading state while checking auth
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center h-screen bg-red-100">
        <p className="text-2xl font-bold text-red-700 mb-4">Error</p>
        <p className="text-lg text-gray-800">{error}</p>
      </div>
    ); // Display an error message if something goes wrong
  }

  return (
    <div className="relative flex flex-col items-center justify-center h-screen text-center p-4 bg-gradient-to-br from-blue-200 via-white to-gray-100 overflow-hidden">
      {/* Abstract AI-inspired background pattern */}
      <div className="absolute inset-0 bg-[url('/ai-pattern.svg')] bg-cover bg-center opacity-10"></div>
      
      <h1 className="text-5xl font-extrabold text-black mb-4 drop-shadow-md">Welcome to EliteResume</h1>
      <p className="text-xl text-gray-800 mb-6 max-w-xl drop-shadow-sm">
        AI-powered resume and cover letter generation to streamline your job application process.
      </p>
      <div className="flex space-x-4 z-10">
        <Button className="bg-blue-600 text-white hover:bg-blue-700 shadow-lg px-6 py-3 text-lg" onClick={() => router.push('/login')}>
          Sign In
        </Button>
        <Button className="border border-gray-700 text-gray-700 hover:bg-gray-200 shadow-lg px-6 py-3 text-lg" onClick={() => router.push('/register')} variant="outline">
          Register
        </Button>
      </div>
    </div>
  );
}
