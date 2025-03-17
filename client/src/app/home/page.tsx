"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";

export default function HomePage() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const response = await fetch("http://localhost:8080/auth/me", {
          method: "GET",
          credentials: "include",
        });

        if (!response.ok) {
          // Not authenticated; redirect to landing
          router.push("/");
        } else {
          // Authenticated
          setLoading(false);
        }
      } catch (err: any) {
        setError(err.message || "Error checking authentication");
        router.push("/");
      }
    };
    checkAuth();
  }, [router]);

  const handleLogout = async () => {
    try {
      const res = await fetch("http://localhost:8080/auth/logout", {
        method: "POST",
        credentials: "include",
      });

      if (res.ok) {
        // Force a full page reload to "/" to avoid stale state
        window.location.href = "/";
      } else {
        setError("Logout failed");
      }
    } catch (err: any) {
      setError(err.message || "Logout error");
    }
  };

  const handleUploadResume = () => {
    router.push("/upload-resume");
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
      {/* Background pattern */}
      <div className="absolute inset-0 bg-[url('/ai-pattern.svg')] bg-cover bg-center opacity-10"></div>

      <h1 className="text-5xl font-extrabold text-black mb-4 drop-shadow-md">
        Welcome
      </h1>
      <p className="text-xl text-gray-800 mb-6 max-w-xl drop-shadow-sm">
        You are successfully authenticated. Navigate freely within the application.
      </p>

      <div className="z-10 flex space-x-4">
        <Button
          className="bg-red-600 text-white hover:bg-red-700 shadow-lg px-6 py-3 text-lg"
          onClick={handleLogout}
        >
          Log Out
        </Button>
        <Button
          className="bg-blue-600 text-white hover:bg-blue-700 shadow-lg px-6 py-3 text-lg"
          onClick={handleUploadResume}
        >
          Upload Resume
        </Button>
      </div>

      {error && (
        <p className="text-red-500 mt-4">{error}</p>
      )}
    </div>
  );
}
