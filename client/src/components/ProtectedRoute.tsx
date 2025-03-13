"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/AuthProvider";
import { useToast } from "@/contexts/ToastProvider";

export default function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();
  const router = useRouter();
  const { showInfo } = useToast();

  useEffect(() => {
    //TODO: Fix double toast
    if (isAuthenticated === false) {
      showInfo("You must be logged in to view this page");
      setTimeout(() => router.push("/login"), 100);
    }
  }, [isAuthenticated]);

  // Don't render anything until auth check is complete
  if (isAuthenticated !== true) {
    return null;
  }

  return <>{children}</>;
}
