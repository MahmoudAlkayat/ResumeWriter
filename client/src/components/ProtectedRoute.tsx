"use client";

import { useEffect, useRef } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/AuthProvider";
import { useToast } from "@/contexts/ToastProvider";
import LoadingScreen from "./LoadingScreen";

const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { isAuthenticated, isLogout } = useAuth();
  const router = useRouter();
  const { showInfo } = useToast();
  const isMounted = useRef(false);

  useEffect(() => {
    if (isAuthenticated === false && !isLogout) { //Don't trigger on logout
      if (!isMounted.current) showInfo("You must be logged in to view this page");
      isMounted.current = true;
      router.replace("/login");
    }
  }, [isAuthenticated]);

  //To avoid showing page content on initial load
  if (isAuthenticated !== true) {
    return <LoadingScreen />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
