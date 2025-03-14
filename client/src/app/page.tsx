'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/contexts/AuthProvider';
import LoadingScreen from '@/components/LoadingScreen';

export default function LandingPage() {
  const { isAuthenticated } = useAuth();
  const router = useRouter();

  //Redirect logic and avoiding initial page render
  useEffect(() => {
    if (isAuthenticated) {
      router.replace('/home');
    }
  }, [isAuthenticated, router]);

  if (isAuthenticated === null || isAuthenticated) {
    return <LoadingScreen />;
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