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
  const { showSuccess } = useToast();
  const { login } = useAuth();

  const setUser = () => {
    const id = searchParams.get('id')
    const email = searchParams.get('email')
    const firstName = searchParams.get('firstName')
    const lastName = searchParams.get('lastName')
    if (!id || !email || !firstName || !lastName) return;
    login({id: Number(id), email: email!, firstName: firstName!, lastName: lastName!})
  }

  useEffect(() => {
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
  }, [router, searchParams, showSuccess]);

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