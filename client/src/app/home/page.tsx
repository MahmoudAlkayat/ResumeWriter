"use client";

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/contexts/AuthProvider';
import { useToast } from '@/contexts/ToastProvider';

export default function HomePage() {
  const router = useRouter();
  const { isAuthenticated, logout } = useAuth();
  const { showInfo } = useToast();

  useEffect(() => {
    const checkAuth = () => {
      if (!isAuthenticated) {
        showInfo("You must logged in to view this page")
        router.push("/")
      }
    };

    checkAuth(); // Check on initial render

    // const interval = setInterval(checkAuth, 5000); // Check every 5 seconds
    // window.addEventListener("storage", checkAuth); // Listen for token removal in other tabs

    // return () => {
    //   clearInterval(interval);
    //   window.removeEventListener("storage", checkAuth);
    // };
  }, [router]);

  const handleLogout = () => {
    logout()
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen bg-gray-100">
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className="relative flex flex-col items-center justify-center h-screen text-center p-4 bg-gradient-to-br from-blue-200 via-white to-gray-100 overflow-hidden">
      {/* Abstract AI-inspired background pattern */}
      {/* <div className="absolute inset-0 bg-[url('/ai-pattern.svg')] bg-cover bg-center opacity-10"></div> */}
      
      <h1 className="text-5xl font-extrabold text-black mb-4 drop-shadow-md">Welcome</h1>
      <p className="text-xl text-gray-800 mb-6 max-w-xl drop-shadow-sm">
        You are successfully authenticated. Navigate freely within the application.
      </p>
      <div className="z-10">
        <Button
          className="bg-red-600 text-white hover:bg-red-700 shadow-lg px-6 py-3 text-lg"
          onClick={handleLogout}
        >
          Log Out
        </Button>
      </div>
      {error && (
        <p className="text-red-500 mt-4">{error}</p>
      )}
    </div>
  );
}
