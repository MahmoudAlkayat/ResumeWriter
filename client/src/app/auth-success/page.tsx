'use client';

import { Suspense, useEffect, useRef } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import LoadingScreen from '@/components/LoadingScreen';
import { useToast } from '@/contexts/ToastProvider';

const AuthSuccessPage: React.FC = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const isMounted = useRef(false);
  const { showSuccess } = useToast();

  useEffect(() => {
    if (searchParams.get('oauth') === 'google' && !isMounted.current) {
      showSuccess('Google login successful');
      isMounted.current = true;
    }
    if (searchParams.get('oauth') === 'linkedin' && !isMounted.current) {
      showSuccess('LinkedIn login successful');
      isMounted.current = true;
    }
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