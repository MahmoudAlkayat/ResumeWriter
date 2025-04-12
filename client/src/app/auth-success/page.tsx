'use client';

import { Suspense, useEffect, useRef } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import LoadingScreen from '@/components/LoadingScreen';
import { useToast } from '@/contexts/ToastProvider';
import { useAuth } from '@/hooks/auth';

const AuthSuccessPage: React.FC = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const isMounted = useRef(false);
  const { showSuccess, showError } = useToast();
  const { login, isAuthenticated } = useAuth();

  const setUser = () => {
    const id = searchParams.get('id')
    const email = searchParams.get('email')
    const firstName = searchParams.get('firstName')
    const lastName = searchParams.get('lastName')
    const themePreference = searchParams.get('themePreference') || 'light'
    if (!id && !email && !firstName && !lastName) return;
    login({id: Number(id), email: email!, firstName: firstName!, lastName: lastName!}, themePreference)
  }

  useEffect(() => {
    // If not authenticated (no valid JWT cookie), redirect to login
    if (isAuthenticated === false) {
      router.replace('/login');
      if (!isMounted.current) {
        showError('Unauthorized access');
        isMounted.current = true;
      }
      return;
    }

    // Only process OAuth success if authenticated
    if (isAuthenticated === true) {
      if (searchParams.get('oauth') === 'google' && !isMounted.current) {
        showSuccess('Google login successful');
        isMounted.current = true;
      }
      if (searchParams.get('oauth') === 'linkedin' && !isMounted.current) {
        showSuccess('LinkedIn login successful');
        isMounted.current = true;
      }
      setUser();
      router.replace('/home');
    }
  }, [router, searchParams, showSuccess, isAuthenticated]);

  return <LoadingScreen />;
} 

const AuthSuccess: React.FC = () => {
    return (
        <Suspense fallback={<LoadingScreen />}>
            <AuthSuccessPage />
        </Suspense>
    )
}

export default AuthSuccess;