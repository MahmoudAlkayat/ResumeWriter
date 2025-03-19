'use client';

import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import LoadingScreen from '@/components/LoadingScreen';
import { useAuthRedirect } from '@/hooks/auth';
import Image from 'next/image';

export default function LandingPage() {
  const router = useRouter();

  const { isAuthLoading } = useAuthRedirect({
    redirectTo: '/home',
    protectedRoute: false
  });

  if (isAuthLoading) {
    return <LoadingScreen />;
  }

  return (
    <div className="relative flex flex-col items-center justify-center h-screen text-center p-4 bg-gradient-to-br from-blue-200 via-white to-gray-100 overflow-hidden">
      <h1 className="text-6xl font-bold text-black drop-shadow-md -mb-6">
        Welcome to
        <Image src="/logo.svg" alt="Icon" className="inline h-30 mb-4" />
      </h1>
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