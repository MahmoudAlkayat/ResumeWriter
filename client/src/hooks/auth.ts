"use client";
import { useEffect, useRef, useContext } from 'react';
import { useRouter } from 'next/navigation';
import { AuthContext } from '@/contexts/AuthProvider';
import { useToast } from '@/contexts/ToastProvider';

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};

interface UseAuthRedirectOptions {
  redirectTo: string;
  protectedRoute?: boolean;
}

export const useAuthRedirect = ({ 
  redirectTo, 
  protectedRoute = true,
}: UseAuthRedirectOptions) => {
  const { isAuthenticated, isLogout } = useAuth();
  const router = useRouter();
  const { showInfo } = useToast();
  const isMounted = useRef(false);

  useEffect(() => {
    // Authentication is initialized and doesn't match protected route 
    const redirect = (isAuthenticated !== null) && (isAuthenticated !== protectedRoute);

    if (redirect) {
      router.replace(redirectTo);
      if (protectedRoute && !isMounted.current && !isLogout) { 
        showInfo("You must be logged in to view this page");
        isMounted.current = true;
      }
    }
  }, [isAuthenticated, redirectTo, router, showInfo]);

  // Return loading state and authenticated state for convenience
  return {
    isAuthLoading: isAuthenticated === null || isAuthenticated !== protectedRoute,
    isAuthenticated
  };
};