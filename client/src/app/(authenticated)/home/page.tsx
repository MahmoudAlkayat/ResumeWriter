"use client";

import { Background } from '@/components/ui/background';

export default function HomePage() {

  return (
    <Background className="relative flex flex-col items-center justify-center h-screen text-center p-8">
      <h1 className="text-5xl font-bold text-foreground mb-4 drop-shadow-md">Welcome</h1>
      <p className="text-xl text-muted-foreground mb-6 max-w-xl drop-shadow-sm">
        Start by uploading your resume for AI-powered analysis or manually entering your information.
      </p>
    </Background>
  );
}
